package Perun::ExtSourcesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'extSourcesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub getExtSourceById
{
	return Perun::Common::callManagerMethod('getExtSourceById', 'ExtSource', @_);
}

sub getExtSourceByName
{
	return Perun::Common::callManagerMethod('getExtSourceByName', 'ExtSource', @_);
}

sub getVoExtSources
{
	return Perun::Common::callManagerMethod('getVoExtSources', '[]ExtSource', @_);
}

sub getExtSources
{
	return Perun::Common::callManagerMethod('getExtSources', '[]ExtSource', @_);
}

sub addExtSource
{
	return Perun::Common::callManagerMethod('addExtSource', '', @_);
}

sub removeExtSource
{
	return Perun::Common::callManagerMethod('removeExtSource', '', @_);
}

sub createExtSource
{
	return Perun::Common::callManagerMethod('createExtSource', 'ExtSource', @_);
}
sub loadExtSourcesDefinitions
{
	return Perun::Common::callManagerMethod('loadExtSourcesDefinitions', '', @_);
}

sub deleteExtSource
{
	return Perun::Common::callManagerMethod('deleteExtSource', '', @_);
}

1;
