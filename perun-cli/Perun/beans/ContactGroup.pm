package Perun::beans::ContactGroup;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $contactGroup = Perun::Common::fromHash(@_);
	for my $owner (@{$contactGroup->{_owners}}) {
		$owner = Perun::beans::Owner::fromHash("Perun::beans::Owner", $owner);
	}
	for my $user (@{$contactGroup->{_users}}) {
		$user = Perun::beans::RichUser::fromHash("Perun::beans::RichUser", $user);
	}
	for my $group (@{$contactGroup->{_groups}}) {
		$group = Perun::beans::Group::fromHash("Perun::beans::Group", $group);
	}
	my $facility = $contactGroup->{_facility};
	$contactGroup->{_facility} = Perun::beans::Facility::fromHash("Perun::beans::Facility", $facility);
	return $contactGroup;
}

sub TO_JSON
{
	my $self = shift;

	return { name               => $self->{_name}, facility => $self->{_facility}, groups => $self->{_groups}, owners =>
		$self->{_owners}, users => $self->{_users} };
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

sub getFacility
{
	my $self = shift;

	return $self->{_facility};
}

sub setFacility
{
	my $self = shift;
	$self->{_facility} = shift;

	return;
}

sub getGroups
{
	my $self = shift;

	return @{$self->{_groups}};
}

sub setGroups
{
	my $self = shift;
	$self->{_groups} = shift;

	return;
}

sub getOwners
{
	my $self = shift;

	return @{$self->{_owners}};
}

sub setOwners
{
	my $self = shift;
	$self->{_owners} = shift;

	return;
}

sub getUsers
{
	my $self = shift;

	return @{$self->{_users}};
}

sub setUsers
{
	my $self = shift;
	$self->{_users} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_name}, $self->{_facility});
}

sub getCommonArrayRepresentationHeading {
	return ('Name', 'Facility');
}

1;
