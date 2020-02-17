package Perun::TasksAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'tasksManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getTaskResultsForDestinations
{
	return Perun::Common::callManagerMethod('getTaskResultsForDestinations', '[]TaskResult', @_);
}

1;
