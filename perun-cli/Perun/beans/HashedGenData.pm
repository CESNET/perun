package Perun::beans::HashedGenData;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $class = shift;
	$class = ref $class if ref $class;

	my $self = { };
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		my %hash = @_;
		$hash = \%hash;
	}

	$self->{_hierarchy} = $hash->{hierarchy};
	$self->{_attributes} = $hash->{attributes};

	bless ($self, $class);
}

sub TO_JSON
{
	my $self = shift;
	my $attributes = $self->{_attributes};
	my $hierarchy = $self->{_hierarchy};

	return { attributes => $attributes, hierarchy => $hierarchy };
}

sub getAttributes
{
	my $self = shift;

	return $self->{_attributes};
}

sub setAttributes
{
	my $self = shift;
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		$hash = \%_;
	}

	$self->{_attributes} = $hash;

	return;
}

sub getHierarchy
{
	my $self = shift;

	return %{$self->{_hierarchy}};
}

sub setHierarchy
{
	my $self = shift;
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		$hash = \%_;
	}

	$self->{_hierarchy} = $hash;

	return;
}

sub isMemberAssignedToResource
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $resourceId = $args{resource};
	unless($memberId) { die "MemberId is mandatory to check if member is assigned to resource!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to check if member is assigned to resource!\n"; }
	my $facilityId = $self->getFacilityId;
	if($self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{m}->{$memberId}) {
		return 1;
	} else {
		return 0;
	}
}

sub isMemberAssignedToVo
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $voId = $args{vo};
	unless($memberId) { die "MemberId is mandatory to check if member is assigned to vo!\n"; }
	unless($voId) { die "VoId is mandatory to check if member is assigned to vo!\n"; }
	my $facilityId = $self->getFacilityId;
	foreach my $resourceId ($self->getResourceIds()) {
		if($self->getVoIdForResource( resource => $resourceId ) eq $voId) {
			if($self->isMemberAssignedToResource( member => $memberId, resource => $resourceId )) {
				return 1;
			}
		}
	}
	return 0;
}

#--------------------------------------------------
#--------------GET OBJECT IDs methods--------------
#--------------------------------------------------

sub getFacilityId ()
{
	my $self = shift;
	my @facilityIds = keys %{$self->{_hierarchy}};

	if(@facilityIds == 1) {
		return $facilityIds[0];
	} else {
		die "There are more than 1 facility inside of hashed data!\n";
	}
}

sub getMemberIdsForFacility ()
{
	my $self = shift;
	my @memberIds = ();
	my $facilityId = $self->getFacilityId;
	foreach my $memberId (sort keys %{$self->{_hierarchy}->{$facilityId}->{m}}) {
		push @memberIds, $memberId;
	}
	return @memberIds;
}

sub getResourceIds ()
{
	my $self = shift;
	my @resourceIds = ();
	my $facilityId = $self->getFacilityId;
	foreach my $resourceId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}}) {
		push @resourceIds, $resourceId;
	}
	return @resourceIds;
}

sub getResourceIdsForMember ()
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get resourceIds from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	my @resourceIds = ();
	foreach my $resourceId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}}) {
		if($self->isMemberAssignedToResource( member => $memberId, resource => $resourceId )) {
			push @resourceIds, $resourceId;
		}
	}
	return @resourceIds;
}

sub getVoIds ()
{
	my $self = shift;
	my @vosIds = ();
	my $facilityId = $self->getFacilityId;
	foreach my $resourceId ($self->getResourceIds()) {
		push @vosIds, $self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{v};
	}
	return @vosIds;
}

sub getGroupIds()
{
	my $self = shift;
	my @groupIds = ();
	my $facilityId = $self->getFacilityId;
	foreach my $resourceId ($self->getResourceIds()) {
		foreach my $groupId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{c}}) {
			push @groupIds, $groupId;
		}
	}
	return @groupIds;
}


sub getVoIdForResource ()
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	unless($resourceId) { die "ResourceId is mandatory to get voId from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	return $self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{v};
}

sub getVoIdsForMember()
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get voIds from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	my @voIds = ();
	foreach my $resourceId ($self->getResourceIds()) {
		if($self->isMemberAssignedToVo( member => $memberId, vo => $resourceId )) {
			push @voIds, $self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{v};
		}
	}
	return @voIds;
}

sub getMemberIdsForResource ($)
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	unless($resourceId) { die "ResourceId is mandatory to get members from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	my @memberIds = ();
	foreach my $memberId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{m}}) {
		push @memberIds, $memberId;
	}
	return @memberIds;
}

sub getGroupIdsForVo ($)
{
	my $self = shift;
	my %args = @_;
	my $voId = $args{vo};
	unless($voId) { die "VoId is mandatory to get groups from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	my @groupIds = ();
	foreach my $resourceId ($self->getResourceIds()) {
		if($self->getVoIdForResource( resource => $resourceId ) eq $voId) {
			foreach my $groupId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{c}}) {
				push @groupIds, $groupId;
			}
		}
	}
	return @groupIds;
}

sub getGroupIdsForResource ($)
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	unless($resourceId) { die "ResourceId is mandatory to get groups from hierarchy!\n"; }
	my $facilityId = $self->getFacilityId;
	my @groupIds = ();
	foreach my $groupId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{c}}) {
		push @groupIds, $groupId;
	}
	return @groupIds;
}

sub getMemberIdsForResourceAndGroup ($$)
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	my $groupId = $args{group};
	unless($resourceId) { die "ResourceId is mandatory to get members for resource and group!\n"; }
	unless($groupId) { die "GroupId is mandatory to get members for resource and group!\n"; }
	my $facilityId = $self->getFacilityId;
	my @memberIds = ();
	foreach my $memberId (sort keys %{$self->{_hierarchy}->{$facilityId}->{c}->{$resourceId}->{c}->{$groupId}->{m}}) {
		push @memberIds, $memberId;
	}
	return @memberIds;
}

sub getUserIdForMember ($)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get user for member!\n"; }
	my $facilityId = $self->getFacilityId;
	return $self->{_hierarchy}->{$facilityId}->{m}->{$memberId};
}

#--------------------------------------------------
#----------GET ATTRIBUTES by IDs methods-----------
#--------------------------------------------------

sub getFacilityAttributeValue ($)
{
	my $self = shift;
	my %args = @_;
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for facility!\n"; }
	unless($attrName =~ m/^urn:perun:facility:attribute-def:/) { die "AttrName '$attrName' is not legit for facility attribute!\n"; }
	my $facilityId = $self->getFacilityId;
	my $facilityHash = "f-" . $facilityId;
	return $self->{_attributes}->{$facilityHash}->{$attrName};
}

sub getResourceAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for resource!\n"; }
	unless($attrName =~ m/^urn:perun:resource:attribute-def:/) { die "AttrName '$attrName' is not legit for resource attribute!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to get attribute for resource!\n"; }
	my $resourceHash = "r-" . $resourceId;
	return $self->{_attributes}->{$resourceHash}->{$attrName}
}

sub getVoAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $voId = $args{vo};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for vo!\n"; }
	unless($attrName =~ m/^urn:perun:vo:attribute-def:/) { die "AttrName '$attrName' is not legit for vo attribute!\n"; }
	unless($voId) { die "VoId is mandatory to get attribute for vo!\n"; }
	my $voHash = "v-" . $voId;
	return $self->{_attributes}->{$voHash}->{$attrName}
}

sub getGroupAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $groupId = $args{group};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for group!\n"; }
	unless($attrName =~ m/^urn:perun:group:attribute-def:/) { die "AttrName '$attrName' is not legit for group attribute!\n"; }
	unless($groupId) { die "GroupId is mandatory to get attribute for group!\n"; }
	my $groupHash = "g-" . $groupId;
	return $self->{_attributes}->{$groupHash}->{$attrName}
}

sub getUserAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for user!\n"; }
	unless($attrName =~ m/^urn:perun:user:attribute-def:/) { die "AttrName '$attrName' is not legit for user attribute!\n"; }
	unless($memberId) { die "MemberId is mandatory to get attribute for user!\n"; }
	my $userId = $self->getUserIdForMember( member => $memberId );
	unless($userId) { die "Can't find user for chosen member:$memberId in the hierarchy data!\n"; }
	my $userHash = "u-" . $userId;
	return $self->{_attributes}->{$userHash}->{$attrName}
}

sub getMemberAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for member!\n"; }
	unless($attrName =~ m/^urn:perun:member:attribute-def:/) { die "AttrName '$attrName' is not legit for member attribute!\n"; }
	unless($memberId) { die "MemberId is mandatory to get attribute for member!\n"; }
	my $memberHash = "m-" . $memberId;
	return $self->{_attributes}->{$memberHash}->{$attrName}
}

sub getUserFacilityAttributeValue ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for user and facility!\n"; }
	unless($attrName =~ m/^urn:perun:user_facility:attribute-def:/) { die "AttrName '$attrName' is not legit for user-facility attribute!\n"; }
	unless($memberId) { die "MemberId is mandatory to get attribute for user and facility!\n"; }
	my $userId = $self->getUserIdForMember( member => $memberId );
	unless($userId) { die "Can't find user for chosen member:$memberId in the hierarchy data!\n"; }
	my $facilityId = $self->getFacilityId;
	my $userFacilityHash = "u-f-" . $userId . "-" . $facilityId;
	return $self->{_attributes}->{$userFacilityHash}->{$attrName};
}

sub getMemberResourceAttributeValue ($$$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $resourceId = $args{resource};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for member and resource!\n"; }
	unless($attrName =~ m/^urn:perun:member_resource:attribute-def:/) { die "AttrName '$attrName' is not legit for member-resource attribute!\n"; }
	unless($memberId) { die "MemberId is mandatory to get attribute for member and resource!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to get attribute for member and resource!\n"; }
	my $memberResourceHash = "m-r-" . $memberId . "-" . $resourceId;
	return $self->{_attributes}->{$memberResourceHash}->{$attrName};
}

sub getMemberGroupAttributeValue ($$$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $groupId = $args{group};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for member and group!\n"; }
	unless($attrName =~ m/^urn:perun:member_group:attribute-def:/) { die "AttrName '$attrName' is not legit for member-group attribute!\n"; }
	unless($memberId) { die "MemberId is mandatory to get attribute for member and group!\n"; }
	unless($groupId) { die "GroupId is mandatory to get attribute for member and group!\n"; }
	my $memberGroupHash = "m-g-" . $memberId . "-" . $groupId;
	return $self->{_attributes}->{$memberGroupHash}->{$attrName};
}

sub getGroupResourceAttributeValue ($$$)
{
	my $self = shift;
	my %args = @_;
	my $groupId = $args{group};
	my $resourceId = $args{resource};
	my $attrName = $args{attrName};
	unless($attrName) { die "AttrName is mandatory to get attribute for group and resource!\n"; }
	unless($attrName =~ m/^urn:perun:group_resource:attribute-def:/) { die "AttrName '$attrName' is not legit for group-resource attribute!\n"; }
	unless($groupId) { die "GroupId is mandatory to get attribute for group and resource!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to get attribute for group and resource!\n"; }
	my $groupResourceHash = "g-r-" . $groupId . "-" . $resourceId;
	return $self->{_attributes}->{$groupResourceHash}->{$attrName};
}

#--------------------------------------------------
#----------GET ALL ATTRIBUTES methods-----------
#--------------------------------------------------

sub getAllFacilityAttributes ($)
{
	my $self = shift;
	my %args = @_;
	my $facilityId = $self->getFacilityId;
	my $facilityHash = "f-" . $facilityId;
	return $self->{_attributes}->{$facilityHash};
}

sub getAllResourceAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $resourceId = $args{resource};
	unless($resourceId) { die "ResourceId is mandatory to get all resource attributes!\n"; }
	my $resourceHash = "r-" . $resourceId;
	return $self->{_attributes}->{$resourceHash};
}

sub getAllVoAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $voId = $args{vo};
	unless($voId) { die "VoId is mandatory to get all vo attributes!\n"; }
	my $voHash = "v-" . $voId;
	return $self->{_attributes}->{$voHash};
}

sub getAllGroupAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $groupId = $args{group};
	unless($groupId) { die "GroupId is mandatory to get all attributes for group!\n"; }
	my $groupHash = "g-" . $groupId;
	return $self->{_attributes}->{$groupHash};
}

sub getAllUserAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get all attributes for user!\n"; }
	my $userId = $self->getUserIdForMember( member => $memberId );
	unless($userId) { die "Can't find user for chosen member:$memberId in the hierarchy data!\n"; }
	my $userHash = "u-" . $userId;
	return $self->{_attributes}->{$userHash};
}

sub getAllMemberAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get all attributes for member!\n"; }
	my $memberHash = "m-" . $memberId;
	return $self->{_attributes}->{$memberHash};
}

sub getAllUserFacilityAttributes ($$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	unless($memberId) { die "MemberId is mandatory to get all attributes for user and facility!\n"; }
	my $userId = $self->getUserIdForMember( member => $memberId );
	unless($userId) { die "Can't find user for chosen member:$memberId in the hierarchy data!\n"; }
	my $facilityId = $self->getFacilityId;
	my $userFacilityHash = "u-f-" . $userId . "-" . $facilityId;
	return $self->{_attributes}->{$userFacilityHash};
}

sub getAllMemberResourceAttributes ($$$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $resourceId = $args{resource};
	unless($memberId) { die "MemberId is mandatory to get all attributes for member and resource!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to get all attribute for member and resource!\n"; }
	my $memberResourceHash = "m-r-" . $memberId . "-" . $resourceId;
	return $self->{_attributes}->{$memberResourceHash};
}

sub getAllMemberGroupAttributes ($$$)
{
	my $self = shift;
	my %args = @_;
	my $memberId = $args{member};
	my $groupId = $args{group};
	unless($memberId) { die "MemberId is mandatory to get all attributes for member and group!\n"; }
	unless($groupId) { die "GroupId is mandatory to get all attributes for member and group!\n"; }
	my $memberGroupHash = "m-g-" . $memberId . "-" . $groupId;
	return $self->{_attributes}->{$memberGroupHash};
}

sub getAllGroupResourceAttributes ($$$)
{
	my $self = shift;
	my %args = @_;
	my $groupId = $args{group};
	my $resourceId = $args{resource};
	unless($groupId) { die "GroupId is mandatory to get all attributes for group and resource!\n"; }
	unless($resourceId) { die "ResourceId is mandatory to get all attributes for group and resource!\n"; }
	my $groupResourceHash = "g-r-" . $groupId . "-" . $resourceId;
	return $self->{_attributes}->{$groupResourceHash};
}

1;
