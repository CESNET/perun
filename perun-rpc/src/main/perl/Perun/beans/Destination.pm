package Perun::beans::Destination;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $destination = $self->{_destination};
	my $type = $self->{_type};

	my $str = 'Destination (';
	$str .= "id: $id, " if ($id);
	$str .= "destination: $destination, " if ($destination);
	$str .= "type: $type" if ($type);
	$str .= ')';

	return $str;
}

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

	my $destination;
	if (defined($self->{_destination})) {
		$destination = "$self->{_destination}";
	} else {
		$destination = undef;
	}

	my $type;
	if (defined($self->{_type})) {
		$type = "$self->{_type}";
	} else {
		$type = undef;
	}

	return { id => $id, destination => $destination, type => $type };
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

sub getDestination
{
	my $self = shift;

	return $self->{_destination};
}

sub setDestination
{
	my $self = shift;
	$self->{_destination} = shift;

	return;
}

sub getType
{
	my $self = shift;

	return $self->{_type};
}

sub setType
{
	my $self = shift;
	$self->{_type} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_destination}, $self->{_type});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Destination', 'Type');
}

1;
