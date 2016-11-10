package Perun::beans::Vo;

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

	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}

	my $shortName;
	if (defined($self->{_shortName})) {
		$shortName = "$self->{_shortName}";
	} else {
		$shortName = undef;
	}

	return { id => $id, name => $name, shortName => $shortName, beanName => "Vo" };
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

sub getShortName
{
	my $self = shift;

	return $self->{_shortName};
}

sub setShortName
{
	my $self = shift;
	$self->{_shortName} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_shortName}, $self->{_name});
}

sub getCommonArrayRepresentationHeading {
	return ('VO id', 'VO short name', 'VO name');
}


1;
