package Perun::RegistrarAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'registrarManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub copyForm
{
	return Perun::Common::callManagerMethod('copyForm', 'null', @_);
}

sub copyMails
{
	return Perun::Common::callManagerMethod('copyMails', 'null', @_);
}

sub createApplicationForm
{
	return Perun::Common::callManagerMethod('createApplicationForm', 'null', @_);
}

sub updateForm
{
	return Perun::Common::callManagerMethod('updateForm', 'null', @_);
}

sub getApplicationForm
{
	return Perun::Common::callManagerMethod('getApplicationForm', 'ApplicationForm', @_);
}

sub setApplicationForm
{
	return Perun::Common::callManagerMethod('setApplicationForm', 'null', @_);
}

sub getFormItems
{
	return Perun::Common::callManagerMethod('getFormItems', '[]ApplicationFormItem', @_);
}

sub updateFormItems
{
	return Perun::Common::callManagerMethod('updateFormItems', 'null', @_);
}

sub getApplicationById
{
	return Perun::Common::callManagerMethod('getApplicationById', 'Application', @_);
}

sub addApplicationMail
{
	return Perun::Common::callManagerMethod('addApplicationMail', 'number', @_);
}

sub getApplicationMails
{
	return Perun::Common::callManagerMethod('getApplicationMails', '[]ApplicationMail', @_);
}

sub getApplicationMailById
{
	return Perun::Common::callManagerMethod('getApplicationMailById', 'ApplicationMail', @_);
}

sub updateApplicationMail
{
	return Perun::Common::callManagerMethod('updateApplicationMail', 'null', @_);
}

sub deleteApplicationMail
{
	return Perun::Common::callManagerMethod('deleteApplicationMail', 'null', @_);
}

1;
