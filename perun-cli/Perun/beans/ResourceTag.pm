package Perun::beans::ResourceTag;

use strict;
use warnings;

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

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id} * 1;
	} else {
		$id = 0;
	}

	my $tagName;
	if (defined($self->{_tagName})) {
		$tagName = "$self->{_tagName}";
	} else {
		$tagName = undef;
	}

	my $voId;
	if (defined($self->{_voId})) {
		$voId = "$self->{_voId}";
	} else {
		$voId = undef;
	}

	return { id => $id, tagName => $tagName, voId => $voId, beanName => "ResourceTag" };
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

sub getTagName
{
	my $self = shift;

	return $self->{_tagName};
}

sub setTagName
{
	my $self = shift;
	$self->{_tagName} = shift;

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

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_tagName}, $self->{_voId});
}

sub getCommonArrayRepresentationHeading {
	return ('Tag id', 'Tag name', 'VO Id');
}


1;
