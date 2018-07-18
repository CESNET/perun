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

1;
