	#!/usr/bin/perl
package perunServicesInit;

use Exporter 'import';
@EXPORT_OK = qw(init);
@EXPORT= qw(getDirectory getDestinationDirectory getHierarchicalData getDataWithGroups);

use strict;
use warnings;
use Getopt::Long qw(:config no_ignore_case);
use Perun::Agent;
use File::Path;
use File::Temp qw/ tempdir /;
use File::Temp qw/ :mktemp  /;
use File::Copy;
use Data::Dumper;
use IO::Compress::Gzip qw(gzip $GzipError) ;

#die at the very end of skript when any warning occured during executing
our $DIE_AT_END=0;
$SIG{__WARN__} = sub { $DIE_AT_END=1; warn @_; };
END { if($DIE_AT_END) { die "Died because of warning(s) occured during procesing.\n"; } };

my ($agent, $service, $facility, $servicesAgent, $directory, $tmp_directory, $tmp_directory_destination, $getData_directory);

# Prepare direactory for file which will be generated
# Create VERSION file in this directory. This file contains protocol version
# 
# This method REQUIRE acess to $::SERVICE_NAME and $::PROTOCOL_VERSION
# this can be achieved by folowing lines in your main script: (for example)
#     local $::SERVICE_NAME = "passwd";
#     local $::PROTOCOL_VERSION = "3.0.0";
sub init {

	unless(defined $::SERVICE_NAME) { die; }
	unless(defined $::PROTOCOL_VERSION) {die;}
	
	my $facilityId;
	GetOptions ("facilityId|f=i" => \$facilityId) || die;

	unless(defined $facilityId) { die "ERROR: facilityId required"; }

	$agent = Perun::Agent->new();
	$servicesAgent = $agent->getServicesAgent;
	my $facilitiesAgent = $agent->getFacilitiesAgent;
	$service = $servicesAgent->getServiceByName( name => $::SERVICE_NAME);
	$facility = $facilitiesAgent->getFacilityById( id => $facilityId);

	$directory = "spool/" . $facility->getName . "/" . $service->getName."/";
	$tmp_directory = "spool/tmp/" . $facility->getName . "/" . $service->getName."/";
	$tmp_directory_destination = $tmp_directory . "/_destination/";
	$getData_directory = "spool/tmp/getData/" . $facility->getName . "/" . $service->getName."/";

	my $err;
	rmtree($tmp_directory,  { error => \$err } );
	if(@$err) { die "Can't rmtree( $tmp_directory  )" };
	mkpath($tmp_directory, { error => \$err } );
	if(@$err) { die "Can't mkpath( $tmp_directory  )" };
	mkpath($tmp_directory_destination, { error => \$err } );
	if(@$err) { die "Can't mkpath( $tmp_directory_destination  )" };
	createVersionFile();
	createServicesFile();
	createFacilityNameFile($facility->getName);

	rmtree($getData_directory);
	if(@$err) { die "Can't rmtree( $getData_directory  )" };
	mkpath($getData_directory);
	if(@$err) { die "Can't mkpath( $getData_directory  )" };
}

sub finalize {
	unless($DIE_AT_END) {
		my $err;
		rmtree($directory, { error => \$err } );
		if(@$err) { die "Can't rmtree( $directory  )" };

		mkpath($directory, { error => \$err } );
		if(@$err) { die "Can't mkpath( $directory  )" };

		move("$tmp_directory", "$directory") or die "Cannot move $tmp_directory to $directory dir: ", $!;
	}
}

#Commented because of big amount of data in memory (usage)
sub logData {
	#my $data = shift || die "No data";
	#my $file = shift || "data";
	#my $dataFile = new IO::Compress::Gzip "$getData_directory/$file.gz" or die "IO::Compress::Gzip failed: $GzipError\n";
	#print $dataFile Dumper($data);
}

sub getAgent {
	return $agent;
}

sub getFacility {
	return $facility;
}

sub getHierarchicalData {
	my $data = $servicesAgent->getHierarchicalData(service => $service->getId, facility => $facility->getId);
	logData $data, 'hierarchicalData';
	return $data;
}

sub getFlatData {
	my $data = $servicesAgent->getFlatData(service => $service->getId, facility => $facility->getId);
	logData $data, 'flatData';
	return $data;
}

sub getDataWithGroups {
	my $data = $servicesAgent->getDataWithGroups(service => $service->getId, facility => $facility->getId);
	logData $data, 'dataWithGroups';
	return $data;
}

#Returns directory for storing generated files
sub getDirectory {
	return $tmp_directory;
}

#Creates directory for destination from param. Returns path to that directory.
sub getDestinationDirectory {
	my $destination = shift;
	unless($destination) { die "getDestinationDirectory: no destination specified"; }
	my $path = "$tmp_directory_destination/$destination/";
	my $err;
	mkpath($path, { error => \$err } );
	if(@$err) { die "Can't mkpath( $tmp_directory_destination  )" };
	return $path;
}

sub createVersionFile {
	open VERSION_FILE, ">$tmp_directory" . "/VERSION";
	print VERSION_FILE $::PROTOCOL_VERSION, "\n";
	close VERSION_FILE;
}

sub createServicesFile {
	open SERVICE_FILE, ">$tmp_directory" . "/SERVICE";
	print SERVICE_FILE $::SERVICE_NAME, "\n";
	close SERVICE_FILE;
}

sub createFacilityNameFile($) {
	open FACILITY_NAME_FILE, ">$tmp_directory" . "/FACILITY";
	print FACILITY_NAME_FILE $_[0], "\n";
	close FACILITY_NAME_FILE;
}

return 1;
