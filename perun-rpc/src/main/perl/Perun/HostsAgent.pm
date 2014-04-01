package Perun::HostsAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'hostsManager';

use fields qw(_agent _manager);

sub new {
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#(id => $number)
sub getHostById
{
	return Perun::Common::callManagerMethod('getHostById', 'Host', @_);
}


#(host => $host)
sub createHost
{
	print STDERR "Warning: method 'createHost' is deprecated.";
	return Perun::Common::callManagerMethod('createHost', 'Host', @_);
}

#(host => $hostId)
sub deleteHost
{
	print STDERR "Warning: method 'deleteHost' is deprecated.";
	return Perun::Common::callManagerMethod('deleteHost', '', @_);
}

1;
