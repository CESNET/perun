package Perun::beans::Candidate;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $candidate = Perun::Common::fromHash(@_);
	for my $aues (@{$candidate->{_additionalUserExtSources}}) {
		$aues = Perun::beans::UserExtSource::fromHash("Perun::beans::UserExtSource", $aues);
	}
	return $candidate;
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

	my $userExtSource = $self->{_userExtSource};
	my $additionalUserExtSources = $self->{_additionalUserExtSources};
	my $attributes = $self->{_attributes};

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

	return { id    => $id, userExtSource => $userExtSource, additionalUserExtSources => $additionalUserExtSources,
		attributes => $attributes, firstName => $firstName, lastName => $lastName,
		middleName => $middleName, titleBefore => $titleBefore, titleAfter => $titleAfter };
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

sub getCommonName
{
	my $self = shift;

	return ($self->{_firstName}.' '.$self->{_middleName}.' '.$self->{_lastName});
}

sub getDisplayName
{
	my $self = shift;

	return ($self->{_titleBefore}.' '.$self->{_firstName}.' '.$self->{_middleName}.' '.$self->{_lastName}.' '.$self->{_titleAfter});
}

sub getUserExtSource
{
	my $self = shift;

	if (ref($self->{_userExtSource}) eq 'HASH') {
		return Perun::beans::UserExtSource->fromHash( $self->{_userExtSource} );
	} else {
		return $self->{_userExtSource};
	}
}

sub setUserExtSource
{
	my $self = shift;
	$self->{_userExtSource} = shift;

	return;
}

sub getAdditionalUserExtSources
{
	my $self = shift;

	if (ref($self->{_additionalUserExtSources}) eq 'HASH') {
		return Perun::beans::UserExtSource->fromHash( $self->{_additionalUserExtSources} );
	} else {
		return $self->{_additionalUserExtSources};
	}
}

sub setAdditionalUserExtSources
{
	my $self = shift;
	$self->{_additionalUserExtSources} = shift;

	return;
}

sub getAttributes
{
	my $self = shift;

	return $self->{_attributes};
}

sub setAttributes
{
	my $self = shift;
	$self->{_attributes} = shift;

	return;
}

1;
