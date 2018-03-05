#!/usr/bin/perl

######################################################
#
# PASSWORD MANAGER FOR EDUROAM
#
# It takes up to 4 parameters in following order: action, namespace, login, pass
# where action can be "check", "change", "reserve", "validate", "reserve_random", "delete"
# where namespace represents login-namespace used by radius server (eduroam) and should match passed login
# namespace also must match PWDM config file /etc/perun/pwdchange.[namespace].eduroam
# where login is users login to reserve
# where password is users password in plaintext (required only for actions check, change, reserve)
#
#####################################################

use strict;
use warnings FATAL => 'all';
use Switch;
use File::Slurp qw(read_file write_file);
use ScriptLock;

##########
#
# RUN SCRIPT ITSELF
#
##########

# read input parameters
my $action = $ARGV[0];
my $entry = $ARGV[1];
# should be something like: /etc/raddb/users
my $file_location = "/etc/raddb/users";
my $touch_file_location = "./last-changed";

# do stuff based on password manager action type
switch ($action){

	# create service lock
	my $lock = ScriptLock->new("eduroam");

	# for cuncurrent runs, try to finish for 1 minute, after that report error.
	my $success = 0;
	my $counter = 0;
	while ($success == 0 && $counter < 60) {
		$success = $lock->lock();
		$counter++;
		sleep 1;
	}
	if ($success == 0) {
		exit 12; # lock timeout
	}

	case("change"){

		my $changed = 0;
		my @parts = split( /:=/ , $entry);

		my @lines = read_file( $file_location );
		foreach my $line (@lines) {
			if ($line =~ /^$parts[0].*$/) {
				$line = $entry . "\n";
				$changed = 1;
			}
		}
		if ($changed == 0) {
			# entry not found - add as new reservation
			push (@lines, $entry . "\n");
		}

		# write back all lines to file
		write_file ($file_location, @lines);
		system("touch $touch_file_location");
		$lock->unlock();
		exit 0;

	}

	case("check"){

		my @lines = read_file( $file_location );
		foreach my $line (@lines) {
			if ($line =~ /^$entry.*$/) {
				$lock->unlock();
				exit 0;
			}
		}
		# password doesn't match
		$lock->unlock();
		exit 1;

	}

	case("reserve"){

		# prevent duplicates !
		my @parts = split( /:=/ , $entry);
		my @lines = read_file( $file_location );
		foreach my $line (@lines) {
			if ($line =~ /^$parts[0].*$/) {
				# entry already exists
				$lock->unlock();
				exit 4; # creation of new password failed
			}
		}

		# append new user entry
		push (@lines, $entry . "\n");
		write_file ($file_location, @lines);
		system("touch $touch_file_location");
		$lock->unlock();
		exit 0;

	}

	case("delete") {

		my $found = 0;
		my @parts = split( /:=/ , $entry);

		my @lines = read_file( $file_location );
		my @newlines;
		foreach my $line (@lines) {
			if ($line =~ /^$parts[0].*$/) {
				$found = 1;
				# skip entry to delete
			} else {
				# push back not affected entries
				push (@newlines, $line);
			}
		}
		if ($found == 0) {
			# entry to delete not found
			$lock->unlock();
			exit 5; # can't delete password
		} else {
			# entry to delete found -> write content without it
			write_file ($file_location, @newlines);
			system("touch $touch_file_location");
			$lock->unlock();
			exit 0;
		}

	}

	case("validate") {

		$lock->unlock();
		exit 0;

	}

	case("reserve_random") {

		# prevent duplicates !
		my @parts = split( /:=/ , $entry);
		my @lines = read_file( $file_location );
		foreach my $line (@lines) {
			if ($line =~ /^$parts[0].*$/) {
				# entry already exists
				$lock->unlock();
				exit 4; # creation of new password failed
			}
		}

		# append new user entry
		push (@lines, $entry . "\n");
		write_file ($file_location, @lines);
		system("touch $touch_file_location");
		$lock->unlock();
		exit 0;

	}

	else {
		$lock->unlock();
		exit 10;
	}

}
