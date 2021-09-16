package Perun::beans::EnrichedGroup;

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

	return { group => $self->{_group}, attributes => $self->{_attributes} };
}

sub getGroupId {
	return shift->{_group}->{id};
}

sub getAttributes {
	my $self = shift;
	return @{$self->{_attributes}};
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_group}->{id}, $self->{_group}->{voId}, $self->{_group}->{name},
		$self->{_group}->{description}, $self->{_group}->{parentGroupId});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'VO ID', 'Name', 'Description', 'Parent Group ID');
}

1;
