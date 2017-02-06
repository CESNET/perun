package Perun::beans::Authorship;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $authorship = Perun::Common::fromHash(@_);
	return $authorship;
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

	my $userId;
	if (defined($self->{_userId})) {
		$userId = $self->{_userId} * 1;
	} else {
		$userId = 0;
	}

	my $createdBy;
	if (defined($self->{_createdBy})) {
		$createdBy = "$self->{_createdBy}";
	} else {
		$createdBy = undef;
	}

	my $createdDate;
	if (defined($self->{_createdDate})) {
		$createdDate = $self->{_createdDate};
	} else {
		$createdDate = undef;
	}

	return { id => $id, publicationId => $publicationId, userId => $userId, createdBy => $createdBy, createdDate =>
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

sub getCreatedBy
{
	my $self = shift;

	return $self->{_createdBy};
}

sub setCreatedBy
{
	my $self = shift;
	$self->{_createdBy} = shift;

	return;
}

sub getCreatedDate
{
	my $self = shift;

	return $self->{_createdDate};
}

sub setCreatedDate
{
	my $self = shift;
	$self->{_createdDate} = shift;

	return;
}

1;
