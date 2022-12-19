package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;

import java.util.List;

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
	Owner createOwner(PerunSession perunSession, Owner owner) throws PrivilegeException;

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
	void deleteOwner(PerunSession perunSession, Owner owner) throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException;

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
	void deleteOwner(PerunSession perunSession, Owner owner, boolean forceDelete) throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException;

	/**
	 * Delete owners from underlaying data source.
	 *
	 * @param sess perun session
	 * @param owners list of owners
	 * @param forceDelete
	 *
	 * @throws OwnerNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwners(PerunSession sess, List<Owner> owners, boolean forceDelete) throws OwnerNotExistsException, PrivilegeException, RelationExistsException, OwnerAlreadyRemovedException;

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
	Owner getOwnerById(PerunSession perunSession, int id) throws OwnerNotExistsException, PrivilegeException;

	/**
	 * Find owner by name.
	 *
	 * @param perunSession perun session
	 * @param name name of the owner
	 *
	 * @return Owner with specified name
	 *
	 * @throws OwnerNotExistsException if owner with given name does not exist
	 * @throws PrivilegeException if user does not have sufficient permissions
	 */
	Owner getOwnerByName(PerunSession perunSession, String name) throws OwnerNotExistsException, PrivilegeException;

	/**
	 * Return all owners.
	 *
	 * @param perunSession
	 * @return list of owners
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Owner> getOwners(PerunSession perunSession) throws PrivilegeException;

}
