package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.PersonalDataChangeConfig;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
import cz.metacentrum.perun.core.bl.ConfigManagerBl;
import cz.metacentrum.perun.core.implApi.ConfigManagerImplApi;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConfigManagerBlImpl implements ConfigManagerBl {

  private final ConfigManagerImplApi configManagerImpl;

  public ConfigManagerBlImpl(ConfigManagerImplApi configManagerImpl) {
    this.configManagerImpl = configManagerImpl;
  }

  public ConfigManagerImplApi getConfigManagerImpl() {
    return configManagerImpl;
  }

  @Override
  public OidcConfig getPerunOidcConfig(String requestUrl)
      throws OidcConfigNotExistsException, OidcConfigFileNotExistsException {
    String domain;
    try {
      URL url = new URL(requestUrl);
      domain = url.getHost().startsWith("www.") ? url.getHost().substring(4) : url.getHost();
    } catch (MalformedURLException ex) {
      throw new InternalErrorException("request url is malformed", ex);
    }

    return configManagerImpl.getPerunOidcConfig(domain);
  }

  @Override
  public void reloadAppsConfig() {
    configManagerImpl.reloadAppsConfig();
  }

  @Override
  public boolean isArchiveSpoolEnabled() {
    return configManagerImpl.isArchiveSpoolEnabled();
  }

  @Override
  public PersonalDataChangeConfig getPersonalDataChangeConfig() {
    PersonalDataChangeConfig config = new PersonalDataChangeConfig();

    config.setEnableLinkedName(configManagerImpl.getEnableLinkedName());
    config.setEnableCustomName(configManagerImpl.getEnableCustomName());
    config.setCustomNameRequiresApprove(configManagerImpl.getCustomNameRequiresApprove());

    config.setEnableLinkedOrganization(configManagerImpl.getEnableLinkedOrganization());
    config.setEnableCustomOrganization(configManagerImpl.getEnableCustomOrganization());
    config.setCustomOrganizationRequiresApprove(configManagerImpl.getCustomOrganizationRequiresApprove());

    config.setEnableLinkedEmail(configManagerImpl.getEnableLinkedEmail());
    config.setEnableCustomEmail(configManagerImpl.getEnableCustomEmail());
    config.setCustomEmailRequiresVerification(configManagerImpl.getCustomEmailRequiresVerification());

    return config;
  }
}
