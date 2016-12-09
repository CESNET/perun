package Perun::beans::Member;

use strict;
use warnings;

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

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id} * 1;
	} else {
		$id = 0;
	}

	my $userId = $self->{_userId};

	return { id => $id, userId => $userId };
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub setId
{
	my $self = shift;
	$self->{_id} = shift;

	return;
}

sub getUserId
{
	my $self = shift;

	return $self->{_userId};
}

sub setUserId
{
	my $self = shift;
	$self->{_userId} = shift;

	return;
}

sub getStatus {
	my $self = shift;
	return $self->{_status};
}

sub getMembershipType {
	my $self = shift;
	return $self->{_membershipType};
}

sub getCommonArrayRepresentation {
	my $member = shift;
	return ($member->getId, $member->getUserId, $member->getStatus, $member->getMembershipType);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'UserId', 'Status', 'Membership type');
}

1;
