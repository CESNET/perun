package Perun::SecurityTeamsAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'securityTeamsManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub createSecurityTeam
{
	return Perun::Common::callManagerMethod('createSecurityTeam', 'SecurityTeam', @_);
}

sub deleteSecurityTeam
{
	return Perun::Common::callManagerMethod('deleteSecurityTeam', 'null', @_);
}

sub updateSecurityTeam
{
	return Perun::Common::callManagerMethod('updateSecurityTeam', 'SecurityTeam', @_);
}

sub getSecurityTeamById
{
	return Perun::Common::callManagerMethod('getSecurityTeamById', 'SecurityTeam', @_);
}

sub getAllSecurityTeams
{
	return Perun::Common::callManagerMethod('getAllSecurityTeams', '[]SecurityTeam'
		, @_);
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

sub getAdminGroups
{
        return Perun::Common::callManagerMethod('getAdminGroups', '[]Group', @_);
}

sub addUserToBlacklist
{
	return Perun::Common::callManagerMethod('addUserToBlacklist', 'null', @_);
}

sub removeUserFromBlacklist
{
	return Perun::Common::callManagerMethod('removeUserFromBlacklist', 'null', @_);
}

sub getBlacklist
{
	return Perun::Common::callManagerMethod('getBlacklist', '[]User', @_);
}

1;
