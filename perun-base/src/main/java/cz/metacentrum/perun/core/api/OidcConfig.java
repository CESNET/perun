package cz.metacentrum.perun.core.api;

import java.util.Objects;

public class OidcConfig {

  private String clientId;
  private String oidcDeviceCodeUri;
  private String oidcTokenEndpointUri;
  private String oidcTokenRevokeEndpointUri;
  private String acrValues;
  private String scopes;
  private String perunApiEndpoint;
  private boolean enforceMfa;

  public OidcConfig() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OidcConfig that = (OidcConfig) o;
    return Objects.equals(getClientId(), that.getClientId()) &&
           Objects.equals(getOidcDeviceCodeUri(), that.getOidcDeviceCodeUri()) &&
           Objects.equals(getOidcTokenEndpointUri(), that.getOidcTokenEndpointUri()) &&
           Objects.equals(getOidcTokenRevokeEndpointUri(), that.getOidcTokenRevokeEndpointUri()) &&
           Objects.equals(getPerunApiEndpoint(), that.getPerunApiEndpoint()) &&
           Objects.equals(getAcrValues(), that.getAcrValues()) && Objects.equals(getScopes(), that.getScopes()) &&
           Objects.equals(getEnforceMfa(), that.getEnforceMfa());
  }

  public String getAcrValues() {
    return acrValues;
  }

  public void setAcrValues(String acrValues) {
    this.acrValues = acrValues;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public boolean getEnforceMfa() {
    return enforceMfa;
  }

  public void setEnforceMfa(boolean enforceMfa) {
    this.enforceMfa = enforceMfa;
  }

  public String getOidcDeviceCodeUri() {
    return oidcDeviceCodeUri;
  }

  public void setOidcDeviceCodeUri(String oidcDeviceCodeUri) {
    this.oidcDeviceCodeUri = oidcDeviceCodeUri;
  }

  public String getOidcTokenEndpointUri() {
    return oidcTokenEndpointUri;
  }

  public void setOidcTokenEndpointUri(String oidcTokenEndpointUri) {
    this.oidcTokenEndpointUri = oidcTokenEndpointUri;
  }

  public String getOidcTokenRevokeEndpointUri() {
    return oidcTokenRevokeEndpointUri;
  }

  public void setOidcTokenRevokeEndpointUri(String oidcTokenRevokeEndpointUri) {
    this.oidcTokenRevokeEndpointUri = oidcTokenRevokeEndpointUri;
  }

  public String getPerunApiEndpoint() {
    return perunApiEndpoint;
  }

  public void setPerunApiEndpoint(String perunApiEndpoint) {
    this.perunApiEndpoint = perunApiEndpoint;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClientId(), getOidcDeviceCodeUri(), getOidcTokenEndpointUri(),
        getOidcTokenRevokeEndpointUri(), getPerunApiEndpoint(), getAcrValues(), getScopes(), getEnforceMfa());
  }

  @Override
  public String toString() {
    return "OidcConfig{" + "clientId='" + clientId + '\'' + ", oidcDeviceCodeUri='" + oidcDeviceCodeUri + '\'' +
           ", oidcTokenEndpointUri='" + oidcTokenEndpointUri + '\'' + ", oidcTokenRevokeEndpointUri='" +
           oidcTokenRevokeEndpointUri + '\'' + ", acrValues='" + acrValues + '\'' + ", scopes='" + scopes + '\'' +
           ", perunApiEndpoint='" + perunApiEndpoint + '\'' + ", enforceMfa='" + enforceMfa + '\'' + '}';
  }
}
