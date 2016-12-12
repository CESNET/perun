package Perun::beans::BanOnResource;

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

	my $memberId;
	if (defined($self->{_memberId})) {
		$memberId = "$self->{_memberId}";
	} else {
		$memberId = undef;
	}

	my $resourceId;
	if (defined($self->{_resourceId})) {
		$resourceId = "$self->{_resourceId}";
	} else {
		$resourceId = undef;
	}

	return { id => $id, validityTo => $validityTo, description => $description, memberId => $memberId, resourceId =>
		$resourceId };
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

sub getMemberId
{
	my $self = shift;

	return $self->{_memberId};
}

sub setMemberId
{
	my $self = shift;
	$self->{_memberId} = shift;

	return;
}

sub getResourceId
{
	my $self = shift;

	return $self->{_resourceId};
}

sub setResourceId
{
	my $self = shift;
	$self->{_resourceId} = shift;

	return;
}

1;
