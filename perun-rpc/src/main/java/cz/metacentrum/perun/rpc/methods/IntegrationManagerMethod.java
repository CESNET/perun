package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum IntegrationManagerMethod implements ManagerMethod {

	getGroupMembersDirectRelations {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getIntegrationManager();
		}
	}
}
