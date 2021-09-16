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

sub getResourceByName
{
	return Perun::Common::callManagerMethod('getResourceByName', 'Resource', @_);
}

sub createResource
{
	return Perun::Common::callManagerMethod('createResource', 'Resource', @_);
}

sub updateResource
{
	return Perun::Common::callManagerMethod('updateResource', 'Resource', @_);
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

sub getAssignedMembers
{
	return Perun::Common::callManagerMethod('getAssignedMembers', '[]Member', @_);
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

sub addAdmin
{
        return Perun::Common::callManagerMethod('addAdmin', 'null', @_);
}

sub removeAdmin
{
        return Perun::Common::callManagerMethod('removeAdmin', 'null', @_);
}

sub getAdmins
{
        return Perun::Common::callManagerMethod('getAdmins', '[]User', @_);
}

sub getRichAdmins
{
        return Perun::Common::callManagerMethod('getRichAdmins', '[]RichUser', @_);
}

sub getRichAdminsWithAttributes
{
        return Perun::Common::callManagerMethod('getRichAdminsWithAttributes', '[]RichUser', @_);
}

sub getAdminGroups
{
        return Perun::Common::callManagerMethod('getAdminGroups', '[]Group', @_);
}

# copyResource (templateResource => $srcResource, destinationResource => $destResource, withGroups => boolean)
sub copyResource {
	return Perun::Common::callManagerMethod('copyResource', '', @_);
}

sub assignResourceTagToResource {
	return Perun::Common::callManagerMethod('assignResourceTagToResource', '', @_);
}

sub activateGroupResourceAssignment
{
	return Perun::Common::callManagerMethod('activateGroupResourceAssignment', '', @_);
}

sub deactivateGroupResourceAssignment
{
	return Perun::Common::callManagerMethod('deactivateGroupResourceAssignment', '', @_);
}

sub getGroupAssignments
{
	return Perun::Common::callManagerMethod('getGroupAssignments', '[]AssignedGroup', @_);
}

sub getResourceAssignments
{
	return Perun::Common::callManagerMethod('getResourceAssignments', '[]AssignedResource', @_);
}

1;
