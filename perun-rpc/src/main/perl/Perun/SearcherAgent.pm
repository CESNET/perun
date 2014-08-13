package Perun::SearcherAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'searcher';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getUsers
{
	return Perun::Common::callManagerMethod('getUsers', '[]User', @_);
}

sub getMembersByUserAttributes
{
	return Perun::Common::callManagerMethod('getMembersByUserAttributes', '[]Member', @_);
}

1;
