package Perun::beans::EnrichedResource;

use strict;
use warnings;
use 5.010;

use Perun::Common;

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

	return { resource => $self->{_resource}, attributes => $self->{_attributes} };
}

sub getResourceId {
	return shift->{_resource}->{id};
}

sub getAttributes {
	my $self = shift;
	return @{$self->{_attributes}};
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_resource}->{id}, $self->{_resource}->{name}, $self->{_resource}->{voId},
		$self->{_resource}->{facilityId}, $self->{_resource}->{description});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name', 'VO ID', 'Facility ID', 'Description');
}

1;
