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

	my $sponsored;
	if (defined($self->{_sponsored})) {
		$sponsored = $self->{_sponsored};
	} else {
		$sponsored = undef;
	}

	return { id => $id, userId => $userId, sponsored => $sponsored };
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

sub isSponsoredToPrint
{
	my $self = shift;

	return ($self->{_sponsored}) ? 'true' : 'false';
}

sub isSponsored
{
	my $self = shift;

	return ($self->{_sponsored}) ? 1 : 0;
}

sub setSponsored
{
  my $self = shift;
  my $value = shift;
  if (ref $value eq "JSON::XS::Boolean")
  {
    $self->{_sponsored} = $value;
  } elsif ($value eq 'true' || $value eq 1)
  {                                                                                           
    $self->{_sponsored} = JSON::XS::true;
  } else
  {
    $self->{_sponsored} = JSON::XS::false;
  }

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
	return ($member->getId, $member->getUserId, $member->getStatus, $member->getMembershipType, $member->isSponsoredToPrint);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'UserId', 'Status', 'Membership type', 'Sponsored');
}

1;
