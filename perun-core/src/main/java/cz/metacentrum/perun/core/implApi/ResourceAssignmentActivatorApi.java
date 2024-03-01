package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.GroupResourceAssignment;

public interface ResourceAssignmentActivatorApi {

  /**
   * Tries to activate assignment in transaction.
   *
   * @param assignment
   */
  void tryActivateAssignment(GroupResourceAssignment assignment);
}
