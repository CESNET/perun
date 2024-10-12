package Perun::beans::MailText;
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

	my $locale;
	if (defined($self->{_locale})) {
		$locale = $self->{_locale};
	} else {
		$locale = undef;
	}

	my $subject;
	if (defined($self->{_subject})) {
		$subject = $self->{_subject};
	} else {
		$subject = undef;
	}

	my $text;
	if (defined($self->{_text})) {
		$text = $self->{_text};
	} else {
		$text = undef;
	}

	my $htmlFormat;
	if (defined($self->{_htmlFormat})) {
		$htmlFormat = $self->{_htmlFormat};
	} else {
		$htmlFormat = undef;
	}

	return { locale => $locale, subject => $subject, text => $text, htmlFormat => $htmlFormat };
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

sub getText
{
	my $self = shift;

	return $self->{_text};
}

sub setText
{
	my $self = shift;
	$self->{_text} = shift;
	return;
}

sub isHtmlFormat
{
	my $self = shift;
	return ($self->{_htmlFormat}) ? 1 : 0;
}

sub isHtmlFormatToPrint
{
	my $self = shift;
	return ($self->{_htmlFormat}) ? 'true' : 'false';
}

sub setIsHtml
{
	my $self = shift;
	my $value = shift;
	if (ref $value eq "JSON::XS::Boolean")
	{
		$self->{_htmlFormat} = $value;
	} elsif ($value eq 'true' || $value eq 1)
	{
		$self->{_htmlFormat} = JSON::XS::true;
	} else
	{
		$self->{_htmlFormat} = JSON::XS::false;
	}
	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_locale}, $self->isHtmlFormatToPrint, $self->{_subject});
}

sub getCommonArrayRepresentationHeading {
	return ('Locale', 'HTML', 'Subject');
}

1;
