package Perun::beans::AttributePolicyCollection;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $policyCollection = Perun::Common::fromHash(@_);
	foreach my $policy (@{$policyCollection->{_policies}}) {
		$policy = Perun::beans::AttributePolicy::fromHash("Perun::beans::AttributePolicy", $policy);
	}

	return $policyCollection;
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

	my $attributeId;
	if (defined($self->{_attributeId})) {
		$attributeId = $self->{_attributeId} * 1;
	} else {
		$attributeId = 0;
	}

	my $action;
	if (defined($self->{_action})) {
		$action = "$self->{_action}";
	} else {
		$action = undef;
	}

	my @policies;
	if (defined($self->{_policies})) {
		@policies = @{$self->{_policies}};
	} else {
		@policies = undef;
	}

	return { id => $id, attributeId => $attributeId, action => $action, policies => \@policies };
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

sub getAttributeId
{
	my $self = shift;

	return $self->{_attributeId};
}

sub setAttributeId
{
	my $self = shift;
	$self->{_attributeId} = shift;

	return;
}

sub getAction
{
	my $self = shift;

	return $self->{_action};
}

sub setAction
{
	my $self = shift;
	$self->{_action} = shift;

	return;
}

sub getPolicies {
	my $self = shift;
	return @{$self->{_policies}};
}

sub setPolicies
{
	my $self = shift;
	$self->{_policies} = shift;

	return;
}

sub addPolicy
{
	my $self = shift;
	push(@{$self->{_policies}}, shift);

	return;
}

1;
