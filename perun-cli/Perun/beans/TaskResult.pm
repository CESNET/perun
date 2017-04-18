package Perun::beans::TaskResult;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $taskId = $self->{_taskId};
	my $destinationId = $self->{_destinationId};
	my $errorMessage = $self->{_errorMessage};
	my $standardMessage = $self->{_standardMessage};
	my $returnCode = $self->{_returnCode};
	my $timestamp = $self->{_timestamp};
	my $status = $self->{_status};
	my $destination = $self->{_destination};
	my $service = $self->{_service};

	my $str = 'TaskResult (';
	$str .= "id: $id, " if ($id);
	$str .= "taskId: $taskId, " if ($taskId);
	$str .= "destinationId: $destinationId, " if ($destinationId);
	$str .= "errorMessage: $errorMessage, " if ($errorMessage);
	$str .= "standardMessage: $standardMessage, " if ($standardMessage);
	$str .= "returnCode: $returnCode, " if ($returnCode);
	$str .= "timestamp: $timestamp, " if ($timestamp);
	$str .= "status: $status" if ($status);
	$str .= "destination: $destination" if ($destination);
	$str .= "service: $service" if ($service);
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

	my $taskId;
	if (defined($self->{_taskId})) {
		$taskId = $self->{_taskId} * 1;
	} else {
		$taskId = 0;
	}

	my $destinationId;
	if (defined($self->{_destinationId})) {
		$destinationId = $self->{_destinationId} * 1;
	} else {
		$destinationId = 0;
	}

	my $errorMessage;
	if (defined($self->{_errorMessage})) {
		$errorMessage = "$self->{_errorMessage}";
	} else {
		$errorMessage = undef;
	}

	my $standardMessage;
	if (defined($self->{_standardMessage})) {
		$standardMessage = "$self->{_standardMessage}";
	} else {
		$standardMessage = undef;
	}

	my $returnCode;
	if (defined($self->{_returnCode})) {
		$returnCode = $self->{_returnCode} * 1;
	} else {
		$returnCode = 0;
	}

	my $service;
	if (defined($self->{_service})) {
		$service = $self->{_service};
	} else {
		$service = undef;
	}

	my $timestamp = $self->{_timestamp};
	my $status = $self->{_status};
	my $destination = $self->{_destination};

	return { id         => $id, taskId => $taskId, destinationId => $destinationId, errorMessage => $errorMessage,
		standardMessage => $standardMessage, returnCode => $returnCode, timestamp => $timestamp, status => $status,
		destination     => $destination, service => $service };
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_taskId}, $self->{_destinationId}, $self->{_errorMessage},
		$self->{_standardMessage}, $self->{_returnCode}, $self->{_timestamp}, $self->{_status},
		$self->{_destination}->{destination}, $self->{_service}->{name});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'TaskId', 'DestinationId', 'stderr', 'stdout', 'ReturnCode', 'Timestamp', 'Status', 'Destination',
		'Service');
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

sub getTaskId
{
	my $self = shift;

	return $self->{_taskId};
}

sub setTaskId
{
	my $self = shift;
	$self->{_taskId} = shift;

	return;
}

sub getDestinationId
{
	my $self = shift;

	return $self->{_destinationId};
}

sub setDestinationId
{
	my $self = shift;
	$self->{_destinationId} = shift;

	return;
}

sub getErrorMessage
{
	my $self = shift;

	return $self->{_errorMessage};
}

sub setErrorMessage
{
	my $self = shift;
	$self->{_errorMessage} = shift;

	return;
}

sub getStandardMessage
{
	my $self = shift;

	return $self->{_standardMessage};
}

sub setStandardMessage
{
	my $self = shift;
	$self->{_standardMessage} = shift;

	return;
}

sub getReturnCode
{
	my $self = shift;

	return $self->{_returnCode};
}

sub setReturnCode
{
	my $self = shift;
	$self->{_returnCode} = shift;

	return;
}

sub getTimestamp
{
	my $self = shift;

	return $self->{_timestamp};
}

sub setTimestamp
{
	my $self = shift;
	$self->{_timestamp} = shift;

	return;
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

sub getDestination
{
	my $self = shift;

	return $self->{_destination};
}

sub setDestination
{
	my $self = shift;
	$self->{_destination} = shift;

	return;
}

sub getDestinationName
{
	my $destination = shift->{_destination};

	return $destination->{destination};
}

sub setService
{
	my $self = shift;
	$self->{_service} = shift;

	return;
}

sub getServiceName
{
	my $service = shift->{_service};

	return $service->{service};
}

sub getServiceId
{
	my $service = shift->{_service};

	return $service->{id};
}
1;
