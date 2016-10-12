#!/usr/bin/perl -w

########################
#
#  Script for generating delete from all Perun table SQL file.
#  Used for clearing DB before new import (prod->devel).
#
########################

use DBI;
use strict;
use POSIX qw(:errno_h);
use Getopt::Long qw(:config no_ignore_case);

my $perun_root = $ENV{PERUN_ROOT};
$ENV{NLS_LANG} = "american_america.utf8";

# IMPORTANT: these arguments must be set before using script or provided on commandline

my $user="perun";                    # PostgreSQL user
my $tableOrderFile="table_order";    # Defined list of tables and their order
my $tablesToDelete="ADD_tablesToDelete";
my $filename="DB_tables_delete.sql";

sub help {
	return qq{
	Create 'delete from all Perun tables' SQL to clear DB before new import.
	------------------------------------------------------------------------
	Available options:

	--user       | -u Username for Postgres DB (default "perun")
	--file       | -f File with list of tables to export (default "table_order")
	--outputFile | -o File for output (default "DB_tables_delete.sql")
	--help       | -h prints this help

};
}

GetOptions ("help|h" => sub { print help(); exit 0;}, "user|u=s" => \$user, "file|f=s" => \$tableOrderFile, "outputFile|o=s" => \$filename ) || die help();

my @tables = ();
open POR,$tableOrderFile or die "[ERROR] Cannot open $tableOrderFile: $! \n";
while (my $lin=<POR>) {
	chomp($lin);
	unshift (@tables,$lin);
}
close POR;

open POR,$tablesToDelete or die "[ERROR] Cannot open $tablesToDelete: $! \n";
while (my $lin=<POR>) {
	chomp($lin);
	unshift (@tables,$lin);
}
close POR;

open (DBD,">$filename") or die "[ERROR] Cannot open $filename ! \n";
binmode DBD,":utf8";

while (@tables) {
	my $table=shift(@tables);
	my $text='delete from "'.$user.'"."'.$table.'";';
	print DBD $text;
	print DBD "\n";
}

close DBD;
