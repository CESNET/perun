package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
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

  @Override
  public boolean getEnableLinkedName() {
    return BeansUtils.getCoreConfig().getEnableLinkedName();
  }

  @Override
  public boolean getEnableCustomName() {
    return BeansUtils.getCoreConfig().getEnableCustomName();
  }

  @Override
  public boolean getCustomNameRequiresApprove() {
    return BeansUtils.getCoreConfig().getCustomNameRequiresApprove();
  }

  @Override
  public boolean getEnableLinkedOrganization() {
    return BeansUtils.getCoreConfig().getEnableLinkedOrganization();
  }

  @Override
  public boolean getEnableCustomOrganization() {
    return BeansUtils.getCoreConfig().getEnableCustomOrganization();
  }

  @Override
  public boolean getCustomOrganizationRequiresApprove() {
    return BeansUtils.getCoreConfig().getCustomOrganizationRequiresApprove();
  }

  @Override
  public boolean getEnableLinkedEmail() {
    return BeansUtils.getCoreConfig().getEnableLinkedEmail();
  }

  @Override
  public boolean getEnableCustomEmail() {
    return BeansUtils.getCoreConfig().getEnableCustomEmail();
  }

  @Override
  public boolean getCustomEmailRequiresVerification() {
    return BeansUtils.getCoreConfig().getCustomEmailRequiresVerification();
  }
}
