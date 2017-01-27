package Perun::beans::Author;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $author = Perun::Common::fromHash(@_);

	for my $authorship (@{$author->{_authorships}}) {
		$authorship = Perun::beans::Authorship::fromHash("Perun::beans::Authorship", $authorship);
	}
	return $author;

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

	my $firstName;
	if (defined($self->{_firstName})) {
		$firstName = "$self->{_firstName}";
	} else {
		$firstName = undef;
	}

	my $lastName;
	if (defined($self->{_lastName})) {
		$lastName = "$self->{_lastName}";
	} else {
		$lastName = undef;
	}

	my $middleName;
	if (defined($self->{_middleName})) {
		$middleName = "$self->{_middleName}";
	} else {
		$middleName = undef;
	}

	my $titleBefore;
	if (defined($self->{_titleBefore})) {
		$titleBefore = "$self->{_titleBefore}";
	} else {
		$titleBefore = undef;
	}

	my $titleAfter;
	if (defined($self->{_titleAfter})) {
		$titleAfter = "$self->{_titleAfter}";
	} else {
		$titleAfter = undef;
	}

	my @authorships;
	if (defined($self->{_authorships})) {
		@authorships = "$self->{_authorships}";
	} else {
		@authorships = undef;
	}

	return { id                  => $id, firstName => $firstName, lastName => $lastName, middleName => $middleName,
		titleBefore              => $titleBefore, titleAfter => $titleAfter,
		authorships              => \@authorships };
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

sub getFirstName
{
	my $self = shift;

	return $self->{_firstName};
}

sub setFirstName
{
	my $self = shift;
	$self->{_firstName} = shift;

	return;
}

sub getMiddleName
{
	my $self = shift;

	return $self->{_middleName};
}

sub setMiddleName
{
	my $self = shift;
	$self->{_middleName} = shift;

	return;
}

sub getLastName
{
	my $self = shift;

	return $self->{_lastName};
}

sub setLastName
{
	my $self = shift;
	$self->{_lastName} = shift;

	return;
}

sub getTitleBefore
{
	my $self = shift;

	return $self->{_titleBefore};
}

sub setTitleBefore
{
	my $self = shift;
	$self->{_titleBefore} = shift;

	return;
}

sub getTitleAfter
{
	my $self = shift;

	return $self->{_titleAfter};
}

sub setTitleAfter
{
	my $self = shift;
	$self->{_titleAfter} = shift;

	return;
}

sub getAuthorships
{
	my $self = shift;

	return @{$self->{_authorships}};
}

1;
