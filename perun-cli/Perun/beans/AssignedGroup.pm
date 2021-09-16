package Perun::beans::AssignedGroup;

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

	return { enrichedGroup => $self->{_enrichedGroup}, status => $self->{_status},
		sourceGroupId => $self->{_sourceGroupId}, failureCause => $self->{_failureCause},
		autoAssignSubgroups => $self->{_autoAssignSubgroups} };
}

sub getGroupId {
	return shift->{_enrichedGroup}->{group}->{id};
}

sub getVoId {
	return shift->{_enrichedGroup}->{group}->{voId};
}

sub getName {
	return shift->{_enrichedGroup}->{group}->{name};
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

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getGroupId, $self->getVoId, $self->getName, $self->{_enrichedGroup}->{group}->{description},
		$self->getStatusToPrint, $self->{_sourceGroupId}, $self->isAutoAssignSubgroupsToPrint);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'VO ID', 'Name', 'Description', 'Status', 'Source group ID', 'Assign subgroups');
}
1;
