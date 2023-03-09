package Perun::ServicesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'servicesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#
sub listServices {
	return Perun::Common::callManagerMethod('listServices', '[]Service', @_);
}

#service => $serviceId
sub getService {
	return Perun::Common::callManagerMethod('getService', 'Service', @_);
}

# service => $serviceId, facility => $facilityId
sub blockServiceOnFacility {
	return Perun::Common::callManagerMethod('blockServiceOnFacility', '', @_);
}

# service => $serviceId, destination => $destinationId
sub blockServiceOnDestination {
	return Perun::Common::callManagerMethod('blockServiceOnDestination', '', @_);
}

# service => $serviceId, facility => $facilityId
sub unblockServiceOnFacility {
	return Perun::Common::callManagerMethod('unblockServiceOnFacility', '', @_);
}

# service => $serviceId, destination => $destinationId
sub unblockServiceOnDestination {
	return Perun::Common::callManagerMethod('unblockServiceOnDestination', '', @_);
}

sub unblockAllServicesOnDestination {
	return Perun::Common::callManagerMethod('unblockAllServicesOnDestination', '', @_);
}

# facility => $facilityId
sub getServicesBlockedOnFacility {
	return Perun::Common::callManagerMethod('getServicesBlockedOnFacility', '[]Service', @_);
}

# destination => $destinationId
sub getServicesBlockedOnDestination {
	return Perun::Common::callManagerMethod('getServicesBlockedOnDestination', '[]Service', @_);
}

# facility => $facilityId
sub freeAllDenialsOnFacility {
	return Perun::Common::callManagerMethod('freeAllDenialsOnFacility', '', @_);
}

# destination => $destinationId
sub freeAllDenialsOnDestination {
	return Perun::Common::callManagerMethod('freeAllDenialsOnDestination', '', @_);
}

#service => $serviceId, facility => facility_id
sub forceServicePropagation
{
	return Perun::Common::callManagerMethod('forceServicePropagation', '', @_);
}

#service => $serviceId, facility => facility_id
sub planServicePropagation
{
	return Perun::Common::callManagerMethod('planServicePropagation', '', @_);
}

#(service => $service, owner => $ownerId)
sub createService
{
	return Perun::Common::callManagerMethod('createService', 'Service', @_);
}

#(service => $serviceId)
sub deleteService
{
	return Perun::Common::callManagerMethod('deleteService', '', @_);
}

#(service => $service)
sub updateService
{
	return Perun::Common::callManagerMethod('updateService', '', @_);
}

#(id => $number)
sub getServiceById
{
	return Perun::Common::callManagerMethod('getServiceById', 'Service', @_);
}

#(name => $string)
sub getServiceByName
{
	return Perun::Common::callManagerMethod('getServiceByName', 'Service', @_);
}

#bez parametru
sub getServices
{
	return Perun::Common::callManagerMethod('getServices', '[]Service', @_);
}

#(service => $serviceId, facility => $facilityId, consentEval => true|false)
sub getHashedHierarchicalData
{
	return Perun::Common::callManagerMethod('getHashedHierarchicalData', 'HashedGenData', @_);
}

#(service => $serviceId, facility => $facilityId, consentEval => true|false)
sub getHashedDataWithGroups
{
	return Perun::Common::callManagerMethod('getHashedDataWithGroups', 'HashedGenData', @_);
}

#bez parametru
sub getServicesPackages
{
	return Perun::Common::callManagerMethod('getServicesPackages', '[]ServicesPackage', @_);
}

#(servicesPackageId => $number)
sub getServicesPackageById
{
	return Perun::Common::callManagerMethod('getServicesPackageById', 'ServicesPackage', @_);
}

#(name => $servicesPackageName)
sub getServicesPackageByName
{
	return Perun::Common::callManagerMethod('getServicesPackageByName', 'ServicesPackage', @_);
}

#(servicesPackage => $servicesPackage)
sub createServicesPackage
{
	return Perun::Common::callManagerMethod('createServicesPackage', 'ServicesPackage', @_);
}

#(servicesPackage => $servicesPackageId)
sub deleteServicesPackage
{
	return Perun::Common::callManagerMethod('deleteServicesPackage', '', @_);
}

#(servicesPackage => $servicesPackage)
sub updateServicesPackage
{
	return Perun::Common::callManagerMethod('updateServicesPackage', '', @_);
}

#(servicesPackage => $servicesPackageId, service => $serviceId)
sub addServiceToServicesPackage
{
	return Perun::Common::callManagerMethod('addServiceToServicesPackage', '', @_);
}

#(servicesPackage => $servicesPackageId, service => $serviceId)
sub removeServiceFromServicesPackage
{
	return Perun::Common::callManagerMethod('removeServiceFromServicesPackage', '', @_);
}

#(servicesPackage => $servicesPackageId)
sub getServicesFromServicesPackage
{
	return Perun::Common::callManagerMethod('getServicesFromServicesPackage', '[]Service', @_);
}

#(service => $service, attribute => $attributeId)
sub addRequiredAttribute
{
	return Perun::Common::callManagerMethod('addRequiredAttribute', '', @_);
}

#(service => $service, attributes => $attributeIdsArray)
sub addRequiredAttributes
{
	return Perun::Common::callManagerMethod('addRequiredAttributes', '', @_);
}

#(service => $service, attribute => $attributeId)
sub removeRequiredAttribute
{
	return Perun::Common::callManagerMethod('removeRequiredAttribute', '', @_);
}

#(service => $service, attributes => $attributeIdsArray)
sub removeRequiredAttributes
{
	return Perun::Common::callManagerMethod('removeRequiredAttributes', '', @_);
}

#(service => $service)
sub removeAllRequiredAttributes
{
	return Perun::Common::callManagerMethod('removeAllRequiredAttributes', '', @_);
}

#( id => $destinationId)
sub getDestinationById
{
	return Perun::Common::callManagerMethod('getDestinationById', 'Destination', @_);
}

#( service => $serviceId, facility => $facilityId)
sub getDestinations
{
	return Perun::Common::callManagerMethod('getDestinations', '[]Destination', @_);
}

#( service => $serviceId, facility => $facilityId, destination => $destination, type => $destination_type)
sub addDestination
{
	return Perun::Common::callManagerMethod('addDestination', '', @_);
}

#( service => $serviceId, facility => $facilityId, destination => $destination, type => $destination_type)
sub removeDestination
{
	return Perun::Common::callManagerMethod('removeDestination', '', @_);
}

#( service => $serviceId, facility => $facilityId)
sub removeAllDestinations
{
	return Perun::Common::callManagerMethod('removeAllDestinations', '', @_);
}

sub getOwner
{
	return Perun::Common::callManagerMethod('getOwner', 'Owner', @_);
}

sub getAssignedServices
{
	return Perun::Common::callManagerMethod('getAssignedServices', '[]Service', @_);
}

sub getFacilitiesDestinations
{
	return Perun::Common::callManagerMethod('getFacilitiesDestinations', '[]Destination', @_);
}

sub getAssignedResources
{
	return Perun::Common::callManagerMethod('getAssignedResources', '[]Resource', @_);
}

1;
