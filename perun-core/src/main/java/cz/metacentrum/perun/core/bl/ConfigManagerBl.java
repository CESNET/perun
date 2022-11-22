package cz.metacentrum.perun.core.bl;


import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;

/**
 * ConfigManager serves to manage configuration files.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface ConfigManagerBl {

	/**
	 * Reloads the configuration of brandings and their respective apps (see perun-apps-config.yml)
	 */
	void reloadAppsConfig();

	/**
	 * Returns Oidc Configuration for this Perun instance (to be used for CLI communication).
	 *
	 * @param requestUrl url of request
	 * @throws OidcConfigNotExistsException when configuration under such name doesn't exist
	 * @throws OidcConfigFileNotExistsException when configuration file for oidc configs doesn't exist.
	 * @return oidcConfig
	 */
	OidcConfig getPerunOidcConfig(String requestUrl) throws OidcConfigNotExistsException, OidcConfigFileNotExistsException;
}
