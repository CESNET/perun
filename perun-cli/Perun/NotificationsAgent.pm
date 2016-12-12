package Perun::NotificationsAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'notificationManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

# Methods for PerunNotifReceiver
sub getPerunNotifReceiverById
{
	return Perun::Common::callManagerMethod('getPerunNotifReceiverById', 'NotifReceiver', @_);
}

sub getAllPerunNotifReceivers
{
	return Perun::Common::callManagerMethod('getAllPerunNotifReceivers', '[]NotifReceiver', @_);
}

sub createPerunNotifReceiver
{
	return Perun::Common::callManagerMethod('createPerunNotifReceiver', 'NotifReceiver', @_);
}

sub updatePerunNotifReceiver
{
	return Perun::Common::callManagerMethod('updatePerunNotifReceiver', 'NotifReceiver', @_);
}

sub removePerunNotifReceiverById
{
	return Perun::Common::callManagerMethod('removePerunNotifReceiverById', 'null', @_);
}

# Methods for PerunNotifRegex
sub getPerunNotifRegexById
{
	return Perun::Common::callManagerMethod('getPerunNotifRegexById', 'NotifRegex', @_);
}

sub getAllPerunNotifRegexes
{
	return Perun::Common::callManagerMethod('getAllPerunNotifRegexes', '[]NotifRegex', @_);
}

sub createPerunNotifRegex
{
	return Perun::Common::callManagerMethod('createPerunNotifRegex', 'NotifRegex', @_);
}

sub updatePerunNotifRegex
{
	return Perun::Common::callManagerMethod('updatePerunNotifRegex', 'NotifRegex', @_);
}

sub removePerunNotifRegexById
{
	return Perun::Common::callManagerMethod('removePerunNotifRegexById', 'null', @_);
}

sub saveTemplateRegexRelation
{
	return Perun::Common::callManagerMethod('saveTemplateRegexRelation', 'null', @_);
}

sub getRelatedRegexesForTemplate
{
	return Perun::Common::callManagerMethod('getRelatedRegexesForTemplate', '[]NotifRegex', @_);
}


sub removePerunNotifTemplateRegexRelation
{
	return Perun::Common::callManagerMethod('removePerunNotifTemplateRegexRelation', 'null', @_);
}

# Methods for perunNotifTemplateMessage
sub getPerunNotifTemplateMessageById
{
	return Perun::Common::callManagerMethod('getPerunNotifTemplateMessageById', 'NotifTemplateMessage', @_);
}

sub getAllPerunNotifTemplateMessages
{
	return Perun::Common::callManagerMethod('getAllPerunNotifTemplateMessages', '[]NotifTemplateMessage', @_);
}

sub createPerunNotifTemplateMessage
{
	return Perun::Common::callManagerMethod('createPerunNotifTemplateMessage', 'NotifTemplateMessage', @_);
}

sub updatePerunNotifTemplateMessage
{
	return Perun::Common::callManagerMethod('updatePerunNotifTemplateMessage', 'NotifTemplateMessage', @_);
}

sub removePerunNotifTemplateMessage
{
	return Perun::Common::callManagerMethod('removePerunNotifTemplateMessage', 'null', @_);
}

# Methods for perunNotifTemplate
sub getPerunNotifTemplateById
{
	return Perun::Common::callManagerMethod('getPerunNotifTemplateById', 'NotifTemplate', @_);
}

sub getAllPerunNotifTemplates
{
	return Perun::Common::callManagerMethod('getAllPerunNotifTemplates', '[]NotifTemplate', @_);
}

sub createPerunNotifTemplate
{
	return Perun::Common::callManagerMethod('createPerunNotifTemplate', 'NotifTemplate', @_);
}

sub updatePerunNotifTemplate
{
	return Perun::Common::callManagerMethod('updatePerunNotifTemplate', 'NotifTemplate', @_);
}

sub removePerunNotifTemplateById
{
	return Perun::Common::callManagerMethod('removePerunNotifTemplateById', 'null', @_);
}

sub stopNotifications
{
	return Perun::Common::callManagerMethod('stopNotifications', 'null', @_);
}

sub startNotifications
{
	return Perun::Common::callManagerMethod('startNotifications', 'null', @_);
}

1;
