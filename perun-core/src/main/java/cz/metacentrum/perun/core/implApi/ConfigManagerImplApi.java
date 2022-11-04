package cz.metacentrum.perun.core.implApi;


import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.impl.PerunAppsConfigLoader;
import cz.metacentrum.perun.core.impl.PerunOidcConfigLoader;

/**
 * ConfigManager serves to manage configuration files.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface ConfigManagerImplApi {

	/**
	 * Sets the PerunAppsConfigLoader
	 *
	 * @param perunAppsConfigLoader loader to set
	 */
	void setPerunAppsConfigLoader(PerunAppsConfigLoader perunAppsConfigLoader);

	/**
	 * Sets the PerunOidcConfigLoader
	 *
	 * @param perunOidcConfigLoader loader to set
	 */
	void setPerunOidcConfigLoader(PerunOidcConfigLoader perunOidcConfigLoader);

	/**
	 * Reloads the configuration of brandings and their respective apps (see perun-apps-config.yml)
	 *
	 */
	void reloadAppsConfig();

	/**
	 * Returns Oidc Configuration for this Perun instance (to be used for CLI communication).
	 *
	 * @return oidcConfig
	 */
	OidcConfig getPerunOidcConfig();
}
