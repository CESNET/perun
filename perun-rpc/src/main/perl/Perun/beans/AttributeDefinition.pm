package Perun::beans::AttributeDefinition;

use strict;
use warnings;

use Perun::Common;

use overload
'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->getId;
	my $friendlyName = $self->getFriendlyName;
	my $displayName = $self->getDisplayName;
	my $namespace = $self->getNamespace;
	my $description = $self->getDescription;
	my $type = $self->getType;

	my $str = 'Attribute (';
	$str .= "id: $id, " if (defined($id));
	$str .= "friendlyName: $friendlyName, " if (defined($friendlyName));
	$str .= "displayName: $displayName, " if (defined($displayName));
	$str .= "namespace: $namespace, " if (defined($namespace));
	$str .= "description: $description, " if (defined($description));
	$str .= "type: $type, " if (defined($type));
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

sub TO_JSON
{
	my $self = shift;

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id}*1;
	} else {
		$id = 0;
	}

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

	return {id => $id, friendlyName => $friendlyName, displayName => $displayName,
		namespace => $namespace, description => $description, type => $type};
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

	return ($self->{_namespace} . ':' . $self->{_friendlyName});
}

sub getFriendlyName
{
	my $self = shift;

	return $self->{_friendlyName};
}

sub setDisplayName
{
	my $self = shift;
	$self->{_displayName} = shift;

	return;
}

sub getDisplayName
{
	my $self = shift;

	return $self->{_displayName};
}

sub setFriendlyName
{
	my $self = shift;
	$self->{_friendlyName} = shift;

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

sub getType
{
	my $self = shift;
	my $type = $self->{_type};

	if ($type eq 'java.lang.Integer') {
		$type = 'integer';
	} elsif ($type eq 'java.lang.String') {
		$type = 'string';
	} elsif ($type eq 'java.util.ArrayList') {
		$type = 'array';
	} elsif ($type eq 'java.util.LinkedHashMap') {
		$type = 'hash';
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
	} elsif ($type eq 'array') {
		$type = 'java.util.ArrayList';
	} elsif ($type eq 'hash') {
		$type = 'java.util.LinkedHashMap';
	}

	$self->{_type} = $type;
	return;
}

1;
