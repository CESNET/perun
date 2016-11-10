package Perun::beans::Host;

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

	my $hostname;
	if (defined($self->{_hostname})) {
		$hostname = "$self->{_hostname}";
	} else {
		$hostname = undef;
	}

	return { id => $id, hostname => $hostname };
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

sub getHostname
{
	my $self = shift;

	return $self->{_hostname};
}

sub setHostname
{
	my $self = shift;
	$self->{_hostname} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getId, $self->getHostname);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name');
}

1;
