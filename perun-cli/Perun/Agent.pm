package Perun::Agent;
my $agentVersion = '0.0.0';
my $agentVersionMajor;
if ($agentVersion !~ /^(\d+)(?{ $agentVersionMajor = $^N })\..*/i)
{
	die Perun::Exception->fromHash( { type => WRONG_AGENT_VERSION, errorInfo =>
		"Tools have an invalid version: $agentVersion" } );
}

use strict;
use warnings;
use overload
	'""' => sub { return "Perun::Agent object - version: $agentVersion \n"; };

use Switch;
use HTTP::Request::Common;
use LWP::UserAgent;
use JSON::XS;
use URI;
use Perun::Common;
use Perun::Exception;
use Perun::VosAgent;
use Perun::MembersAgent;
use Perun::UsersAgent;
use Perun::GroupsAgent;
use Perun::ExtSourcesAgent;
use Perun::ServicesAgent;
use Perun::FacilitiesAgent;
use Perun::AttributesAgent;
use Perun::ResourcesAgent;
use Perun::OwnersAgent;
use Perun::AuthzResolverAgent;
use Perun::AuditMessagesAgent;
use Perun::TasksAgent;
use Perun::CabinetAgent;
use Perun::ConfigAgent;
use Perun::NotificationsAgent;
use Perun::SearcherAgent;
use Perun::RegistrarAgent;
use Perun::BanOnResourceAgent;
use Perun::BanOnFacilityAgent;
use Perun::auth::OidcAuth;
use Sys::Hostname;

my $format = 'json';
my $contentType = 'application/json; charset=utf-8';

use fields qw(_url _lwpUserAgent _jsonXs _vosAgent _configAgent _membersAgent _usersAgent _groupsAgent _extSourcesAgent _servicesAgent _searcherAgent _facilitiesAgent _resourcesAgent _attributesAgent _ownersAgent _authzResolverAgent _auditMessagesAgent _tasksAgent _cabinetAgent _notificationsAgent _registrarAgent _banOnResourceAgent _banOnFacilityAgent _useNon);

use constant {
	AUTHENTICATION_FAILED   => "Authentication failed",
	SERVER_ERROR            => "Perun server error",
	WRONG_URL               => "Wrong PERUN_URL",
	MISSING_URL             => "Missing PERUN_URL environment variable",
	WRONG_AGENT_VERSION     => "Version of the command tools and Perun server mismatch",
	MFA_PRIVILEGE_EXCEPTION => "Multi-Factor Authentication required",
	MFA_PRIVILEGE_EXC_NAME  => "MfaPrivilegeException",
	MFA_ROLE_EXC_NAME		=> "MfaRolePrivilegeException"
};

sub gotMfaPrivilegeException;

sub new {
	my $self = fields::new(shift);

	# load custom data format
	my $wanted_format = shift;
	if (defined $wanted_format) {
		$format = $wanted_format;
	}

	# use non authorization
	$self->{_useNon} = shift;
	if ($self->{_useNon}) {
		if (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1") {
			my $config = Perun::auth::OidcAuth::loadConfiguration();
			$ENV{PERUN_URL} = convertUrlToNonAuth($config->{"perun_api_endpoint"} . "/");
			$ENV{PERUN_OIDC} = 0;
		} else {
			if (!defined($ENV{PERUN_URL})) {die Perun::Exception->fromHash({ type => MISSING_URL });};
			$ENV{PERUN_URL} = convertUrlToNonAuth($ENV{PERUN_URL});
		}
	}

	# Extract RPC type from ENV (if not defined, use "Perun RPC")
	my $rpcType = "Perun RPC";
	if (defined($ENV{PERUN_RPC_TYPE})) {
		$rpcType = $ENV{PERUN_RPC_TYPE};
	}

	# OIDC authorization
	if (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1") {
		my $accessToken;

		my $config = Perun::auth::OidcAuth::loadConfiguration();
		$self->{_url} = $config->{"perun_api_endpoint"} . "/";

		$accessToken = Perun::auth::OidcAuth::loadAccessToken();

		$self->{_lwpUserAgent} = LWP::UserAgent->new( agent => "Agent.pm/$agentVersion", timeout => 4000 );
		$self->{_lwpUserAgent}->default_header( 'authorization' => "bearer $accessToken" );

	} else {

		# Check if the PERUN_URL is defined
		if (!defined($ENV{PERUN_URL})) {die Perun::Exception->fromHash({ type => MISSING_URL });};
		$self->{_url} = $ENV{PERUN_URL};

		# Extract login/password from ENV if available
		my ($login, $pass) = split '/', $ENV{PERUN_USER}, 2 if $ENV{PERUN_USER};

		$self->{_lwpUserAgent} = LWP::UserAgent->new(agent => "Agent.pm/$agentVersion", timeout => 4000);

		# if $login is defined then use login/password authentication
		if (defined($login)) {
			my $uri = URI->new($self->{_url});
			my $port = defined($uri->port) ? $uri->port : $uri->schema == "https" ? 443 : 80;
			$self->{_lwpUserAgent}->credentials($uri->host . ":" . $port, $rpcType, $login => $pass);
		}
	}

	# Enable cookies if enviromental variable with path exists or home env is available
	if (defined($ENV{PERUN_COOKIE})) {
		local $SIG{'__WARN__'} = sub {warn @_ unless $_[0] =~ /does not seem to contain cookies$/;}; #supress one concrete warning message from package HTTP::Cookies
		$self->{_lwpUserAgent}->cookie_jar({ file => $ENV{PERUN_COOKIE}, autosave => 1, ignore_discard => 1 });
	}
	elsif (defined($ENV{HOME})) {
		my $hostname = hostname();
		my $grp = getpgrp;
		local $SIG{'__WARN__'} = sub {warn @_ unless $_[0] =~ /does not seem to contain cookies$/;}; #supress one concrete warning message from package HTTP::Cookies
		$self->{_lwpUserAgent}->cookie_jar({ file => $ENV{HOME} . "/perun-cookie-$hostname-$grp.txt", autosave => 1,
			ignore_discard                        => 1 });
	}

	$self->{_jsonXs} = JSON::XS->new->utf8->convert_blessed->allow_nonref;

	# Connect to the Perun server
	my $response = $self->{_lwpUserAgent}->request( GET($self->{_url}) );

	# Check the connection
	unless ($response->is_success) {
		my $code = $response->code;

		# if using OIDC and ended with 401, token might be expired
		if (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1" && $code eq 401) {
			my $accessToken;

			# try to refresh tokens
			Perun::auth::OidcAuth::refreshAccessToken();
			$accessToken = Perun::auth::OidcAuth::getAccessToken();

			$self->{_lwpUserAgent}->default_header( 'authorization' => "bearer $accessToken" );

			# Reconnect to the Perun server
			$response = $self->{_lwpUserAgent}->request( GET($self->{_url}) );
		}

		unless ($response->is_success) {
			# Connection was OK, so check the return code
			switch($code) {
				case 401 {die Perun::Exception->fromHash({ type => AUTHENTICATION_FAILED });}
				case 500 {die Perun::Exception->fromHash({ type => SERVER_ERROR, errorInfo =>
					("HTTP STATUS CODE: $code") });}
				case 302 {next;}
				case 405 {next;}
				case 404 {die Perun::Exception->fromHash({ type => WRONG_URL, errorInfo => $self->{_url} });}
				else {die Perun::Exception->fromHash({ type => SERVER_ERROR, errorInfo =>
					("HTTP STATUS CODE: $code") });}
			}
		}
	}

	# Some error occured during connection
	if ($response->is_error) {
		die Perun::Exception->fromHash( { type => SERVER_ERROR } );
	}

	# Check if the reponse contains string 'OK'
	if ($response->content !~ /^OK! /) {
		die Perun::Exception->fromHash( { type => WRONG_URL, errorInfo => $self->{_url} } );
	}

	# Check the version of the Perun server
	if ($response->content !~ /Version: $agentVersionMajor.[0-9]+.[0-9]+/) {
		$response->content =~ m/Version: ([0-9]+.[0-9]+.[0-9]+([^ ]*))/;
		my $perunVersion = $1;
		die Perun::Exception->fromHash( { type => WRONG_AGENT_VERSION, errorInfo =>
					"Tools version $agentVersion, Perun version $perunVersion" } );
	}

	return $self;
}

sub call
{
	my ($self, $class, $method, $hash) = @_;

	my $fullUrl = "$self->{_url}/$format/$class/$method";

	my $content = $self->{_jsonXs}->encode( $hash );

	my $accessToken = undef;
	if (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1") {
		$accessToken = Perun::auth::OidcAuth::loadAccessToken();
		$self->{_lwpUserAgent}->default_header('authorization' => "bearer $accessToken");
	}
	my $response = $self->{_lwpUserAgent}->request( PUT($fullUrl, Content_Type => $contentType, Content => $content) );
	my $code = $response->code;

	if (defined($ENV{PERUN_OIDC}) && $ENV{PERUN_OIDC} eq "1") {

		# if using OIDC and ended with 401, token might be expired
		if ($code eq 401) {
			# try to refresh tokens
			Perun::auth::OidcAuth::refreshAccessToken();
			$accessToken = Perun::auth::OidcAuth::getAccessToken();

			# retry
			$self->{_lwpUserAgent}->default_header('authorization' => "bearer $accessToken");
			$response = $self->{_lwpUserAgent}->request(PUT($fullUrl, Content_Type => $contentType, Content => $content));
			$code = $response->code;
		}

		if (gotMfaPrivilegeException $response) {
			if (Perun::auth::OidcAuth::loadConfiguration()->{"enforce_mfa"}) {
				# mfa_timestamp might have timeouted
				Perun::auth::OidcAuth::reauthenticate();
				$accessToken = Perun::auth::OidcAuth::getAccessToken();

				# retry
				$self->{_lwpUserAgent}->default_header('authorization' => "bearer $accessToken");
				$response = $self->{_lwpUserAgent}->request(PUT($fullUrl, Content_Type => $contentType, Content => $content));
				$code = $response->code;
			}
			else {
				die Perun::Exception->fromHash({ type => MFA_PRIVILEGE_EXC_NAME, errorInfo => MFA_PRIVILEGE_EXCEPTION . ". To continue, update your OIDC configuration with 'enforce_mfa' to true and retry request." });
			}
		}
	} elsif (gotMfaPrivilegeException $response) {
		die Perun::Exception->fromHash( { type => MFA_PRIVILEGE_EXC_NAME, errorInfo => MFA_PRIVILEGE_EXCEPTION . ", please use OIDC to connect to Perun. Ensure to have 'enforce_mfa' enabled in OIDC configuration." } );
	}

	unless ($code == 200 || $code == 400 || $code == 500) {
		die Perun::Exception->fromHash( { type => 'http', errorInfo =>
					("HTTP STATUS CODE: $code\nCONTENT:\n".$response->decoded_content) } );
	}

	my $returnedHash;
	eval {
		$returnedHash = $self->{_jsonXs}->decode( $response->decoded_content );
	};
	if ($@) {
		die Perun::Exception->fromHash( { type => 'parse_error', errorInfo => ("CONTENT:\n".$response->decoded_content) } );
	}

	if ($code == 256 || $code == 400 || $code == 500) {
		die Perun::Exception->fromHash( $returnedHash );
	}

	return $returnedHash;
}

sub getVosAgent
{
	my $self = shift;

	if (!$self->{_vosAgent}) {
		$self->{_vosAgent} = Perun::VosAgent->new( $self );
	}

	return $self->{_vosAgent};
}

sub getMembersAgent
{
	my $self = shift;

	if (!$self->{_membersAgent}) {
		$self->{_membersAgent} = Perun::MembersAgent->new( $self );
	}

	return $self->{_membersAgent};
}

sub getUsersAgent
{
	my $self = shift;

	if (!$self->{_usersAgent}) {
		$self->{_usersAgent} = Perun::UsersAgent->new( $self );
	}

	return $self->{_usersAgent};
}

sub getGroupsAgent
{
	my $self = shift;

	if (!$self->{_groupsAgent}) {
		$self->{_groupsAgent} = Perun::GroupsAgent->new( $self );
	}

	return $self->{_groupsAgent};
}

sub getExtSourcesAgent
{
	my $self = shift;

	if (!$self->{_extSourcesAgent}) {
		$self->{_extSourcesAgent} = Perun::ExtSourcesAgent->new( $self );
	}

	return $self->{_extSourcesAgent};
}

sub getServicesAgent
{
	my $self = shift;

	if (!$self->{_servicesAgent}) {
		$self->{_servicesAgent} = Perun::ServicesAgent->new( $self );
	}

	return $self->{_servicesAgent};
}

sub getSearcherAgent
{
	my $self = shift;

	if (!$self->{_searcherAgent}) {
		$self->{_searcherAgent} = Perun::SearcherAgent->new( $self );
	}

	return $self->{_searcherAgent};
}

sub getFacilitiesAgent
{
	my $self = shift;

	if (!$self->{_facilitiesAgent}) {
		$self->{_facilitiesAgent} = Perun::FacilitiesAgent->new( $self );
	}

	return $self->{_facilitiesAgent};
}

sub getConfigAgent
{
	my $self = shift;

	if (!$self->{_configAgent}) {
		$self->{_configAgent} = Perun::ConfigAgent->new( $self );
	}

	return $self->{_configAgent};
}

sub getResourcesAgent
{
	my $self = shift;

	if (!$self->{_resourcesAgent}) {
		$self->{_resourcesAgent} = Perun::ResourcesAgent->new( $self );
	}

	return $self->{_resourcesAgent};
}

sub getAttributesAgent
{
	my $self = shift;

	if (!$self->{_attributesAgent}) {
		$self->{_attributesAgent} = Perun::AttributesAgent->new( $self );
	}

	return $self->{_attributesAgent};
}

sub getOwnersAgent
{
	my $self = shift;

	if (!$self->{_ownersAgent}) {
		$self->{_ownersAgent} = Perun::OwnersAgent->new( $self );
	}

	return $self->{_ownersAgent};
}

sub getAuthzResolverAgent
{
	my $self = shift;

	if (!$self->{_authzResolverAgent}) {
		$self->{_authzResolverAgent} = Perun::AuthzResolverAgent->new( $self );
	}

	return $self->{_authzResolverAgent};
}

sub getAuditMessagesAgent {
	my $self = shift;

	if (!$self->{_auditMessagesAgent}) {
		$self->{_auditMessagesAgent} = Perun::AuditMessagesAgent->new( $self );
	}

	return $self->{_auditMessagesAgent};
}

sub getTasksAgent {
	my $self = shift;

	if (!$self->{_tasksAgent}) {
		$self->{_tasksAgent} = Perun::TasksAgent->new( $self );
	}

	return $self->{_tasksAgent};
}

sub getCabinetAgent {
	my $self = shift;

	if (!$self->{_cabinetAgent}) {
		$self->{_cabinetAgent} = Perun::CabinetAgent->new( $self );
	}

	return $self->{_cabinetAgent};
}

sub getNotificationsAgent {
	my $self = shift;

	if (!$self->{_notificationsAgent}) {
		$self->{_notificationsAgent} = Perun::NotificationsAgent->new( $self );

		return $self->{_notificationsAgent};
	}
}

sub getRegistrarAgent {
	my $self = shift;

	if (!$self->{_registrarAgent}) {
		$self->{_registrarAgent} = Perun::RegistrarAgent->new( $self );

		return $self->{_registrarAgent};
	}
}

sub getBanOnResourceAgent {
	my $self = shift;

	if (!$self->{_banOnResourceAgent}) {
		$self->{_banOnResourceAgent} = Perun::BanOnResourceAgent->new( $self );

		return $self->{_banOnResourceAgent};
	}
}

sub getBanOnFacilityAgent {
	my $self = shift;

	if (!$self->{_banOnFacilityAgent}) {
		$self->{_banOnFacilityAgent} = Perun::BanOnFacilityAgent->new( $self );

		return $self->{_banOnFacilityAgent};
	}
}

###################################
##        HELPER METHODS         ##
###################################

# Checks, if MfaPrivilegeException was thrown for the request
# Arguments:
# - response for the request
sub gotMfaPrivilegeException {
	my $response = shift;
	my $content;

	return 0 if $response->is_success;

	$content = JSON::XS->new->decode($response->decoded_content);
	return $content && $content->{errorId} && $content->{type} &&
		($content->{type} eq MFA_PRIVILEGE_EXC_NAME || $content->{type} eq MFA_ROLE_EXC_NAME);
}

sub convertUrlToNonAuth {
	my $url = shift;
	my $uri = URI->new($url);
	my @segments = $uri->path_segments();
	$segments[1] = "non";
	$uri->path_segments(@segments);
	return $uri->as_string;

}

1;
