package Perun::beans::ApplicationForm;

use strict;
use warnings;

use Perun::Common;

sub new
{
        bless({});
}

sub fromHash
{
        my $applicationForm = Perun::Common::fromHash(@_);
	my $vo = $applicationForm->{_vo};
	$vo = Perun::beans::Vo::fromHash("Perun::beans::Vo", $vo);
	my $group = $applicationForm->{_group};
	$group = Perun::beans::Group::fromHash("Perun::beans::Group", $group);
	return $applicationForm;
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

	my $vo;
        if (defined($self->{_vo})) {
                $vo = $self->{_vo};
        } else {
                $vo = undef;
        }

	my $group;
        if (defined($self->{_group})) {
                $group = $self->{_group};
        } else {
                $group = undef;
        }

	my $automaticApproval;
        if (defined($self->{_automaticApproval})) {
                $automaticApproval = $self->{_automaticApproval};
        } else {
                $automaticApproval = undef;
        }

	my $automaticApprovalExtension;
        if (defined($self->{_automaticApprovalExtension})) {
                $automaticApprovalExtension = $self->{_automaticApprovalExtension};
        } else {
                $automaticApprovalExtension = undef;
        }

	my $moduleClassName;
        if (defined($self->{_moduleClassName})) {
                $moduleClassName = "$self->{_moduleClassName}";
        } else {
                $moduleClassName = undef;
        }

	return { id => $id, vo => $vo, group => $group, automaticApproval => $automaticApproval, automaticApprovalExtension => $automaticApprovalExtension, moduleClassName => $moduleClassName };
}

sub getId
{
        my $self = shift;

        return $self->{_id};
}

sub getVoId
{
        my $self = shift;

        return $self->{_vo}->{id};
}

sub getGroupId
{
        my $self = shift;

        return $self->{_group}->{id};
}

sub isAutomaticApproval
{
        my $self = shift;

        return ($self->{_automaticApproval}) ? 1 : 0;
}

sub setAutomaticApproval
{
        my $self = shift;
        my $value = shift;
        if (ref $value eq "JSON::XS::Boolean")
        {
                $self->{_automaticApproval} = $value;
        } elsif ($value eq 'true' || $value eq 1)
        {
                $self->{_automaticApproval} = JSON::XS::true;
        } else
        {
                $self->{_automaticApproval} = JSON::XS::false;
        }

}

sub isAutomaticApprovalExtension
{
        my $self = shift;

        return ($self->{_automaticApprovalExtension}) ? 1 : 0;
}

sub setAutomaticApprovalExtension
{
        my $self = shift;
        my $value = shift;
        if (ref $value eq "JSON::XS::Boolean")
        {
                $self->{_automaticApprovalExtension} = $value;
        } elsif ($value eq 'true' || $value eq 1)
        {
                $self->{_automaticApprovalExtension} = JSON::XS::true;
        } else
        {
                $self->{_automaticApprovalExtension} = JSON::XS::false;
        }

}

sub getModuleClassName
{
        my $self = shift;

        return $self->{_moduleClassName};
}

sub setModuleClassName
{
        my $self = shift;
        $self->{_moduleClassName} = shift;
}

1;
