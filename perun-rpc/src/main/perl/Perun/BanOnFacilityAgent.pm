package Perun::BanOnFacilityAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'facilitiesManager';

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
	return Perun::Common::callManagerMethod('setBan', 'BanOnFacility', @_);
}

sub removeBan
{
	return Perun::Common::callManagerMethod('removeBan', 'null', @_);
}

sub updateBan
{
	return Perun::Common::callManagerMethod('updateBan', 'BanOnFacility', @_);
}

sub getBanById
{
	return Perun::Common::callManagerMethod('getBanById', 'BanOnFacility', @_);
}

sub getBan
{
	return Perun::Common::callManagerMethod('getBan', 'BanOnFacility', @_);
}

sub getBansForFacility
{
	return Perun::Common::callManagerMethod('getBansForFacility', '[]BanOnFacility', @_);
}

sub getBansForUser
{
	return Perun::Common::callManagerMethod('getBansForUser', '[]BanOnFacility', @_);

}

1;
