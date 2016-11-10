package Perun::beans::ServiceAttributes;

use strict;
use warnings;

use Perun::Common;

sub new
{
	bless({});
}

sub fromHash
{
	my $class = shift;
	$class = ref $class if ref $class;

	my $self = { };
	my @attributes;
	my @childElements;
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		my %hash = @_;
		$hash = \%hash;
	}

	foreach (@{$hash->{attributes}}) {
		if (!defined($_)) {
			push(@attributes, undef);
		}
		elsif (ref($_) eq 'HASH') {
			push(@attributes, Perun::beans::Attribute->fromHash( $_ ));
		}
		else {
			die Perun::Exception->fromHash( { type => 'wrong_type_in_array', errorInfo => (ref($_).' not HASHref') } );
		}
	}

	foreach (@{$hash->{childElements}}) {
		if (!defined($_)) {
			push(@childElements, undef);
		}
		elsif (ref($_) eq 'HASH') {
			push(@childElements, Perun::beans::ServiceAttributes->fromHash( $_ ));
		}
		else {
			die Perun::Exception->fromHash( { type => 'wrong_type_in_array', errorInfo => (ref($_).' not HASHref') } );
		}
	}

	$self->{_attributes} = \@attributes;
	$self->{_childElements} = \@childElements;

	bless ($self, $class);
}

sub TO_JSON
{
	my $self = shift;
	my $attributes = $self->{_attributes};
	my $childElements = $self->{_childElements};

	return { attributes => $attributes, childElements => $childElements };
}

sub getAttributes
{
	my $self = shift;

	return @{$self->{_attributes}};
}

sub setAttributes
{
	my $self = shift;
	my $array;
	my @value;

	if ((@_ == 1) && (ref($_[0]) eq 'ARRAY')) {
		$array = $_[0];
	} else {
		$array = \@_;
	}

	foreach (@$array) {
		if (defined($_) && (ref($_) ne 'Perun::beans::Attribute')) {
			die Perun::Exception->fromHash( { type => 'wrong_type_in_array', errorInfo =>
						(ref($_).' not Perun::beans::Attribute') } );
		}

		push(@value, $_);
	}

	$self->{_attributes} = \@value;

	return;
}

sub getChildElements
{
	my $self = shift;

	return @{$self->{_childElements}};
}

sub setChildElements
{
	my $self = shift;
	my $array;
	my @value;

	if ((@_ == 1) && (ref($_[0]) eq 'ARRAY')) {
		$array = $_[0];
	} else {
		$array = \@_;
	}

	foreach (@$array) {
		if (defined($_) && (ref($_) ne 'Perun::beans::ServiceAttributes')) {
			die Perun::Exception->fromHash( { type => 'wrong_type_in_array', errorInfo =>
						(ref($_).' not Perun::beans::ServiceAttributes') } );
		}

		push(@value, $_);
	}

	$self->{_childElements} = \@value;

	return;
}

1;
