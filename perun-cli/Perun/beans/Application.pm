package Perun::beans::Application;
use strict;
use warnings;
use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $application = Perun::Common::fromHash(@_);
	my $vo = $application->{_vo};
	$vo = Perun::beans::Vo::fromHash("Perun::beans::Vo", $vo);
	my $group = $application->{_group};
	$group = Perun::beans::Group::fromHash("Perun::beans::Group", $group);
	my $user = $application->{_user};
	$user = Perun::beans::User::fromHash("Perun::beans::User", $user);
	return $application;
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

	my $vo;
	if (defined($self->{_vo})) {
		$vo = $self->{_vo};
	} else {
		$vo = undef;
	}

	my $group;
	if (defined($self->{_group})) {
		$group = $self->{_group};
	} else {
		$group = undef;
	}

	my $user;
	if (defined($self->{_user})) {
		$user = $self->{_user};
	} else {
		$user = undef;
	}

	my $type;
	if (defined($self->{_type})) {
		$type = $self->{_type};
	} else {
		$type = undef;
	}

	my $state;
	if (defined($self->{_state})) {
		$type = $self->{_state};
	} else {
		$type = undef;
	}

	my $autoApproveError;
	if (defined($self->{_autoApproveError})) {
		$autoApproveError = $self->{_autoApproveError};
	} else {
		$autoApproveError = undef;
	}

	return { id => $id, vo => $vo, group => $group, user => $user, type => $type, state => $state, autoApproveError => $autoApproveError };
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub getVo
{
	my $self = shift;

	return $self->{_vo};
}

sub getGroup
{
	my $self = shift;

	return $self->{_group};
}

sub getUser
{
	my $self = shift;

	return $self->{_user};
}

sub getType
{
	my $self = shift;

	return $self->{_type};
}

sub getState
{
	my $self = shift;

	return $self->{_state};
}

sub getAutoApproveError
{
	my $self = shift;

	return $self->{_autoApproveError};
}

sub getActor
{
	my $self = shift;

	return $self->{_createdBy};
}

sub getExtSourceType
{
	my $self = shift;

	return $self->{_extSourceType};
}

sub getExtSourceName
{
	my $self = shift;

	return $self->{_extSourceName};
}

sub getUserDisplayName
{
	my $self = shift;
	my $user = $self->{_user};
	$user = Perun::beans::User::fromHash("Perun::beans::User", $user);
	return $user->getDisplayName;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_type}, $self->{_state}, $self->{_vo}->{id} . " / " . $self->{_vo}->{shortName},
		((defined $self->{_group}) ? $self->{_group}->{id} . " / " . $self->{_group}->{name} : ""),
		((defined $self->{_user}) ? $self->getUserDisplayName : $self->{_createdBy} . " / " . $self->{_extSourceName}),
		((defined $self->{_autoApproveError}) ? $self->{_autoApproveError} : ""));
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Type', 'State', 'Vo', 'Group', 'Submitted by', 'Automatic approval error');
}

1;
