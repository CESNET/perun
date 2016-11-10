package Perun::BanOnResourceAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'resourcesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub setBan
{
	return Perun::Common::callManagerMethod('setBan', 'BanOnResource', @_);
}

sub removeBan
{
	return Perun::Common::callManagerMethod('removeBan', 'null', @_);
}

sub updateBan
{
	return Perun::Common::callManagerMethod('updateBan', 'BanOnResource', @_);
}

sub getBanById
{
	return Perun::Common::callManagerMethod('getBanById', 'BanOnResource', @_);
}

sub getBan
{
	return Perun::Common::callManagerMethod('getBan', 'BanOnResource', @_);
}

sub getBansForResource
{
	return Perun::Common::callManagerMethod('getBansForResource', '[]BanOnResource', @_);
}

sub getBansForMember
{
	return Perun::Common::callManagerMethod('getBansForMember', '[]BanOnResource', @_);

}

1;
