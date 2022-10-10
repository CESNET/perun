package cz.metacentrum.perun.core.bl;


import cz.metacentrum.perun.core.api.OidcConfig;

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
	 * @return oidcConfig
	 */
	OidcConfig getPerunOidcConfig();
}
