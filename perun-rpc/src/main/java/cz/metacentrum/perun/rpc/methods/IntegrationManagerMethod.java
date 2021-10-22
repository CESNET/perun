package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.integration.model.GroupMemberRelations;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.util.List;

public enum IntegrationManagerMethod implements ManagerMethod {

	/*#
	 * Get all member-group relations for all groups with all member-group attributes.
	 *
	 * @param sess session
	 * @return list of all member-group relations
	 */
	getGroupMemberRelations {
		@Override
		public List<GroupMemberRelations> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getIntegrationManager().getGroupMemberRelations(ac.getSession());
		}
	}
}
