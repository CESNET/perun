package Perun::beans::BanOnFacility;

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

	my $validityTo;
	if (defined($self->{_validityTo})) {
		$validityTo = "$self->{_validityTo}";
	} else {
		$validityTo = undef;
	}

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	my $userId;
	if (defined($self->{_userId})) {
		$userId = "$self->{_userId}";
	} else {
		$userId = undef;
	}

	my $facilityId;
	if (defined($self->{_facilityId})) {
		$facilityId = "$self->{_facilityId}";
	} else {
		$facilityId = undef;
	}

	return { id => $id, validityTo => $validityTo, description => $description, userId => $userId, facilityId =>
		$facilityId };
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

sub getValidityTo
{
	my $self = shift;

	return $self->{_validityTo};
}

sub setValidityTo
{
	my $self = shift;
	$self->{_validityTo} = shift;

	return;
}

sub getDescription
{
	my $self = shift;

	return $self->{_description};
}

sub setDescription
{
	my $self = shift;
	$self->{_description} = shift;

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

sub getFacilityId
{
	my $self = shift;

	return $self->{_facilityId};
}

sub setFacilityId
{
	my $self = shift;
	$self->{_facilityId} = shift;

	return;
}

1;
