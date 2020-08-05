package Perun::beans::FormItem;

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
        $id = 0;
    }

    my $shortname;
    if (defined($self->{_shortname})) {
        $shortname = $self->{_shortname};
    } else {
        $shortname = undef;
    }

    my $required;
    if (defined($self->{_required})) {
        $required = $self->{_required};
    } else {
        $required = undef;
    }

    my $type;
    if (defined($self->{_type})) {
        $type = $self->{_type};
    } else {
        $type = undef;
    }

    my $federationAttribute;
    if (defined($self->{_federationAttribute})) {
        $federationAttribute = $self->{_federationAttribute};
    } else {
        $federationAttribute = undef;
    }

    my $perunSourceAttribute;
    if (defined($self->{_perunSourceAttribute})) {
        $perunSourceAttribute = $self->{_perunSourceAttribute};
    } else {
        $perunSourceAttribute = undef;
    }

    my $perunDestinationAttribute;
    if (defined($self->{_perunDestinationAttribute})) {
        $perunDestinationAttribute = $self->{_perunDestinationAttribute};
    } else {
        $perunDestinationAttribute = undef;
    }

    my $regex;
    if (defined($self->{_regex})) {
        $regex = $self->{_regex};
    } else {
        $regex = undef;
    }

    my $applicationTypes;
    if (defined($self->{_applicationTypes})) {
        $applicationTypes = $self->{_applicationTypes};
    } else {
        $applicationTypes = undef;
    }

    my $ordnum;
    if (defined($self->{_ordnum})) {
        $ordnum = $self->{_ordnum} * 1;
    } else {
        $ordnum = 0;
    }

    my $forDelete;
    if (defined($self->{_forDelete})) {
        $forDelete = $self->{_forDelete};
    } else {
        $forDelete = undef;
    }

    my $i18n;
    if (defined($self->{_i18n})) {
        $i18n = $self->{_i18n};
    } else {
        $i18n = undef;
    }

    return { id                   => $id,
        shortname                 => $shortname,
        required                  => $required,
        type                      => $type,
        federationAttribute       => $federationAttribute,
        perunSourceAttribute      => $perunSourceAttribute,
        perunDestinationAttribute => $perunDestinationAttribute,
        regex                     => $regex,
        applicationTypes          => $applicationTypes,
        ordnum                    => $ordnum,
        forDelete                 => $forDelete,
        i18n                      => $i18n,
        beanName                  => 'FormItem'
    };
}

sub getId
{
    my $self = shift;

    return $self->{_id};
}

sub getShortname
{
    my $self = shift;

    return $self->{_shortname};
}

sub setShortname
{
    my $self = shift;
    $self->{_shortname} = shift;

    return;
}

sub getRequired
{
    my $self = shift;

    return $self->{_required};
}

sub setRequired
{
    my $self = shift;
    $self->{_required} = shift;

    return;
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

sub getFederationAttribute
{
    my $self = shift;

    return $self->{_federationAttribute};
}

sub setFederationAttribute
{
    my $self = shift;
    $self->{_federationAttribute} = shift;

    return;
}

sub getPerunSourceAttribute
{
    my $self = shift;

    return $self->{_perunSourceAttribute};
}

sub setPerunSourceAttribute
{
    my $self = shift;
    $self->{_perunSourceAttribute} = shift;

    return;
}

sub getPerunDestinationAttribute
{
    my $self = shift;

    return $self->{_perunDestinationAttribute};
}

sub setPerunDestinationAttribute
{
    my $self = shift;
    $self->{_perunDestinationAttribute} = shift;

    return;
}

sub getRegex
{
    my $self = shift;

    return $self->{_regex};
}

sub setRegex
{
    my $self = shift;
    $self->{_regex} = shift;

    return;
}

sub getApplicationTypes
{
    my $self = shift;

    return $self->{_applicationTypes};
}

sub setApplicationTypes
{
    my $self = shift;
    $self->{_applicationTypes} = shift;

    return;
}

sub getOrdnum
{
    my $self = shift;

    return $self->{_ordnum};
}

sub setOrdnum
{
    my $self = shift;
    $self->{_ordnum} = shift;

    return;
}

sub getForDelete
{
    my $self = shift;

    return $self->{_forDelete};
}

sub setForDelete
{
    my $self = shift;
    $self->{_forDelete} = shift;

    return;
}

sub getI18n
{
    my $self = shift;

    return $self->{_i18n};
}

sub setI18n
{
    my $self = shift;
    $self->{_i18n} = shift;

    return;
}

1;
