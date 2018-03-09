package Perun::beans::UserExtSource;

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

	my $login;
	if (defined($self->{_login})) {
		$login = "$self->{_login}";
	} else {
		$login = undef;
	}

	my $loa;
	if (defined($self->{_loa})) {
		$loa = $self->{_loa} * 1;
	} else {
		$loa = 0;
	}

	my $persistent;
	if (defined($self->{_persistent})) {
		$persistent = $self->{_persistent};
	} else {
		$persistent = undef;
	}

	my $userId;
	if (defined($self->{_userId})) {
		$userId = $self->{_userId} * 1;
	} else {
		$userId = 0;
	}

	my $extSource = $self->{_extSource};

	return { id => $id, login => $login, loa => $loa, userId => $userId, persistent => $persistent, extSource => $extSource };
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

sub getLoa
{
	my $self = shift;

	return $self->{_loa};
}

sub setLoa
{
	my $self = shift;
	$self->{_loa} = shift;

	return;
}

sub getLogin
{
	my $self = shift;

	return $self->{_login};
}

sub setLogin
{
	my $self = shift;
	$self->{_login} = shift;

	return;
}

sub isPersistentToPrint
{
	my $self = shift;
	return ($self->{_persistent}) ? 'true' : 'false';
}

sub isPersistent
{
	my $self = shift;
	return ($self->{_persistent}) ? 1 : 0;
}

sub setPersistent
{
	my $self = shift;
	my $value = shift;
	if (ref $value eq "JSON::XS::Boolean")
	{
		$self->{_persistent} = $value;
	} elsif ($value eq 'true' || $value eq 1)
	{
		$self->{_persistent} = JSON::XS::true;
	} else
	{
		$self->{_persistent} = JSON::XS::false;
	}

	return;
}

sub getUserId
{
	my $self = shift;

	return $self->{_userId};
}

sub setUserId
{
	my $self = shift;
	$self->{_userId} = shift;

	return;
}

sub getExtSource
{
	my $self = shift;

	if (ref($self->{_extSource}) eq 'HASH') {
		return Perun::beans::ExtSource->fromHash( $self->{_extSource} );
	} else {
		return $self->{_extSource};
	}
}

sub setExtSource
{
	my $self = shift;
	$self->{_extSource} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getId, $self->getLogin, $self->getExtSource->getName, $self->getExtSource->getId, $self->getLoa,
		$self->getExtSource->getType);
}

sub getCommonArrayRepresentationHeading {
	return ('UserExtSource id', 'Login', 'ExtSource name', 'ExtSource id', 'LoA', 'Type');
}

1;
