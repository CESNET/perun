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

my $user;                            # Oracle DB user
my $pwd;                             # Oracle DB password
my $newuser="perun";                 # PostgreSQL user
my $tableOrderFile="table_order";    # Defined list of tables and their order
my $tab;                             # Single table to export if preferred

sub help {
	return qq{
	Export data from Oracle and create Postgres compliant inserts
	--------------------------------------------------------------
	Available options:

	--user     | -u Username for Oracle DB (required)
	--password | -w Password for Oracle DB (required)
	--newUser  | -n Username for Postgres DB (if not set, use "perun")
	--file     | -f File with list of tables to export (if not set, use "table_order")
	--table    | -t Name of single table to export (if set, ignore --file|-f option)
	--help     | -h prints this help

	Usage:

	Export all tables: -u [login] -w [pass]
	Export base tables: -u [login] -w [pass] -f table_order_base
	Export single table: -u [login] -w [pass] -t [table_name]
	Export custom tables: -u [login] -w [pass] -f [file_with_table_names]
	};
}

GetOptions ("help|h" => sub { print help(); exit 0;},"user|u=s" => \$user, "password|w=s" => \$pwd, "newUser|n=s" => \$newuser, "file|f=s" => \$tableOrderFile, "table|t=s" => \$tab) || die help();

if (!defined $user) { print "[ERROR] Username for Oracle DB is required! Use --help | -h to print help.\n"; exit 1; }
if (!defined $pwd) { print "[ERROR] Password for Oracle DB is required! Use --help | -h to print help.\n"; exit 1; }

my $dbh = DBI->connect('dbi:Oracle:',$user,$pwd,{RaiseError=>1,AutoCommit=>0,LongReadLen=>65530, ora_charset => 'AL32UTF8'}) or die EPERM," Connect";

my $filename;
my @tabulky = ();
if (defined($tab)) {
	push (@tabulky, $tab);
	$filename=$tab."_data.sql";
} else {
	open POR,$tableOrderFile or die "[ERROR] Cannot open $tableOrderFile: $! \n";
	while (my $lin=<POR>) {
		chomp($lin);
		push (@tabulky,$lin);
	}
	close POR;
	$filename="DB_data.sql";

	# Commented old way of exporting all tables without exact order

	#my $tablename = $dbh->prepare(qq{
	#select lower(table_name) from all_tables where lower(owner)=?});
	#$tablename->execute($user);
	#my $tbn;
	#while ($tbn = $tablename->fetch) {
	#push (@tabulky, $$tbn[0]);
	#}

}

open (DBD,">$filename");
binmode DBD,":utf8";

my $tabcolumn = $dbh->prepare(qq{select lower(column_name),data_type from all_tab_columns where lower(table_name)=? and lower(owner)=?});

while (@tabulky) {
	my $tbl=shift(@tabulky);
	$tabcolumn->execute($tbl,$user);
	my $textinsert='insert into "'.$newuser.'"."'.$tbl.'" (';
	my $tcol;
	my %columns;
	my $textcols="";
	my @cols=();
	my @savecols=();
	while ($tcol=$tabcolumn->fetch) {
		$columns{$$tcol[0]}=$$tcol[1];
		push (@cols,$$tcol[0]);
		push (@savecols,$$tcol[0]);
		$textcols=$textcols.$$tcol[0].",";
	}
	if (@cols == 0) {warn "[WARN] Non-existing table name $tbl\n"; next;}
	$textcols=substr($textcols,0,-1);
	$textinsert=$textinsert.$textcols.") values (";
	my $tsel="select ";
	my $tsel2;
	@cols=@savecols;
	while (@cols) {
		my $column=shift(@cols);
		if ($columns{$column} eq "DATE") {$tsel=$tsel."to_char($column,'YYYY-MM-DD HH24:MI:SS.D'),";
		} else {
			$tsel=$tsel.$column.",";
		}
	}
	$tsel=substr($tsel,0,-1)." from $tbl";
	if ($tbl eq "groups") {
		$tsel2=$tsel." where parent_group_id is not null order by id";
		$tsel=$tsel." where parent_group_id is null order by id";
	}

	# Keep only last 10 days of table auditer_log
	if ($tbl eq "auditer_log") {
		$tsel=$tsel." where created_at>sysdate-10 order by created_at";
	}
	SEL:  my $colval = $dbh->prepare($tsel);
	$colval->execute();
	my $val;
	while ($val=$colval->fetch) {
		my $tval="";
		my $ii=0;
		@cols=@savecols;
		while (@cols) {
			my $column=shift(@cols);
			if (not defined($$val[$ii])) {
				$tval=$tval."null,";
			} else {
				if ($columns{$column} eq "NUMBER") {$tval=$tval.$$val[$ii].",";}
				if ($columns{$column} eq "VARCHAR2" or $columns{$column} eq "NVARCHAR2") {$$val[$ii] =~ s/'/''/g; $tval=$tval."'".$$val[$ii]."',";}
				if ($columns{$column} eq "CHAR") {$$val[$ii] =~ s/'/''/g; $tval=$tval."'".$$val[$ii]."',";}
				if ($columns{$column} eq "CLOB") {$$val[$ii] =~ s/'/''/g; $tval=$tval."'".$$val[$ii]."',";}
				if ($columns{$column} eq "DATE") {$tval=$tval."timestamp '".$$val[$ii]."',";}
			}
			$ii++;
		}
		$tval=substr($tval,0,-1).");";
		print DBD $textinsert;
		print DBD $tval."\n";
	}
	if (defined $tsel2) {$tsel=$tsel2; undef($tsel2); goto SEL; }
}

print DBD "\n\n";

my %sequences;
my $seqname = $dbh->prepare(qq{select sequence_name from all_sequences where sequence_name like ?||'%'});

if (defined($tab)) {
	my $seq=$dbh->selectrow_array($seqname,{},uc($tab));
	$sequences{$seq}=$tab;
} else {
	my $tablename = $dbh->prepare(qq{select table_name from all_tables where lower(owner)=? order by table_name});
	$tablename->execute($user);

	while (my $tbn = $tablename->fetch) {
		$seqname->execute($$tbn[0]);

		while (my $seqn = $seqname->fetch) {
			$sequences{$$seqn[0]}=$$tbn[0];
		}
		# Manually match this sequence, since it's name is not standardized as for others
		$sequences{"CABINET_PUB_SYS_ID_SEQ"}="CABINET_PUBLICATION_SYSTEMS";
	}
}

while (my ($seq,$tbl) = each(%sequences)) {
	$seq=lc($seq);
	unless (defined($tbl)) { warn "[WARN] No table found for sequence: $seq \n"; next;}
	my $max=$dbh->selectrow_array("select nvl(max(id),0)+1 from $tbl",{});
	unless (defined($max)) { warn "[WARN] No maxvalue found for sequence: $seq and table: $tbl\n"; next;}
	print DBD "drop sequence \"".$seq."\";\n";
	print DBD "create sequence \"".$seq."\" start with ".$max." maxvalue 9223372036854775807;\n";
}

close DBD;
commit $dbh;
