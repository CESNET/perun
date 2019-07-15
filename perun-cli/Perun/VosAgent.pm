package Perun::VosAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'vosManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getVos
{
	return Perun::Common::callManagerMethod('getVos', '[]Vo', @_);
}

sub deleteVo
{
	return Perun::Common::callManagerMethod('deleteVo', 'null', @_);
}

sub createVo
{
	return Perun::Common::callManagerMethod('createVo', 'Vo', @_);
}

sub updateVo
{
	return Perun::Common::callManagerMethod('updateVo', 'Vo', @_);
}

sub getVoByShortName
{
	return Perun::Common::callManagerMethod('getVoByShortName', 'Vo', @_);
}

sub getVoById
{
	return Perun::Common::callManagerMethod('getVoById', 'Vo', @_);
}

sub findCandidates
{
	return Perun::Common::callManagerMethod('findCandidates', '[]Candidate', @_);
}

sub addAdmin
{
	return Perun::Common::callManagerMethod('addAdmin', 'null', @_);
}

sub removeAdmin
{
	return Perun::Common::callManagerMethod('removeAdmin', 'null', @_);
}

sub getAdmins
{
	return Perun::Common::callManagerMethod('getAdmins', '[]User', @_);
}

sub getRichAdmins
{
	return Perun::Common::callManagerMethod('getRichAdmins', '[]RichUser', @_);
}

sub getAdminGroups
{
	return Perun::Common::callManagerMethod('getAdminGroups', '[]Group', @_);
}

sub getRichAdminsWithAttributes
{
	return Perun::Common::callManagerMethod('getRichAdminsWithAttributes', '[]RichUser', @_);
}

sub addSponsorRole
{
	return Perun::Common::callManagerMethod('addSponsorRole', 'null', @_);
}
sub removeSponsorRole
{
	return Perun::Common::callManagerMethod('removeSponsorRole', 'null', @_);
}

1;
