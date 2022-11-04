package Perun::auth::OidcAuth;

use strict;
use warnings;

use YAML::XS 'LoadFile';
use JSON::XS;
use LWP::UserAgent;
use File::Basename;
use Try::Tiny;
use Perun::Exception;

my $PYTHON = "python3";
my $MFA_REFS = "https://refeds.org/profile/mfa";
my $dirname = dirname(__FILE__);
my $configFilename = $dirname . '/oidc_config.yml';
our $chosenConfigName = $ENV{PERUN_OIDC_CONFIG} || getDefaultConfig();
my $PERUN_OIDC = "perun_oidc" . "_" . $chosenConfigName;
my $contentType = "application/x-www-form-urlencoded";
my $authenticationFailed = "Authentication failed";
my $tokenRevocationFailed = "Token revocation failed";

sub getAccessToken
{
	if (defined $_[0]) {
		$PERUN_OIDC = "perun_oidc" . "_" . $_[0];
	}

	my $ret = `$PYTHON -c "import keyring; print('token:' + str(keyring.get_password('$PERUN_OIDC', 'access_token')), end='')"`;
	return processToken($ret);
}

sub getRefreshToken
{
	if (defined $_[0]) {
		$PERUN_OIDC = "perun_oidc" . "_" . $_[0];
	}

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

	print `qrencode -m 2 -t ASCIIi '$response->{"verification_uri_complete"}'`;
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
	my $config = loadConfiguration();
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
			} elsif ($error eq "unmet_authentication_requirements" && $config->{"enforce_mfa"}) {
				die Perun::Exception->fromHash({ type =>$authenticationFailed, errorInfo => "No MFA is set up - please enroll an active MFA token first." });
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
# If encorce_mfa is set to true, adds MFA acr value before acr_values
#
# Throws Perun::Exception when the response isn't successful.
# Returns structure containing response (e.g. structure->{"device_code"}).
sub authenticationRequest
{
	my $config = loadConfiguration();

	if (!$ENV{PERUN_OIDC_CONFIG} && $config->{"default"}) {
		print "Environment variable PERUN_OIDC_CONFIG not set, using default config $chosenConfigName.\n";
	}

	my $ua = LWP::UserAgent->new;
	my $acr_values = $config->{"acr_values"};
	if ($config->{"enforce_mfa"}) {
		length($acr_values) ? $acr_values = $MFA_REFS . " " . $acr_values : $acr_values = $MFA_REFS;
	}
	my $response = $ua->post(
		$config->{"oidc_device_code_uri"},
		"content_type" => $contentType,
		Content => {
			"client_id"  => $config->{"client_id"},
			"acr_values" => $acr_values,
			"scope"      => $config->{"scopes"},
			"prompt"     => "login"
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

	reauthenticate();
}

sub reauthenticate
{
	my $auth = authentication();
	unless ($auth->{"error"}) {
		setAccessToken($auth->{"access_token"});
		setAccessTokenValidity(time() + $auth->{"expires_in"});
		setRefreshToken($auth->{"refresh_token"});
	}
}

sub loadAllConfigurations
{
	unless (-e $configFilename) {
		die "OIDC configuration file is missing!\n";
	}

	return LoadFile($configFilename);
}

sub loadConfiguration
{
	unless (-e $configFilename) {
		die "OIDC configuration file is missing!\n";
	}

	checkConfigExists($chosenConfigName);
	return LoadFile($configFilename)->{$chosenConfigName};
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
	if (defined $_[0]) {
		$PERUN_OIDC = "perun_oidc" . "_" . $_[0];
	}

	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'access_token')"`;
	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'access_token_validity')"`;
}

sub removeAllAccessTokens
{
	my %config = % { loadAllConfigurations() };
	foreach my $key (keys %config) {
		if (getAccessToken($key)) {
			removeAccessToken($key);
		}
	}
}

sub revokeAllAccessTokens
{
	my %config = % { loadAllConfigurations() };
	foreach my $key (keys %config) {
		if (getAccessToken($key)) {
			revokeToken($key, "access_token");
		}
	}
}

sub removeRefreshToken
{
	if (defined $_[0]) {
		$PERUN_OIDC = "perun_oidc" . "_" . $_[0];
	}

	`$PYTHON -c "import keyring; keyring.delete_password('$PERUN_OIDC', 'refresh_token')"`;
}

sub removeAllRefreshTokens
{
	my %config = % { loadAllConfigurations() };
	foreach my $key (keys %config) {
		if (getRefreshToken($key)) {
			removeRefreshToken($key);
		}
	}
}

sub revokeAllRefreshTokens
{
	my %config = % { loadAllConfigurations() };
	foreach my $key (keys %config) {
		if (getRefreshToken($key)) {
			revokeToken($key, "refresh_token");
		}
	}
}

sub revokeToken
{
	my ($configName, $type) = @_;

	my %configs = % { loadAllConfigurations() };
	my $config = $configs{$configName};
	my $ua = LWP::UserAgent->new;
	my $token;
	if ($type eq "access_token") {
		$token = getAccessToken($configName);
	} elsif ($type eq "refresh_token") {
		$token = getRefreshToken($configName);
	}
	my $response = $ua->post(
		$config->{"oidc_token_revoke_endpoint_uri"},
		"content_type" => $contentType,
		Content => {
			"client_id"       => $config->{"client_id"},
			"token"           => $token,
			"token_type_hint" => $type
		}
	);

	unless ($response->is_success) {
		my $content = eval { decode_json $response->decoded_content };
		if ($@ or !exists($content->{"error"})) {
			die Perun::Exception->fromHash({ type => $tokenRevocationFailed, name => $response->code, errorInfo => $response->message });
		}
		die Perun::Exception->fromHash({ type => $tokenRevocationFailed, name => $content->{"error"}, errorInfo => $content->{"error_description"} });
	}
	if ($type eq "access_token") {
		removeAccessToken($configName);
	} elsif ($type eq "refresh_token") {
		removeRefreshToken($configName);

	}
}

sub checkConfigExists
{
	my $configName = $_[0];
	my %configs = % { loadAllConfigurations() };

	if (!exists $configs{$configName}) {
		die "ERROR: configuration \"$configName\" does not exist!\n";
	}
}

sub getDefaultConfig
{
	unless (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1") {
		return "";
	}
	my %configs;
	try {
		%configs = % { loadAllConfigurations() };
	} catch {
		warn "ERROR: configuration file empty or format invalid!\n";
		exit 0
	};
	foreach my $config (keys %configs) {
		if ($configs{$config}->{"default"}) {
			return $config;
		}
	}
	my $firstConfig = (sort keys %configs)[0];
	print "Environment variable PERUN_OIDC_CONFIG not set and default config not found, continuing using config $firstConfig\n";
	return $firstConfig;
}

1;
