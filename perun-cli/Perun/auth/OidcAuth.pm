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

sub setAccessToken
{
	my $token = shift;
	`$PYTHON -c "import keyring; keyring.set_password('$PERUN_OIDC', 'access_token', '$token')"`;
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
	if (exists($response->{"error"})) {
		print $response->{"error"}, ": ", $response->{"error_description"}, "\n";
		return;
	}

	print "Please, authenticate at ", $response->{"verification_uri_complete"}, "\nHit enter after you authenticate.\n";
	<STDIN>;

	my $interval = exists $response->{"interval"} ? $response->{"interval"} : 10;
	return polling($response->{"device_code"}, $interval);
}

# Polling token URI to request an access token in device code flow.
#
# arguments:
# deviceCode - unique code for the device returned by initial authentication request
# interval - interval in seconds at which the token URI is polled
#
# Returns structure containing response (e.g. structure->{"access_token"}).
sub polling
{
	my ($deviceCode, $interval) = @_;
	while (1) {
		my $response = tokenRequest($deviceCode);
		if (exists($response->{"error"})) {
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
				print("Expired token, try again....\n");  # TODO start auth again
			} else {
				print STDERR $error, ": ", $response->{"error_description", "\n"};
			}

			return $response;
		} else {
			return $response;
		}
	}
}

# Sends token device code flow request.
#
# Arguments:
# deviceCode - unique code for the device returned by initial authentication request
#
# Returns structure containing response (e.g. structure->{"access_token"}).
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
		if ($@ or !exists($content->{"error"}) or !exists($content->{"error_description"})) {
			print STDERR $response->status_line, "\n";
			exit 0;
		}
		return $content;
	}
}

# Sends authentication device code flow request.
#
# Returns structure containing response (e.g. structure->{"device_code"}).
sub authenticationRequest
{
	my $config = loadConfiguration();

	my $ua = LWP::UserAgent->new;
	my $scope = $config->{"scopes"};
	$scope =~ s/ /+/g;
	my $response = $ua->post(
		$config->{"oidc_device_code_uri"},
		"content_type" => $contentType,
		Content => {
			"client_id" => $config->{"client_id"},
			"scope" => $scope
		}
	);

	if ($response->is_success) {
		return decode_json $response->decoded_content;
	}
	else {
		print STDERR $response->status_line, "\n";
		exit 0;
	}
}

# Sends OIDC refresh token request.
#
# Arguments:
# refreshToken - refreshToken to use
#
# Returns structure containing response (e.g. structure->{"access_token"}).
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

	return decode_json $response->decoded_content;
}

sub refreshAccessToken
{
	my $refreshToken = getRefreshToken;
	if ($refreshToken) {
		my $res = refreshTokenRequest($refreshToken);
		unless ($res->{"error"}) {
			setAccessToken($res->{"access_token"});
			setRefreshToken($res->{"refresh_token"});
			return;
		}
	}

	my $auth = authentication();
	setAccessToken($auth->{"access_token"});
	setRefreshToken($auth->{"refresh_token"});
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
	unless ($accessToken) {
		refreshAccessToken();
		$accessToken = getAccessToken();
	}
	return $accessToken;
}

1;
