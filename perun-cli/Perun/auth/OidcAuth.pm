package Perun::auth::OidcAuth;

use strict;
use warnings;

use Passwd::Keyring::Auto;
use YAML::XS 'LoadFile';
use File::Basename;
use LWP::UserAgent;
use JSON::XS;

my $USERNAME = "perun_oidc";
my $dirname = dirname(__FILE__);
my $keyring = get_keyring(app=>"Perun", group=>"OIDC tokens");
my $contentType = "application/x-www-form-urlencoded";

sub getAccessToken
{
	return $keyring->get_password($USERNAME, "access_token");
}

sub getRefreshToken
{
	return $keyring->get_password($USERNAME, "refresh_token");
}

sub setAccessToken
{
	my $token = shift;
	$keyring->set_password($USERNAME, $token, "access_token");
}

sub setRefreshToken
{
	my $token = shift;
	$keyring->set_password($USERNAME, $token, "refresh_token");
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
				print $error, ": ", $response->{"error_description", "\n"};
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

	if ($response->is_success) {
		return decode_json $response->decoded_content;
	}
	else {
		print STDERR $response->status_line, "\n";
		exit 0;
	}
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
		print "OIDC configuration file is missing!\n";
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
