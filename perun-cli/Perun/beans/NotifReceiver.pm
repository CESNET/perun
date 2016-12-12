package Perun::beans::NotifReceiver;

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
		$id = undef;
	}

	my $type;
	if (defined($self->{_typeOfReceiver})) {
		$type = "$self->{_typeOfReceiver}";
	} else {
		$type = undef;
	}

	my $target;
	if (defined($self->{_target})) {
		$target = "$self->{_target}";
	} else {
		$target = undef;
	}

	my $templateId;
	if (defined($self->{_templateId})) {
		$templateId = $self->{_templateId} * 1;
	} else {
		$templateId = undef;
	}

	my $locale;
	if (defined($self->{_locale})) {
		$locale = "$self->{_locale}";
	} else {
		$locale = undef;
	}

	return { id => $id, typeOfReceiver => $type, target => $target, templateId => $templateId, locale => $locale };
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

sub getType
{
	my $self = shift;

	return $self->{_typeOfReceiver};
}

sub setType
{
	my $self = shift;
	$self->{_typeOfReceiver} = shift;

	return;
}

sub getTarget
{
	my $self = shift;

	return $self->{_target};
}

sub setTarget
{
	my $self = shift;
	$self->{_target} = shift;

	return;
}

sub getTemplateId
{
	my $self = shift;

	return $self->{_templateId};
}

sub setTemplateId
{
	my $self = shift;
	$self->{_templateId} = shift;

	return;
}

sub getLocale
{
	my $self = shift;
	return $self->{_locale};
}

sub setLocale
{
	my $self = shift;
	$self->{_locale} = shift;
	return;
}

sub getCommonArrayRepresentation {
	my $receiver = shift;
	return ($receiver->getId, $receiver->getType, $receiver->getTarget, $receiver->getTemplateId, $receiver->getLocale);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'Type', 'Target', 'TemplateId', 'locale');
}

1;
