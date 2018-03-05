#!/usr/bin/perl

######################################################
#
# PASSWORD MANAGER FOR EDUROAM
#
# It takes up to 4 parameters in following order: action, namespace, login, pass
# where action can be "check", "change", "reserve", "validate", "reserve_random", "delete"
# where namespace represents login-namespace used by radius server (eduroam) and should match passed login
# namespace also must match PWDM config file /etc/perun/pwchange.[namespace].eduroam
# where login is users login to reserve
# where password is users password in plaintext (required only for actions check, change, reserve)
#
#####################################################

use strict;
use warnings FATAL => 'all';
use Switch;
use String::Random qw( random_string );
# Import shared AD library
use ADConnector;

sub edu_log;
sub getPassword;
sub getEntry;
sub checkAgainstAD;

##########
#
# RUN SCRIPT ITSELF
#
##########

# read input parameters
my $action = $ARGV[0];
my $namespace = $ARGV[1];
my $login = $ARGV[2];

my $filename = "/etc/perun/pwchange.".$namespace.".eduroam";
unless (-e $filename) {
	edu_log("Configuration file for namespace \"" . $namespace . "\" doesn't exist!");
	exit 2; # login-namespace is not supported
}

# load configuration file
open FILE, "<" . $filename;
my @lines = <FILE>;
close FILE;

# remove new-line characters from the end of lines
chomp @lines;

# settings
my $key_path = $lines[0];  # path to the SSH key
my $server = $lines[1];    # radius server hostname/ip

# do stuff based on password manager action type
switch ($action) {

	case("change"){

		my $pass = <STDIN>;
		chomp($pass);

		unless (0 == checkAgainstAD($namespace, $login, $pass)) {
			edu_log("[PWDM] Same password for $login in AD exists!");
			exit 11;
		}

		my $entry = getEntry($login, getPassword($pass));

		# timeout 120s kill after 120 more sec.
		my $ret = system(qq^timeout -k 120 120 ssh -i $key_path $server '~/eduroamPwdmgrServer.pl $action "'"$entry"'"'^);
		$ret = $ret>>8;  # shift 8 bits to get original return code
		if ($ret != 0) {
			# error adding entry
			edu_log("[PWDM] Change of password for $login failed with return code: $ret");
			exit 3; # setting of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Password for $login changed.");
		}

	}

	case("check"){

		my $entry = getEntry($login, getPassword());

		# timeout 120s kill after 120 more sec.
		my $ret = system(qq^timeout -k 120 120 ssh -i $key_path $server '~/eduroamPwdmgrServer.pl $action "'"$entry"'"'^);
		$ret = $ret>>8;  # shift 8 bits to get original return code
		if ($ret != 0) {
			# error checking entry
			edu_log("[PWDM] Check of password failed for $login with return code: $ret");
			exit 6; # checking old password failed
		} else {
			# entry added
			edu_log("[PWDM] Password for $login checked.");
		}

	}

	case("reserve"){

		my $pass = <STDIN>;
		chomp($pass);

		unless (0 == checkAgainstAD($namespace, $login, $pass)) {
			edu_log("[PWDM] Same password for $login in AD exists!");
			exit 11;
		}

		my $entry = getEntry($login, getPassword($pass));

		# timeout 120s kill after 120 more sec.
		my $ret = system(qq^timeout -k 120 120 ssh -i $key_path $server '~/eduroamPwdmgrServer.pl $action "'"$entry"'"'^);
		$ret = $ret>>8;  # shift 8 bits to get original return code
		if ($ret != 0) {
			# error adding entry
			edu_log("[PWDM] Creation of password for $login failed with return code: $ret");
			exit 4; # creation of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Password for $login reserved.");
		}

	}

	case("delete") {

		my $entry = getEntry($login, undef);

		# timeout 120s kill after 120 more sec.
		my $ret = system(qq^timeout -k 120 120 ssh -i $key_path $server '~/eduroamPwdmgrServer.pl $action "'"$entry"'"'^);
		$ret = $ret>>8;  # shift 8 bits to get original return code
		if ($ret != 0) {
			# error deleting entry
			edu_log("[PWDM] Deletion of password for $login failed with return code: $ret");
			exit 5; # creation of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Password for $login deleted.");
		}

	}

	case("validate") {

		exit 0;

	}

	case("reserve_random") {

		my $pass = random_string("Cn!CccncCn");

		unless (0 == checkAgainstAD($namespace, $login, $pass)) {
			edu_log("[PWDM] Same password for $login in AD exists!");
			exit 11;
		}

		my $entry = getEntry($login, getPassword($pass));

		# timeout 120s kill after 120 more sec.
		my $ret = system(qq^timeout -k 120 120 ssh -i $key_path $server '~/eduroamPwdmgrServer.pl $action "'"$entry"'"'^);
		$ret = $ret>>8;  # shift 8 bits to get original return code
		if ($ret != 0) {
			# error adding entry
			edu_log("[PWDM] Creation of random password for $login failed with return code: $ret");
			exit 4; # creation of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Random password for $login reserved.");
		}

	}

	else {
		edu_log("[PWDM] Unknown action for handling passwords.");
		exit 10;
	}

}

#
# Log any message to pwdm.log file located in same folder as script.
# Each message starts at new line with date.
#
sub edu_log() {

	my $message = (@_)[0];
	open(LOGFILE, ">>./pwdm.log");
	print LOGFILE (localtime(time) . ": " . $message . "\n");
	close(LOGFILE);

}

#
# Reads password from STDIN and converts it to the NTLM hash
# if password is passed as param, value is used instead
#
sub getPassword() {

	my $user_pass;
	my $pass = shift;
	unless($pass) {
		$user_pass = <STDIN>;
	} else {
		$user_pass = $pass;
	}

	chomp($user_pass);

	my $converted_pass = substr `printf '%s' "$user_pass" | iconv -t utf16le | openssl md4` , 10;
	chomp($converted_pass);

	return $converted_pass;

}

#
# Return RADIUS "users" file entry for login and hashed password.
# If password is not passed, partial entry is returned.
#
sub getEntry() {

	my $username = shift;
	my $converted_pass = shift;

	if ($converted_pass) {
		return qq^\\\\\\"$username\\\\\\" NT-Password := \\\\\\"$converted_pass\\\\\\"^;
	} else {
		return qq^\\\\\\"$username\\\\\\" NT-Password := \\\\\\"^;
	}

}

#
# Check if user doesn't use same passowrd as in AD.
#
# return 0 if OK (passwords not the same)
# return 1 if NOK (passwords are the same)
sub checkAgainstAD($$$) {

	my $namespace = shift;
	my $login = shift;
	my $pass = shift;

	# AD config in /etc/perun/$namespace.ad
	my @credentials = init_config($namespace);
	my $ad_location = resolve_pdc($credentials[0]);
	my $ad = ldap_connect($ad_location);

	my $mesg = $ad->bind( "$login" , password => "$pass" );

	if ( $mesg->code == 0) {
		$ad->unbind;
		return 1;
	} else {
		return 0;
	}

}
