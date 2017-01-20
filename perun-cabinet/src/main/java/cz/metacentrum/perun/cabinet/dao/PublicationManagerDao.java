package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.bl.SortParam;
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
	 * @throws CabinetException When publication already exists
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	Publication createPublication(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException;

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
	 * @param publication Publication to create
	 * @throws CabinetException When publication not exists
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	public void deletePublication(Publication publication) throws CabinetException, InternalErrorException;

	/**
	 * Filter result by exact match in any filed (and operator is used between fields)
	 *
	 * @param p publication
	 * @return list of publications by filter
	 */
	List<Publication> findPublicationsByFilter(Publication p);

	Publication findPublicationById(Integer publicationId);

	/**
	 * Return PublicationForGUI with every property set directly from DB.
	 * Apply GUI filter when searching.
	 *
	 * id = exact match (used when search for publication of authors)
	 * title = if "like" this substring
	 * year = exact match
	 * isbn = if "like" this substring
	 * category = exact match
	 * yearTill = if year <= yearTill
	 * yearSince = if year >= yearSince
	 *
	 * @param p Publication to search for by properties (null if not used)
	 * @param yearSince year range
	 * @param yearTill year range
	 * @return publication with everything set
	 */
	List<PublicationForGUI> findRichPublicationsByGUIFilter(Publication p, Integer userId, int yearSince, int yearTill);

	/**
	 * Return PublicationForGUI with every property set directly from DB
	 *
	 * @param publicationId ID of publication to search for
	 * @return publication
	 */
	PublicationForGUI findRichPublicationById(Integer publicationId);

	/**
	 * Return list of PublicationForGUI with every property set directly from DB
	 *
	 * @param p publication with params to filter by (null if not used)
	 * @param userId filter results by userId (aka author)
	 * @return list of filtered publications
	 */
	List<PublicationForGUI> findRichPublicationsByFilter(Publication p, Integer userId);

	// NOTE - all rich publications can be retrieved like:
	// findRichPublicationsByGUIFilter(null, null, 0, 0);
	List<Publication> findAllPublications();

	List<Publication> findPublicationsByFilter(Publication publication, SortParam sp);

	int getPublicationsCount();

	int lockPublications(boolean lockState, List<Publication> pubs);

}
