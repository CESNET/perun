#!/usr/bin/perl
package perunServicesUtils;
use Exporter;
@ISA = ('Exporter');
@EXPORT = qw(attributesToHash dataToAttributesHashes getAttributeSorting quotaToKb uniqList convertNonAsciiToEscapedUtf8Form);

use strict;
use warnings;
use Getopt::Long qw(:config no_ignore_case);
use feature qw(switch);

#use Perun::Agent;

# Get attributes from getData function and convert them into hash
#
# Usage:
# my $data = perunServicesInit::getHierarchicalData;
# my %facilityAttributes = attributesToHash $data->getAttributes
# print $facilityAttributes{"urn:perun:facility:attribute-def:core:name"}, "\n";
#
sub attributesToHash {
	my %attributesHash;
	foreach my $attr (@_) {
		$attributesHash{$attr->getName}=$attr->getValue;
	}
	return %attributesHash;
}

# Get "data" from getData function and convert them into array of attributes. Attributes is represented by REFERENCE to hash. 
#
# Usage:
# my $data = perunServicesInit::getHierarchicalData;
# foreach my $resourceAttributes (dataToAttributesHashes $data->getChildElements) {
#   print $resourceAttributes->{"urn:perun:resource:attribute-def:core:name"}, "\n";
# }
#
sub dataToAttributesHashes {
	my @arrayOfHashes = ();
	foreach my $entity (@_) {
		push @arrayOfHashes, { attributesToHash $entity->getAttributes } ;
	}
	return @arrayOfHashes;
}

# Return sorting function which can be used as parameter for sort
# This function can sort hashREF.

# First param is key (i.e. AttributeName) and hashes will be sorted by values which responds this key

# Second argument afect the sorting behavior.
#     If it's ommited or it's 0 the function compare values as numbers.
#     If it's equals 1 function compare valeus as strings - case-insesitive.
#
# Usage:
sub getAttributeSorting {
	my $param = shift;
	if(shift) { 
		return sub ($$) {uc($_[0]->{$param}) cmp uc($_[1]->{$param}) }
	} else {
		return sub ($$) {$_[0]->{$param} <=> $_[1]->{$param} }
	}
}

our $NS_F_D;      *NS_F_D =  \"urn:perun:facility:attribute-def:def:";
our $NS_F_O;      *NS_F_O =  \"urn:perun:facility:attribute-def:opt:";
our $NS_F_C;      *NS_F_C =  \"urn:perun:facility:attribute-def:core:";
our $NS_F_V;      *NS_F_V =  \"urn:perun:facility:attribute-def:virt:";

our $NS_R_D;      *NS_R_D =  \"urn:perun:resource:attribute-def:def:";
our $NS_R_O;      *NS_R_O =  \"urn:perun:resource:attribute-def:opt:";
our $NS_R_C;      *NS_R_C =  \"urn:perun:resource:attribute-def:core:";
our $NS_R_V;      *NS_R_V =  \"urn:perun:resource:attribute-def:virt:";

our $NS_MR_D;     *NS_MR_D = \"urn:perun:member_resource:attribute-def:def:";
our $NS_MR_O;     *NS_MR_O = \"urn:perun:member_resource:attribute-def:opt:";
our $NS_MR_V;     *NS_MR_V = \"urn:perun:member_resource:attribute-def:virt:";

our $NS_M_C;      *NS_M_C =  \"urn:perun:member:attribute-def:core:";
our $NS_M_D;      *NS_M_D =  \"urn:perun:member:attribute-def:def:";
our $NS_M_O;      *NS_M_O =  \"urn:perun:member:attribute-def:opt:";
our $NS_M_V;      *NS_M_V =  \"urn:perun:member:attribute-def:virt:";

our $NS_UF_D;     *NS_UF_D = \"urn:perun:user_facility:attribute-def:def:";
our $NS_UF_O;     *NS_UF_O = \"urn:perun:user_facility:attribute-def:opt:";
our $NS_UF_V;     *NS_UF_V = \"urn:perun:user_facility:attribute-def:virt:";

our $NS_U_C;      *NS_U_C =  \"urn:perun:user:attribute-def:core:";
our $NS_U_D;      *NS_U_D =  \"urn:perun:user:attribute-def:def:";
our $NS_U_O;      *NS_U_O =  \"urn:perun:user:attribute-def:opt:";
our $NS_U_V;      *NS_U_V =  \"urn:perun:user:attribute-def:virt:";

our $NS_V_D;      *NS_V_D =  \"urn:perun:vo:attribute-def:def:";
our $NS_V_O;      *NS_V_O =  \"urn:perun:vo:attribute-def:opt:";
our $NS_V_C;      *NS_V_C =  \"urn:perun:vo:attribute-def:core:";
our $NS_V_V;      *NS_V_V =  \"urn:perun:vo:attribute-def:virt:";

our $NS_G_D;      *NS_G_D =  \"urn:perun:group:attribute-def:def:";
our $NS_G_O;      *NS_G_O =  \"urn:perun:group:attribute-def:opt:";
our $NS_G_C;      *NS_G_C =  \"urn:perun:group:attribute-def:core:";
our $NS_G_V;      *NS_G_V =  \"urn:perun:group:attribute-def:virt:";

our $NS_H_C;      *NS_H_C =  \"urn:perun:host:attribute-def:core:";
our $NS_H_D;      *NS_H_D =  \"urn:perun:host:attribute-def:def:";
our $NS_H_O;      *NS_H_O =  \"urn:perun:host:attribute-def:opt:";
our $NS_H_V;      *NS_H_V =  \"urn:perun:host:attribute-def:virt:";

our $NS_GR_D;     *NS_GR_D = \"urn:perun:group_resource:attribute-def:def:";
our $NS_GR_O;     *NS_GR_O = \"urn:perun:group_resource:attribute-def:opt:";
our $NS_GR_V;     *NS_GR_V = \"urn:perun:group_resource:attribute-def:virt:";

our $NS_E_D;      *NS_E_D =  \"urn:perun:entityless:attribute-def:def:";
our $NS_E_O;      *NS_E_O =  \"urn:perun:entityless:attribute-def:opt:";

# Converts quota written in format [number][k|K|KB||m|M|MB|g|G|GB|t|T|TB|p|P|PB||e|E|EB] into the number of kilobytes
sub quotaToKb($) {
	if(!defined $_[0]) { return 0; }

	$_[0] =~ /^([0-9]+((,|\.)[0-9]+)?)\s*([A-z]*)\s*$/;
	my $quota = $1;
	my $units = $4;
	$quota =~ s/,/./;

	unless(defined $quota) { die "quota not specified"; }
	$units = lc(substr($units, 0, 1));
	given($units) {
		when("")  { $quota *= 1024**2 } #giga is default
		when("k") { }
		when("m") { $quota *= 1024 }
		when("g") { $quota *= 1024**2 }
		when("t") { $quota *= 1024**3 }
		when("p") { $quota *= 1024**4 }
		when("e") { $quota *= 1024**5 }
		default { die "Unknown units in quota. Units= $units"; }
	}
	return $quota;
}

# input: list of items 
# output: array with uniq items
sub uniqList(@) {
		return keys %{{ map { $_ => 1 } @_ }};
}

#input: string 
#output: sting where non-ascii characters are substituted for utf8 escape characters (for example 'á' = '\xC3\xA1')
sub convertNonAsciiToEscapedUtf8Form($) {
	local($_) = shift;
	my $bytes; { use bytes; $bytes = length }
	if($bytes > length) {
		s/([\x{80}-\x{7FF}])/                                                                                                    '\x' . sprintf("%02X",(ord($1)>>6)  | 0xC0) . '\x' . sprintf("%02X",ord($1) & 0x3F | 0x80)/ge;
		s/([\x{800}-\x{FFFF}])/                                                    '\x' . sprintf("%02X",(ord($1)>>12) | 0xE0) . '\x' . sprintf("%02X",(ord($1)>>6)  | 0xC0) . '\x' . sprintf("%02X",ord($1) & 0x3F | 0x80)/ge;
		s/([\x{10000}-\x{10FFFF}])/  '\x' . sprintf("%02X",(ord($1)>>18) | 0xF0) . '\x' . sprintf("%02X",(ord($1)>>12) | 0xE0) . '\x' . sprintf("%02X",(ord($1)>>6)  | 0xC0) . '\x' . sprintf("%02X",ord($1) & 0x3F | 0x80)/ge;
	}
	return $_;
}

return 1;
