package Perun::beans::AttributeRights;

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

	my $attributeId;
	if (defined($self->{_attributeId})) {
		$attributeId = $self->{_attributeId} * 1;
	} else {
		$attributeId = 0;
	}

	my $role;
	if (defined($self->{_role})) {
		$role = "$self->{_role}";
	} else {
		$role = undef;
	}

	my @rights;
	if (defined($self->{_rights})) {
		@rights = @{$self->{_rights}};
	} else {
		@rights = undef;
	}

	return { attributeId => $attributeId, role => $role, rights => \@rights };
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

sub getRights {
	my $self = shift;
	return @{$self->{_rights}};
}

sub setRights
{
	my $self = shift;
	$self->{_rights} = shift;

	return;
}

1;
