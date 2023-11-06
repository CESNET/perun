package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;

import java.util.List;

/**
 * OwnersManager
 *
 * @author Slavek Licehammer
 */
public interface OwnersManagerImplApi {

	/**
	 * Check if owner exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 * @return true if owner exists in underlaying data source, false othewise
	 *
	 * @throws InternalErrorException
	 */
	boolean ownerExists(PerunSession perunSession, Owner owner);

	/**
	 * Check if owner exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws InternalErrorException
	 * @throws OwnerNotExistsException
	 */
	void checkOwnerExists(PerunSession perunSession, Owner owner) throws OwnerNotExistsException;

	/**
	 * Create owner in the underlaying data source
	 *
	 * @param perunSession
	 * @param owner
	 * @return owner with id set
	 *
	 * @throws InternalErrorException
	 */
	Owner createOwner(PerunSession perunSession, Owner owner);

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws InternalErrorException
	 */
	void deleteOwner(PerunSession perunSession, Owner owner);

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
	 */
	Owner getOwnerById(PerunSession perunSession, int id) throws OwnerNotExistsException;

	/**
	 * Find owner by name.
	 *
	 * @param perunSession perun session
	 * @param name name of the owner
	 *
	 * @return Owner with specified name
	 *
	 * @throws OwnerNotExistsException if owner with given name does not exist
	 */
	Owner getOwnerByName(PerunSession perunSession, String name) throws OwnerNotExistsException;

	/**
	 * Return all owners.
	 *
	 * @param perunSession
	 * @return list of owners
	 *
	 * @throws InternalErrorException
	 */
	List<Owner> getOwners(PerunSession perunSession);

}
