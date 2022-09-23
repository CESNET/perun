package Perun::beans::AttributeRules;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $attributeRules = Perun::Common::fromHash(@_);
	foreach my $policyCollection (@{$attributeRules->{_attributePolicyCollections}}) {
		$policyCollection = Perun::beans::AttributePolicy::fromHash("Perun::beans::AttributePolicyCollection", $policyCollection);
		foreach my $policy (@{$policyCollection->{_policies}}) {
			$policy = Perun::beans::AttributePolicy::fromHash("Perun::beans::AttributePolicy", $policy);
		}
	}

	return $attributeRules;
}

sub TO_JSON
{
	my $self = shift;

	my @policyCollections;
	if (defined($self->{_attributePolicyCollections})) {
		@policyCollections = @{$self->{_attributePolicyCollections}};
	} else {
		@policyCollections = undef;
	}

	my @criticalActions;
	if (defined($self->{_criticalActions})) {
		@criticalActions = @{$self->{_criticalActions}};
	} else {
		@criticalActions = undef;
	}

	return { attributePolicyCollections => \@policyCollections, criticalActions => \@criticalActions };
}

sub getAttributePolicyCollections {
	my $self = shift;
	return @{$self->{_attributePolicyCollections}};
}

sub setAttributePolicyCollections
{
	my $self = shift;
	$self->{_attributePolicyCollections} = shift;

	return;
}

sub getCriticalActions {
	my $self = shift;
	return @{$self->{_criticalActions}};
}

sub setCriticalActions
{
	my $self = shift;
	$self->{_criticalActions} = shift;

	return;
}

1;
