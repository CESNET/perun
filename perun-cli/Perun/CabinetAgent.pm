package Perun::CabinetAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'cabinetManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

sub findPublicationsByGUIFilter
{
	return Perun::Common::callManagerMethod('findPublicationsByGUIFilter', '[]Publication', @_);
}

sub getPublicationById
{
	return Perun::Common::callManagerMethod('findPublicationById', 'Publication', @_);
}

sub createPublication
{
	return Perun::Common::callManagerMethod('createPublication', 'Publication', @_);
}

sub deletePublication
{
	return Perun::Common::callManagerMethod('deletePublication', 'Id', @_);
}

sub getPublicationAuthors
{
	return Perun::Common::callManagerMethod('findAuthorsByPublicationId', 'Id', @_);
}

sub assignAuthorToPublication
{
	return Perun::Common::callManagerMethod('createAuthorship', '', @_);
}

sub removeAuthorFromPublication
{
	return Perun::Common::callManagerMethod('deleteAuthorship', '', @_);
}

sub getPublicationThanks
{
	return Perun::Common::callManagerMethod('getRichThanksByPublicationId', 'Id', @_);
}

sub assignThankToPublication
{
	return Perun::Common::callManagerMethod('createThanks', '', @_);
}

sub removeThankFromPublication
{
	return Perun::Common::callManagerMethod('deleteThanks', '', @_);
}

sub lockPublication
{
	return Perun::Common::callManagerMethod('lockPublications', '', 'lock' => 1, @_);
}

sub unLockPublication
{
	return Perun::Common::callManagerMethod('lockPublications', '', 'lock'=>0, @_);
}

sub findAllAuthors
{
	return Perun::Common::callManagerMethod('findAllAuthors', '[]Author', @_);
}

sub findAllCategories
{
	return Perun::Common::callManagerMethod('getCategories', '[]Category', @_);
}

1;
