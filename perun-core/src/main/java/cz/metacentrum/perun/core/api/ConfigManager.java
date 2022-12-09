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
	 * Reloads the configuration of brandings and their respective apps (see perun-apps-config.yml)
	 * @param sess user's session in Perun
	 * @throws PrivilegeException wrong privilege to call this method
	 */
	void reloadAppsConfig(PerunSession sess) throws PrivilegeException;

	/**
	 * Returns Oidc Configuration for this Perun instance (to be used for CLI communication).
	 *
	 * @param sess session
	 * @param requestUrl url of the request
	 * @throws OidcConfigNotExistsException when configuration under such name doesn't exist
	 * @throws OidcConfigFileNotExistsException when configuration file for oidc configs doesn't exist.
	 * @return oidcConfig
	 */
	OidcConfig getPerunOidcConfig(PerunSession sess, String requestUrl) throws OidcConfigNotExistsException, OidcConfigFileNotExistsException;
}
