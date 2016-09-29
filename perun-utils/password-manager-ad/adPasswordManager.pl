#!/usr/bin/perl

######################################################
#
# PASSWORD MANAGER FOR ACTIVE DIRECTORY (LDAP)
#
# It takes up to 4 parameters in following order: action, namespace, login, pass
# where action can be "check", "change", "reserve", "validate", "reserve_random", "delete"
# where namespace represents login-namespace used by this LDAP and should match passed login
# namespace also must match LDAPs PWDM config file /etc/perun/pwdchange.[namespace].ad
# where login is users login to reserve
# where password is users password in plaintext (required only for actions check, change, reserve)
#
#####################################################

use strict;
use warnings;
use Switch;
use Net::LDAPS;
use Net::LDAP::Entry;
use Net::LDAP::Message;
use Net::LDAP::LDIF;
use Encode;
use MIME::Base64;

sub ldap_connect;
sub ldap_disconnect;
sub ldap_log;
sub convertPassword;
sub getEntryByLogin;

# ldap instance holder
my $ldap = undef;

# base DN
my $base_dn = undef;

##########
#
# RUN SCRIPT ITSELF
#
##########

# read input parameters
my $action = $ARGV[0];
my $namespace = $ARGV[1];
my $login = $ARGV[2];
my $user_pass = undef;

# do stuff based on password manager action type
switch ($action){

	case("change"){

		$user_pass = <STDIN>;
		my $converted_pass = convertPassword($user_pass);
		ldap_connect($namespace);
		my $mesg;
		eval {
			my $entry = getEntryByLogin();
			$mesg = $ldap->modify( $entry->dn() ,
				replace => {
					unicodePwd  => $converted_pass
				}
			);
		};
		if ( $@ ) {

			# Error thrown by $ldap
			ldap_log("[PWDM] Password change failed with AD return code: " . $@);
			ldap_disconnect();
			exit 3; # setting new password failed
		} else {

			# Ok case
			ldap_disconnect();
			my $code = $mesg->code;
			if ($code != 0) {
				ldap_log("[PWDM] Password change failed with AD return code: ".$code);
				exit 3; # setting new password failed
			}

			# CHANGE WAS SUCCESSFUL
			ldap_log("[PWDM] Password changed.");
		}

	}

	case("check"){

		# ONLY WAY TO CHECK PASSWORD IS TO PERFORM BIND AS USER
		$user_pass = <STDIN>;
		chomp($user_pass);

		# get entry from LDAP to find out users DN
		ldap_connect($namespace);
		my $entry = getEntryByLogin();
		my $dn = $entry->dn();
		ldap_disconnect();

		my $filename = "/etc/perun/pwchange.".$namespace.".ad";
		unless (-e $filename) {
			ldap_log("[PWDM] Configuration file for namespace \"" . $namespace . "\" doesn't exist!");
			exit 2; # login-namespace is not supported
		}

		# load configuration file
		open FILE, "<" . $filename;
		my @lines = <FILE>;
		close FILE;

		# remove new-line characters from the end of lines
		chomp @lines;

		# read configuration
		my $ldap_location = $lines[0];
		$base_dn = $lines[3];

		# LDAP connect
		$ldap = Net::LDAPS->new( "$ldap_location" , onerror => 'die' , timeout => 5 , debug => 0, verify => 'require', capath => '/etc/ssl/certs/', port => 636);

		my $mesg;
		eval {
			$mesg = $ldap->bind( $dn , password => $user_pass );
			ldap_log("[AD] connected as: " . $dn );
		};
		if ( $@ ) {
			# ERROR WHEN CHECKING - e.g. entry doesn't exists
			ldap_log("[PWDM] Password check failed with AD return code: ".$@);
			ldap_disconnect();
			exit 1; # Password doesn't match (in a fact, it can be any other reason (e.g. account disabled), but response is just error string).
		} else {
			# CHECK WAS SUCCESSFUL - READ CHECK RESULT
			ldap_disconnect();
			my $code = $mesg->code();
			if ($code != 0) {
				ldap_log("[PWDM] Password check failed with AD return code: ".$code);
				exit 6; # TODO - is this right exit code ?
			}
		}

	}

	case("reserve"){

		$user_pass = <STDIN>;
		my $converted_pass = convertPassword($user_pass);
		ldap_connect( $namespace );

		my $filename = "/etc/perun/pwchange.".$namespace.".ad";
		unless (-e $filename) {
			ldap_log("[PWDM] Configuration file for namespace \"" . $namespace . "\" doesn't exist!");
			exit 2; # login-namespace is not supported
		}

		# load configuration file
		open FILE, "<" . $filename;
		my @lines = <FILE>;
		close FILE;

		# remove new-line characters from the end of lines
		chomp @lines;

		# read configuration
		my $uac = $lines[4];

		# By default AD creates normal disabled entry with no password required (userAccountControl = 546)
		my $entry = Net::LDAP::Entry->new;
		$entry->dn("cn=" . $login . "," . $base_dn );
		$entry->add(
			objectClass => ["top", "user", "person", "organizationalPerson"] ,
			unicodePwd => $converted_pass ,
			cn => $login ,
			sn => $login ,
			samAccountName => $login ,
			# create normal disabled account which requires password
			userAccountControl => $uac
		);

		my $mesg;
		eval {
			$mesg = $ldap->add( $entry );
		};
		if ( $@ ) {

			# error adding entry
			ldap_log("[PWDM] Creation of password failed with AD return code: ".$@);
			ldap_disconnect();
			exit 4; # creation of new password failed
		} else {
			ldap_disconnect();
			my $code = $mesg->code;
			if ($code != 0) {
				ldap_log("[PWDM] Creation of password failed with AD return code: ".$code);
				exit 4 # creation of new password failed
			}

			# entry added
			ldap_log("[PWDM] Password reserved.");
		}

	}

	case("delete"){

		ldap_connect( $namespace );
		my $mesg;
		eval {
			my $entry = getEntryByLogin();
			unless ($entry) {
				exit 5; # can't delete password
			}
			$mesg = $ldap->delete( $entry->dn() );
		};
		if ( $@ ) {
			ldap_log("[PWDM] Password deletion failed with AD return code: " . $@);
			ldap_disconnect();
			exit 5; # can't delete password
		} else {
			ldap_disconnect();
			my $code = $mesg->code;
			if($code != 0) {
				ldap_log("[PWDM] Password deletion failed with AD return code: ".$code);
				exit 5 # can't delete password
			}
			ldap_log("[PWDM] Password deleted.");
		}

	}

	case("validate"){

		ldap_connect( $namespace );
		my $mesg;
		eval {
			# Get current entry from LDAP
			my $entry = getEntryByLogin();
			unless ($entry) {
				exit 6;
			}
			my $value = $entry->get_value('userAccountControl');
			# Enable account
			$value = $value & ~2;
			# Update local entry
			$entry->replace(
				userAccountControl => $value
			);
			# Update LDAP
			$mesg = $entry->update($ldap);
		};
		if ( $@ ) {

			# ERROR WHEN VALIDATING
			ldap_log("[PWDM] Password validation failed with AD return code: " . $@);
			ldap_disconnect();
			exit 6; # TODO - is this right exit code ?
		} else {

			# ENTRY VALIDATED
			ldap_disconnect();
			my $code = $mesg->code;
			if($code != 0) {
				ldap_log("[PWDM] Password validation failed with AD return code: ".$code);
				exit 6; # TODO - is this right exit code ?
			}
			ldap_log("[PWDM] Password validated.");
		}

	}

	case("reserve_random"){

		ldap_connect( $namespace );

		# CREATE ENTRY WITHOUT PASSWORD and as inactive
		# By default AD creates normal disabled entry with no password required (userAccountControl = 546)
		my $entry = Net::LDAP::Entry->new;
		$entry->dn("cn=" . $login . "," . $base_dn );
		$entry->add(
			objectClass => ["top", "user", "person", "organizationalPerson"] ,
			#unicodePwd => $converted_pass ,
			cn => $login ,
			sn => $login ,
			samAccountName => $login ,
			# create disabled entry with no password required
			userAccountControl => 546
		);

		my $mesg;
		eval {
			$mesg = $ldap->add( $entry );
		};
		if ( $@ ) {

			# ERROR WHEN RESERVING
			ldap_log("[PWDM] Random password reservation failed with AD return code: " . $@);
			ldap_disconnect();
			exit 4; # creation of new password failed
		} else {
			my $code = $mesg->code;
			ldap_disconnect();
			if ($code != 0) {
				ldap_log("[PWDM] Random password reservation failed with AD return code: " . $code);
			}
			exit 4 # creation of new password failed
		}
		ldap_log("[PWDM] Random password reserved.");
	}

	else {
		ldap_log("[PWDM] Unknown action for handling passwords.");
		exit 10;
	}

}

###########################################
#
# Auxiliary functions used by main script
#
###########################################

#
# Connects to LDAP
#
# @param login-namespace for loading config data
#
sub ldap_connect{

	my $namespace = shift;

	# check if config file for namespace exists
	my $filename = "/etc/perun/pwchange.".$namespace.".ad";
	unless (-e $filename) {
		ldap_log("[PWDM] Configuration file for namespace \"" . $namespace . "\" doesn't exist!");
		exit 2; # login-namespace is not supported
	}

	# load configuration file
	open FILE, "<" . $filename;
	my @lines = <FILE>;
	close FILE;

	# remove new-line characters from the end of lines
	chomp @lines;

	# read configuration
	my $ldap_location = $lines[0];
	my $ldap_user = $lines[1];
	my $ldap_pass = $lines[2];
	$base_dn = $lines[3];

	# LDAP connect
	$ldap = Net::LDAPS->new( "$ldap_location" , onerror => 'die' , timeout => 5 , debug => 0, verify => 'require', capath => '/etc/ssl/certs/', port => 636);

	# LDAP log-in
	if ($ldap) {
		my $mesg = $ldap->bind( "$ldap_user" , password => "$ldap_pass" );
		ldap_log("[AD] connected as: $ldap_user");
	} else {
		ldap_log("[AD] can't connect to AD.");
		exit 1;
	}

}

#
# Disconnect from LDAP if connected
#
sub ldap_disconnect(){
	if ($ldap) {
		my $mesg = $ldap->unbind;
		ldap_log("[AD] disconnected.");
	} else {
		ldap_log("[AD] can't disconnect from AD (connection not exists).");
	}
}

#
# Log any message to pwdm.log file located in same folder as script.
# Each message starts at new line with date.
#
sub ldap_log() {

	my $message = (@_)[0];
	open(LOGFILE, ">>/usr/local/bin/pwdm.log");
	print LOGFILE (localtime(time) . ": " . $message . "\n");
	close(LOGFILE);

}

#
# Convert password into unicode / utf-16-le required by AD
# Calls convertPassword.py script
#
sub convertPassword() {

	my $user_pass = (@_)[0];
	chomp($user_pass);
	my $converted_pass = encode("UTF-16LE",'"'.$user_pass.'"');
	# Do not convert to base64 since it will be performed automatically during print/transmission
	#$converted_pass = encode_base64($converted_pass);
	if (!defined $converted_pass || $converted_pass eq '') {
		ldap_log("[PWDM] Unable to convert password to unicode / utf-16-le.");
		# setting new password failed
		exit 3;
	}
	return $converted_pass;

}

#
# Return single entry by users login since not
# in all cases login is part of DN (cn), eg. CEITEC.
# It must be called after ldap_connect() and
# entry must be used before ldap_disconnect().
#
sub getEntryByLogin() {

	my $mesg;
	# Get current entry from LDAP
	$mesg = $ldap->search( base => $base_dn ,
		scope => 'sub' ,
		filter => "(samAccountName=$login)" ,
		attrs => ['cn','samAccountName','userAccountControl']
	);

	if ($mesg->entries != 1) {
		return undef;
	} else {
		my @entries = $mesg->entries;
		my $entry = pop(@entries);
		return $entry;
	}

}
