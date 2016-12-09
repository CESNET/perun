package Perun::beans::NotifTemplate;

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
		$id = undef;
	}

	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}

	my $primaryProperties;
	if (defined($self->{_primaryProperties})) {
		$primaryProperties = $self->{_primaryProperties};
	} else {
		$primaryProperties = undef;
	}

	my $notifyTrigger;
	if (defined($self->{_notifyTrigger})) {
		$notifyTrigger = "$self->{_notifyTrigger}";
	} else {
		$notifyTrigger = undef;
	}

	my $oldestMessageTime;
	if (defined($self->{_oldestMessageTime})) {
		$oldestMessageTime = $self->{_oldestMessageTime} * 1;
	} else {
		$oldestMessageTime = undef;
	}

	my $youngestMessageTime;
	if (defined($self->{_youngestMessageTime})) {
		$youngestMessageTime = $self->{_youngestMessageTime} * 1;
	} else {
		$youngestMessageTime = undef;
	}

	my $sender;
	if (defined($self->{_sender})) {
		$sender = "$self->{_sender}";
	} else {
		$sender = undef;
	}

	return { id             => $id, name => $name, primaryProperties => $primaryProperties,
		notifyTrigger       => $notifyTrigger, oldestMessageTime => $oldestMessageTime,
		youngestMessageTime => $youngestMessageTime,
		sender              => $sender };
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

sub getName
{
	my $self = shift;

	return $self->{_name};
}

sub setName
{
	my $self = shift;
	$self->{_name} = shift;

	return;
}

sub getPrimaryProperties
{
	my $self = shift;

	return $self->{_primaryProperties};
}

sub setPrimaryProperties
{
	my $self = shift;
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		my %hash = @_;
		$hash = \%hash;
	}

	$self->{_primaryProperties} = $hash;

	return;
}

sub getNotifyTrigger
{
	my $self = shift;

	return $self->{_notifyTrigger};
}

sub setNotifyTrigger
{
	my $self = shift;
	$self->{_notifyTrigger} = shift;

	return;
}

sub getOldestMessageTime
{
	my $self = shift;

	return $self->{_oldestMessageTime};
}

sub setOldestMessageTime
{
	my $self = shift;
	$self->{_oldestMessageTime} = shift;

	return;
}

sub getYoungestMessageTime
{
	my $self = shift;

	return $self->{_youngestMessageTime};
}

sub setYoungestMessageTime
{
	my $self = shift;
	$self->{_youngestMessageTime} = shift;

	return;
}

sub getSender
{
	my $self = shift;

	return $self->{_sender};
}

sub setSender
{
	my $self = shift;
	$self->{_sender} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $object = shift;

	my $hashInString = "";
	for (keys %{$object->getPrimaryProperties}) {
		$hashInString = $hashInString.$_."=".join("/", @{${$object->getPrimaryProperties}{$_}})."; ";
	}

	return ($object->getId, $object->getName, $hashInString, $object->getNotifyTrigger,
		$object->getOldestMessageTime, $object->getYoungestMessageTime, $object->getSender);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'Name', 'Primary Properties', 'Notify Trigger', 'Oldest Message Time', 'Youngest Message Time',
		'Sender');
}

1;
