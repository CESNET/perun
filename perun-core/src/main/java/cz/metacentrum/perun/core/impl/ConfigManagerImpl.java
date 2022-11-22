package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigFileNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OidcConfigNotExistsException;
import cz.metacentrum.perun.core.implApi.ConfigManagerImplApi;

/**
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConfigManagerImpl implements ConfigManagerImplApi {

	private PerunAppsConfigLoader perunAppsConfigLoader;
	private PerunOidcConfigLoader perunOidcConfigLoader;

	private final PerunOidcConfigContainer perunOidcConfigContainer = new PerunOidcConfigContainer();

	public void initialize() {
		this.perunOidcConfigContainer.setOidcConfigs(perunOidcConfigLoader.loadPerunOidcConfigs());
	}
	@Override
	public void setPerunAppsConfigLoader(PerunAppsConfigLoader perunAppsConfigLoader) {
		this.perunAppsConfigLoader = perunAppsConfigLoader;
	}

	public void setPerunOidcConfigLoader(PerunOidcConfigLoader perunOidcConfigLoader) {
		this.perunOidcConfigLoader = perunOidcConfigLoader;
	}

	@Override
	public void reloadAppsConfig() {
		perunAppsConfigLoader.initialize();
	}

	public OidcConfig getPerunOidcConfig(String name) throws OidcConfigNotExistsException, OidcConfigFileNotExistsException {
		return perunOidcConfigContainer.getOidcConfig(name);
	}
}
