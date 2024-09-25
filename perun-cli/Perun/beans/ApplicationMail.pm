package Perun::beans::ApplicationMail;
use strict;
use warnings;
use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $applicationMail = Perun::Common::fromHash(@_);
	foreach my $locale (keys %{$applicationMail->{_message}}) {
		my $msg = Perun::beans::MailText::fromHash("Perun::beans::MailText", $applicationMail->{_message}->{$locale});
		$applicationMail->{_message}->{$locale} = $msg;
	}
	foreach my $locale (keys %{$applicationMail->{_htmlMessage}}) {
		my $msg = Perun::beans::MailText::fromHash("Perun::beans::MailText", $applicationMail->{_htmlMessage}->{$locale});
		$applicationMail->{_htmlMessage}->{$locale} = $msg;
	}
	return $applicationMail;
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

	my $appType;
	if (defined($self->{_appType})) {
		$appType = $self->{_appType};
	} else {
		$appType = undef;
	}

	my $formId;
	if (defined($self->{_formId})) {
		$formId = $self->{_formId} * 1;
	} else {
		$formId = 0;
	}

	my $mailType;
	if (defined($self->{_mailType})) {
		$mailType = $self->{_mailType};
	} else {
		$mailType = undef;
	}

	my $send;
	if (defined($self->{_send})) {
		$send = $self->{_send};
	} else {
		$send = undef;
	}

	my $message;
	if (defined($self->{_message})) {
		$message = $self->{_message};
	} else {
		$message = undef;
	}

	my $htmlMessage;
	if (defined($self->{_htmlMessage})) {
		$htmlMessage = $self->{_htmlMessage};
	} else {
		$htmlMessage = undef;
	}

	return { id => $id, appType => $appType, formId => $formId, mailType => $mailType, send => $send, message => $message, htmlMessage => $htmlMessage };
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub getApplicationType
{
	my $self = shift;

	return $self->{_appType};
}

sub getFormId
{
	my $self = shift;

	return $self->{_formId};
}

sub getMailType
{
	my $self = shift;

	return $self->{_mailType};
}

sub isSend
{
	my $self = shift;
	return ($self->{_send}) ? 1 : 0;
}

sub isSendToPrint
{
	my $self = shift;
	return ($self->{_send}) ? 'true' : 'false';
}

sub setSend
{
	my $self = shift;
	my $value = shift;
	if (ref $value eq "JSON::XS::Boolean")
	{
		$self->{_send} = $value;
	} elsif ($value eq 'true' || $value eq 1)
	{
		$self->{_send} = JSON::XS::true;
	} else
	{
		$self->{_send} = JSON::XS::false;
	}

}

sub getMessage
{
	my $self = shift;
	my $locale = shift;
	return $self->{_message}->{$locale};
}

sub setMessage
{
	my $self = shift;
	my $locale = shift;
	$self->{_message}->{$locale} = shift;

	return;
}

sub getHtmlMessage
{
	my $self = shift;
	my $locale = shift;
	return $self->{_htmlMessage}->{$locale};
}

sub setHtmlMessage
{
	my $self = shift;
	my $locale = shift;
	$self->{_message}->{$locale} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_mailType}, $self->{_appType}, $self->isSendToPrint);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Mail Type', 'App Type', 'Sending enabled');
}

1;
