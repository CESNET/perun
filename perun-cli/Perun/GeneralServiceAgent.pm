package Perun::GeneralServiceAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'generalServiceManager';

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
sub deleteService {
	return Perun::Common::callManagerMethod('deleteService', '', @_);
}


#service => $serviceId
sub getService {
	return Perun::Common::callManagerMethod('getService', 'Service', @_);
}

# without param this list all execServices
# service => serviceId
sub listExecServices {
	return Perun::Common::callManagerMethod('listExecServices', '[]ExecService', @_);
}

#
sub countExecServices {
	return Perun::Common::callManagerMethod('countExecServices', 'number', @_);
}

# execService => execServiceId
sub getExecService {
	return Perun::Common::callManagerMethod('getExecService', 'ExecService', @_);
}

# TODO
sub insertExecService {
	return Perun::Common::callManagerMethod('insertExecService', 'number', @_);
}

# execService => $execService
sub updateExecService {
	return Perun::Common::callManagerMethod('updateExecService', '', @_);
}

# execService => $execServiceId
sub deleteExecService {
	return Perun::Common::callManagerMethod('deleteExecService', '', @_);
}

# execService => $execServiceId, facility => $facilityId
sub banExecServiceOnFacility {
	return Perun::Common::callManagerMethod('banExecServiceOnFacility', '', @_);
}

# execService => $execServiceId, destination => $destinationId
sub banExecServiceOnDestination {
	return Perun::Common::callManagerMethod('banExecServiceOnDestination', '', @_);
}

# facility => $facilityId
sub listDenialsForFacility {
	return Perun::Common::callManagerMethod('listDenialsForFacility', '[]ExecService', @_);
}

# destination => $destinationId
sub listDenialsForDestination {
	return Perun::Common::callManagerMethod('listDenialsForDestination', '[]ExecService', @_);
}

# facility => $facilityId
sub isExecServiceDeniedOnFacility {
	return Perun::Common::callManagerMethod('isExecServiceDeniedOnFacility', 'number', @_);
}

# destination => $destinationId
sub isExecServiceDeniedOnDestination {
	return Perun::Common::callManagerMethod('isExecServiceDeniedOnDestination', 'number', @_);
}

# facility => $facilityId
sub freeAllDenialsOnFacility {
	return Perun::Common::callManagerMethod('freeAllDenialsOnFacility', '', @_);
}

# destination => $destinationId
sub freeAllDenialsOnDestination {
	return Perun::Common::callManagerMethod('freeAllDenialsOnDestination', '', @_);
}

# execService => $execServiceId, facility => $facilityId
sub freeDenialOfExecServiceOnFacility {
	return Perun::Common::callManagerMethod('freeDenialOfExecServiceOnFacility', '', @_);
}

# execService => $execServiceId, destination => $destinationId
sub freeDenialOfExecServiceOnDestination {
	return Perun::Common::callManagerMethod('freeDenialOfExecServiceOnDestination', '', @_);
}

# execService => $execServiceId, dependantExecService => $dependantExecServiceId
sub createDependency {
	return Perun::Common::callManagerMethod('createDependency', '', @_);
}

# execService => $execServiceId, dependantExecService => $dependantExecServiceId
sub removeDependency {
	return Perun::Common::callManagerMethod('removeDependency', '', @_);
}

# execService => $execServiceId, dependantExecService => $dependantExecServiceId
sub isThereDependency {
	return Perun::Common::callManagerMethod('isThereDependency', 'number', @_);
}

# execService => $execServiceId
sub listExecServicesDependingOn {
	return Perun::Common::callManagerMethod('listExecServicesDependingOn', '[]ExecService', @_);
}

# execService => $execServiceId
sub listExecServicesThisExecServiceDependsOn {
	return Perun::Common::callManagerMethod('listExecServicesThisExecServiceDependsOn', '[]ExecService', @_);
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

1;
