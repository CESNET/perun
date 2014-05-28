#!/usr/bin/perl
# Skript pro jednorazove odeslani dat vygenerovanych sluzbami na konkretni stroj

# Changelog
#   stavamichal@gmail.com - 27.5.2014 - remove facility TYPE
#   michalp@ics.muni.cz -  6.3.2013 - get user's uid and set $KRB5CCNAME
#   glory@ics.muni.cz   - 19.7.2012 - bugfix in regexp
#                                   - fixed error handling
#   michalp@ics.muni.cz - 16.5.2012 - removed getting the krb5 ticket, the scripts run under the perunv3 acount which already has krb5 ticket
#                                   - added syslog loggging
#   michalp@ics.muni.cz -  7.3.2012 - automatically propage to all non-host destinations for the pair service-facility and for one defined host destination defined by the principal
#   michalp@ics.muni.cz -  2.2.2012 - initial release

use strict;
use warnings;
use lib '/software/perun-3.0/lib';
use Perun::Agent;
use Perun::beans::Destination;
use Sys::Syslog;

openlog("perun.propagate", "pid", "LOG_LOCAL0");

$ENV{'PERUN_URL'} = "https://perun.metacentrum.cz/perun-rpc-krb/";
my $remote_user = $ENV{'REMOTE_USER'};
$remote_user =~ /^[a-z0-9]*\/(.*)@[a-zA-Z._-]*$/;
my $destination = $1;

# Setup right environment variable for kerberos credential cache
my ($name, $pass, $uid, $gid, $quota, $comment, $gcos, $dir, $shell, $expire) = getpwnam($ENV{"USER"});
$ENV{'KRB5CCNAME'} = "FILE:/tmp/krb5cc_" . $uid;

unless($destination) { die "Cannot get destination from $remote_user"; }

# Get krb5 ticket
# We are using ticket refreshed under user account perunv3, because this scripts run under account perunv3
#$ENV{'KRB5CCNAME'} = "FILE:/tmp/krb5cc_perun-propagation";
#my @args = ("/usr/bin/kinit", "-t", "/home/perunv3/perun-registrar.keytab", "-k", "-c", "FILE:/tmp/krb5cc_perun-propagation", 'perunv3-registrar@META');
#system(@args) == 0 or die "Cannot get KRB ticket";

# Start Perun Agent
my $agent = Perun::Agent->new();
my $facilitiesAgent = $agent->getFacilitiesAgent;
my $servicesAgent = $agent->getServicesAgent;

my @facilities = $facilitiesAgent->getFacilitiesByDestination(destination => $destination);

unless(@facilities) { die "The destination $destination doesn't have assigned any facility"; }

#chdir("/home/perunv3/perun-engine-1/send");

syslog('info', "Propagating for $destination.");

foreach my $facility (@facilities) {
	my @services = $servicesAgent->getAssignedServices(facility => $facility->getId);

	foreach my $service (@services) {
		my @destinations = $servicesAgent->getDestinations(facility => $facility->getId, service => $service->getId);

		foreach my $serviceDestination (@destinations) {
			# If the destination type is different from host, then propagate
			# If the destination is type host, then propagate only on the destination defined in the principal
			if (($serviceDestination->getType ne 'host') || ($serviceDestination->getType eq 'host' && $serviceDestination->getDestination eq $destination)) {
				chdir("/home/perunv3/perun-engine-1/send") or die "Can't chdir: $!";
				my @args = ("./" . $service->getName, $facility->getName, $serviceDestination->getDestination);
				if(system(@args) == 0) {
					print $service->getName . " " . $facility->getName . " " . $serviceDestination->getDestination . " - Propagated\n";
				} else {
					warn "Cannot run propagation of service " . $service->getName . " on facility " . $facility->getName . " with destination " . $serviceDestination->getDestination . "\n";
				}
			}
		}
	}
}

#system("/usr/bin/kdestroy", "-c", "/tmp/krb5cc_perun-propagation");
exit 0;
