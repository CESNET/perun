package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum DatabaseManagerMethod implements ManagerMethod {

	/*#
	 * Gets current version of DB schema like String (eg. 1.1.1).
	 *
	 * @exampleResponse "3.1.21"
	 * @return String Current version of DB schema like String
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
	 *@exampleResponse "PostgreSQL Native Driver-PostgreSQL 9.0 JDBC4 (build 801)"
	 * @return String Current database driver name and version
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
	 * @exampleResponse "PostgreSQL-9.1.15"
	 * @return String Current database name and version
	 */
	getDatabaseInformation {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getDatabaseInformation(ac.getSession());
		}
	},

	/**
	 *	Get time in ns "nanoseconds" of calling 1 simple update query to DB.
	 *  This query will update property for this purpose in configurations table.
	 *
	 * @exampleResponse "196920"
	 * @return time of processing query in nanoseconds
	 */
	getTimeOfQueryPerformance {

		@Override
		public Long call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getDatabaseManager().getTimeOfQueryPerformance(ac.getSession());
		}
	};
}
