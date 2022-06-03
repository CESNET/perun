package Perun::beans::RichUser;

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

	return { id => $self->{_id}, uuid => $self->{_uuid}, userAttributes => $self->{_userAttributes}, userExtSources =>
		$self->{_userExtSources} };
}

sub getId {
	return shift->{_id};
}

sub setId
{
	my $self = shift;
	$self->{_id} = shift;

	return;
}

sub getUuid
{
	return shift->{_uuid};
}

sub setUuid
{
  my $self = shift;
  $self->{_uuid} = shift;

  return;
}

sub getUserAttributes {
	return shift->{_userAttributes};
}

sub getLastName {
	return shift->{_lastName};
}

sub getCommonName
{
	my $self = shift;
	my $commonName = $self->{_firstName}.' '.(defined $self->{_middleName} ? $self->{_middleName}.' ' : '').$self->{_lastName};
	$commonName=~ s/^\s+|\s+$//g;
	return $commonName;
}

sub getDisplayName
{
	my $self = shift;
	my $displayName = ($self->{_titleBefore} ? $self->{_titleBefore}.' ' : "").($self->{_firstName} ? $self->{_firstName}.' ' : "").($self->{_middleName} ? $self->{_middleName}.' ' : "").($self->{_lastName} ? $self->{_lastName}.' ' : "").($self->{_titleAfter} ? $self->{_titleAfter} : "");
	$displayName =~ s/^\s+|\s+$//g;
	return $displayName;
}


sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->getDisplayName);
}

sub getCommonArrayRepresentationHeading {
	return ('User Id', 'Name');
}

1;
