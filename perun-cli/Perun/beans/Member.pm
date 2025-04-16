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

	my $dualMembership;
	if (defined($self->{_dualMembership})) {
		$dualMembership = $self->{_dualMembership};
	} else {
		$dualMembership = undef;
	}

	return { id => $id, userId => $userId, sponsored => $sponsored, dualMembership => $dualMembership };
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

sub getSourceGroupId
{
	my $self = shift;
	return ($self->{_sourceGroupId}) ? $self->{_sourceGroupId} : -1;
}

sub getVoId
{
	my $self = shift;

	return $self->{_voId};
}

sub setVoId
{
	my $self = shift;
	$self->{_voId} = shift;
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

sub isDualMembershipToPrint
{
	my $self = shift;

	return ($self->{_dualMembership}) ? 'true' : 'false';
}

sub isDualMembership
{
	my $self = shift;

	return ($self->{_dualMembership}) ? 1 : 0;
}

sub setDualMembership
{
  my $self = shift;
  my $value = shift;
  if (ref $value eq "JSON::XS::Boolean")
  {
    $self->{_dualMembership} = $value;
  } elsif ($value eq 'true' || $value eq 1)
  {
    $self->{_dualMembership} = JSON::XS::true;
  } else
  {
    $self->{_dualMembership} = JSON::XS::false;
  }

	return;
}

sub getStatus {
	my $self = shift;
	return $self->{_status};
}


sub getGroupStatus {
	my $self = shift;
	return $self->{_groupStatus};
}

sub getMembershipType {
	my $self = shift;
	return $self->{_membershipType};
}

sub getCommonArrayRepresentation {
	my $member = shift;
	return ($member->getId, $member->getUserId, $member->getStatus, $member->getGroupStatus, $member->getMembershipType, $member->isSponsoredToPrint, $member->isDualMembershipToPrint);
}

sub getCommonArrayRepresentationHeading {
	return('Id', 'UserId', 'Status', 'Group Status', 'Membership type', 'Sponsored', 'DualMembership');
}

1;
