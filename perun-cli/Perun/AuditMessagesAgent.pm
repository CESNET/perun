package Perun::AuditMessagesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'auditMessagesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getMessages
{
	return Perun::Common::callManagerMethod('getMessages', '[]AuditMessage', @_);
}

sub log
{
	return Perun::Common::callManagerMethod('log', '', @_);
}

1;
