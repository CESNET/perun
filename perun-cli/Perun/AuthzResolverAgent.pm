package Perun::AuthzResolverAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'authzResolver';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getPrincipalRoleNames
{
	return Perun::Common::callManagerMethod('getPrincipalRoleNames', '', @_);
}

sub isVoAdmin
{
	return Perun::Common::callManagerMethod('isVoAdmin', '', @_);
}

sub isGroupAdmin
{
	return Perun::Common::callManagerMethod('isGroupAdmin', '', @_);
}

sub isFacilityAdmin
{
	return Perun::Common::callManagerMethod('isFacilityAdmin', '', @_);
}

sub isPerunAdmin
{
	return Perun::Common::callManagerMethod('isPerunAdmin', '', @_);
}

sub setRole
{
	return Perun::Common::callManagerMethod('setRole', '', @_);
}

sub unsetRole
{
	return Perun::Common::callManagerMethod('unsetRole', '', @_);
}

sub getUserRoleNames
{
	return Perun::Common::callManagerMethod('getUserRoleNames', '', @_);
}

sub getGroupRoleNames
{
	return Perun::Common::callManagerMethod('getGroupRoleNames', '', @_);
}

1;
