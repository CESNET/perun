package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum ConfigManagerMethod implements ManagerMethod {

	/*#
	 * Reloads configuration of the perun-apps-config.yml file.
	 *
	 */
	reloadAppsConfig {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getConfigManager().reloadAppsConfig(ac.getSession());
			return null;
		}
	}
}
