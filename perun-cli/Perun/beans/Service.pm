package Perun::beans::Service;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $name = $self->{_name};
	my $description = $self->{_description};
	my $delay = $self->{_delay};
	my $recurrence = $self->{_recurrence};
	my $enabled = $self->{_enabled};
	my $useExpiredMembers = $self->{_useExpiredMembers};
	my $useExpiredVoMembers = $self->{_useExpiredVoMembers};
	my $useBannedMembers = $self->{_useBannedMembers};
	my $script = $self->{_script};

	my $str = 'Service (';
	$str .= "id: $id, " if ($id);
	$str .= "name: $name, " if ($name);
	$str .= "description: $description, " if ($description);
	$str .= "delay: $delay, ";
	$str .= "recurrence: $recurrence, ";
	$str .= "enabled: $enabled, ";
	$str .= "useExpiredMembers: $useExpiredMembers, ";
	$str .= "useExpiredVoMembers: $useExpiredVoMembers, ";
	$str .= "useBannedMembers: $useBannedMembers, ";
	$str .= "script: $script, ";
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

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	my $script;
	if (defined($self->{_script})) {
		$script = "$self->{_script}";
	} else {
		$script = undef;
	}

	my $enabled;
	if (defined($self->{_enabled})) {
		$enabled = "$self->{_enabled}";
	} else {
		$enabled = "true";
	}

	my $useExpiredMembers;
	if (defined($self->{_useExpiredMembers})) {
		$useExpiredMembers = "$self->{_useExpiredMembers}";
	} else {
		$useExpiredMembers = "true";
	}

	my $useExpiredVoMembers;
	if (defined($self->{_useExpiredVoMembers})) {
		$useExpiredVoMembers = "$self->{_useExpiredVoMembers}";
	} else {
		$useExpiredVoMembers = "false";
	}

	my $useBannedMembers;
	if (defined($self->{_useBannedMembers})) {
		$useBannedMembers = "$self->{_useBannedMembers}";
	} else {
		$useBannedMembers = "true";
	}

	return { id => $id, name => $name, beanName => "Service", description => $description, script => $script, enabled => $enabled, useExpiredMembers => $useExpiredMembers, useExpiredVoMembers => $useExpiredVoMembers, useBannedMembers => $useBannedMembers };
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

sub getDescription
{
	my $self = shift;

	return $self->{_description};
}

sub setDescription
{
	my $self = shift;
	$self->{_description} = shift;

	return;
}

sub getDelay
{
	my $self = shift;

	return $self->{_delay};
}

sub setDelay
{
	my $self = shift;
	$self->{_delay} = shift;

	return;
}

sub getRecurrence
{
	my $self = shift;

	return $self->{_recurrence};
}

sub setRecurrence
{
	my $self = shift;
	$self->{_recurrence} = shift;

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

sub getUseExpiredMembers
{
	my $self = shift;

	return $self->{_useExpiredMembers};
}
sub setUseExpiredMembers
{
	my $self = shift;
	$self->{_useExpiredMembers} = shift;
	return;
}

sub getUseExpiredVoMembers
{
	my $self = shift;

	return $self->{_useExpiredVoMembers};
}
sub setUseExpiredVoMembers
{
	my $self = shift;
	$self->{_useExpiredVoMembers} = shift;
	return;
}

sub getUseBannedMembers
{
	my $self = shift;

	return $self->{_useBannedMembers};
}
sub setUseBannedMembers
{
	my $self = shift;
	$self->{_useBannedMembers} = shift;
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

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_name}, $self->{_delay}, $self->{_recurrence}, $self->{_enabled}, $self->{_script}, $self->{_description}, $self->{_useExpiredMembers}, $self->{_useExpiredVoMembers}, $self->{_useBannedMembers});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name', 'Delay','Recurrence','Enaled','Script','Description', 'UseExpiredMembers', 'UseExpiredVoMembers', 'UseBannedMembers');
}


1;
