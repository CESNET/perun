package Perun::GroupsAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'groupsManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub createGroup
{
	return Perun::Common::callManagerMethod('createGroup', 'Group', @_);
}

sub deleteGroup
{
	return Perun::Common::callManagerMethod('deleteGroup', 'null', @_);
}

sub updateGroup
{
	return Perun::Common::callManagerMethod('updateGroup', 'Group', @_);
}

sub getGroupById
{
	return Perun::Common::callManagerMethod('getGroupById', 'Group', @_);
}

sub getGroupByName
{
	return Perun::Common::callManagerMethod('getGroupByName', 'Group', @_);
}

sub addMember
{
	return Perun::Common::callManagerMethod('addMember', 'null', @_);
}

sub removeMember
{
	return Perun::Common::callManagerMethod('removeMember', 'null', @_);
}

sub getGroupMembers
{
	return Perun::Common::callManagerMethod('getGroupMembers', '[]Member', @_);
}

sub getGroupRichMembers
{
	return Perun::Common::callManagerMethod('getGroupRichMembers', '[]RichMember', @_);
}

sub getGroupRichMembersWithAttributes
{
	return Perun::Common::callManagerMethod('getGroupRichMembersWithAttributes', '[]RichMember', @_);
}

sub getGroupMembersCount
{
	return Perun::Common::callManagerMethod('getGroupMembersCount', 'number', @_);
}

sub getAllGroups
{
	return Perun::Common::callManagerMethod('getAllGroups', '[]Group', @_);
}

sub getParentGroup
{
	return Perun::Common::callManagerMethod('getParentGroup', 'Group', @_);
}

sub getSubGroups
{
	return Perun::Common::callManagerMethod('getSubGroups', '[]Group', @_);
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

sub getRichAdminsWithAttributes
{
	return Perun::Common::callManagerMethod('getRichAdminsWithAttributes', '[]RichUser', @_);
}

sub getAdminGroups
{
	return Perun::Common::callManagerMethod('getAdminGroups', '[]Group', @_);
}

sub getGroups
{
	return Perun::Common::callManagerMethod('getGroups', '[]Group', @_);
}

sub getGroupsCount
{
	return Perun::Common::callManagerMethod('getGroupsCount', 'number', @_);
}

sub getSubGroupsCount
{
	return Perun::Common::callManagerMethod('getSubGroupsCount', 'number', @_);
}

sub deleteAllGroups
{
	return Perun::Common::callManagerMethod('deleteAllGroups', '', @_);
}

sub forceGroupSynchronization
{
	return Perun::Common::callManagerMethod('forceGroupSynchronization', 'null', @_);
}

sub createGroupUnion
{
	return Perun::Common::callManagerMethod('createGroupUnion', 'Group', @_);
}

sub removeGroupUnion
{
	return Perun::Common::callManagerMethod('removeGroupUnion', '', @_);
}

sub getGroupUnions
{
	return Perun::Common::callManagerMethod('getGroupUnions', '[]Group', @_);
}

sub isGroupMember
{
	return Perun::Common::callManagerMethod('isGroupMember', '' , @_);
}

sub moveGroup
{
	return Perun::Common::callManagerMethod('moveGroup', '', @_);
}

sub getMemberGroups
{
	return Perun::Common::callManagerMethod('getMemberGroups', '[]Group', @_);
}

1;
