package Perun::PropagationStatsReaderAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'propagationStatsReader';

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
