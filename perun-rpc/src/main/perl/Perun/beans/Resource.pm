package Perun::beans::Resource;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $voId = $self->{_voId};
	my $name = $self->{_name};
	my $description = $self->{_description};
	my $facilityId = $self->{_facilityId};

	my $str = 'Resource (';
	$str .= "id: $id, " if ($id);
	$str .= "voId: $voId, " if ($voId);
	$str .= "name: $name, " if ($name);
	$str .= "description: $description," if ($description);
	$str .= "facilityId: $facilityId" if ($facilityId);
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

	my $voId;
	if (defined($self->{_voId})) {
		$voId = $self->{_voId} * 1;
	} else {
		$voId = 0;
	}

	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	my $facilityId;
	if (defined($self->{_facilityId})) {
		$facilityId = "$self->{_facilityId}";
	} else {
		$facilityId = 0;
	}

	return { id => $id, voId => $voId, name => $name, description => $description, facilityId => $facilityId, beanName
				=> "Resource" };
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

sub getVoId
{
	my $self = shift;

	return $self->{_voId};
}

sub setVoId
{
	my $self = shift;
	$self->{_voId} = shift;

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

sub getFacilityId
{
	my $self = shift;

	return $self->{_facilityId};
}

sub setFacilityId
{
	my $self = shift;
	$self->{_facilityId} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_voId}, $self->{_name}, $self->{_facilityId}, $self->{_description});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name', 'VO ID', 'Facility ID', 'Description');
}


1;
