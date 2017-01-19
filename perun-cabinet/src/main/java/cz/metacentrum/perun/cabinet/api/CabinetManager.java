package cz.metacentrum.perun.cabinet.api;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
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
	PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws InternalErrorException, PrivilegeException;

	/**
	 * Update PublicationSystem in Perun (name,type,url,loginNamespace) by its ID.
	 *
	 * @param session PerunSession
	 * @param ps PublicationSystem to update
	 * @return Updated PublicationSystem
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID.
	 * @throws InternalErrorException When implementation fails
	 */
	PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, InternalErrorException, PrivilegeException;

	/**
	 * Delete PublicationSystem by its ID.
	 *
	 * @param sess PerunSession
	 * @param ps PublicationSystem to be deleted
	 * @throws CabinetException When PublicationSystem doesn't exists by its ID
	 * @throws InternalErrorException When implementation fails
	 */
	void deletePublicationSystem(PerunSession sess, PublicationSystem ps) throws CabinetException, InternalErrorException, PrivilegeException;

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


	// category -------------------------------------


	/**
	 * Creates new Category for Publications with specified name and rank.
	 *
	 * @param sess PerunSession
	 * @param category new Category object
	 * @return Created Category with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	Category createCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException;

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
	Category updateCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException;

	/**
	 * Delete category by its ID. If category contains any publications,
	 * it can't be deleted.
	 *
	 * @param sess PerunSession
	 * @param category Category to be deleted
	 * @throws CabinetException When Category doesn't exists or has publications
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException;

	/**
	 * Return list of all Categories in Perun or empty list of none present.
	 *
	 * @return List of all categories
	 * @throws InternalErrorException When implementation fails
	 */
	List<Category> getCategories() throws InternalErrorException;

	/**
	 * Get Category by its ID. Throws exception, if not exists.
	 *
	 * @param id ID of category to be found
	 * @return Category by its ID.
	 * @throws CabinetException When Category doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Category getCategoryById(int id) throws CabinetException, InternalErrorException;


	// thanks ---------------------------------------


	/**
	 * Creates new Thanks for Publication
	 *
	 * @param sess PerunSession
	 * @param thanks new Thanks object
	 * @return Created Thanks with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	Thanks createThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException, PrivilegeException;

	/**
	 * Delete Thanks by its ID.
	 *
	 * @param sess PerunSession
	 * @param thanks Thanks to be deleted
	 * @throws CabinetException When Thanks doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException, PrivilegeException;

	/**
	 * Check if same Thanks exists by ID or OwnerId,PublicationId combination.
	 *
	 * @param thanks Thanks to check by
	 * @return TRUE = Thanks for same Owner and Publication or with same ID exists / FALSE = Same Thanks not found
	 * @throws InternalErrorException When implementation fails
	 */
	boolean thanksExist(Thanks thanks) throws InternalErrorException;

	/**
	 * Get Thanks by its ID. Throws exception, if not exists.
	 *
	 * @param id ID of Thanks to be found
	 * @return Thanks by its ID.
	 * @throws CabinetException When Thanks doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Thanks getThanksById(int id) throws CabinetException, InternalErrorException;

	/**
	 * Get Thanks of Publication specified by its ID or empty list.
	 *
	 * @param publicationId ID of Publication to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When Publication by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<Thanks> getThanksByPublicationId(int publicationId) throws CabinetException, InternalErrorException;

	/**
	 * Get ThanksForGUI of Publication specified by its ID or empty list.
	 *
	 * @param publicationId ID of Publication to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When Publication by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws CabinetException, InternalErrorException;

	/**
	 * Get ThanksForGUI of User specified by its ID or empty list.
	 *
	 * @param userId ID of User to get Thanks for
	 * @return List of Publications Thanks
	 * @throws CabinetException When User by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<ThanksForGUI> getRichThanksByUserId(int userId) throws CabinetException, InternalErrorException;


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
	Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException;

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
	boolean authorshipExists(Authorship authorship) throws InternalErrorException;

	/**
	 * Delete Authorship by its ID. After deletion users "priorityCoefficient" is recalculated.
	 *
	 * @param sess PerunSession
	 * @param authorship Authorship to delete by its ID
	 * @throws PrivilegeException When you don't have right to delete Authorship
	 * @throws CabinetException When Authorship by ID doesn't exist
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException, PrivilegeException;

	/**
	 * Get Authorship by its ID
	 *
	 * @param id ID to get Authorship by
	 * @return Authorship by its ID
	 * @throws CabinetException When Authorship by ID doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Authorship getAuthorshipById(int id) throws CabinetException, InternalErrorException;

	/**
	 * Get Authorships by its User ID or empty list.
	 *
	 * @param id ID of user to get Authorships for
	 * @return Authorship by its user ID or empty list
	 * @throws InternalErrorException When implementation fails
	 */
	List<Authorship> getAuthorshipsByUserId(int id) throws InternalErrorException;

	/**
	 * Get Authorships by its Publication ID or empty list.
	 *
	 * @param id ID of publication to get Authorships for
	 * @return Authorship by its publication ID or empty list
	 * @throws InternalErrorException When implementation fails
	 */
	List<Authorship> getAuthorshipsByPublicationId(int id) throws InternalErrorException;

	/**
	 * Get Authorship by its user and publication IDs
	 *
	 * @param userId ID of User to get Authorship by
	 * @param publicationId ID of Publication to get Authorship by
	 * @return Authorship by its user and publication IDs
	 * @throws CabinetException When Authorship by IDs doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException, InternalErrorException;

	/**
	 * Gets overall rank of given user as sum of all his publications Authorships.
	 *
	 * @param userId ID of user to get Rank for
	 * @return Total rank of user or 1.0 if user has no Authorships yet (default rank).
	 */
	double getRank(int userId) throws InternalErrorException, CabinetException;

	/**
	 * Return Author by its ID. If user is not author of any Publication, exception is thrown.
	 *
	 * @param id ID of Author to get
	 * @return Author by its ID.
	 * @throws CabinetException When Author (User) has no Publications
	 * @throws InternalErrorException When implementation fails
	 */
	Author getAuthorById(int id) throws CabinetException, InternalErrorException;

	/**
	 * Return all Authors of Publications. Empty list of none found.
	 *
	 * @return List of all Authors of Publications. Empty list of none found.
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAllAuthors() throws InternalErrorException;

	/**
	 * Return all Authors of Publication specified by its ID. Empty list of none found.
	 *
	 * @param id ID of Publication to look by
	 * @return List of Authors of Publication specified its ID. Empty list of none found.
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAuthorsByPublicationId(int id) throws InternalErrorException;

	/**
	 * Return all Authors of Publication specified by Authorships ID. Empty list of none found.
	 *
	 * @param sess PerunSession for authz
	 * @param id ID of Authorship to look by
	 * @return List of Authors of Publication specified by Authorships ID. Empty list of none found.
	 * @throws CabinetException When authorship doesn't exists
	 * @throws InternalErrorException When implementation fails
	 */
	List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException, InternalErrorException;

}
