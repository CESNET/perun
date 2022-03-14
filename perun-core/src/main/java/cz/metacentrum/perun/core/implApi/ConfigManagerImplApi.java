package cz.metacentrum.perun.core.implApi;


import cz.metacentrum.perun.core.impl.PerunAppsConfigLoader;

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
	 * Reloads the configuration of brandings and their respective apps (see perun-apps-config.yml)
	 *
	 */
	void reloadAppsConfig();
}
