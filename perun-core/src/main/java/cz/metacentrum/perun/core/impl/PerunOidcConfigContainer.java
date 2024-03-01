package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;

import java.util.HashMap;
import java.util.Map;

public class PerunOidcConfigContainer {

  private Map<String, OidcConfig> oidcConfigs = new HashMap<>();

  public void setOidcConfigs(Map<String, OidcConfig> oidcConfigs) {
    this.oidcConfigs = oidcConfigs;
  }

  public OidcConfig getOidcConfig(String name) throws OidcConfigNotExistsException, OidcConfigFileNotExistsException {
    if (oidcConfigs == null) {
      throw new OidcConfigFileNotExistsException("Configuration file for OIDC configs does not exist.");
    }
    if (oidcConfigs.get(name) == null) {
      throw new OidcConfigNotExistsException("OidcConfig for " + name + " does not exist.");
    }

    return oidcConfigs.get(name);
  }

  public Map<String, OidcConfig> getAllOidcConfigs() throws OidcConfigFileNotExistsException {
    if (oidcConfigs == null) {
      throw new OidcConfigFileNotExistsException("Configuration file for OIDC configs does not exist.");
    }
    return oidcConfigs;
  }
}
