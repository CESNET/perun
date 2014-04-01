#!/usr/bin/perl

my $table_begin = 0;  #true if table start tag was already printed

while(<>) {
	chomp;
	s/^\s*//; #remove whitespaces at begining of each line
	if(/^--\w+ /) { #matches line with command option
		if(!$table_begin) {
			print "{| width=75%\n";
			$table_begin = 1;
		} else {
			print "|-\n";  #print this beetwen lines of ouput table
		}
		s/^(--\w+) +\| (-\w) *(( [^\s]+)+) */| \1  || '''\2'''  || \3  || /; #format the line with command option
		s/\|\|\s*$//;  #remove trailing '||'
	}

	print $_, "\n";
}

if($table_begin) { #prints end table tag
	print "|}\n";
}
