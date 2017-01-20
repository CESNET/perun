package cz.metacentrum.perun.cabinet.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface for Perun-Cabinet API. Use this for access all features of this library.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface CabinetApi extends Serializable {

	/**
	 * Finds publications of perun's user specified in param
	 * Search is done in external publication systems (MU, ZCU)
	 * All parameters are required.
	 *
	 * @param sess
	 * @param userId from Perun
	 * @param yearSince
	 * @param yearTill (must be equal or greater then yearSince)
	 * @param pubSysNamespace (MU or ZCU)
	 * @throws CabinetException
	 *
	 * @return list of publications or empty list if nothing is found
	 * @throws CabinetException
	 */
	List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException, InternalErrorException;

	/**
	 * Finds owners in Perun.
	 *
	 * @return list of owners or empty list if none is found
	 * @throws CabinetException
	 */
	List<Owner> findAllOwners(PerunSession sess) throws CabinetException;

	/**
	 * Resolves whether publication exists or not. Publication exists if a/
	 * property id is provided and publication with this id is in db or b/
	 * property externalId and publicationSystemId exist in db false
	 * otherwise
	 * TODO add checks in createPublication/update and don't allow two equal publications in db, because it makes no sesne!!!
	 *
	 * @param p
	 * @return true if publication exists in db
	 */
	boolean publicationExists(Publication p);

	/**
	 * Finds publications in db according to provided instance. All set
	 * properties are used with conjunction AND.
	 *
	 * @param p
	 * @return list of results or empty list if nothing is found.
	 */
	List<Publication> findPublicationsByFilter(Publication p);

	List<PublicationForGUI> findRichPublicationsByFilter(Publication p, Integer userId);

	/**
	 * Finds rich publications in Cabinet by GUI filter:
	 *
	 * id = exact match (used when search for publication of authors)
	 * title = if "like" this substring
	 * year = exact match
	 * isbn = if "like" this substring
	 * category = exact match
	 * yearSince = if year >= yearSince
	 * yearTill = if year <= yearTill
	 *
	 * If you don't want to filter by publication params,
	 * pass either 'null' or 'new Publication()' (null is preferred)
	 *
	 * @param p publication to filter by (params as above)
	 * @param userId optional (only for user's publications)
	 * @param yearSince optional year since
	 * @param yearTill optional year till
	 * @return list of results or empty list if nothing is found.
	 */
	List<PublicationForGUI> findRichPublicationsByGUIFilter(Publication p, Integer userId, int yearSince, int yearTill);

	/**
	 * Finds all rich publications in db of given user ID. Returned publications don't
	 * have filled <authors> property. If you desire authors, you have to search
	 * for them manually.
	 *
	 * @param id property userId of author
	 * @return list of author's rich publications or an empty array if hasn't any.
	 * @throws CabinetException
	 */
	List<PublicationForGUI> findRichPublicationsOfAuthor(Integer id) throws CabinetException;

	/**
	 * Finds owner by id in db.
	 *
	 * @param id
	 * @return existing owner or null if not exists.
	 * @throws CabinetException
	 */
	Owner findOwnerById(PerunSession sess, Integer id) throws CabinetException;

	/**
	 * Finds authors by given authorshipId. It finds all authors who reported
	 * the publication related to given authorship (publicationId of given
	 * authorship).
	 *
	 * @param id authorshipId
	 * @return list of authors
	 * @throws CabinetException
	 */
	List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException, InternalErrorException;

	/**
	 * Finds publication in db by it's id. (Not by it's externalId).
	 *
	 * @param publicationId
	 * @return founded publication or null if nothing is found
	 */
	Publication findPublicationById(Integer publicationId);

	/**
	 * Finds rich publication in db by it's id. (Not by it's externalId).
	 *
	 * @param publicationId
	 * @return founded publication or null if nothing is found
	 */
	PublicationForGUI findRichPublicationById(Integer publicationId);

	/**
	 * Finds all publications in db.
	 *
	 * @return
	 */
	List<Publication> findAllPublications();

	/**
	 * Finds all rich publications in db (with category and pub. sys. property filled)
	 *
	 * @return all rich publications
	 */
	List<PublicationForGUI> findAllRichPublications() throws CabinetException;

	List<Publication> findPublicationsByFilter(Publication publication, SortParam sp);

	int getPublicationsCount();

	int updatePublicationById(PerunSession sess, Publication modelObject) throws CabinetException, InternalErrorException;

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
