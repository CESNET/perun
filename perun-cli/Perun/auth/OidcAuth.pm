package Perun::auth::OidcAuth;

use strict;
use warnings;

use YAML::XS 'LoadFile';
use JSON::XS;
use LWP::UserAgent;
use File::Basename;

my $PERUN_OIDC = "perun_oidc";
my $PYTHON = "python3";
my $dirname = dirname(__FILE__);
my $contentType = "application/x-www-form-urlencoded";
my $authenticationFailed = "Authentication failed";

sub getAccessToken
{
	my $ret = `$PYTHON -c "import keyring; print('token:' + str(keyring.get_password('$PERUN_OIDC', 'access_token')), end='')"`;
	return processToken($ret);
}

sub getRefreshToken
{
	my $ret = `$PYTHON -c "import keyring; print('token:' + str(keyring.get_password('$PERUN_OIDC', 'refresh_token')), end='')"`;
	return processToken($ret);
}

# separate token from result or print error and exit
sub processToken
{
	my $ret = shift;
	if (rindex($ret, 'token', 0) eq 0) {
		my @authResult = split(':', $ret);
		my $token = $authResult[1];
		return ($token eq 'None') ? undef : $token;
	} else {
		print STDERR $ret;
		exit 0;
	}
}

sub isAccessTokenValid
{
	my $ret = `$PYTHON -c "import keyring; print('token:' + str(keyring.get_password('$PERUN_OIDC', 'access_token_validity')), end='')"`;
	my $validity = processToken($ret);

	return ($validity and time() < $validity - 5);
}

sub setAccessToken
{
	my $token = shift;
	`$PYTHON -c "import keyring; keyring.set_password('$PERUN_OIDC', 'access_token', '$token')"`;
}

sub setAccessTokenValidity
{
	my $validity = shift;
	`$PYTHON -c "import keyring; keyring.set_password('$PERUN_OIDC', 'access_token_validity', '$validity')"`;
}

sub setRefreshToken
{
	my $token = shift;
	`$PYTHON -c "import keyring; keyring.set_password('$PERUN_OIDC', 'refresh_token', '$token')"`;
}

# Performs OIDC authentication using device code flow.
#
# Returns structure with either OIDC tokens or error description (e.g. structure->{"access_token"}).
sub authentication
{
	my $response = authenticationRequest();

	print "Please, authenticate at ", $response->{"verification_uri_complete"}, "\nHit enter after you authenticate.";
	<STDIN>;

	my $interval = exists $response->{"interval"} ? $response->{"interval"} : 10;
	return polling($response->{"device_code"}, $interval);
}

# Polling token URI to request an access token in device code flow. Polling will
# run for 5 minutes at most.
#
# arguments:
# deviceCode - unique code for the device returned by initial authentication request
# interval - interval in seconds at which the token URI is polled
#
# Returns structure containing response (e.g. structure->{"access_token"}).
sub polling
{
	my ($deviceCode, $interval) = @_;
	my $pollingLimit = int(300 / $interval); # polling for 300 seconds at most
	my @i = (1..$pollingLimit);
	for (@i) {
		my $response = tokenRequest($deviceCode);
		unless (exists($response->{"error"})) {
			return $response;
		} else {
			my $error = $response->{"error"};

			if ($error eq "authorization_pending") {
				print "You are not authenticated yet, checking again...\n";
				sleep($interval);
				next;
			} elsif ($error eq "slow_down") {
				$interval += 2;
				sleep($interval);
				next;
			} elsif ($error eq "expired_token") {
				print("Expired token, restarting authentication\n");
				return authentication;
			} else {
				die Perun::Exception->fromHash({ type => $authenticationFailed, name => $error, errorInfo => $response->{"error_description"} });
			}
		}
	}
	die Perun::Exception->fromHash({ type => $authenticationFailed, errorInfo => "Authentication timed-out." });
}

# Sends token device code flow request.
#
# Arguments:
# deviceCode - unique code for the device returned by initial authentication request
#
# Throws Perun::Exception when the response isn't successful and it's not an OIDC error.
# Returns structure containing response (e.g. structure->{"access_token"}). If an OIDC error
# occurred, the structure contains "error" key and possibly "error_description" key.
sub tokenRequest
{
	my $deviceCode = shift;
	my $config = loadConfiguration();

	my $ua = LWP::UserAgent->new;

	my $response = $ua->post(
		$config->{"oidc_token_endpoint_uri"},
		"content_type" => $contentType,
		Content => {
			"client_id" => $config->{"client_id"},
			"device_code" => $deviceCode,
			"grant_type" => "urn:ietf:params:oauth:grant-type:device_code"
		}
	);

	if ($response->is_success) {
		return decode_json $response->decoded_content;
	} else {
		my $content = eval { decode_json $response->decoded_content };
		if ($@ or !exists($content->{"error"})) {
			die Perun::Exception->fromHash({ type => $authenticationFailed, name => $response->code, errorInfo => $response->message });
		}
		return $content;
	}
}

# Sends authentication device code flow request.
#
# Throws Perun::Exception when the response isn't successful.
# Returns structure containing response (e.g. structure->{"device_code"}).
sub authenticationRequest
{
	my $config = loadConfiguration();

	my $ua = LWP::UserAgent->new;
	my $response = $ua->post(
		$config->{"oidc_device_code_uri"},
		"content_type" => $contentType,
		Content => {
			"client_id" => $config->{"client_id"},
			"acr_values" => $config->{"acr_values"},
			"scope" => $config->{"scopes"}
		}
	);

	if ($response->is_success) {
		return decode_json $response->decoded_content;
	} else {
		my $content = eval { decode_json $response->decoded_content };
		if ($@ or !exists($content->{"error"})) {
			die Perun::Exception->fromHash({ type => $authenticationFailed, name => $response->code, errorInfo => $response->message });
		} else {
			die Perun::Exception->fromHash({ type => $authenticationFailed, name => $content->{"error"}, errorInfo => $content->{"error_description"} });
		}
	}
}

# Sends OIDC refresh token request.
#
# Arguments:
# refreshToken - refreshToken to use
#
# Throws Perun::Exception when the response isn't successful and it's not an OIDC error.
# Returns structure containing response (e.g. structure->{"access_token"}). If an OIDC error
# occurred, the structure contains "error" key and possibly "error_description" key.
sub refreshTokenRequest
{
	my $refreshToken = shift;
	my $config = loadConfiguration();

	my $ua = LWP::UserAgent->new;

	my $response = $ua->post(
		$config->{"oidc_token_endpoint_uri"},
		"content_type" => $contentType,
		Content => {
			"grant_type" => "refresh_token",
			"client_id" => $config->{"client_id"},
			"refresh_token" => $refreshToken
		}
	);

	if ($response->is_success) {
		return decode_json $response->decoded_content;
	} else {
		my $content = eval { decode_json $response->decoded_content };
		if ($@ or !exists($content->{"error"})) {
			die Perun::Exception->fromHash({ type => $authenticationFailed, name => $response->code, errorInfo => $response->message });
		}
		return $content;
	}
}

sub refreshAccessToken
{
	my $refreshToken = getRefreshToken;
	if ($refreshToken) {
		my $res = refreshTokenRequest($refreshToken);
		unless ($res->{"error"}) {
			setAccessToken($res->{"access_token"});
			setAccessTokenValidity(time() + $res->{"expires_in"});
			setRefreshToken($res->{"refresh_token"});
			return;
		}
	}

	my $auth = authentication();
	unless ($auth->{"error"}) {
		setAccessToken($auth->{"access_token"});
		setAccessTokenValidity(time() + $auth->{"expires_in"});
		setRefreshToken($auth->{"refresh_token"});
	}
}

sub loadConfiguration
{
	my $filename = $dirname . '/oidc_config.yml';
	unless (-e $filename) {
		print STDERR "OIDC configuration file is missing!\n";
		exit 0;
	}

	return LoadFile($filename);
}

sub loadAccessToken
{
	my $accessToken = getAccessToken();
	unless ($accessToken and isAccessTokenValid) {
		refreshAccessToken();
		$accessToken = getAccessToken();
	}
	return $accessToken;
}

sub removeAccessToken
{
	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'access_token')"`;
	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'access_token_validity')"`;
}

sub removeRefreshToken
{
	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'refresh_token')"`;
}

1;
