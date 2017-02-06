package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface for handling PublicationSystem entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface PublicationSystemManagerBl {

	/**
	 * Create PublicationSystem in Perun
	 *
	 * @param session PerunSession
	 * @param ps PublicationSystem to create
	 * @return PublicationSystem with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws InternalErrorException;

	/**
	 * Update PublicationSystem in Perun (name,type,url,loginNamespace) by its ID.
	 *
	 * @param session PerunSession
	 * @param ps PublicationSystem to update
	 * @return Updated PublicationSystem
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID.
	 * @throws InternalErrorException When implementation fails
	 */
	PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, InternalErrorException;

	/**
	 * Delete PublicationSystem by its ID.
	 *
	 * @param ps PublicationSystem to be deleted
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID
	 * @throws InternalErrorException When implementation fails
	 */
	void deletePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	/**
	 * Get PublicationSystem by its ID.
	 *
	 * @param id ID to get PS by
	 * @return PublicationSystem by its ID.
	 * @throws CabinetException When PublicationSystem doesn't exist by its ID.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemById(int id) throws InternalErrorException, CabinetException;

	/**
	 * Get PublicationSystem by its name
	 *
	 * @param name Name to get PS by
	 * @return PublicationSystem by its name.
	 * @throws CabinetException When PublicationSystem doesn't exist by its name.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemByName(String name) throws InternalErrorException, CabinetException;

	/**
	 * Get PublicationSystem by its login-namespace
	 *
	 * @param namespace Login-namespace to get PS by
	 * @return PublicationSystem by its login-namespace.
	 * @throws CabinetException When PublicationSystem doesn't exist by its login-namespace.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemByNamespace(String namespace) throws InternalErrorException, CabinetException;

	/**
	 * Get all PublicationSystems in Perun. If none, return empty list.
	 *
	 * @return List of all PublicationSystems or empty list.
	 * @throws InternalErrorException When implementation fails
	 */
	List<PublicationSystem> getPublicationSystems() throws InternalErrorException;

}
