######################################################
#
# PASSWORD MANAGER FOR LDAP
#
# It takes up to 4 parameters in following order: action, namespace, login, pass
# where action can be "check", "change", "reserve", "validate", "reserve_random", "delete"
# where namespace represents login-namespace used by radius server (eduroam) and should match passed login
# namespace also must match PWDM config file /etc/perun/pwdchange.[namespace].eduroam
# where login is users login to reserve
# where password is users password in plaintext (required only for actions check, change, reserve)
#
#####################################################

#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

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

		# always change to the new one
		$user_pass = <STDIN>;
		chomp($user_pass);

		my $converted_pass = `printf '%s' "$user_pass" | iconv -t utf16le | openssl md4`;

		my $entry = "\"$login\@vsup.cz\" NT-Password := \"" . $converted_pass . "\"".

			eval {
				# TODO - CALL change script
			};
		if ( $@ ) {
			# error adding entry
			edu_log("[PWDM] Change of password failed with return code: ".$@);
			exit 3; # setting of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Password changed.");
		}

	}

	case("check"){

		# always change to the new one
		$user_pass = <STDIN>;
		chomp($user_pass);

		my $converted_pass = `printf '%s' "$user_pass" | iconv -t utf16le | openssl md4`;

		my $entry = "\"$login\@vsup.cz\" NT-Password := \"" . $converted_pass . "\"".

		eval {
			# TODO - CALL check script
		};
		if ( $@ ) {
			# error adding entry
			edu_log("[PWDM] Check of password failed with return code: ".$@);
			exit 6; # checking old password failed
		} else {
			# entry added
			edu_log("[PWDM] Password changed.");
		}

	}

	case("reserve"){

		$user_pass = <STDIN>;
		chomp($user_pass);

		my $converted_pass = `printf '%s' "$user_pass" | iconv -t utf16le | openssl md4`;

		my $entry = "\"$login\@vsup.cz\" NT-Password := \"" . $converted_pass . "\"".

		eval {
			# TODO - CALL update script
		};
		if ( $@ ) {
			# error adding entry
			edu_log("[PWDM] Creation of password failed with return code: ".$@);
			exit 4; # creation of new password failed
		} else {
			# entry added
			edu_log("[PWDM] Password reserved.");
		}

	}

	case("delete") {

		# partial entry for delete matching
		my $entry = "\"$login\@vsup.cz\" NT-Password := \""

	}

	case("validate") {

		exit 0;

	}

	case("reserve_random") {

		# TODO - will we support this ?

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
	open(LOGFILE, ">>/usr/local/bin/pwdm.log");
	print LOGFILE (localtime(time) . ": " . $message . "\n");
	close(LOGFILE);

}