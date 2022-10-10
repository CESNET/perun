package Perun::beans::OidcConfig;

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

	my $clientId;
	if (defined($self->{_clientId})) {
		$clientId = "$self->{_clientId}";
	} else {
		$clientId = undef;
	}

	my $oidcDeviceCodeUri;
	if (defined($self->{_oidcDeviceCodeUri})) {
		$oidcDeviceCodeUri = "$self->{_oidcDeviceCodeUri}";
	} else {
		$oidcDeviceCodeUri = undef;
	}

	my $oidcTokenEndpointUri;
	if (defined($self->{_oidcTokenEndpointUri})) {
		$oidcTokenEndpointUri = "$self->{_oidcTokenEndpointUri}";
	} else {
		$oidcTokenEndpointUri = undef;
	}

	my $oidcTokenRevokeEndpointUri;
	if (defined($self->{_oidcTokenRevokeEndpointUri})) {
		$oidcTokenRevokeEndpointUri = "$self->{_oidcTokenRevokeEndpointUri}";
	} else {
		$oidcTokenRevokeEndpointUri = undef;
	}

	my $acrValues;
	if (defined($self->{_acrValues})) {
		$acrValues = "$self->{_acrValues}";
	} else {
		$acrValues = undef;
	}

	my $scopes;
	if (defined($self->{_scopes})) {
		$scopes = "$self->{_scopes}";
	} else {
		$scopes = undef;
	}

	my $perunApiEndpoint;
	if (defined($self->{_perunApiEndpoint})) {
		$perunApiEndpoint = "$self->{_perunApiEndpoint}";
	} else {
		$perunApiEndpoint = undef;
	}

	my $enforceMfa;
	if (defined($self->{_enforceMfa})) {
		$enforceMfa = "$self->{_enforceMfa}";
	} else {
		$enforceMfa = undef;
	}

	return { clientId => $clientId, oidcDeviceCodeUri => $oidcDeviceCodeUri, oidcTokenEndpointUri => $oidcTokenEndpointUri,
		oidcTokenRevokeEndpointUri => $oidcTokenRevokeEndpointUri, acrValues => $acrValues, scopes => $scopes,
		perunApiEndpoint => $perunApiEndpoint, enforceMfa => $enforceMfa };
}

sub getClientId
{
	my $self = shift;

	return $self->{_clientId};
}

sub getOidcDeviceCodeUri
{
	my $self = shift;

	return $self->{_oidcDeviceCodeUri};
}

sub getOidcTokenEndpointUri
{
	my $self = shift;

	return $self->{_oidcTokenEndpointUri};
}

sub getOidcTokenRevokeEndpointUri
{
	my $self = shift;

	return $self->{_oidcTokenRevokeEndpointUri};
}

sub getAcrValues
{
	my $self = shift;

	return $self->{_acrValues};
}

sub getScopes
{
	my $self = shift;

	return $self->{_scopes};
}

sub getPerunApiEndpoint
{
	my $self = shift;

	return $self->{_perunApiEndpoint};
}

sub getEnforceMfa
{
	my $self = shift;

	return $self->{_enforceMfa};
}

1;