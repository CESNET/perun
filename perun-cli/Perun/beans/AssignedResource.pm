package Perun::beans::AssignedResource;

use strict;
use warnings;
use 5.010;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	return Perun::Common::fromHash(@_);
}

sub TO_JSON
{
	my $self = shift;

	return { enrichedResource => $self->{_enrichedResource}, status => $self->{_status},
		sourceGroupId => $self->{_sourceGroupId}, failureCause => $self->{_failureCause},
		facility => $self->{_facility}, resourceTags => $self->{_resourceTags},
		autoAssignSubgroups => $self->{_autoAssignSubgroups} };
}

sub getResourceId {
	return shift->{_enrichedResource}->{resource}->{id};
}

sub getName {
	return shift->{_enrichedResource}->{resource}->{name};
}

sub getStatus {
	return shift->{_status};
}

sub getStatusToPrint {
	my $self = shift;
	return ($self->{_failureCause}) ? $self->{_status} . " - " . $self->{_failureCause} : $self->{_status};
}

sub getSourceGroupId {
	my $self = shift;
	return ($self->{_sourceGroupId}) ? $self->{_sourceGroupId} : -1;
}

sub getFailureCause {
	my $self = shift;
	return $self->{_failureCause};
}

sub isAutoAssignSubgroups {
	my $self = shift;
	return ($self->{_autoAssignSubgroups}) ? 1 : 0;
}

sub isAutoAssignSubgroupsToPrint
{
	my $self = shift;

	return ($self->{_autoAssignSubgroups}) ? 'true' : 'false';
}

sub getFacilityId {
	return shift->{_facility}->{id};
}

sub getResourceTags {
	my $self = shift;
	return @{$self->{_getResourceTags}};
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getResourceId, $self->getName, $self->{_enrichedResource}->{resource}->{voId}, $self->getFacilityId,
		$self->{_enrichedResource}->{resource}->{description}, $self->getStatusToPrint, $self->{_sourceGroupId}, $self->isAutoAssignSubgroupsToPrint);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name', 'VO ID', 'Facility ID', 'Description', 'Status', 'Source group ID', 'Assign subgroups');
}
1;
