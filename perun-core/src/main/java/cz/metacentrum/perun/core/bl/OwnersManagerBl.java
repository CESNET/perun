package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;

import java.util.List;

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
	Owner createOwner(PerunSession perunSession, Owner owner);

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 */
	void deleteOwner(PerunSession perunSession, Owner owner) throws RelationExistsException;

	/**
	 * Delete owner from underlaying data source.
	 *
	 * @param perunSession
	 * @param owner
	 * @param forceDelete
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 */
	void deleteOwner(PerunSession perunSession, Owner owner, boolean forceDelete) throws RelationExistsException;

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

	void checkOwnerExists(PerunSession sess, Owner owner) throws OwnerNotExistsException;
}
