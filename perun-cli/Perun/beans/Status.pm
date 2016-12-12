package Perun::beans::Status;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $status = $self->{_status};

	my $str = 'Status (';
	$str .= "status: $status, " if ($status);
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

	my $status;
	if (defined($self->{_status})) {
		$status = "$self->{_status}";
	} else {
		$status = undef;
	}

	return { status => $status };
}

sub getStatus
{
	my $self = shift;

	return $self->{_status};
}

sub setStatus
{
	my $self = shift;
	$self->{_status} = shift;

	return;
}
1;
