package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface for handling Authorship entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface AuthorshipManagerBl {

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
	 * @throws CabinetException When Authorship by ID doesn't exist
	 * @throws InternalErrorException When implementation fails
	 */
	void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException;

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
	 * Calculates new priorityCoefficient value based on current state of all users publications.
	 *
	 * Used function is SUM on category.rank and publication.rank for each user publication.
	 *
	 * @param userId User to calculate new rank for
	 * @return new value for priorityCoefficient
	 * @see #calculateNewRank(List)
	 * @throws CabinetException When Category or Publication in users Authorships doesn't exists (consistency)
	 * @throws InternalErrorException When implementation fails
	 */
	double calculateNewRank(int userId) throws CabinetException, InternalErrorException;

	/**
	 * Calculates new priorityCoefficient value based on passed Authorships.
	 * This should be called immediately after any update so Authorship would be "current".
	 *
	 * Used function is SUM on category.rank and publication.rank for each of user publications.
	 *
	 * This method is synchronized to make sure concurrent calculations are consistent among publication changes.
	 *
	 * @param authorships Authorships to calculate rank by
	 * @return new value for priorityCoefficient
	 * @throws CabinetException When Category or Publication in Authorships doesn't exists (consistency)
	 * @throws InternalErrorException When implementation fails
	 */
	double calculateNewRank(List<Authorship> authorships) throws InternalErrorException, CabinetException;

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

	/**
	 * Search through all users of Perun in order to allow publication author to add colleagues as co-authors.
	 * Response data are filtered, so only sub-set of users personal information is provided.
	 *
	 * @param sess  PerunSession for authz
	 * @param searchString String to search users by
	 * @return List of new possible authors
	 * @throws CabinetException
	 * @throws InternalErrorException
	 */
	List<Author> findNewAuthors(PerunSession sess, String searchString) throws CabinetException, InternalErrorException;

}
