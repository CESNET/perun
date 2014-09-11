package Perun::MembersAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'membersManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub deleteMember
{
	return Perun::Common::callManagerMethod('deleteMember', '', @_);
}

sub createMember
{
	return Perun::Common::callManagerMethod('createMember', 'Member', @_);
}

sub getMemberByUserExtSource
{
	return Perun::Common::callManagerMethod('getMemberByUserExtSource', 'Member', @_);
}

sub getMemberById
{
	return Perun::Common::callManagerMethod('getMemberById', 'Member', @_);
}

sub getMemberByUser
{
	return Perun::Common::callManagerMethod('getMemberByUser', 'Member', @_);
}

sub getMembers
{
	return Perun::Common::callManagerMethod('getMembers', '[]Member', @_);
}

sub getMembersByUser
{
	return Perun::Common::callManagerMethod('getMembersByUser', '[]Member', @_);
}

sub getRichMembers
{
	return Perun::Common::callManagerMethod('getRichMembers', '[]RichMember', @_);
}

sub getRichMembersWithAttributes
{
	return Perun::Common::callManagerMethod('getRichMembersWithAttributes', '[]RichMember', @_);
}

sub getMembersCount
{
	return Perun::Common::callManagerMethod('getMembersCount', '', @_);
}

sub deleteAllMembers
{
	return Perun::Common::callManagerMethod('deleteAllMembers', '', @_);
}

sub setStatus
{
	return Perun::Common::callManagerMethod('setStatus', 'Member', @_);
}
sub validateMember
{
	return Perun::Common::callManagerMethod('validateMember','Member',@_);
}
sub validateMemberAsync
{
	return Perun::Common::callManagerMethod('validateMemberAsync','Member',@_);
}
1;
