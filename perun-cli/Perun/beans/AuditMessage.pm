package Perun::beans::AuditMessage;

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

	my $event;
	if (defined($self->{_event})) {
		$event = $self->{_event};
	} else {
		$event = undef;
	}

	my $uimessage;
	if (defined($self->{_uimessage})) {
		$uimessage = "$self->{_uimessage}";
	} else {
		$uimessage = undef;
	}

	my $actor;
	if (defined($self->{_actor})) {
		$actor = "$self->{_actor}";
	} else {
		$actor = undef;
	}

	my $createdAt;
	if (defined($self->{_createdAt})) {
		$createdAt = "$self->{_createdAt}";
	} else {
		$createdAt = undef;
	}

	return { id => $id, event => $event, uimessage => $uimessage, actor => $actor, createdAt => $createdAt };
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

sub getEvent
{
	my $self = shift;
	return $self->{_event};
}

sub setEvent
{
	my $self = shift;
	$self->{_event} = shift;

	return;
}

sub getUiMessage
{
	my $self = shift;
	return $self->{_uimessage};
}

sub getActor
{
	my $self = shift;

	return $self->{_actor};
}

sub setActor
{
	my $self = shift;
	$self->{_actor} = shift;

	return;
}

sub getCreatedAt
{
	my $self = shift;

	return $self->{_createdAt};
}

sub setCreatedAt
{
	my $self = shift;
	$self->{_createdAt} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getId, $self->getActor, $self->getUiMessage, $self->getCreatedAt);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Actor', 'Msg', 'CreatedAt');
}

1;
