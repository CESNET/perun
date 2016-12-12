package Perun::beans::Group;

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

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	my $voId;
	if (defined($self->{_voId})) {
		$voId = $self->{_voId} * 1;
	} else {
		$voId = 0;
	}

	return { id => $id, name => $name, description => $description, voId => $voId, beanName => "Group" };
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

sub getVoId
{
	my $self = shift;
	return $self->{_voId};
}

sub setVoId
{
	my $self = shift;
	$self->{_voId} = shift;
}
sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_voId}, $self->{_name}, $self->{_description});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'VO ID', 'Name', 'Description');
}
1;
