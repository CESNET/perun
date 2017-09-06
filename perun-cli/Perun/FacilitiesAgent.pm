package Perun::FacilitiesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'facilitiesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#(id => $number)
sub getFacilityById
{
	return Perun::Common::callManagerMethod('getFacilityById', 'Facility', @_);
}

sub getFacilityByName
{
	return Perun::Common::callManagerMethod('getFacilityByName', 'Facility', @_);
}

sub getFacilitiesByDestination
{
	return Perun::Common::callManagerMethod('getFacilitiesByDestination', '[]Facility', @_);
}

#()
sub getFacilities
{
	return Perun::Common::callManagerMethod('getFacilities', '[]Facility', @_);
}

#(facility => $facilityId)
sub getOwners
{
	return Perun::Common::callManagerMethod('getOwners', '[]Owner', @_);
}

#(facility => $facilityId, owner => $ownerId)
sub addOwner
{
	return Perun::Common::callManagerMethod('addOwner', '', @_);
}

#(facility => $facilityId, owner => $ownerId)
sub removeOwner
{
	return Perun::Common::callManagerMethod('removeOwner', '', @_);
}

#(facility => $facilityId)
sub getAllowedVos
{
	return Perun::Common::callManagerMethod('getAllowedVos', '[]Vo', @_);
}

#(facility => $facilityId)
sub getAssignedResources
{
	return Perun::Common::callManagerMethod('getAssignedResources', '[]Resource', @_);
}

#(facility => $facility)
sub createFacility
{
	return Perun::Common::callManagerMethod('createFacility', 'Facility', @_);
}

#(facility => $facilityId)
sub deleteFacility
{
	return Perun::Common::callManagerMethod('deleteFacility', '', @_);
}

sub getOwnerFacilities
{
	return Perun::Common::callManagerMethod('getOwnerFacilities', '[]Facility', @_);
}

#service => $serviceId
sub getAssignedFacilities
{
	return Perun::Common::callManagerMethod('getAssignedFacilities', '[]Facility', @_);
}

sub getHosts
{
	return Perun::Common::callManagerMethod('getHosts', '[]Host', @_);
}

#(hostname => "prvni.cz")
sub getHostsByHostname
{
	return Perun::Common::callManagerMethod('getHostsByHostname', '[]Host', @_);
}

#(facility => $facilityId)
sub getHostsCount
{
	return Perun::Common::callManagerMethod('getHostsCount', 'number', @_);
}

#(facility => $facilityId, hostnames => ("prvni.cz", "druhy.cz"))
sub addHosts
{
	return Perun::Common::callManagerMethod('addHosts', '[]Host', @_);
}

#(facility => $facilityId, hosts => @hosts)
sub removeHosts
{
	return Perun::Common::callManagerMethod('removeHosts', '', @_);
}

#(facility => $facilityId, hostname => "prvni.cz")
sub addHost
{
	return Perun::Common::callManagerMethod('addHost', 'Host', @_);
}

#(facility => $facilityId, hosts => "prvni.cz")
sub removeHost
{
	return Perun::Common::callManagerMethod('removeHost', '', @_);
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

sub getAllowedUsers
{
	return Perun::Common::callManagerMethod('getAllowedUsers', '[]User', @_);
}

sub addFacilityContacts
{
	return Perun::Common::callManagerMethod('addFacilityContacts', '[]ContactGroup', @_);
}

sub addFacilityContact
{
	return Perun::Common::callManagerMethod('addFacilityContact', 'ContactGroup', @_);
}

sub removeFacilityContacts
{
	return Perun::Common::callManagerMethod('removeFacilityContacts', '', @_);
}

sub removeFacilityContact
{
	return Perun::Common::callManagerMethod('removeFacilityContact', '', @_);
}

sub getAllContactGroupNames
{
	return Perun::Common::callManagerMethod('getAllContactGroupNames', '[]string', @_);
}

sub getFacilityContactGroup
{
	return Perun::Common::callManagerMethod('getFacilityContactGroup', 'ContactGroup', @_);
}

sub getFacilityContactGroups
{
	return Perun::Common::callManagerMethod('getFacilityContactGroups', '[]ContactGroup', @_);
}

sub assignSecurityTeam
{
	return Perun::Common::callManagerMethod('assignSecurityTeam', 'SecurityTeam', @_);
}

sub removeSecurityTeam
{
	return Perun::Common::callManagerMethod('removeSecurityTeam', '', @_);
}

sub getAssignedSecurityTeams
{
	return Perun::Common::callManagerMethod('getAssignedSecurityTeams', '[]SecurityTeam', @_);
}

sub getFacilitiesByHostName
{
	return Perun::Common::callManagerMethod('getFacilitiesByHostName', '[]Facility', @_);
}

sub getFacilityForHost
{
	return Perun::Common::callManagerMethod('getFacilityForHost', 'Facility', @_);
}

1;
