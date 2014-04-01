package Perun::UsersAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'usersManager';

use fields qw(_agent _manager);

sub new
{
    my $self = fields::new(shift);
    $self->{_agent} = shift;
    $self->{_manager} = $manager;

    return $self;
}

sub getUserByUserExtSource
{
    return Perun::Common::callManagerMethod('getUserByUserExtSource', 'User', @_);
}

sub getUserByExtSourceNameAndExtLogin
{
    return Perun::Common::callManagerMethod('getUserByExtSourceNameAndExtLogin', 'User', @_);
}

sub getUserById
{
    return Perun::Common::callManagerMethod('getUserById', 'User', @_);
}

sub getUserByMember
{
    return Perun::Common::callManagerMethod('getUserByMember', 'User', @_);
}

sub getUsers
{
    return Perun::Common::callManagerMethod('getUsers', '[]User', @_);
}

sub createUser
{
    return Perun::Common::callManagerMethod('createUser', 'User', @_);
}

sub deleteUser
{
    return Perun::Common::callManagerMethod('deleteUser', 'null', @_);
}

sub updateUser
{
    return Perun::Common::callManagerMethod('updateUser', 'User', @_);
}

sub getUserExtSources
{
    return Perun::Common::callManagerMethod('getUserExtSources', '[]UserExtSource', @_);
}

sub addUserExtSource
{
    return Perun::Common::callManagerMethod('addUserExtSource', 'UserExtSource', @_);
}

sub removeUserExtSource
{
    return Perun::Common::callManagerMethod('removeUserExtSource', 'null', @_);
}

sub getUserExtSourceByExtLogin
{
    return Perun::Common::callManagerMethod('getUserExtSourceByExtLogin', 'UserExtSource', @_);
}

sub getVosWhereUserIsAdmin
{
    return Perun::Common::callManagerMethod('getVosWhereUserIsAdmin', '[]Vo', @_);
}

sub getVosWhereUserIsMember
{
    return Perun::Common::callManagerMethod('getVosWhereUserIsMember', '[]Vo', @_);
}

sub getGroupsWhereUserIsAdmin
{
    return Perun::Common::callManagerMethod('getGroupsWhereUserIsAdmin', '[]Group', @_);
}

sub getAllowedResources
{
    return Perun::Common::callManagerMethod('getAllowedResources', '[]Resource', @_);
}

sub findUsersByName
{
    return Perun::Common::callManagerMethod('findUsersByName', '[]User', @_);
}

sub findUsers
{
    return Perun::Common::callManagerMethod('findUsers', '[]User', @_);
}

sub isLoginAvailable
{
		return Perun::Common::callManagerMethod('isLoginAvailable', 'number', @_);
}

sub getUsersWithoutVoAssigned
{
		return Perun::Common::callManagerMethod('getUsersWithoutVoAssigned', '[]User', @_);
}

sub getUsersByAttribute
{
		return Perun::Common::callManagerMethod('getUsersByAttribute', '[]User', @_);
}

sub getUsersByAttributeValue
{
		return Perun::Common::callManagerMethod('getUsersByAttributeValue', '[]User', @_);
}
sub makeUserPerunAdmin
{
		return Perun::Common::callManagerMethod('makeUserPerunAdmin', 'null', @_);
}

1;