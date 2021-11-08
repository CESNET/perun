package cz.metacentrum.perun.integration.api;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.integration.model.GroupMemberRelations;

import java.util.List;

public interface IntegrationManager {

	/**
	 * Get all member-group relations for all groups with all member-group attributes.
	 *
	 * @param sess session
	 * @return list of all member-group relations
	 */
	List<GroupMemberRelations> getGroupMemberRelations(PerunSession sess) throws PrivilegeException;
}
