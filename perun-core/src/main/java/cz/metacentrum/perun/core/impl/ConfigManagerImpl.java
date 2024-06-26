package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.implApi.ConfigManagerImplApi;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConfigManagerImpl implements ConfigManagerImplApi {

  private final PerunOidcConfigContainer perunOidcConfigContainer = new PerunOidcConfigContainer();
  private PerunAppsConfigLoader perunAppsConfigLoader;
  private PerunOidcConfigLoader perunOidcConfigLoader;

  public OidcConfig getPerunOidcConfig(String name)
      throws OidcConfigNotExistsException, OidcConfigFileNotExistsException {
    return perunOidcConfigContainer.getOidcConfig(name);
  }

  public void initialize() {
    this.perunOidcConfigContainer.setOidcConfigs(perunOidcConfigLoader.loadPerunOidcConfigs());
  }

  @Override
  public void reloadAppsConfig() {
    perunAppsConfigLoader.initialize();
  }

  @Override
  public boolean isArchiveSpoolEnabled() {
    return BeansUtils.getCoreConfig().isArchiveSpool();
  }

  @Override
  public void setPerunAppsConfigLoader(PerunAppsConfigLoader perunAppsConfigLoader) {
    this.perunAppsConfigLoader = perunAppsConfigLoader;
  }

  public void setPerunOidcConfigLoader(PerunOidcConfigLoader perunOidcConfigLoader) {
    this.perunOidcConfigLoader = perunOidcConfigLoader;
  }
}
