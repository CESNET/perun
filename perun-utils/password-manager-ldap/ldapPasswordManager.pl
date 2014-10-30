#!/usr/bin/perl

######################################################
#
# PASSWORD MANAGER FOR LDAP
#
# It takes up to 4 parameters in following order: action, namespace, login, pass
# where action can be "check", "change", "reserve", "validate", "reserve_random", "delete"
# where namespace represents login-namespace used by this LDAP and should match passed login
# namespace also must match LDAPs PWDM config file /etc/perun/pwdchange.[namespace].ldap
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

sub ldap_connect;
sub ldap_disconnect;
sub ldap_search;
sub ldap_log;

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
		ldap_connect($namespace);
		my $mesg;
		eval {
			$mesg = $ldap->modify( "uid=" . $login . "," . $base_dn ,
				replace => {
					userPassword  => $user_pass
				}
			);
		};
		if ( $@ ) {

			# Error thrown by $ldap
			ldap_log("[PWDM] Password change failed with LDAP return code: " . $@);
			ldap_disconnect();
			exit 3; # setting new password failed
		} else {

			# Ok case
			ldap_disconnect();
			my $code = $mesg->code;
			if ($code != 0) {
				ldap_log("[PWDM] Password change failed with LDAP return code: ".$code);
				exit 3; # setting new password failed
			}

			# CHANGE WAS SUCCESSFUL
			ldap_log("[PWDM] Password changed.");
		}

	}

	case("check"){

		$user_pass = <STDIN>;
		ldap_connect( $namespace );

		my $mesg;
		eval {
			$mesg = $ldap->compare( "uid=" . $login . "," . $base_dn ,
				attr  => 'userPassword',
				value => $user_pass
			);
		};
		if ( $@ ) {

			# ERROR WHEN CHECKING - e.g. entry doesn't exists
			ldap_log("[PWDM] Password check failed with LDAP return code: ".$@);
			ldap_disconnect();
			exit 6; # TODO - is this right exit code ?
		} else {

			# CHECK WAS SUCCESSFULL - READ CHECK RESULT
			ldap_disconnect();
			my $code = $mesg->code;
			if ($code == 5) {
				ldap_log("[PWDM] Password doesn't match.");
				exit 1; # password doesn't match
			}
			if ($code == 6) {
				ldap_log("[PWDM] Password match.");
				exit 0; # password match
			}
			if ($code != 0) {
				ldap_log("[PWDM] Password check failed with LDAP return code: ".$code);
				exit 6; # TODO - is this right exit code ?
			}
		}

	}

	case("reserve"){

		$user_pass = <STDIN>;
		ldap_connect( $namespace );

		my $entry = Net::LDAP::Entry->new;
		$entry->dn("uid=" . $login . "," . $base_dn );
		$entry->add(
			objectClass => ["top", "inetUser", "inetOrgPerson"] ,
			userPassword => $user_pass ,
			inetUserStatus => "inactive" ,
			cn => $login ,
			sn => $login
		);

		my $mesg;
		eval {
			$mesg = $ldap->add( $entry );
		};
		if ( $@ ) {

			# error adding entry
			ldap_log("[PWDM] Creation of password failed with LDAP return code: ".$@);
			ldap_disconnect();
			exit 4; # creation of new password failed
		} else {
			ldap_disconnect();
			my $code = $mesg->code;
			if ($code != 0) {
				ldap_log("[PWDM] Creation of password failed with LDAP return code: ".$code);
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
			$mesg = $ldap->delete( "uid=" . $login . "," . $base_dn );
		};
		if ( $@ ) {
			ldap_log("[PWDM] Password deletion failed with LDAP return code: " . $@);
			ldap_disconnect();
			exit 5; # can't delete password
		} else {
			ldap_disconnect();
			my $code = $mesg->code;
			if($code != 0) {
				ldap_log("[PWDM] Password deletion failed with LDAP return code: ".$code);
				exit 5 # can't delete password
			}
			ldap_log("[PWDM] Password deleted.");
		}

	}

	case("validate"){

		ldap_connect( $namespace );
		my $mesg;
		eval {
			$mesg = $ldap->modify( "uid=" . $login . "," . $base_dn ,
				replace => {
					inetUserStatus  => 'active'
				}
			);
		};
		if ( $@ ) {

			# ERROR WHEN VALIDATING
			ldap_log("[PWDM] Password validation failed with LDAP return code: " . $@);
			ldap_disconnect();
			exit 6; # TODO - is this right exit code ?
		} else {

			# ENTRY VALIDATED
			ldap_disconnect();
			my $code = $mesg->code;
			if($code != 0) {
				ldap_log("[PWDM] Password validation failed with LDAP return code: ".$code);
				exit 6; # TODO - is this right exit code ?
			}
			ldap_log("[PWDM] Password validated.");
		}

	}

	case("reserve_random"){

		ldap_connect( $namespace );
		my $mesg;

		# CREATE ENTRY WITHOUT PASSWORD and as inactive
		eval {
			$mesg = $ldap->add( "uid=" . $login . "," . $base_dn ,
				attrs => [
					objectClass => ['top','inetUser','inetOrgPerson' ] ,

					#userPassword => $user_pass ,
					inetUserStatus  => 'inactive' ,
					cn => $login ,
					sn => $login
				]
			);
		};
		if ( $@ ) {

			# ERROR WHEN RESERVING
			ldap_log("[PWDM] Random password reservation failed with LDAP return code: " . $@);
			ldap_disconnect();
			exit 4; # creation of new password failed
		} else {
			my $code = $mesg->code;
			ldap_disconnect();
			if ($code != 0) {
				ldap_log("[PWDM] Random password reservation failed with LDAP return code: " . $code);
			}
			exit 4 # creation of new password failed
		}
		ldap_log("[PWDM] Random password reserved.");
	}

	#case("search"){
	#  ldap_connect( $namespace );
	#  ldap_search( $login );
	#  ldap_disconnect();
	#}

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
	my $filename = "/etc/perun/pwchange.".$namespace.".ldap";
	unless (-e $filename) {
		ldap_log("[PWDM] Configuration file for namespace \"" . $namespace . "\" doesn't exist!");
		exit 2; # login-namespace is not supported
	}

	# load configuration file
	open FILE, "<" . $filename;
	my @lines = <FILE>;

	# remove new-line characters from the end of lines
	chomp @lines;

	# read configuration
	my $ldap_location = $lines[0];
	my $ldap_user = $lines[1];
	my $ldap_pass = $lines[2];
	$base_dn = $lines[3];

	# LDAP connect
	$ldap = Net::LDAPS->new( "$ldap_location" , onerror => 'die' , timeout => 5 , debug => 0, verify => 'none', port => 636);

	# LDAP log-in
	if ($ldap) {
		my $mesg = $ldap->bind( "$ldap_user" , password => "$ldap_pass" );
		ldap_log("[LDAP] connected as: $ldap_user");
	} else {
		ldap_log("[LDAP] can't connect to LDAP.");
		exit 1;
	}

}

#
# Disconnect from LDAP if connected
#
sub ldap_disconnect(){
	if ($ldap) {
		my $mesg = $ldap->unbind;
		ldap_log("[LDAP] disconnected.");
	} else {
		ldap_log("[LDAP] can't disconnect from LDAP (connection not exists).");
	}
}

#
# Search used only for debugging purposes
#
sub ldap_search {

	my $login = (@_)[0];

	my $mesg = $ldap->search( base => $base_dn ,
		scope => 'sub' ,
		filter => "(uid=$login)" ,
		attrs => ['dn','userPassword', 'inetUserStatus']
		);

	#print "\n[LDAP] Search return code: " . $mesg->code;
	my @size = $mesg->entries;
	print "\n[LDAP] Found entries: " . ~~@size;
	Net::LDAP::LDIF->new( \*STDOUT, "w" )->write( $mesg->entries );

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