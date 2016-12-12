package Perun::beans::Category;

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

	my $rank;
	if (defined($self->{_rank})) {
		$rank = $self->{_rank} * 1;
	} else {
		$rank = 0;
	}

	return { id => $id, name => $name, rank => $rank };
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

sub getRank
{
	my $self = shift;

	return $self->{_rank};
}

sub setRank
{
	my $self = shift;
	$self->{_rank} = shift;

	return;
}

1;
