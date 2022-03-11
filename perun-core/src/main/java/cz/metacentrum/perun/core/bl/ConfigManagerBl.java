package cz.metacentrum.perun.core.bl;


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
}
