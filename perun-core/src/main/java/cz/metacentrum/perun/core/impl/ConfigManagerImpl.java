package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.implApi.ConfigManagerImplApi;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConfigManagerImpl implements ConfigManagerImplApi {

	private PerunAppsConfigLoader perunAppsConfigLoader;

	@Override
	public void setPerunAppsConfigLoader(PerunAppsConfigLoader perunAppsConfigLoader) {
		this.perunAppsConfigLoader = perunAppsConfigLoader;
	}

	@Override
	public void reloadAppsConfig() {
		perunAppsConfigLoader.initialize();
	}
}
