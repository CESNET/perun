package cz.metacentrum.perun.cabinet.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Interface for Perun-Cabinet API. Use this for access all features of this library.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface ICabinetApi extends Serializable {

	/**
	 * Finds publications of perun's user specified in param
	 * Search is done in external publication systems (MU, ZCU)
	 * All parameters are required.
	 *
	 * @param user from Perun
	 * @param yearSince
	 * @param yearTill (must be equal or greater then yearSince)
	 * @param pubSysNamespace (MU or ZCU)
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 *
	 * @return list of publications or empty list if nothing is found
	 * @throws CabinetException
	 */
	List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException;

	/**
	 * Finds owners in Perun.
	 *
	 * @return list of owners or empty list if none is found
	 * @throws CabinetException
	 */
	List<Owner> findAllOwners(PerunSession sess) throws CabinetException;

	/**
	 * Saves publication. Keep in mind that properties title, year, categoryId, createdBy are obligatory. Also keep in mind, that authors property is irrevelant in this method.
	 *
	 * @param sess PerunSession
	 * @param p publication
	 * @return id of new publication
	 * @throws CabinetException
	 */
	int createPublication(PerunSession sess, Publication p) throws CabinetException;

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
	 * Checks whether author exists. Author exists iff author with equal userId
	 * is in perun.
	 *
	 * @param a
	 * @return true if author with equal property userId is in db. otherwise false
	 */
	boolean authorExists(Author a);

	/**
	 * Saves authorship to db.
	 *
	 * @param a
	 * @return id of created authorship.
	 * @throws CabinetException
	 */
	int createAuthorship(PerunSession sess, Authorship a) throws CabinetException;

	/**
	 * Finds all categories in db.
	 *
	 * @return list of categories or empty list if none exist
	 */
	List<Category> findAllCategories();

	/**
	 * Resolves whether given authorship exists. Authorship is assumed to exists
	 * if: a/ id property is provided and this authorship with the id is in db.
	 * or b/ if property publicationId and userId are set in some authorship in
	 * db. otherwise returns false
	 *
	 * @param authorship
	 * @return true if authorship exists
	 */
	boolean authorshipExists(Authorship authorship);

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
	 * Checks whether thanks exists in db. Thanks is supposed to exist if: a/ id
	 * property is filled and record with given id is in db b/ ownerId and
	 * reportId properties are filled and given record is in db.
	 *
	 * @param t
	 * @return true if exists, false otherwise
	 */
	boolean thanksExists(Thanks t);

	/**
	 * creates thanks entity. Dependent fields (i.e. reportId) must already
	 * exist in db. If property createdDate is null, current timestamp will be
	 * used.
	 *
	 * @param sess
	 * @param t
	 * @return id of created thanks
	 */
	int createThanks(PerunSession sess, Thanks t) throws CabinetException;

	/**
	 * Finds authorships according to filter. Between properties AND conjunction
	 * is used. Page and size are for paging. If you search all records, you
	 * provide empty (but not null) authorship.
	 *
	 * @param authorship
	 * @param sortParam
	 *            might be null or some properties might be null (then they will
	 *            be ignored), however it is not safe(exception can raise)
	 * @return list of selected authorships or empty list if nothing is found.
	 */
	List<Authorship> findAuthorshipsByFilter(Authorship a, SortParam sortParam);

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
	 * Finds thanks by filter. Between properties AND conjunction is used.
	 *
	 * @param t
	 * @return list of thanks or empty list if nothing is found.
	 */
	List<Thanks> findThanksByFilter(Thanks t);

	/**
	 * Find thanks related to selected publication
	 *
	 * @param id ID of selected publication
	 * @return thanks related to publication
	 */
	List<Thanks> findThanksByPublicationId(int id);

	/**
	 * Find rich thanks related to selected publication
	 *
	 * @param id ID of publication
	 * @return list of rich thanks
	 */
	List<ThanksForGUI> findRichThanksByPublicationId(int id);

	/**
	 * Return thanks by it's ID property
	 *
	 * @param id
	 * @return thanks
	 */
	Thanks findThanksById(int id);

	/**
	 * Finds category by id in db.
	 *
	 * @param categoryId
	 * @return existing category or null if nothing is found.
	 */
	Category findCategoryById(Integer categoryId);

	/**
	 * Gets the overall rank of given user as sum of all his publications'
	 * authorships.
	 *
	 * @param userId
	 * @return total rank of user or 1.0 if user has no reports (=authorship)
	 *         yet (default rank).
	 */
	Double getRank(Integer userId);

	/**
	 * Gets date of last user's created (last created authorship of his
	 * publication) authorship (when he did it himself). Note, that this is done
	 * via createdBy property, so another user might create authorship for given
	 * author with later date. If there is no authorship from this user, null is
	 * returned.
	 *
	 * @param userId
	 * @return
	 */
	Date getLastReportDate(Integer userId);

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
	List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException;

	/**
	 * Finds all authors of publication by given publicationId
	 * (users who reported publication)
	 *
	 * @param id id of publication
	 * @return list of authors
	 */
	List<Author> findAuthorsByPublicationId(Integer id);

	/**
	 * Finds all authorships stored in db. If none exists, empty list is
	 * returned.
	 *
	 * @return
	 */
	List<Authorship> findAllAuthorships();

	/**
	 * Returns a count of authorships stored in db.
	 *
	 * @return
	 */
	int getAuthorshipsCount();

	/**
	 * Finds records in db according to filter. Between filled properties is
	 * used conjunction AND. If none result matches, empty array is returned.
	 *
	 * @param authorship
	 * @return
	 */
	List<Authorship> findAuthorshipsByFilter(Authorship authorship);

	/**
	 * Finds author by his userId
	 *
	 * @param userId
	 * @return
	 */
	Author findAuthorById(Integer userId);

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
	 * Finds authorship in db.
	 *
	 * @param id
	 * @return authorship or null if record with given id does not exist in db
	 */
	Authorship findAuthorshipById(Integer id);

	/**
	 * Updates authorship
	 * @param a
	 * @return
	 * @throws CabinetException
	 */
	int updateAuthorship(PerunSession sess, Authorship a) throws CabinetException;

	/**
	 * Deletes authorship by it's id.
	 *
	 * @param id
	 * @return
	 * @throws CabinetException
	 */
	int deleteAuthorshipById(PerunSession sess, Integer id) throws CabinetException;

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

	PublicationSystem findPublicationSystemById(Integer publicationSystemId);

	int updatePublicationById(PerunSession sess, Publication modelObject) throws CabinetException;

	int deletePublicationById(PerunSession sess, Integer id) throws CabinetException;

	int getCategoriesCount();

	/**
	 * Updates category according it's id.
	 *
	 * @param session
	 * @param modelObject
	 * @return number of updated rows in db
	 * @throws CabinetException
	 */
	int updateCategoryById(PerunSession sess, Category category) throws CabinetException;

	/**
	 * Deletes category by it's id.
	 *
	 * @param id
	 * @return number of deleted categories
	 */
	int deleteCategoryById(Integer id);

	/**
	 * Creates a new category in db.
	 *
	 * @param modelObject
	 * @return id of new category
	 */
	int createCategory(Category modelObject);

	/**
	 * Finds all authors in db. Note that Author is kind of User and exists, if
	 * the user reports publication. Then author is holder of additional
	 * information about user and can search in perun after user by author's
	 * property userId.
	 *
	 * @return list of founded authors
	 */
	List<Author> findAllAuthors();

	/**
	 * Gets number of authors in cabinet.
	 *
	 * @return count of authors in perun
	 */
	int getAuthorsCount();

	/**
	 * Deletes thanks by its id.
	 *
	 * @param id
	 * @return number of deleted lines in db
	 * @throws CabinetException
	 */
	int deleteThanksById(PerunSession sess, Integer id) throws CabinetException;

	/**
	 * Finds all publication systems in cabinet db.
	 *
	 * @return list of publication systems
	 */
	List<PublicationSystem> findAllPublicationSystems();

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

	/**
	 * Recalculates "publications" attribute for
	 * all users who reported any publication
	 *
	 * @param sess
	 * @throws CabinetException
	 */
	void recalculateThanksAttribute(PerunSession sess) throws CabinetException;

}
