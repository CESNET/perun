package Perun::auth::OidcAuth;

use strict;
use warnings;

use Passwd::Keyring::Auto;
use YAML::XS 'LoadFile';
use File::Basename;

my $USERNAME = "perun_oidc";
my $dirname = dirname(__FILE__);
my $keyring = get_keyring(app=>"Perun", group=>"OIDC tokens");

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

sub authentication
{

}

sub polling
{

}

sub tokenRequest
{

}

sub authenticationRequest
{

}

sub refresh
{

}

sub refreshAccessToken
{
	my $refreshToken = getRefreshToken;
	if ($refreshToken) {
		my $res = refresh($refreshToken);
		unless ($res->{"error"}) {
			setAccessToken($res->{"access_token"});
			setRefreshToken($res->{"refresh_token"});
			return;
		}
	}

	my $auth = authentication();
	my $res = refresh($auth->{"refresh_token"});
	setAccessToken($res->{"access_token"});
	setRefreshToken($res->{"refresh_token"});
}

sub loadConfiguration
{
	my $filename = $dirname . '/oidc_config.yml';
	unless (-e $filename) {
		print "OIDC configuration file is missing!";
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
