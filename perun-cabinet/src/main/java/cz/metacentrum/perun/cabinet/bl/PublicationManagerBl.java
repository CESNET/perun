package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.SortParam;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PublicationManagerBl {

	int createPublication(PerunSession sess, Publication p) throws CabinetException, InternalErrorException;

	boolean publicationExists(Publication p);

	List<Publication> findPublicationsByFilter(Publication p);

	/**
	 * Return list of PublicationForGUI with all properties set filtered by params.
	 *
	 * @param p Publication with properties to filter by (exact match of each, using AND between them)
	 * 			'null' or 'new Publication()' when don't want to use it to filter
	 * @param userId filter results also by author of publications (null if not used)
	 * @return filtered list of PublicationForGUI
	 */
	List<PublicationForGUI> findRichPublicationsByFilter(Publication p, Integer userId);

	Publication findPublicationById(Integer publicationId);

	/**
	 * Return PublicationForGUI with all properties set
	 * filtered by publicationId (primary key)
	 *
	 * @param publicationId filter by primary key
	 * @return PublicationForGUI found by ID / NULL if not found
	 */
	PublicationForGUI findRichPublicationById(Integer publicationId);

	List<Publication> findAllPublications();

	/**
	 * Return list of all PublicationForGUI stored in cabinet
	 *
	 * @return list of all PublicationForGUI
	 */
	List<PublicationForGUI> findAllRichPublications();

	List<Publication> findPublicationsByFilter(Publication publication, SortParam sp);

	/**
	 * List of PublicationForGUI filtered by GUI filter =>
	 * @see for details on filter see javadoc of same method in api layer
	 *
	 * @param publication filter by publication params ('null' or 'new Publication()' when not used)
	 * @param userId filter results by author
	 * @param yearSince year range
	 * @param yearTill year range
	 * @return Filtered list of PublicationForGUI
	 */
	List<PublicationForGUI> findRichPublicationsByGUIFilter(Publication publication, Integer userId, int yearSince, int yearTill);

	int getPublicationsCount();

	int updatePublicationById(PerunSession sess, Publication publication) throws CabinetException;

	/**
	 * Delete Publication by provided ID.
	 * Only author of the record or PerunAdmin can do this.
	 *  - Author deletes authorships and thanks from publication.
	 *  - PerunAdmin also delete publication record.
	 *
	 * @param sess session
	 * @param id publicationId
	 * @return number of updated rows (1=ok / 0 not found)
	 * @throws CabinetException when not authorized or constraint
	 */
	int deletePublicationById(PerunSession sess, Integer id) throws CabinetException;

	/**
	 * Lock / Unlock publications by their ids.
	 *
	 * @param sess session to verify as perunadmin
	 * @param lockState true=lock / false=unlock
	 * @param pubs publications to update
	 * @return number of updated rows
	 * @throws CabinetException when not authorized or something is wrong
	 */
	int lockPublications(PerunSession sess, boolean lockState, List<Publication> pubs) throws CabinetException;

}
