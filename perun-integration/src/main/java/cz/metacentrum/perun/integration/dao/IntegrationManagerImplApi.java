package cz.metacentrum.perun.integration.dao;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.model.GroupMembers;

import java.util.List;

public interface IntegrationManagerImplApi {

	/**
	 * Return all group-member relations from db.
	 *
	 * @param sess session
	 * @return list of all group-member relations
	 */
	List<GroupMembers> getGroupMemberRelations(PerunSession sess);
}
