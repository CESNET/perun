#!/usr/bin/perl -w

########################
#
#  Script for exporting data from Postgres to PostgreSQL compliant insert syntax.
#
########################

use DBI;
use strict;
use POSIX qw(:errno_h);
use Getopt::Long qw(:config no_ignore_case);

$ENV{NLS_LANG} = "american_america.utf8";

# IMPORTANT: these arguments must be set before using script or provided on commandline

my $user;                            # Oracle DB user
my $pwd;                             # Oracle DB password
my $newuser="perun";                 # PostgreSQL user
my $tableOrderFile="table_order";    # Defined list of tables and their order
my $tab;                             # Single table to export if preferred
my $allTables = undef;               # Tag for exporting all tables

sub help {
	return qq{
	Export data from Oracle and create Postgres compliant inserts
	--------------------------------------------------------------
	Available options:

	--user      | -u Username for Oracle DB (required)
	--password  | -w Password for Oracle DB (required)
	--newUser   | -n Username for Postgres DB (if not set, use "perun")
	--file      | -f File with list of tables to export (if not set, use "table_order")
	--table     | -t Name of single table to export (if set, ignore --file|-f option)
        --allTables | -a exports all tables incuding tables usually don't exported
	--help      | -h prints this help

	Usage:

	Export all tables: -u [login] -w [pass]
	Export base tables: -u [login] -w [pass] -f table_order_base
	Export single table: -u [login] -w [pass] -t [table_name]
	Export custom tables: -u [login] -w [pass] -f [file_with_table_names]
        Export all tables: -u [login] -w [pass] -f [file_with_table_names] -a
	};
}

GetOptions ("help|h" => sub { print help(); exit 0;},
"user|u=s" => \$user, 
"password|w=s" => \$pwd, 
"newUser|n=s" => \$newuser, 
"file|f=s" => \$tableOrderFile, 
"allTables|a" => \$allTables,
"table|t=s" => \$tab) || die help();

if (!defined $user) { print "[ERROR] Username for Oracle DB is required! Use --help | -h to print help.\n"; exit 1; }
if (!defined $pwd) { print "[ERROR] Password for Oracle DB is required! Use --help | -h to print help.\n"; exit 1; }

my $dbh = DBI->connect('dbi:Pg:dbname=perun',$user,$pwd,{RaiseError=>1,AutoCommit=>0,pg_enable_utf8=>1}) or die EPERM," Connect";

my $filename;
my @tabulky = ();
my %tabs;
my $tabstodelete="ADD_tablesToDeletePR";

if (defined($tab)) {
	push (@tabulky, $tab);
	$filename=$tab."_data.sql.PR";
} else {
        open (DEL,">$tabstodelete");
	binmode DEL,":utf8";

	open POR,$tableOrderFile or die "[ERROR] Cannot open $tableOrderFile: $! \n";
	while (my $lin=<POR>) {
		chomp($lin);
		push (@tabulky,$lin);
                $tabs{$lin}=1;
	}
	close POR;
	$filename="DB_data.PR.sql";

	# all tables 
	my $tablename = $dbh->prepare(qq{
	    select table_name from INFORMATION_SCHEMA.TABLES where table_schema=? and table_name not like 'scim%'});
	$tablename->execute($user);
	my $tbn;
	while ($tbn = $tablename->fetch) {
	    if (not exists($tabs{$$tbn[0]})) {
	        push (@tabulky, $$tbn[0]) if defined $allTables;
	        print DEL $$tbn[0]."\n";
	    }
        }
       close DEL;
}

open (DBD,">$filename");
binmode DBD,":utf8";

my $tabcolumn = $dbh->prepare(qq{select column_name, data_type from INFORMATION_SCHEMA.COLUMNS where table_name =? and table_schema=? order by column_name});

while (@tabulky) {
	my $tbl=shift(@tabulky);
        my @instext=();
	$tabcolumn->execute($tbl,$user);
	my $textinsert='insert into "'.$newuser.'"."'.$tbl.'" (';
	my $tcol;
	my %columns;
	my $textcols="";
	my @cols=();
	while ($tcol=$tabcolumn->fetch) {
		$columns{$$tcol[0]}=$$tcol[1];
		push (@cols,$$tcol[0]);
	}
	if (@cols == 0) {warn "[WARN] Non-existing table name $tbl\n"; next;}
        # order columns by Perl
        foreach my $col (sort @cols) {
             $textcols=$textcols.$col.",";
        }
	$textcols=substr($textcols,0,-1);
	$textinsert=$textinsert.$textcols.") values (";
	my $tsel="select ";
	my $tsel2;
	foreach my $column (sort @cols) {
	    $tsel=$tsel.$column.",";
	}
	$tsel=substr($tsel,0,-1)." from $tbl";
	if ($tbl eq "groups") {
		$tsel2=$tsel." where parent_group_id is not null";
		$tsel=$tsel." where parent_group_id is null";
	}

	# Keep only last 10 days of table auditer_log
	if ($tbl eq "auditer_log") {
		$tsel=$tsel." where created_at > to_date(to_char(now() - interval '10 days','YYYY-MM-DD'),'YYYY-MM-DD')";
	}
	SEL:  my $colval = $dbh->prepare($tsel);
	$colval->execute();
	my $val;
	while ($val=$colval->fetch) {
		my $tval="";
		my $ii=0;
		foreach my $column ( sort @cols) {
			if (not defined($$val[$ii])) {
				$tval=$tval."null,";
			} else {
				if ($columns{$column} eq "integer" or $columns{$column} eq "numeric" or $columns{$column} eq "bigint" or $columns{$column} eq "boolean") {$tval=$tval.$$val[$ii].",";}
				if ($columns{$column} =~ /character/) {$$val[$ii] =~ s/'/''/g; $tval=$tval."'".$$val[$ii]."',";}
				if ($columns{$column} eq "text") {$$val[$ii] =~ s/'/''/g; $tval=$tval."'".$$val[$ii]."',";}
				if ($columns{$column} =~ /timestamp/) {$tval=$tval."timestamp '".$$val[$ii]."',";}
			}
			$ii++;
		}
		$tval=substr($tval,0,-1).");";
                push (@instext,$textinsert.$tval."\n");
	}
        foreach my $textrow (sort @instext) {
             print DBD $textrow;
        }
	if (defined $tsel2) {$tsel=$tsel2; undef($tsel2); @instext=(); goto SEL; }
}

print DBD "\n\n";

my %sequences;
my $seqname = $dbh->prepare(qq{select sequence_name from INFORMATION_SCHEMA.SEQUENCES  where sequence_name like ?||'%'});

if (defined($tab)) {
	my $seq=$dbh->selectrow_array($seqname,{},$tab);
        unless (defined $seq) { warn "[WARN] No sequence found for table: $tab \n"; close DBD; commit $dbh; exit;}
	$sequences{$seq}=$tab;
} else {
	my $tablename = $dbh->prepare(qq{select table_name from INFORMATION_SCHEMA.TABLES where table_schema=? and table_name not like 'scim%' order by table_name});
	$tablename->execute($user);

	while (my $tbn = $tablename->fetch) {
		$seqname->execute($$tbn[0]);

		while (my $seqn = $seqname->fetch) {
			$sequences{$$seqn[0]}=$$tbn[0];
		}
		# Manually match this sequence, since it's name is not standardized as for others
		$sequences{"cabinet_pub_sys_id_seq"}="cabinet_publication_systems";
	}
}

while (my ($seq,$tbl) = each(%sequences)) {
	$seq=lc($seq);
	unless (defined($tbl)) { warn "[WARN] No table found for sequence: $seq \n"; next;}
	my $max=$dbh->selectrow_array("select coalesce(max(id),0)+1 from $tbl",{});
	unless (defined($max)) { warn "[WARN] No maxvalue found for sequence: $seq and table: $tbl\n"; next;}
	print DBD "drop sequence \"".$seq."\";\n";
	print DBD "create sequence \"".$seq."\" start with ".$max." maxvalue 9223372036854775807;\n";
}

close DBD;
commit $dbh;
