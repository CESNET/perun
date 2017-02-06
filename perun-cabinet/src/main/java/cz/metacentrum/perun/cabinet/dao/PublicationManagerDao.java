package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface of DAO layer for handling Publication entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface PublicationManagerDao {

	/**
	 * Create Publication.
	 *
	 * @param sess Session with authorization
	 * @param publication Publication to create
	 * @return Created publication with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	Publication createPublication(PerunSession sess, Publication publication) throws InternalErrorException;

	/**
	 * Update existing publication by its ID.
	 *
	 * @param sess Session with authorization
	 * @param publication Publication to update
	 * @return Updated publication by its ID
	 * @throws CabinetException When publication already exists
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException;

	/**
	 * Delete publication by its ID.
	 *
	 * @param publication Publication to delete
	 * @throws CabinetException When publication not exists
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	public void deletePublication(Publication publication) throws CabinetException, InternalErrorException;

	/**
	 * Return Publication by its ID.
	 *
	 * @param id ID of Publication
	 * @return Publication by its ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public Publication getPublicationById(int id) throws CabinetException, InternalErrorException;

	/**
	 * Return Publication by its External ID and PublicationSystem ID.
	 *
	 * @param externalId ID of Publication in external system
	 * @param publicationSystem ID of external Publication System
	 * @return Publication by its External ID and PublicationSystem ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException, InternalErrorException;

	/**
	 * Return Publications by their Category ID or empty list.
	 *
	 * @param categoryId ID of Publications category
	 * @return Publications by their category ID
	 * @throws InternalErrorException When implementation fails
	 */
	public List<Publication> getPublicationsByCategoryId(int categoryId) throws InternalErrorException;

	/**
	 * Return PublicationForGUI by its ID.
	 *
	 * @param id ID of PublicationForGUI
	 * @return PublicationForGUI by its ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public PublicationForGUI getRichPublicationById(int id) throws CabinetException, InternalErrorException;

	/**
	 * Return Publication by its External ID and PublicationSystem ID.
	 *
	 * @param externalId ID of Publication in external system
	 * @param publicationSystem ID of external Publication System
	 * @return Publication by its External ID and PublicationSystem ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException, InternalErrorException;

	/**
	 * Return PublicationForGUI with every property set directly from DB.
	 * Apply GUI filter when searching.
	 *
	 * id = exact match (used when search for publication of authors)
	 * title = if "like" this substring
	 * year = exact match
	 * isbn = if "like" this substring
	 * category = exact match
	 * userId = exact match if not 0
	 * yearTill = if year <= yearTill
	 * yearSince = if year >= yearSince
	 *
	 * @param p Publication to search for by properties (null if not used)
	 * @param userId ID of User to search publications for
	 * @param yearSince year range
	 * @param yearTill year range
	 * @return publication with everything set
	 */
	List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill) throws InternalErrorException;

	/**
	 * (Un)Lock passed Publications for changes.
	 *
	 * @param lockState TRUE (lock) / FALSE (unlock)
	 * @param publications Publications to (un)lock
	 * @throws InternalErrorException When implementation fails
	 */
	void lockPublications(boolean lockState, List<Publication> publications) throws InternalErrorException;

}
