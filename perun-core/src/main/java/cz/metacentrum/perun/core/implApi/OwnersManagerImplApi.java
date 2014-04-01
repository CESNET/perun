package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;

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
	boolean ownerExists(PerunSession perunSession, Owner owner) throws InternalErrorException;

	/**
	 * Check if owner exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws InternalErrorException
	 * @throws OwnerNotExistsException
	 */
	void checkOwnerExists(PerunSession perunSession, Owner owner) throws InternalErrorException, OwnerNotExistsException;

	/**
	 * Create owner in the underlaying data source
	 *
	 * @param perunSession
	 * @param owner
	 * @return owner with id set
	 *
	 * @throws InternalErrorException
	 */
	Owner createOwner(PerunSession perunSession, Owner owner) throws InternalErrorException;

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws InternalErrorException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwner(PerunSession perunSession, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException;

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
	Owner getOwnerById(PerunSession perunSession, int id) throws OwnerNotExistsException, InternalErrorException;

	/**
	 * Return all owners.
	 *
	 * @param perunSession
	 * @return list of owners
	 *
	 * @throws InternalErrorException
	 */
	List<Owner> getOwners(PerunSession perunSession) throws InternalErrorException;

}
