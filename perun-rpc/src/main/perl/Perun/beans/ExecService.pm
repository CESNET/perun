package Perun::beans::ExecService;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $defaultDelay = $self->{_defaultDelay};
	my $defaultRecurrence = $self->{_defaultRecurrence};
	my $enabled = $self->{_enabled};
	my $service = $self->{_service};
	my $script = $self->{_script};
	my $execServiceType = $self->{_execServiceType};

	my $str = 'ExecService (';
	$str .= "id: $id, ";
	$str .= "defaultDelay: $defaultDelay, ";
	$str .= "defaultRecurrence: $defaultRecurrence, ";
	$str .= "enabled: $enabled, ";
	$str .= "service: $service->toString(), ";
	$str .= "script: $script, ";
	$str .= "execServiceType: $execServiceType, ";
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

	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}

	return { id => $id, name => $name };
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

sub getDefaultDelay
{
	my $self = shift;

	return $self->{_defaultDelay};
}

sub setDefaultDelay
{
	my $self = shift;
	$self->{_defaultDelay} = shift;

	return;
}


sub getDefaultRecurrence
{
	my $self = shift;

	return $self->{_defaultRecurrence};
}

sub setDefaultRecurrence
{
	my $self = shift;
	$self->{_defaultRecurrence} = shift;
	return;
}

sub getEnabled
{
	my $self = shift;

	return $self->{_enabled};
}

sub setEnabled
{
	my $self = shift;
	$self->{_enabled} = shift;
	return;
}

sub getScript
{
	my $self = shift;

	return $self->{_script};
}

sub setScript
{
	my $self = shift;
	$self->{_script} = shift;
	return;
}

sub getExecServiceType
{
	my $self = shift;

	return $self->{_execServiceType};
}

sub setExecServiceType
{
	my $self = shift;
	$self->{_execServiceType} = shift;
	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_service}->{id}, $self->{_service}->{name}, $self->{_type}, $self->{_script},
		$self->{_enabled}, $self->{_defaultDelay}, $self->{_defaultRecurrence});
}

sub getCommonArrayRepresentationHeading {
	return ("ExecService\nID", "Service\nID", "Service\nname", 'Type', 'Script', 'Enabled', "Default\nDelay",
		"Default\nRecurrence" );
}

1;
