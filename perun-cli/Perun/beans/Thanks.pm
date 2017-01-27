package Perun::beans::Thanks;

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

	my $publicationId;
	if (defined($self->{_publicationId})) {
		$publicationId = $self->{_publicationId} * 1;
	} else {
		$publicationId = 0;
	}

	my $ownerId;
	if (defined($self->{_ownerId})) {
		$ownerId = $self->{_ownerId} * 1;
	} else {
		$ownerId = 0;
	}

	my $createdBy;
	if (defined($self->{_createdBy})) {
		$createdBy = "$self->{_createdBy}";
	} else {
		$createdBy = undef;
	}

	my $createdDate;
	if (defined($self->{_createdDate})) {
		$createdDate = "$self->{_createdDate}";
	} else {
		$createdDate = undef;
	}

	return { id => $id, publicationId => $publicationId, ownerId => $userId, createdBy => $createdBy, createdDate =>
		$createdDate };
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

sub getPublicationId
{
	my $self = shift;

	return $self->{_publicationId};
}

sub setPublicationId
{
	my $self = shift;
	$self->{_publicationId} = shift;

	return;
}

sub getOwnerId
{
	my $self = shift;

	return $self->{ownerId};
}

sub setOwnerId
{
	my $self = shift;
	$self->{ownerId} = shift;

	return;
}

sub getCreatedBy
{
	my $self = shift;

	return $self->{createdBy};
}

sub setCreatedBy
{
	my $self = shift;
	$self->{createdBy} = shift;

	return;
}

sub getCreatedDate
{
	my $self = shift;

	return $self->{createdDate};
}

sub setCreatedDate
{
	my $self = shift;
	$self->{createdDate} = shift;

	return;
}

1;
