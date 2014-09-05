package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum DatabaseManagerMethod implements ManagerMethod {

	/*#
	 * Gets current version DB like string (ex. 1.1.1)
	 *
	 * @return Object current version DB like String
	 */
	getCurrentDatabaseVersion {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getCurrentDatabaseVersion(ac.getSession());
		}
	},
	
	/*#
	 * Gets current database driver name and version
	 *
	 * @return Object current database driver name and version
	 */
	getDatabaseDriverInformation {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getDatabaseDriverInformation(ac.getSession());
		}
	},
	
	/*#
	 * Gets current database name and version
	 *
	 * @return Object current database name and version
	 */
	getDatabaseInformation {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getDatabaseInformation(ac.getSession());
		}
	};
}
