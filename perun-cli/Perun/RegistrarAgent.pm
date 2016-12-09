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

1;
