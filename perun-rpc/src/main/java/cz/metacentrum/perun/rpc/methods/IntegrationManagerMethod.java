package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.integration.model.GroupMemberData;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum IntegrationManagerMethod implements ManagerMethod {

  /*#
   * Get all member-group relations for all groups with all member-group attributes.
   *
   * @param sess session
   * @return GroupMemberData group-member relations with attributes
   */
  getGroupMemberData {
    @Override
    public GroupMemberData call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getIntegrationManager().getGroupMemberData(ac.getSession());
    }
  }
}
