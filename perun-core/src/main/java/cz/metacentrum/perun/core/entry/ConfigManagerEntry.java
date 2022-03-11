package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ConfigManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.ConfigManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * ConfigManager entry logic.
 *
 * @author David Flor <493294@mail.muni.cz>
 */

public class ConfigManagerEntry implements ConfigManager {
	private ConfigManagerBl configManagerBl;
	private PerunBl perunBl;

	public ConfigManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.configManagerBl = perunBl.getConfigManagerBl();
	}
	
	public ConfigManagerEntry() {}
	
	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBlImpl perunBl) {
		this.perunBl = perunBl;
	}

	public void setConfigManagerBl(ConfigManagerBl configManagerBl) {
		this.configManagerBl = configManagerBl;
	}

	@Override
	public void reloadAppsConfig(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "reloadAppsConfig_policy"))
			throw new PrivilegeException(sess, "reloadAppsConfig");

		configManagerBl.reloadAppsConfig();
	}
}
