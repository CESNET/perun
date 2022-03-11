package Perun::ConfigAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'configManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#
sub reloadAppsConfig {
	return Perun::Common::callManagerMethod('reloadAppsConfig', '', @_)
}