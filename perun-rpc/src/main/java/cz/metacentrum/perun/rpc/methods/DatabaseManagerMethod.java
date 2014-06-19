package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum DatabaseManagerMethod implements ManagerMethod {

	/*#
	 * Get current version DB like string (ex. 1.1.1)
	 *
	 * @return current version DB like String
	 */
	getCurrentDatabaseVersion {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getCurrentDatabaseVersion(ac.getSession());
		}
	};
}
