package Perun::beans::Owner;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $name = $self->{_name};
	my $contact = $self->{_contact};
	my $type = $self->{_type};

	my $str = 'Owner (';
	$str .= "id: $id, " if ($id);
	$str .= "name: $name, " if ($name);
	$str .= "contact: $contact" if ($contact);
	$str .= "type: $type" if ($type);
	$str .= ')';

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
		$id = $self->{_id} * 1;
	} else {
		$id = 0;
	}

	return { id => $id, name => $self->{_name}, contact => $self->{_contact}, type => $self->{_type} };
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

	return $self->{_name};
}

sub setName
{
	my $self = shift;
	$self->{_name} = shift;

	return;
}

sub getContact
{
	my $self = shift;

	return $self->{_contact};
}

sub setContact
{
	my $self = shift;
	$self->{_contact} = shift;

	return;
}

sub getType
{
	my $self = shift;

	return $self->{_type};
}

sub setType
{
	my $self = shift;
	$self->{_type} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $owner = shift;
	return ($owner->getId, $owner->getName, $owner->getType, $owner->getContact);
}

sub getCommonArrayRepresentationHeading {
	return qw(Id Name Type Contact);
}




1;
