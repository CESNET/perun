package Perun::beans::AttributePolicy;

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

	my $role;
	if (defined($self->{_role})) {
		$role = "$self->{_role}";
	} else {
		$role = undef;
	}

	my $object;
	if (defined($self->{_object})) {
		$object = "$self->{_object}";
	} else {
		$object = undef;
	}

	my $policyCollectionId;
	if (defined($self->{_policyCollectionId})) {
		$id = $self->{_policyCollectionId} * 1;
	}

	return { id => $id, role => $role, object => $object, policyCollectionId => $policyCollectionId };
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

sub getRole
{
	my $self = shift;

	return $self->{_role};
}

sub setRole
{
	my $self = shift;
	$self->{_role} = shift;

	return;
}

sub getObject
{
	my $self = shift;

	return $self->{_object};
}

sub setObject
{
	my $self = shift;
	$self->{_object} = shift;

	return;
}

sub getPolicyCollectionId
{
	my $self = shift;

	return $self->{_policyCollectionId};
}

sub setPolicyCollectionId
{
	my $self = shift;
	$self->{_policyCollectionId} = shift;

	return;
}

1;
