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
		$id = $self->{_id}*1;
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
    $loa = $self->{_loa}*1;
  } else {
    $loa = 0;
  }

	my $extSource = $self->{_extSource};

	return {id => $id, login => $login, loa => $loa, extSource => $extSource};
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

sub getExtSource
{
	my $self = shift;

    if ( ref($self->{_extSource}) eq 'HASH' ) {
    	return Perun::beans::ExtSource->fromHash($self->{_extSource});
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
  return ($self->getId, $self->getLogin, $self->getExtSource->getName, $self->getExtSource->getId, $self->getLoa, $self->getExtSource->getType);
}

sub getCommonArrayRepresentationHeading {
  return ('UserExtSource id','Login','ExtSource name', 'ExtSource id', 'LoA', 'Type');
}

1;