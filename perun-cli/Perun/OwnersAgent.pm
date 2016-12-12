package Perun::OwnersAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'ownersManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#(owner => $owner)
sub createOwner
{
	return Perun::Common::callManagerMethod('createOwner', 'Owner', @_);
}

#(owner => $ownerId)
sub deleteOwner
{
	return Perun::Common::callManagerMethod('deleteOwner', '', @_);
}

#(id => $number)
sub getOwnerById
{
	return Perun::Common::callManagerMethod('getOwnerById', 'Owner', @_);
}

sub getOwners
{
	return Perun::Common::callManagerMethod('getOwners', '[]Owner', @_);
}

1;
