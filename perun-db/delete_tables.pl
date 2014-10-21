#!/usr/bin/perl -w

########################
#
#  Script for exporting data from Oracle to PostgreSQL compliant insert syntax.
#
########################

use DBI;
use strict;
use POSIX qw(:errno_h);
use Getopt::Long qw(:config no_ignore_case);

my $perun_root = $ENV{PERUN_ROOT};
$ENV{NLS_LANG} = "american_america.utf8";

# IMPORTANT: these arguments must be set before using script or provided on commandline

my $newuser="perun";                 # PostgreSQL user
my $tableOrderFile="table_order";    # Defined list of tables and their order
my $filename="/tmp/DB_tables_delete";

sub help {
	return qq{
	Export data from Oracle and create Postgres compliant inserts
	--------------------------------------------------------------
	Available options:

	--newUser    | -n Username for Postgres DB (default "perun")
	--file       | -f File with list of tables to export (default "table_order")
	--outputfile | -o Fole for otput (default "/tmp/DB_tables_delete")
	--help       | -h prints this help
	};
}

GetOptions ("help|h" => sub { print help(); exit 0;}, "newUser|n=s" => \$newuser, "file|f=s" => \$tableOrderFile, "outputfile|o=s" => \$filename ) || die help();

my @tabulky = ();
open POR,$tableOrderFile or die "[ERROR] Cannot open $tableOrderFile: $! \n";
while (my $lin=<POR>) {
	chomp($lin);
	unshift (@tabulky,$lin);
}
close POR;

open (DBD,">$filename");
binmode DBD,":utf8";

while (@tabulky) {
	my $tbl=shift(@tabulky);
	my $text='delete from "'.$newuser.'"."'.$tbl.'"';
	print DBD $text;
	print DBD "\n";
}

close DBD;
