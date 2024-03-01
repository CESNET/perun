package cz.metacentrum.perun.integration.bl;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.model.GroupMemberData;

public interface IntegrationManagerBl {

  /**
   * Get all member-group relations for all groups with all member-group attributes.
   *
   * @param sess session
   * @return list of all member-group relations
   */
  GroupMemberData getGroupMemberData(PerunSession sess);
}
