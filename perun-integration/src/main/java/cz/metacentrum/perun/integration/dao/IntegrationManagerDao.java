package cz.metacentrum.perun.integration.dao;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.model.GroupMemberRelation;
import java.util.List;

public interface IntegrationManagerDao {

  /**
   * Return all group-member relations from db.
   *
   * @param sess session
   * @return list of all group-member relations
   */
  List<GroupMemberRelation> getGroupMemberRelations(PerunSession sess);
}
