package Perun::Agent;
my $agentVersion = '3.0.0';

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
use Perun::ControlPanel;
use Perun::AuthzResolverAgent;
use Perun::HostsAgent;
use Perun::GeneralServiceAgent;
use Perun::AuditMessagesAgent;
use Perun::PropagationStatsReaderAgent;
use Perun::CabinetAgent;

my $format = 'json';
my $contentType = 'application/json; charset=utf-8';

use fields qw(_url _lwpUserAgent _jsonXs _vosAgent _membersAgent _usersAgent _groupsAgent _extSourcesAgent _servicesAgent _facilitiesAgent _resourcesAgent _controlPanel _attributesAgent _ownersAgent _authzResolverAgent _hostsAgent _clustersAgent _generalServiceAgent _auditMessagesAgent _propagationStatsReaderAgent _cabinetAgent);

use constant {
  AUTHENTICATION_FAILED => "Authentication failed",
  SERVER_ERROR => "Perun server error",
  WRONG_URL => "Wrong PERUN_URL",
  MISSING_URL => "Missing PERUN_URL environment variable",
  WRONG_AGENT_VERSION => "Version of the command tools and Perun server mismatch",
};

sub new {
    my $self = fields::new(shift);
    
    # Check if the PERUN_URL is defined
    if (!defined($ENV{PERUN_URL})) { die Perun::Exception->fromHash({ type => MISSING_URL }); };
    $self->{_url} = $ENV{PERUN_URL};

    # Extract login/password from ENV if available
    my ($login,$pass) = split '/',$ENV{PERUN_USER} if $ENV{PERUN_USER};
    
    $self->{_lwpUserAgent} = LWP::UserAgent->new(agent => "Agent.pm/$agentVersion");
		# Enable cookies if the HOME env is available
		if (defined($ENV{HOME})) {
			local $SIG{'__WARN__'} = sub { warn @_ unless $_[0] =~ /does not seem to contain cookies$/; };  #supress one concrete warning message from package HTTP::Cookies
			$self->{_lwpUserAgent}->cookie_jar({ file => $ENV{HOME} . "/.perun-engine-cookies.txt", autosave => 1, ignore_discard => 1 });
		}

    $self->{_jsonXs} = JSON::XS->new->utf8->convert_blessed->allow_nonref;
		
    # if $login is defined then use login/password authentication
    if (defined($login)) {
				my $uri = URI->new($self->{_url});
				my $port = defined($uri->port) ? $uri->port : $uri->schema == "https" ? 443 : 80;
        $self->{_lwpUserAgent}->credentials($uri->host.":".$port, 'Kerberos META', $login => $pass);
    }
    
    # Connect to the Perun server
    my $response = $self->{_lwpUserAgent}->request( GET($self->{_url}) );
    
    # Check the connection
    unless ($response->is_success) {
        my $code = $response->code;
        
        # Connection was OK, so check the return code
        switch($code) {
          case 401 { die Perun::Exception->fromHash({ type => AUTHENTICATION_FAILED }); } 
          case 500 { die Perun::Exception->fromHash({ type => SERVER_ERROR, errorInfo => ("HTTP STATUS CODE: $code") }); }
          case 302 { next; }
          case 405 { next; }
          case 404 { die Perun::Exception->fromHash({ type => WRONG_URL, errorInfo => $self->{_url} }); }
          else { die Perun::Exception->fromHash({ type => SERVER_ERROR, errorInfo => ("HTTP STATUS CODE: $code") }); }
        }
    }
    
    # Some error occured during connection
    if($response->is_error) {
      die Perun::Exception->fromHash({ type => SERVER_ERROR }); 
    }
    
    # Check if the reponse contains string 'OK'
    if($response->content !~ /^OK! /) { 
      die Perun::Exception->fromHash({ type => WRONG_URL, errorInfo => $self->{_url} }); 
    }
    
    # Check the version of the Perun server
    if ($response->content !~ /Version: $agentVersion/) {
      $response->content =~ m/Version: ([0-9]+.[0-9]+.[0-9]+)/;
      my $perunVersion = $1;
      die Perun::Exception->fromHash({ type => WRONG_AGENT_VERSION, errorInfo => "Tools version $agentVersion, Perun version $perunVersion" }); 
    }

    return $self;
}

sub call
{
    my ($self, $class, $method, $hash) = @_;
    
    my $fullUrl = "$self->{_url}/$format/$class/$method";

    my $content = $self->{_jsonXs}->encode($hash);
    my $response = $self->{_lwpUserAgent}->request( PUT($fullUrl, Content_Type => $contentType, Content => $content) );
    my $code = $response->code;
    my $decodedContent = $response->decoded_content;
    
    #print $response->decoded_content;
    #print "\n\n\n";
    
    unless ($code == 200 || $code == 400 || $code == 500) {
        die Perun::Exception->fromHash({ type => 'http', errorInfo => ("HTTP STATUS CODE: $code\nCONTENT:\n" . $decodedContent) });
    }
    
    my $returnedHash;
    eval {
        $returnedHash = $self->{_jsonXs}->decode($decodedContent);
    };
    if ($@) {
        die Perun::Exception->fromHash({ type => 'parse_error', errorInfo => ("CONTENT:\n" . $decodedContent) });
    }
    
    if ($code == 256 || $code == 400 || $code == 500) {
        die Perun::Exception->fromHash($returnedHash);
    }
    
    return $returnedHash;
}

sub getVosAgent
{
    my $self = shift;

    if (!$self->{_vosAgent}) {
        $self->{_vosAgent} = Perun::VosAgent->new($self);
    }

    return $self->{_vosAgent};
}

sub getMembersAgent
{
    my $self = shift;

    if (!$self->{_membersAgent}) {
        $self->{_membersAgent} = Perun::MembersAgent->new($self);
    }

    return $self->{_membersAgent};
}

sub getUsersAgent
{
    my $self = shift;

    if (!$self->{_usersAgent}) {
        $self->{_usersAgent} = Perun::UsersAgent->new($self);
    }

    return $self->{_usersAgent};
}

sub getGroupsAgent
{
    my $self = shift;

    if (!$self->{_groupsAgent}) {
        $self->{_groupsAgent} = Perun::GroupsAgent->new($self);
    }

    return $self->{_groupsAgent};
}

sub getExtSourcesAgent
{
    my $self = shift;

    if (!$self->{_extSourcesAgent}) {
        $self->{_extSourcesAgent} = Perun::ExtSourcesAgent->new($self);
    }

    return $self->{_extSourcesAgent};
}

sub getServicesAgent
{
    my $self = shift;

    if (!$self->{_servicesAgent}) {
        $self->{_servicesAgent} = Perun::ServicesAgent->new($self);
    }

    return $self->{_servicesAgent};
}

sub getFacilitiesAgent
{
    my $self = shift;

    if (!$self->{_facilitiesAgent}) {
        $self->{_facilitiesAgent} = Perun::FacilitiesAgent->new($self);
    }

    return $self->{_facilitiesAgent};
}

sub getResourcesAgent
{
    my $self = shift;

    if (!$self->{_resourcesAgent}) {
        $self->{_resourcesAgent} = Perun::ResourcesAgent->new($self);
    }

    return $self->{_resourcesAgent};
}

sub getControlPanel
{
    my $self = shift;

    if (!$self->{_controlPanel}) {
        $self->{_controlPanel} = Perun::ControlPanel->new($self);
    }

    return $self->{_controlPanel};
}

sub getAttributesAgent
{
    my $self = shift;

    if (!$self->{_attributesAgent}) {
        $self->{_attributesAgent} = Perun::AttributesAgent->new($self);
    }

    return $self->{_attributesAgent};
}

sub getOwnersAgent
{
    my $self = shift;

    if (!$self->{_ownersAgent}) {
        $self->{_ownersAgent} = Perun::OwnersAgent->new($self);
    }

    return $self->{_ownersAgent};
}

sub getAuthzResolverAgent
{
    my $self = shift;

    if (!$self->{_authzResolverAgent}) {
        $self->{_authzResolverAgent} = Perun::AuthzResolverAgent->new($self);
    }

    return $self->{_authzResolverAgent};
}

sub getHostsAgent {
    my $self = shift;

    if (!$self->{_hostsAgent}) {
        $self->{_hostsAgent} = Perun::HostsAgent->new($self);
    }

    return $self->{_hostsAgent};
}

sub getGeneralServicesAgent {
    my $self = shift;

    if (!$self->{_generalServiceAgent}) {
        $self->{_generalServiceAgent} = Perun::GeneralServiceAgent->new($self);
    }

    return $self->{_generalServiceAgent};
}

sub getAuditMessagesAgent {
    my $self = shift;

    if (!$self->{_auditMessagesAgent}) {
        $self->{_auditMessagesAgent} = Perun::AuditMessagesAgent->new($self);
    }

    return $self->{_auditMessagesAgent};
}

sub getPropagationStatsReaderAgent {
    my $self = shift;

    if (!$self->{_propagationStatsReaderAgent}) {
        $self->{_propagationStatsReaderAgent} = Perun::PropagationStatsReaderAgent->new($self);
    }

    return $self->{_propagationStatsReaderAgent};
}

sub getCabinetAgent {
    my $self = shift;

    if (!$self->{_cabinetAgent}) {
        $self->{_cabinetAgent} = Perun::CabinetAgent->new($self);
    }

    return $self->{_cabinetAgent};
}
1;
