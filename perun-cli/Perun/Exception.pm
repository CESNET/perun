package Perun::Exception;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $type = $self->{_type};
	my $name = $self->{_name};
	my $errorId = $self->{_errorId};
	my $errorInfo = $self->{_errorInfo};

	my $str = 'ERROR: ';
	$str .= "$type " if ($type);
	$str .= "$name " if ($name);

	$str .= "(ErrorId: $errorId)" if ($errorId);

	$str .= ": $errorInfo" if ($errorInfo);

	$str .= "\n";
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

sub getErrorId
{
	my $self = shift;

	return $self->{_errorId};
}

sub setErrorId
{
	my $self = shift;
	$self->{_errorId} = shift;

	return;
}

sub getErrorInfo
{
	my $self = shift;

	return $self->{_errorInfo};
}

sub setErrorInfo
{
	my $self = shift;
	$self->{_errorInfo} = shift;

	return;
}

1;
