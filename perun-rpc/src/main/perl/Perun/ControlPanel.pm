package Perun::ControlPanel;

use strict;
use warnings;

use Perun::Common;

my $manager = 'controlPanel';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub forceServicesPropagation
{
	return Perun::Common::callManagerMethod('forceServicesPropagation', '', @_);
}

1;
