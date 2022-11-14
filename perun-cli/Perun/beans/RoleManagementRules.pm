package Perun::beans::RoleManagementRules;

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

	my $roleName;
	if (defined($self->{_roleName})) {
		$roleName = "$self->{_roleName}";
	} else {
		$roleName = undef;
	}

	my $primaryObject;
	if (defined($self->{_primaryObject})) {
		$primaryObject = "$self->{_primaryObject}";
	} else {
		$primaryObject = undef;
	}

	my @privilegedRolesToManage;
	if (defined($self->{_privilegedRolesToManage})) {
		@privilegedRolesToManage = @{%{$self->{_privilegedRolesToManage}}};
	} else {
		@privilegedRolesToManage = undef;
	}

	my @privilegedRolesToRead;
	if (defined($self->{_privilegedRolesToRead})) {
		@privilegedRolesToRead = @{%{$self->{_privilegedRolesToRead}}};
	} else {
		@privilegedRolesToRead = undef;
	}

	my %entitiesToManage;
	if (defined($self->{_entitiesToManage})) {
		%entitiesToManage = %{$self->{_entitiesToManage}};
	} else {
		%entitiesToManage = undef
	}

	my %assignedObjects;
	if (defined($self->{_assignedObjects})) {
		%assignedObjects = %{$self->{_assignedObjects}};
	} else {
		%assignedObjects = undef
	}

	my @assignmentCheck;
	if (defined($self->{_assignmentCheck})) {
		@assignmentCheck = @{%{$self->{_assignmentCheck}}};
	} else {
		@assignmentCheck = undef;
	}

	my @associatedReadRoles;
	if (defined($self->{_associatedReadRoles})) {
		@associatedReadRoles = @{$self->{_associatedReadRoles}};
	} else {
		@associatedReadRoles = undef;
	}

	my $assignableToAttributes;
	if (defined($self->{_assignableToAttributes})) {
		$assignableToAttributes = $self->{_assignableToAttributes};
	} else {
		$assignableToAttributes = undef;
	}

	my $skipMFA;
	if (defined($self->{_skipMFA})) {
		$skipMFA = $self->{_skipMFA};
	} else {
		$skipMFA = undef;
	}

	my $mfaCriticalRole;
	if (defined($self->{_mfaCriticalRole})) {
		$mfaCriticalRole = $self->{_mfaCriticalRole};
	} else {
		$mfaCriticalRole = undef;
	}

	my $displayName;
	if (defined($self->{_displayName})) {
		$displayName = "$self->{_displayName}";
	} else {
		$displayName = undef;
	}

	return { roleName => $roleName, primaryObject => $primaryObject, privilegedRolesToManage => \@privilegedRolesToManage,
		privilegedRolesToRead => \@privilegedRolesToRead, entitiesToManage => \%entitiesToManage, assignedObjects => \%assignedObjects,
		assignmentCheck => \@assignmentCheck, associatedReadRoles => \@associatedReadRoles, assignableToAttributes => $assignableToAttributes,
		skipMFA => $skipMFA, mfaCriticalRole => $mfaCriticalRole, displayName => $displayName };

}

sub getRoleName
{
	my $self = shift;

	return $self->{_roleName};
}

sub setRoleName
{
	my $self = shift;
	$self->{_roleName} = shift;

	return;
}

sub getPrimaryObject
{
	my $self = shift;

	return $self->{_primaryObject};
}

sub setPrimaryObject
{
	my $self = shift;
	$self->{_primaryObject} = shift;

	return;
}

sub getPrivilegedRolesToManage
{
	my $self = shift;

	return @{$self->{_privilegedRolesToManage}};
}

sub setPrivilegedRolesToManage
{
	my $self = shift;
	$self->{_privilegedRolesToManage} = shift;

	return;
}

sub getPrivilegedRolesToRead
{
	my $self = shift;

	return @{$self->{_privilegedRolesToRead}};
}

sub setPrivilegedRolesToRead
{
	my $self = shift;
	$self->{_privilegedRolesToRead} = shift;

	return;
}

sub getEntitiesToManage
{
	my $self = shift;

	return $self->{_entitiesToManage};
}

sub setEntitiesToManage
{
	my $self = shift;
	$self->{_entitiesToManage} = shift;

	return;
}

sub getAssignedObjects
{
	my $self = shift;

	return $self->{_assignedObjects};
}

sub setAssignedObjects
{
	my $self = shift;
	$self->{_assignedObjects} = shift;

	return;
}

sub getAssignmentCheck
{
	my $self = shift;

	return @{$self->{_assignmentCheck}};
}

sub setAssignmentCheck
{
	my $self = shift;
	$self->{_assignmentCheck} = shift;

	return;
}

sub getAssociatedReadRoles
{
	my $self = shift;

	return @{$self->{_associatedReadRoles}};
}

sub setAssociatedReadRoles
{
	my $self = shift;
	$self->{_associatedReadRoles} = shift;

	return;
}

sub getAssignableToAttributes
{
	my $self = shift;

	return $self->{_assignableToAttributes};
}

sub setAssignableToAttributes
{
	my $self = shift;
	$self->{_assignableToAttributes} = shift;

	return;
}

sub getSkipMFA
{
	my $self = shift;

	return $self->{_skipMFA};
}

sub setSkipMFA
{
	my $self = shift;
	$self->{_skipMFA} = shift;

	return;
}

sub getMfaCriticalRole
{
	my $self = shift;

	return $self->{_mfaCriticalRole};
}

sub setMfaCriticalRole
{
	my $self = shift;
	$self->{_mfaCriticalRole} = shift;

	return;
}

sub getDisplayName
{
	my $self = shift;

	return $self->{_displayName};
}

sub setDisplayName
{
	my $self = shift;
	$self->{_displayName} = shift;

	return;
}

1;