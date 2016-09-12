#!/usr/bin/perl

###############################################################################
# Tool for parse AuthzResolver authorization from java source code            #
# and print it to STDOUT as html table.                                       #
#                                                                             #
# This script parse FILE(s) from comand line agruments, or standart input.    #
#                                                                             #
# Usage: authz-parser VosManagerEntry.java                                    #
#        authz-parser VosManagerEntry.java  | w3m -T text/html                #
#        find . -name *.java | authz-parser                                   #
#                                                                             #
# Known bugs: Can't recognize (and ignore) comments.                          #
#                                                                             #
###############################################################################

use Data::Dumper;

my $methodName;
my $authorizations;  #data structure

my @roles = ("UNAUTHORIZED","PERUNADMIN","VOADMIN","VOOBSERVER","GROUPADMIN", "FACILITYADMIN","SELF","SERVICE","REGISTRAR","RPC","ENGINE","SECURITYADMIN","AUTHZRESOLVER","TOPGROUPCREATOR","SPONSOR","SERVICEUSER");

print htmlHeader();
print tableHeader(@roles);

while(<>) {

	#line with method definition
	#if(/^\s*public\s+[^\s]+\s+(\w+\s*\(.*\)).*\{\s*$/) {  #this regex accept line with method
	if (/^\s*public([^\(]+)\(([^\)]+)\).*$/) {
		@arr = reverse(split(/\s/,$1)); #last string before (
		$pom=$2; #$2 string inside ()
		$pom =~ s/\</\&lt\;/;
		$pom =~ s/\>/\&gt\;/;
		$methodName = $arr[0]."(".$pom.")";
		$authorizations->{$methodName}->{"UNAUTHORIZED"} = 1;
		next;
	}


	if(/AuthzResolver[\w]*\.isAuthorized/) {
		#get roles and complementary objects and store them into $authorizations data structure
		my @rolesAndObjects = ($_ =~ /AuthzResolver[\w]*\.isAuthorized\(\w+,\s*Role\.([^\)]*)\)/g) ;
		foreach $roleAndObject (@rolesAndObjects) {
			$roleAndObject =~ /^(\w+)(,\s*(\w+))?/;
			my $role = $1;
			my $complementaryObject = $3;
			#print "$methodName,ROLE:$role,$complementaryObject\n";
			#store
			if (defined ($authorizations->{$methodName}->{$role})) {
				if ($authorizations->{$methodName}->{$role} == 1) {
					if (defined($complementaryObject)) {
						$authorizations->{$methodName}->{$role} = $complementaryObject;
					}
				} else {
					if (defined($complementaryObject)) {
						$authorizations->{$methodName}->{$role}=$authorizations->{$methodName}->{$role}." & ".$complementaryObject;
					}
				}
			} else {
				if (defined($complementaryObject)) {
					$authorizations->{$methodName}->{$role} = $complementaryObject;
				} else {
					$authorizations->{$methodName}->{$role} = 1;
				}
			}
		}
	}

	if (/^.*this\.([\w]+)\(([^\)]+)\).*$/) {
		my $calledFunction = $1;
		$pom=$2; #$2 string inside ()
		$pom =~ s/\</\&lt\;/;
		$pom =~ s/\>/\&gt\;/;

		if (not $calledFunction =~ /Bl/) {
			$authorizations->{$methodName}->{"UNAUTHORIZED"} = $calledFunction."(".$pom.")";
		}
	}

}

#output
foreach $methodName (sort keys %$authorizations) {
	for my $role (@roles) {
		if (defined($authorizations->{$methodName}->{$role}) and $role ne "UNAUTHORIZED") { $authorizations->{$methodName}->{"UNAUTHORIZED"} = undef;}
	}

	print "<tr><td>$methodName</td>";
	for my $role (@roles) {
		if(defined($authorizations->{$methodName}->{$role})) {
			print "<td class='ok'>OK";
			if ($authorizations->{$methodName}->{$role} != 1) {print ": ",  $authorizations->{$methodName}->{$role};}
			print "</td>";
		} else {
			print "<td class='nook'>--</td>";
		}
	}
	print "</tr>\n";
}


print tableFooter();
print htmlFooter();
print "\n\n";




#####################################################################################################
# Methods for print to HTML

sub htmlHeader {
	return qq{
	<html>
	<head>
	<title>AuthzResolver</title>
	<style>
	.ok \{ background-color: #66FF66 ; \}
	.nook \{ background-color: #FF3333 ; \}
	</style>
	</head>
	<body>
	};
}

sub htmlFooter {
	return qq{
	</body>
	</html>
	};
}

sub tableHeader {
	$out = "<table border=1><tr>";
	$out .= "<td></td>"; #first column is used for labels, not for data
	for $role (@_) {
		$out .= "<td>$role</td>";
	}
	$out .= "</tr>\n";
	return $out;
}

sub tableFooter {
	return "</table>\n";
}
