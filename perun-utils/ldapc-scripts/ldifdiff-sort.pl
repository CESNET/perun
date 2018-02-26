#!/usr/bin/perl

my $TYPE_BRANCH = "BRANCH";
my $TYPE_PEOPLE = "PEOPLE";
my $TYPE_USER = "USER";
my $TYPE_GROUP = "GROUP";
my $TYPE_RESOURCE = "RESOURCE";
my $TYPE_VO = "VO";
my $TYPE_SPECIAL_USERS = "SPECIAL_USERS";

my $OP_ADD = "add";
my $OP_MODIFY = "modify";
my $OP_DELETE = "delete";

my $object_read = 0;
my $operation_read = 0;

my $object_type;
my $operation;
my $object;

my $categorized_objects = {};

while(<>) {


	if(!$object_read) {
		#skip empty lines at start of file
		next if /^$/;

		$object_read = 1;
		if (/^dn: dc=/) {
			$object_type = $TYPE_BRANCH;
		} elsif (/^dn: ou=People/) {
			$object_type = $TYPE_PEOPLE;
		} elsif(/^dn: perunUserId/) {
			$object_type = $TYPE_USER;
		} elsif (/^dn: perunGroupId/) {
			$object_type = $TYPE_GROUP;
		} elsif (/^dn: perunResourceId/) {
			$object_type = $TYPE_RESOURCE;
		} elsif (/^dn: perunVoId/) {
			$object_type = $TYPE_VO;
		} elsif (/^dn: cn=/) {
			$object_type = $TYPE_SPECIAL_USERS;
		} else {
			die "Unknown object: $_";
		}
	} elsif(!$operation_read) {
		$operation_read = 1;
		if(/^changetype: modify$/) {
			$operation = $OP_MODIFY;
		} elsif(/^changetype: add$/) {
			$operation = $OP_ADD;
		} elsif(/^changetype: delete$/) {
			$operation = $OP_DELETE;
		} else {
			die "Unknown changetype: $_";
		}
	} elsif(/^$/) {
		#empty line is separator of objects

		push @{$categorized_objects->{$operation}->{$object_type}}, $object;
		$object = "";
		$object_read = 0;
		$operation_read = 0;
	}

	$object .= $_;

}
push @{$categorized_objects->{$operation}->{$object_type}}, $object;


print join "\n", (@{$categorized_objects->{$OP_ADD}->{$TYPE_BRANCH}}, 
                  @{$categorized_objects->{$OP_ADD}->{$TYPE_PEOPLE}}, 
                  @{$categorized_objects->{$OP_ADD}->{$TYPE_VO}}, 
                  @{$categorized_objects->{$OP_ADD}->{$TYPE_GROUP}}, 
                  @{$categorized_objects->{$OP_ADD}->{$TYPE_RESOURCE}},
                  @{$categorized_objects->{$OP_ADD}->{$TYPE_USER}},
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_BRANCH}}, 
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_PEOPLE}}, 
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_VO}}, 
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_GROUP}}, 
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_RESOURCE}}, 
                  @{$categorized_objects->{$OP_MODIFY}->{$TYPE_USER}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_USER}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_RESOURCE}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_GROUP}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_VO}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_PEOPLE}}, 
                  @{$categorized_objects->{$OP_DELETE}->{$TYPE_BRANCH}},
                 # WE want to skip special users from modification 
                 #@{$categorized_objects->{$OP_ADD}->{$TYPE_SPECIAL_USERS}},
                 #@{$categorized_objects->{$OP_MODIFY}->{$TYPE_SPECIAL_USERS}}, 
                 #@{$categorized_objects->{$OP_DELETE}->{$TYPE_SPECIAL_USERS}}, 
                 );
print "\n";
