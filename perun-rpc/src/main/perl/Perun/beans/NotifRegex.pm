package Perun::beans::NotifRegex;

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

	my $regex;
	if (defined($self->{_regex})) {
		$regex = "$self->{_regex}";
	} else {
		$regex = undef;
	}

	my $note;
	if (defined($self->{_note})) {
		$note = "$self->{_note}";
	} else {
		$note = undef;
	}

	return { id => $id, regex => $regex, note => $note };
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

sub getRegex
{
	my $self = shift;

	return $self->{_regex};
}

sub setRegex
{
	my $self = shift;
	$self->{_regex} = shift;

	return;
}

sub getNote
{
	my $self = shift;

	return $self->{_note};
}

sub setNote
{
	my $self = shift;
	$self->{_note} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $object = shift;
	return ($object->getId, $object->getRegex, $object->getNote);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'Regex', 'Note');
}

1;
