package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.bl.ConfigManagerBl;
import cz.metacentrum.perun.core.implApi.ConfigManagerImplApi;

/**
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConfigManagerBlImpl implements ConfigManagerBl {

	private final ConfigManagerImplApi configManagerImpl;

	public ConfigManagerBlImpl(ConfigManagerImplApi configManagerImpl) {
		this.configManagerImpl = configManagerImpl;
	}

	public ConfigManagerImplApi getConfigManagerImpl() {
		return configManagerImpl;
	}

	@Override
	public void reloadAppsConfig() {
		configManagerImpl.reloadAppsConfig();
	}

	@Override
	public OidcConfig getPerunOidcConfig() {
		return configManagerImpl.getPerunOidcConfig();
	}

}
