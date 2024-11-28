#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';


use DBI;
use POSIX qw(:errno_h);
use Getopt::Long qw(:config no_ignore_case);

sub help {
    return qq{
        Adds ORGANIZATIONMEMBERSHIPMANAGER role to attribute policies.
        ORGANIZATIONMEMBERSHIPMANAGER can read user, member, user-facility and member-group attributes that VOADMIN can
        read. For other entities it can read some handpicked attributes plus those that GROUPMEMBERSHIPMANAGER can read.
        For write policies it simply follows GROUPMEMBERSHIPMANAGER + some manually picked attributes.
        The script creates new policy collections for these attributes with single policy containing the new role.
        If the role is not in the database, the script inserts it first.
        --------------------------------------
        Available options:

        --user      | -u Username for Perun DB (required)
        --password  | -w Password for Perun DB (required)
        --overwrite | -o removes all existing COLLECTIONS! containing policy with Organizationmembershipmanager role
        };
}

my ($user, $pwd, $overwrite);

GetOptions ("help|h" => sub { print help(); exit 0;},
    "user|u=s" => \$user,
    "password|w=s" => \$pwd,
    "overwrite|o" => \$overwrite) || die help();

if (!defined $user) { print "[ERROR] Username for Perun DB is required! Use --help | -h to print help.\n"; exit 1; }
if (!defined $pwd) { print "[ERROR] Password for Perun DB is required! Use --help | -h to print help.\n"; exit 1; }

my $dbh = DBI->connect('dbi:Pg:dbname=perun',$user,$pwd,{RaiseError=>1,AutoCommit=>0,pg_enable_utf8=>1}) or die EPERM," Connect";

# additional (not in GROUPMEMBERSHIPMANAGER) attributes for reading
my @readAttributes = (
    "urn:perun:vo:attribute-def:def:applicationViewPreferences",
    "urn:perun:vo:attribute-def:core:id",
    "urn:perun:vo:attribute-def:core:shortName",
    "urn:perun:vo:attribute-def:core:name",
    "urn:perun:vo:attribute-def:def:membershipExpirationRules",
    "urn:perun:vo:attribute-def:def:autoApproveByGroupMembership"
);
# additional (not in GROUPMEMBERSHIPMANAGER) attributes for writing
my @writeAttributes = (
    "urn:perun:group:attribute-def:def:autoApproveByGroupMembership",
    "urn:perun:group:attribute-def:def:applicationViewPreferences",
    "urn:perun:vo:attribute-def:def:applicationViewPreferences",
    "urn:perun:vo:attribute-def:def:autoApproveByGroupMembership",
    "urn:perun:member:attribute-def:def:membershipExpiration"
);
my $writeAttributesList = join(",", map { $dbh->quote($_) } @writeAttributes);

my $sthGroupMembershipManagerReadAttrs = $dbh->prepare(q{select a.attr_name from attr_names a, attribute_policy_collections c,
	attribute_policies p, roles r where p.policy_collection_id=c.id and c.attr_id=a.id
	and r.name = 'groupmembershipmanager' and r.id=p.role_id and c.action='READ';});

my $sthCollectionsRead = $dbh->prepare(q{select a.id, a.attr_name from attr_names a, attribute_policy_collections c,
	attribute_policies p, roles r where p.policy_collection_id=c.id and c.attr_id=a.id
	and r.name = 'voadmin' and r.id=p.role_id and c.action='READ';});

my $sthCollectionsWrite = $dbh->prepare(qq{select a.id, a.attr_name from attr_names a, attribute_policy_collections c,
	attribute_policies p, roles r where p.policy_collection_id=c.id and c.attr_id=a.id
	and (r.name = 'groupmembershipmanager' OR a.attr_name IN ($writeAttributesList)) and r.id=p.role_id and c.action='WRITE';});

my $sthNewCollection = $dbh->prepare(q{insert into attribute_policy_collections (id, attr_id, action) values (?,?,?);});

my $sthNewPolicy = $dbh->prepare(q{insert into attribute_policies (id, role_id, object, policy_collection_id) values (?,?,?,?);});

my $sthInsertRole = $dbh->prepare(q{insert into roles (id, name) values (?,'organizationmembershipmanager');});

my $sthDelCollections = $dbh->prepare(q{delete from attribute_policy_collections where id=?;});

my $sthExistingPolicies = $dbh -> prepare(q{select policy_collection_id from attribute_policies where role_id=?;});

#-------------------------------------------------------------------------------------------------- execution

# Insert ORGANIZATIONMEMBERSHIPMANAGER role to DB if not created yet
my $roleId = $dbh->selectrow_array(q{select id from roles where name='organizationmembershipmanager'});
unless ($roleId) {
    $roleId = $dbh->selectrow_array('select nextval(\'roles_id_seq\')');
    $sthInsertRole->execute($roleId);
    $dbh->commit() or die $dbh->errstr;
    print "INFO: Organizationmembershipmanager role created\n";
}

# Remove all collections where any policy with Organizationmembershipmanager exists
if ($overwrite) {
    $sthExistingPolicies->execute($roleId);
    while (my @row = $sthExistingPolicies->fetchrow_array()) {
        my ($collectionId) = @row;
        $sthDelCollections->execute($collectionId);
    }
    $dbh->commit() or die $dbh->errstr;
    print "INFO: Existing policy collections successfully removed.\n";
}

$sthGroupMembershipManagerReadAttrs->execute();

# Fetch all attr_names that group membership manager can read
my @groupMembershipManagerReadAttrsNames;
while (my $row = $sthGroupMembershipManagerReadAttrs->fetchrow_arrayref) {
    push @groupMembershipManagerReadAttrsNames, $row->[0];
}

# add READ policy for ORGANIZATIONMEMBERSHIPMANAGER
$sthCollectionsRead->execute();
while (my @row = $sthCollectionsRead->fetchrow_array()) {
    my ($attrId, $attrName) = @row;
    my $attrType = (split ':', $attrName)[2];

    # add the manually defined attributes + all of the group membership manager read attributes
    my ($shouldRead) = (grep {$attrName eq $_} @readAttributes) ||
        (grep {$attrName eq $_} @groupMembershipManagerReadAttrsNames);

    if ($attrType eq "member" || $attrType eq "member_group" || $attrType eq "user_facility" || $attrType eq "user" || $shouldRead) {
        my $nextCollectionId = $dbh->selectrow_array('select nextval(\'attribute_policy_collections_id_seq\')');
        my $nextPolicyId = $dbh->selectrow_array('select nextval(\'attribute_policies_id_seq\')');
        $sthNewCollection->execute($nextCollectionId, $attrId, "READ");          #(id, attr_id, action)
        $sthNewPolicy->execute($nextPolicyId, $roleId, "Vo", $nextCollectionId); #(id, role_id, object, policy_collection_id)
    }
}

# add WRITE policy to ORGANIZATIONMEMBERSHIPMANAGER
$sthCollectionsWrite->execute();
while (my @row = $sthCollectionsWrite->fetchrow_array()) {
    my ($attrId, $attrName) = @row;

    my $nextCollectionId = $dbh->selectrow_array('select nextval(\'attribute_policy_collections_id_seq\')');
    my $nextPolicyId = $dbh->selectrow_array('select nextval(\'attribute_policies_id_seq\')');
    $sthNewCollection->execute($nextCollectionId, $attrId, "WRITE"); #(id, attr_id, action)
    $sthNewPolicy->execute($nextPolicyId, $roleId, "Vo", $nextCollectionId); #(id, role_id, object, policy_collection_id)
}

$dbh->commit() or die $dbh->errstr;

print "INFO: Organizationmembershipmanager role successfully set.\n";
print "===================================================\n";
$dbh->disconnect() or die $dbh->errstr;
