package Perun::beans::Sponsor;

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

	return { user => $self->{_user} };
}

sub getUserId {
        return shift->{_user}->{id};
}

sub getUserFirstName {
        return shift->{_user}->{firstName};
}

sub getUserLastName {
        return shift->{_user}->{lastName};
}

sub getValidytyTo {
	return shift->{_validytyTo};
}	


# used only for sorting purpose: LastName FirstName MiddleName
sub getSortingName {
	my $self = shift;
	return (($self->{_user}->{_lastName} ? $self->{_user}->{_lastName}.' ' : "").($self->{_user}->{_firstName} ? $self->{_user}->{_firstName}.' ' : "").($self->{_user}->{_middleName} ? $self->{_user}->{_middleName}.' ' : ""));
}

sub getCommonArrayRepresentation {
	my $user = shift;
	return ($user->getId, $user->getDisplayName, $user->isServiceUserToPrint, $user->isSponsoredUserToPrint);
}

1;
