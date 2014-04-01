package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;

/**
 * OwnersManager
 *
 * @author Slavek Licehammer
 */
public interface OwnersManagerBl {

	/**
	 * Create owner in the underlaying data source
	 *
	 * @param perunSession
	 * @param owner
	 *
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
	 * @throws RelationExistsException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwner(PerunSession perunSession, Owner owner) throws InternalErrorException, RelationExistsException, OwnerAlreadyRemovedException;

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 * @param forceDelete
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 * @throws OwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteOwner(PerunSession perunSession, Owner owner, boolean forceDelete) throws InternalErrorException, RelationExistsException, OwnerAlreadyRemovedException;

	/**
	 * Find owner by id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return Owner with specified id
	 *
	 * @throws InternalErrorException
	 */
	Owner getOwnerById(PerunSession perunSession, int id) throws InternalErrorException, OwnerNotExistsException;

	/**
	 * Return all owners.
	 *
	 * @param perunSession
	 * @return list of owners
	 *
	 * @throws InternalErrorException
	 */
	List<Owner> getOwners(PerunSession perunSession) throws InternalErrorException;

	void checkOwnerExists(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException;
}
