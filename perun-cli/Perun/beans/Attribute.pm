package Perun::beans::Attribute;

use strict;
use warnings;
use Switch;
use Data::Dumper;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->getId;
	my $value = $self->getValue;
	my $friendlyName = $self->getFriendlyName;
	my $displayName = $self->getDisplayName;
	my $namespace = $self->getNamespace;
	my $description = $self->getDescription;
	my $type = $self->getType;
	my $unique = $self->isUnique;

	my $str = 'Attribute (';
	$str .= "id: $id, " if (defined($id));
	$str .= "value: $value, " if (defined($value));
	$str .= "friendlyName: $friendlyName, " if (defined($friendlyName));
	$str .= "displayName: $displayName, " if (defined($displayName));
	$str .= "namespace: $namespace, " if (defined($namespace));
	$str .= "description: $description, " if (defined($description));
	$str .= "type: $type, " if (defined($type));
	$str .= "unique: $unique, " if (defined($unique));
	$str .= ")";

	return $str;
}

sub new
{
	bless({});
}

sub fromHash
{
	return Perun::Common::fromHash(@_);
}

sub fromAttributeDefinition
{
	my $class = shift;
	$class = ref $class if ref $class;
	my $self = { };
	my $definition = $_[0];

	$self->{'_id'} = $definition->{'_id'};
	$self->{'_friendlyName'} = $definition->{'_friendlyName'};
	$self->{'_displayName'} = $definition->{'_displayName'};
	$self->{'_namespace'} = $definition->{'_namespace'};
	$self->{'_description'} = $definition->{'_description'};
	$self->{'_type'} = $definition->{'_type'};
	$self->{'_unique'} = $definition->{'_unique'};
	$self->{'_value'} = undef;

	bless ($self, $class);
}

sub TO_JSON
{
	my $self = shift;

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id} * 1;
	} else {
		$id = 0;
	}

	my $value = $self->{_value};

	my $friendlyName;
	if (defined($self->{_friendlyName})) {
		$friendlyName = "$self->{_friendlyName}";
	} else {
		$friendlyName = undef;
	}

	my $displayName;
	if (defined($self->{_displayName})) {
		$displayName = "$self->{_displayName}";
	} else {
		$displayName = undef;
	}

	my $namespace;
	if (defined($self->{_namespace})) {
		$namespace = "$self->{_namespace}";
	} else {
		$namespace = undef;
	}

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	my $type;
	if (defined($self->{_type})) {
		$type = "$self->{_type}";
	} else {
		$type = undef;
	}

	my $unique;
	if (defined($self->{_unique})) {
		$unique = $self->{_unique};
	} else {
		$unique = undef;
	}

	return { id   => $id, value => $value, friendlyName => $friendlyName, displayName => $displayName,
		namespace => $namespace, description => $description, type => $type, unique => $unique };
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub setId
{
	my $self = shift;
	$self->{_id} = shift;

	return;
}

sub getName
{
	my $self = shift;

	return ($self->{_namespace}.':'.$self->{_friendlyName});
}

sub getFriendlyName
{
	my $self = shift;

	return $self->{_friendlyName};
}

sub setFriendlyName
{
	my $self = shift;
	$self->{_friendlyName} = shift;

	return;
}

sub getDisplayName
{
	my $self = shift;

	return $self->{_displayName};
}

sub setDisplayName
{
	my $self = shift;
	$self->{_displayName} = shift;

	return;
}

sub getNamespace
{
	my $self = shift;

	return $self->{_namespace};
}

sub setNamespace
{
	my $self = shift;
	$self->{_namespace} = shift;

	return;
}

sub getDescription
{
	my $self = shift;

	return $self->{_description};
}

sub setDescription
{
	my $self = shift;
	$self->{_description} = shift;

	return;
}

sub getValue
{
	my $self = shift;

	return $self->{_value};
}

sub setValue
{
	my $self = shift;
	$self->{_value} = shift;

	return;
}

sub getType
{
	my $self = shift;
	my $type = $self->{_type};

	if ($type eq 'java.lang.Integer') {
		$type = 'integer';
	} elsif ($type eq 'java.lang.String') {
		$type = 'string';
	} elsif ($type eq 'java.lang.LargeString') {
		$type = 'largestring';
	} elsif ($type eq 'java.util.ArrayList') {
		$type = 'array';
	}  elsif ($type eq 'java.util.LargeArrayList') {
		$type = 'largearray';
	} elsif ($type eq 'java.util.LinkedHashMap') {
		$type = 'hash';
	} elsif ($type eq 'java.lang.Boolean') {
		$type = 'boolean';
	}

	return $type;
}

sub setType
{
	my $self = shift;
	my $type = shift;

	if ($type eq 'integer') {
		$type = 'java.lang.Integer';
	} elsif ($type eq 'string') {
		$type = 'java.lang.String';
	} elsif ($type eq 'largestring') {
		$type = 'java.lang.LargeString';
	} elsif ($type eq 'array') {
		$type = 'java.util.ArrayList';
	} elsif ($type eq 'largearray') {
		$type = 'java.util.LargeArrayList';
	} elsif ($type eq 'hash') {
		$type = 'java.util.LinkedHashMap';
	} elsif ($type eq 'boolean') {
		$type = 'java.lang.Boolean';
	}

	$self->{_type} = $type;
	return;
}

sub getValueAsScalar {
	my $self = shift;
	my $value = $self->getValue;

	switch(ref $value) {
		case ""       { return $value }
		case "SCALAR" { return $value }
		case "ARRAY"  { return '["'.join('", "', @$value).'"]' }
		case "HASH"   {
			local $Data::Dumper::Terse = 1;
			local $Data::Dumper::Indent = 0;
			local $Data::Dumper::Useqq = 1;

			{
				no warnings 'redefine';
				sub Data::Dumper::qquote {
					my $s = shift;
					return "'$s'";
				}
			}

			return Dumper($value);
		}
		case "JSON::XS::Boolean" {
			return ($value) ? 'true' : 'false';
		}
		else {
			return 'UNKNOWN VALUE TYPE'
		}
	}
}

sub setValueFromArray {
	my $attribute = shift; #self

	switch ($attribute->getType) {
		case /^string$|^largestring$/ {
			if (scalar @_ > 1) { Perun::Common::printMessage(
				"More than one value passed as attribute value. Taking first one and ignoring the rest.", $::batch); }
			$attribute->setValue( $_[0] );
		}
		case "integer" {
			if (scalar @_ > 1) { Perun::Common::printMessage(
				"More than one value passed as attribute value. Taking first one and ignoring the rest.", $::batch); }
			my $attributeIntegerValue = $_[0] * 1;
			$attribute->setValue( $attributeIntegerValue );
		}
		case "boolean" {
			if (scalar @_ > 1) { Perun::Common::printMessage(
				"More than one value passed as attribute value. Taking first one and ignoring the rest.", $::batch); }
			if (("$_[0]" eq '1') or ("$_[0]" eq 'true')) {
				my $true = JSON::XS::true;
				$attribute->setValue( $true );
			} elsif (("$_[0]" eq '0') or ("$_[0]" eq 'false')) {
				my $false = JSON::XS::false;
				$attribute->setValue( $false );
			} else {
				Perun::Common::printMessage(
					"Value is not of boolean type, please use numbers 1/0 or strings true/false as input.", $::batch);
			}
		}
		case /^array$|^largearray$/ {
			$attribute->setValue( \@_ );
		}
		case "hash" {
			my %hash = @_;
			$attribute->setValue( \%hash );
		}
		else {
			die "Unknown attribute type. Type=".$attribute->getType;
		}
	}
}

sub isUniqueToPrint
{
	my $self = shift;

	return ($self->{_unique}) ? 'true' : 'false';
}

sub isUnique
{
	my $self = shift;

	return ($self->{_unique}) ? 1 : 0;
}

sub setUnique
{
	my $self = shift;
	my $val = shift;
	if (ref $val eq "JSON::XS::Boolean")
	{
		$self->{_unique} = $val;
	} elsif ($val eq 'true' || $val eq 1)
	{
		$self->{_unique} = JSON::XS::true;
	} else
	{
		$self->{_unique} = JSON::XS::false;
	}
	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->getName, $self->getType, $self->isUniqueToPrint, $self->getValueAsScalar);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name', 'Type', 'isUnique', 'Value');
}

1;
