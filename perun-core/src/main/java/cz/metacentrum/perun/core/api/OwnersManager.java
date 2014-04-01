package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;

/**
 * OwnersManager
 *
 * @author Slavek Licehammer
 */
public interface OwnersManager {

	/**
	 * Create owner in the underlaying data source
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @return owner with id set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Owner createOwner(PerunSession perunSession, Owner owner) throws InternalErrorException, PrivilegeException;

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws OwnerNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwner(PerunSession perunSession, Owner owner) throws OwnerNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException;

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 * @param forceDelete
	 *
	 * @throws OwnerNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwner(PerunSession perunSession, Owner owner, boolean forceDelete) throws OwnerNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException;

	/**
	 * Find owner by id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return Owner with specified id
	 *
	 * @throws OwnerNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Owner getOwnerById(PerunSession perunSession, int id) throws OwnerNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Return all owners.
	 *
	 * @param perunSession
	 * @return list of owners
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Owner> getOwners(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

}
