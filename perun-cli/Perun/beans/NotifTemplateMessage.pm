package Perun::beans::NotifTemplateMessage;

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

	my $message;
	if (defined($self->{_message})) {
		$message = "$self->{_message}";
	} else {
		$message = undef;
	}

	my $subject;
	if (defined($self->{_subject})) {
		$subject = "$self->{_subject}";
	} else {
		$subject = undef;
	}

	return { id => $id, templateId => $templateId, locale => $locale, message => $message, subject => $subject };
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

sub getMessage
{
	my $self = shift;

	return $self->{_message};
}

sub setMessage
{
	my $self = shift;
	$self->{_message} = shift;

	return;
}

sub getSubject
{
	my $self = shift;

	return $self->{_subject};
}

sub setSubject
{
	my $self = shift;
	$self->{_subject} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $object = shift;
	return ($object->getId, $object->getTemplateId, $object->getLocale, $object->getMessage, $object->getSubject);
}

sub getCommonArrayRepresentationHeading {
	return ('Id', 'TemplateId', 'Locale', 'Message', 'Subject');
}

1;
