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

1;
