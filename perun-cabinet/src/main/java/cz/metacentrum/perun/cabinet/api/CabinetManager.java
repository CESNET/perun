package cz.metacentrum.perun.cabinet.api;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Top-level API for Publication management in Perun.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
public interface CabinetManager {


	// publication systems -------------------------------------


	/**
	 * Create PublicationSystem in Perun
	 *
	 * @param session PerunSession
	 * @param ps PublicationSystem to create
	 * @return PublicationSystem with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws PrivilegeException;

	/**
	 * Update PublicationSystem in Perun (name,type,url,loginNamespace) by its ID.
	 *
	 * @param session PerunSession
	 * @param ps PublicationSystem to update
	 * @return Updated PublicationSystem
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID.
	 * @throws InternalErrorException When implementation fails
	 */
	PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, PrivilegeException;

	/**
	 * Delete PublicationSystem by its ID.
	 *
	 * @param sess PerunSession
	 * @param ps PublicationSystem to be deleted
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID
	 * @throws InternalErrorException When implementation fails
	 */
	void deletePublicationSystem(PerunSession sess, PublicationSystem ps) throws CabinetException, PrivilegeException;

	/**
	 * Get PublicationSystem by its ID.
	 *
	 * @param id ID to get PS by
	 * @return PublicationSystem by its ID.
	 * @throws CabinetException When PublicationSystem doesn't exist by its ID.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemById(int id) throws CabinetException;

	/**
	 * Get PublicationSystem by its name
	 *
	 * @param name Name to get PS by
	 * @return PublicationSystem by its name.
	 * @throws CabinetException When PublicationSystem doesn't exist by its name.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemByName(String name) throws CabinetException;

	/**
	 * Get PublicationSystem by its login-namespace
	 *
	 * @param namespace Login-namespace to get PS by
	 * @return PublicationSystem by its login-namespace.
	 * @throws CabinetException When PublicationSystem doesn't exist by its login-namespace.
	 * @throws InternalErrorException When implementation fails.
	 */
	PublicationSystem getPublicationSystemByNamespace(String namespace) throws CabinetException;

	/**
	 * Get all PublicationSystems in Perun. If none, return empty list.
	 *
	 *
	 * @param session PerunSession with authorization
	 * @return List of all PublicationSystems or empty list.
	 * @throws InternalErrorException When implementation fails
	 */
	List<PublicationSystem> getPublicationSystems(PerunSession session);


	// category -------------------------------------


	/**
	 * Creates new Category for Publications with specified name and rank.
	 *
	 * @param sess PerunSession
	 * @param category new Category object
	 * @return Created Category with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	Category createCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException;

	/**
	 * Updates publications category in Perun. Category to update
	 * is found by ID. When category rank is changed, priorityCoefficient
	 * for all authors of books from this category, is recalculated.
	 *
	 * @param sess PerunSession
	 * @param category Category to update to
	 * @return Updated category
	 * @throws CabinetException When Category doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Category updateCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException;

	/**
	 * Delete category by its ID. If category contains any publications,
	 * it can't be deleted.
	 *
	 * @param sess PerunSession
	 * @param category Category to be deleted
	 * @throws CabinetException When Category doesn't exists or has publications
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException;

	/**
	 * Return list of all Categories in Perun or empty list of none present.
	 *
	 * @return List of all categories
	 * @throws InternalErrorException When implementation fails
	 */
	List<Category> getCategories();

	/**
	 * Get Category by its ID. Throws exception, if not exists.
	 *
	 * @param id ID of category to be found
	 * @return Category by its ID.
	 * @throws CabinetException When Category doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Category getCategoryById(int id) throws CabinetException;


	// thanks ---------------------------------------


	/**
	 * Creates new Thanks for Publication
	 *
	 * @param sess PerunSession
	 * @param thanks new Thanks object
	 * @return Created Thanks with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	Thanks createThanks(PerunSession sess, Thanks thanks) throws CabinetException, PrivilegeException;

	/**
	 * Delete Thanks by its ID.
	 *
	 * @param sess PerunSession
	 * @param thanks Thanks to be deleted
	 * @throws CabinetException When Thanks doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteThanks(PerunSession sess, Thanks thanks) throws CabinetException, PrivilegeException;

	/**
	 * Check if same Thanks exists by ID or OwnerId,PublicationId combination.
	 *
	 * @param thanks Thanks to check by
	 * @return TRUE = Thanks for same Owner and Publication or with same ID exists / FALSE = Same Thanks not found
	 * @throws InternalErrorException When implementation fails
	 */
	boolean thanksExist(Thanks thanks);

	/**
	 * Get Thanks by its ID. Throws exception, if not exists.
	 *
	 * @param id ID of Thanks to be found
	 * @return Thanks by its ID.
	 * @throws CabinetException When Thanks doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Thanks getThanksById(int id) throws CabinetException;

	/**
	 * Get Thanks of Publication specified by its ID or empty list.
	 *
	 * @param publicationId ID of Publication to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When Publication by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<Thanks> getThanksByPublicationId(int publicationId) throws CabinetException;

	/**
	 * Get ThanksForGUI of Publication specified by its ID or empty list.
	 *
	 * @param publicationId ID of Publication to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When Publication by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws CabinetException;

	/**
	 * Get ThanksForGUI of User specified by its ID or empty list.
	 *
	 * @param userId ID of User to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When User by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<ThanksForGUI> getRichThanksByUserId(int userId) throws CabinetException;


	// Authorships ------------------------------------------


	/**
	 * Creates Authorship. Everything except current date must be already set in Authorship object.
	 * Authorship is checked for existence before creation.
	 * When authorship is successfully created, users priority coefficient is updated.
	 *
	 * @param sess PerunSession
	 * @param authorship Authorship to be created
	 * @return Created authorship
	 * @throws CabinetException When authorship already exists or other exception occurs
	 * @throws InternalErrorException When implementation fails
	 */
	Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException;

	/**
	 * Resolves whether given authorship exists. Authorship is assumed to exists
	 * if: a/ id property is provided and this authorship with the id is in db.
	 * or b/ if property publicationId and userId are set in some authorship in
	 * db. otherwise returns false
	 *
	 * @param authorship Authorship to compare
	 * @return TRUE if authorship exists / FALSE otherwise
	 * @throws InternalErrorException When implementation fails
	 */
	boolean authorshipExists(Authorship authorship);

	/**
	 * Delete Authorship by its ID. After deletion users "priorityCoefficient" is recalculated.
	 *
	 * @param sess PerunSession
	 * @param authorship Authorship to delete by its ID
	 * @throws PrivilegeException When you don't have right to delete Authorship
	 * @throws CabinetException When Authorship by ID doesn't exist
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, PrivilegeException;

	/**
	 * Get Authorship by its ID
	 *
	 * @param id ID to get Authorship by
	 * @return Authorship by its ID
	 * @throws CabinetException When Authorship by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Authorship getAuthorshipById(int id) throws CabinetException;

	/**
	 * Get Authorships by its User ID or empty list.
	 *
	 * @param id ID of user to get Authorships for
	 * @return Authorship by its user ID or empty list
	 * @throws InternalErrorException When implementation fails
	 */
	List<Authorship> getAuthorshipsByUserId(int id);

	/**
	 * Get Authorships by its Publication ID or empty list.
	 *
	 * @param id ID of publication to get Authorships for
	 * @return Authorship by its publication ID or empty list
	 * @throws InternalErrorException When implementation fails
	 */
	List<Authorship> getAuthorshipsByPublicationId(int id);

	/**
	 * Get Authorship by its user and publication IDs
	 *
	 * @param userId ID of User to get Authorship by
	 * @param publicationId ID of Publication to get Authorship by
	 * @return Authorship by its user and publication IDs
	 * @throws CabinetException When Authorship by IDs doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException;

	/**
	 * Gets overall rank of given user as sum of all his publications Authorships.
	 *
	 * @param userId ID of user to get Rank for
	 * @return Total rank of user or 1.0 if user has no Authorships yet (default rank).
	 */
	double getRank(int userId) throws CabinetException;

	/**
	 * Return Author by its ID. If user is not author of any Publication, exception is thrown.
	 *
	 * @param id ID of Author to get
	 * @return Author by its ID.
	 * @throws CabinetException When Author (User) has no Publications
	 * @throws InternalErrorException When implementation fails
	 */
	Author getAuthorById(int id) throws CabinetException;

	/**
	 * Return all Authors of Publications. Empty list of none found.
	 *
	 * @param session PerunSession
	 * @return List of all Authors of Publications. Empty list of none found.
	 * @throws PrivilegeException 
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAllAuthors(PerunSession session) throws PrivilegeException;

	/**
	 * Return all Authors of Publication specified by its ID. Empty list of none found.
	 *
	 * @param session PerunSession for authz
	 * @param id ID of Publication to look by
	 * @return List of Authors of Publication specified its ID. Empty list of none found.
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAuthorsByPublicationId(PerunSession session, int id) throws PrivilegeException, CabinetException;

	/**
	 * Return all Authors of Publication specified by Authorships ID. Empty list of none found.
	 *
	 * @param sess PerunSession for authz
	 * @param id ID of Authorship to look by
	 * @return List of Authors of Publication specified by Authorships ID. Empty list of none found.
	 * @throws CabinetException When authorship doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException;

	/**
	 * Search through all users of Perun in order to allow publication author to add colleagues as co-authors.
	 * Response data are filtered, so only sub-set of users personal information is provided.
	 *
	 * @param sess  PerunSession for authz
	 * @param searchString String to search users by
	 * @return List of new possible authors
	 * @throws PrivilegeException 
	 * @throws CabinetException 
	 * @throws InternalErrorException
	 */
	List<Author> findNewAuthors(PerunSession sess, String searchString) throws PrivilegeException, CabinetException;


	// Publications ----------------------------------


	/**
	 * Create Publication.
	 *
	 * @param sess Session with authorization
	 * @param publication Publication to create
	 * @return Created publication with ID set
	 * @throws CabinetException When same publication already exist
	 * @throws InternalErrorException When implementation fails
	 */
	Publication createPublication(PerunSession sess, Publication publication) throws CabinetException;

	/**
	 * Return TRUE if Publication exists by ID or EXT_ID and PUB_SYS_ID, FALSE otherwise.
	 *
	 * @param publication Publication to check for existence
	 * @return TRUE if same Publication exists (by its ID, EXT_ID,PUB_SYS_ID), FALSE otherwise.
	 * @throws InternalErrorException When implementation fails
	 */
	boolean publicationExists(Publication publication);

	/**
	 * Update existing publication by its ID.
	 *
	 * @param sess Session with authorization
	 * @param publication Publication to update
	 * @return Updated publication by its ID
	 * @throws CabinetException When publication already exists
	 * @throws InternalErrorException When implementation fails
	 */
	public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException, PrivilegeException;

	/**
	 * Delete publication by its ID.
	 * Only author of the record or CabinetAdmin can do this.
	 *  - Author deletes authorships and thanks from publication.
	 *  - CabinetAdmin also delete publication record.
	 *
	 * @param sess PerunSession for authz
	 * @param publication Publication to delete
	 * @throws CabinetException When publication not exists
	 * @throws InternalErrorException When implementation fails
	 */
	public void deletePublication(PerunSession sess, Publication publication) throws CabinetException, PrivilegeException;

	/**
	 * Return Publication by its ID.
	 *
	 * @param id ID of Publication
	 * @return Publication by its ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public Publication getPublicationById(int id) throws CabinetException;

	/**
	 * Return Publication by its External ID and PublicationSystem ID.
	 *
	 * @param externalId ID of Publication in external system
	 * @param publicationSystem ID of external Publication System
	 * @return Publication by its External ID and PublicationSystem ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException;

	/**
	 * Return Publications by their Category ID or empty list.
	 *
	 * @param categoryId ID of Publications category
	 * @return Publications by their category ID
	 * @throws InternalErrorException When implementation fails
	 */
	public List<Publication> getPublicationsByCategoryId(int categoryId);

	/**
	 * Return Publications of author (optionally limited by years range). Empty list if nothing is found.
	 *
	 * @param userId ID of Author (User) to get publications for
	 * @param yearSince to filter results if > 0
	 * @param yearTill to filter results if > 0
	 * @return Publications by their category ID
	 * @throws InternalErrorException When implementation fails
	 */
	public List<Publication> getPublicationsByFilter(int userId, int yearSince, int yearTill);

	/**
	 * Return PublicationForGUI by its ID.
	 *
	 * @param id ID of PublicationForGUI
	 * @return PublicationForGUI by its ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public PublicationForGUI getRichPublicationById(int id) throws CabinetException;

	/**
	 * Return Publication by its External ID and PublicationSystem ID.
	 *
	 * @param externalId ID of Publication in external system
	 * @param publicationSystem ID of external Publication System
	 * @return Publication by its External ID and PublicationSystem ID
	 * @throws CabinetException When such Publication doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException;

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
	List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill);

	/**
	 * (Un)Lock passed Publications for changes.
	 *
	 * @param sess PerunSession for authz
	 * @param lockState TRUE (lock) / FALSE (unlock)
	 * @param publications Publications to (un)lock
	 * @throws InternalErrorException When implementation fails
	 */
	void lockPublications(PerunSession sess, boolean lockState, List<Publication> publications) throws PrivilegeException;

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
	List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException;

}
