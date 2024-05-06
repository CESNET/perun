package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ConfigManager;
import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.ConfigManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * ConfigManager entry logic.
 *
 * @author David Flor <493294@mail.muni.cz>
 */

public class ConfigManagerEntry implements ConfigManager {
  private ConfigManagerBl configManagerBl;
  private PerunBl perunBl;

  public ConfigManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.configManagerBl = perunBl.getConfigManagerBl();
  }

  public ConfigManagerEntry() {
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public OidcConfig getPerunOidcConfig(PerunSession sess, String requestUrl)
      throws OidcConfigNotExistsException, OidcConfigFileNotExistsException {
    Utils.checkPerunSession(sess);


    return configManagerBl.getPerunOidcConfig(requestUrl);
  }

  @Override
  public void reloadAppsConfig(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "reloadAppsConfig_policy")) {
      throw new PrivilegeException(sess, "reloadAppsConfig");
    }

    configManagerBl.reloadAppsConfig();
  }

  @Override
  public boolean isArchiveSpoolEnabled(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "isArchiveSpoolEnabled_policy")) {
      throw new PrivilegeException(sess, "isArchiveSpoolEnabled");
    }

    return configManagerBl.isArchiveSpoolEnabled();
  }

  public void setConfigManagerBl(ConfigManagerBl configManagerBl) {
    this.configManagerBl = configManagerBl;
  }

  public void setPerunBl(PerunBlImpl perunBl) {
    this.perunBl = perunBl;
  }
}
