package Perun::ResourcesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'resourcesManager';

use fields qw(_agent _manager);

sub new
{
    my $self = fields::new(shift);  
    $self->{_agent} = shift;
    $self->{_manager} = $manager;
    
    return $self;
}

sub getResourceById
{
    return Perun::Common::callManagerMethod('getResourceById', 'Resource', @_);
}

sub createResource
{
    return Perun::Common::callManagerMethod('createResource', 'Resource', @_);
}

sub deleteResource
{
    return Perun::Common::callManagerMethod('deleteResource', '', @_);
}

sub getFacility
{
    return Perun::Common::callManagerMethod('getFacility', 'Facility', @_);
}

sub setFacility
{
    return Perun::Common::callManagerMethod('setFacility', '', @_);
}

sub getVo
{
    return Perun::Common::callManagerMethod('getVo', 'Vo', @_);
}

sub getAllowedMembers
{
    return Perun::Common::callManagerMethod('getAllowedMembers', '[]Member', @_);
}

sub getAllowedUsers
{
    return Perun::Common::callManagerMethod('getAllowedUsers', '[]User', @_);
}

sub assignGroupToResource
{
    return Perun::Common::callManagerMethod('assignGroupToResource', '', @_);
}

sub removeGroupFromResource
{
    return Perun::Common::callManagerMethod('removeGroupFromResource', '', @_);
}

sub getAssignedGroups
{
    return Perun::Common::callManagerMethod('getAssignedGroups', '[]Group', @_);
}

sub getAssignedResources
{
    return Perun::Common::callManagerMethod('getAssignedResources', '[]Resource', @_);
}

sub assignService
{
    return Perun::Common::callManagerMethod('assignService', '', @_);
}

sub assignServicesPackage
{
    return Perun::Common::callManagerMethod('assignServicesPackage', '', @_);
}

sub removeService
{
    return Perun::Common::callManagerMethod('removeService', '', @_);
}

sub removeServicesPackage
{
    return Perun::Common::callManagerMethod('removeServicesPackage', '', @_);
}

sub getResources
{
    return Perun::Common::callManagerMethod('getResources', '[]Resource', @_);
}

sub deleteAllResources
{
    return Perun::Common::callManagerMethod('deleteAllResources', '', @_);
}

sub getAssignedServices
{
    return Perun::Common::callManagerMethod('getAssignedServices', '[]Service', @_);
}

1;
