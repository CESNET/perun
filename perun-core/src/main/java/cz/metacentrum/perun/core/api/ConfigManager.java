package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

/**
 * ConfigManager serves to manage configuration files.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface ConfigManager {

  /**
   * Returns Oidc Configuration for this Perun instance (to be used for CLI communication).
   *
   * @param sess       session
   * @param requestUrl url of the request
   * @return oidcConfig
   * @throws OidcConfigNotExistsException     when configuration under such name doesn't exist
   * @throws OidcConfigFileNotExistsException when configuration file for oidc configs doesn't exist.
   */
  OidcConfig getPerunOidcConfig(PerunSession sess, String requestUrl)
      throws OidcConfigNotExistsException, OidcConfigFileNotExistsException;

  /**
   * Reloads the configuration of brandings and their respective apps (see perun-apps-config.yml)
   *
   * @param sess user's session in Perun
   * @throws PrivilegeException wrong privilege to call this method
   */
  void reloadAppsConfig(PerunSession sess) throws PrivilegeException;

  /**
   * Checks whether spool files generated by the engine should be archived
   *
   * @param sess session
   * @return true if archive, false otherwise
   * @throws PrivilegeException insufficient privileges
   */
  boolean isArchiveSpoolEnabled(PerunSession sess) throws PrivilegeException;

  /**
   * Gets personal data change configuration with all related core config properties.
   *
   * @param sess session
   * @return personal data change config
   */
  PersonalDataChangeConfig getPersonalDataChangeConfig(PerunSession sess);
}
