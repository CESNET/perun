package cz.metacentrum.perun.ldapc.model;

import java.util.List;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunResource extends PerunEntry<Resource> {

	/**
	 * Remove resource from LDAP
	 *
	 * @param resource resource from Perun
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void deleteResource(Resource resource) throws InternalErrorException;

	/**
	 * Add resource to LDAP.
	 *
	 * @param resource resource from Perun
	 * @param entityID entityID if exists, or null if not
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void addResource(Resource resource, String entityID) throws InternalErrorException;

	public void updateResource(Resource resource) throws InternalErrorException;
	
	public void assignGroup(Resource resource, Group group) throws InternalErrorException;
	
	public void removeGroup(Resource resource, Group group) throws InternalErrorException;

	public void synchronizeGroups(Resource resource, List<Group> assignedGroups) throws InternalErrorException;
}
