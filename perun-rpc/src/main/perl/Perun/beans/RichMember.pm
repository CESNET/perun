package Perun::beans::RichMember;

use strict;
use warnings;
use 5.010;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
        my $richMember = Perun::Common::fromHash(@_);
        for my $memberAttribute (@{$richMember->{_memberAttributes}}) {
            $memberAttribute = Perun::beans::Attribute::fromHash("Perun::beans::Attribute", $memberAttribute);
        }
        for my $userAttribute (@{$richMember->{_userAttributes}}) {
            $userAttribute = Perun::beans::Attribute::fromHash("Perun::beans::Attribute", $userAttribute);
        }
	return $richMember;
}

sub TO_JSON
{
	my $self = shift;

	return {user => $self->{_user}, userAttributes => $self->{_userAttributes}, userExtSources => $self->{_userExtSources}, memberAttributes => $self->{_memberAttributes}};
}

sub getUserId {
	return shift->{_user}->{id};
}

sub getMemberId {
	return shift->{_id};
}

sub getUserAttributes {
        my $self = shift;
	return @{$self->{_userAttributes}};
}

sub getMemberAttributes {
        my $self = shift;
	return @{$self->{_memberAttributes}};
}

sub getLastName {
       my $user = shift->{_user};
       my $str = "";
       $str .= $user->{lastName}          if defined $user->{lastName};

       return $str;
}

sub getStatus {
        return shift->{_status};
}

sub getMembershipType {
	return shift->{_membershipType};
}

sub getCommonName {
	my $user = shift->{_user};

	my $str = "";
	$str .= $user->{firstName} . ' '   if defined $user->{firstName};
	$str .= $user->{middleName} . ' '  if defined $user->{middleName};
	$str .= $user->{lastName}          if defined $user->{lastName};

	return $str;
}

sub getDisplayName {
	my $user = shift->{_user};

	my $str = "";
	$str .= $user->{titleBefore} . ' ' if defined $user->{titleBefore};
	$str .= $user->{firstName} . ' '   if defined $user->{firstName};
	$str .= $user->{middleName} . ' '  if defined $user->{middleName};
	$str .= $user->{lastName} . ' '    if defined $user->{lastName};
	$str .= $user->{titleAfter}        if defined $user->{titleAfter};

	return $str;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_user}->{id}, $self->getDisplayName, $self->{_status}, $self->{_membershipType});
}

sub getCommonArrayRepresentationHeading {
	return ('Member Id', 'User Id', 'Name', 'Status', 'Membership type');
}

1;
