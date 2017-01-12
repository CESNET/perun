package cz.metacentrum.perun.cabinet.bl;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.SortParam;
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
	 * Creates authorship entity in Cabinet. Everything except current date must be
	 * already set in Authorship object. Authorship is checked for existence before creation.
	 * When authorship is successfully created, user's priority coeficient is updated.
	 *
	 * @param sess PerunSession
	 * @param a Authorship to be created
	 * @return ID of created authorship
	 * @throws CabinetException When authorship already exists or other exception occurs
	 */
	int createAuthorship(PerunSession sess, Authorship a) throws CabinetException, InternalErrorException;

	/**
	 * Helping function which checks for existence of passed Authorship.
	 * Used when creating new Authorship.
	 * Comparison is preferably based on ID (if set) or on also unique
	 * combination of a.userId and a.publicationId property.
	 *
	 * @param a Authorship to be checked
	 * @return true if exists / false if not
	 */
	boolean authorshipExists(Authorship a);

	/**
	 * Calculates new priorityCoeficient value
	 * based on current state of all user's publications.
	 *
	 * Used function is SUM on category.rank and publication.rank
	 * for each of user's publications.
	 *
	 * @param userId User to calculate new rank for
	 * @return new value for priorityCoeficient
	 */
	Double calculateNewRank(Integer userId) throws CabinetException, InternalErrorException;

	/**
	 * Calculates new priorityCoeficient value based on
	 * passed authorships. Should be called immediately after
	 * any update() so authorship would be "current".
	 * Reason: this function saves 1 DB select
	 *
	 * Used function is SUM on category.rank and publication.rank
	 * for each of user's publications.
	 *
	 * @param authorships authorships to get rank by
	 * @return new value for priorityCoeficient
	 */
	Double calculateNewRank(List<Authorship> authorships) throws InternalErrorException, CabinetException;

	/**
	 * Return date, when was user added as author of his last (newest) publication.
	 *
	 * @param userId User's ID
	 * @return date of last created Authorship
	 */
	Date getLastCreatedAuthorshipDate(Integer userId);

	/**
	 * Return all Authors of publication, which is specified by publicationId of Authorship.
	 * Proper authorship is loaded by it's ID.
	 *
	 * @param sess PerunSession
	 * @param id ID of authorship to look for
	 * @return List of authors of publication specified by authorship. Empty list of none found.
	 * @throws CabinetException When authorship doesn't exists or other exception is thrown
	 */
	List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException;

	/**
	 * Return list of all Authorships in Perun
	 *
	 * @return List of all Authorships
	 */
	List<Authorship> findAllAuthorships();

	/**
	 * Return count of all Authorships in Perun
	 *
	 * @return count of all Authorships
	 */
	int getAuthorshipsCount();

	/**
	 * Return count of user's Authorships in Perun
	 *
	 * @return count of user's Authorships
	 */
	int getAuthorshipsCountForUser(Integer userId);

	/**
	 * Return list of Authorships matched by passed Authorship properties.
	 * Between all set properties AND conjunction is used. Return empty list if
	 * nothing found.
	 *
	 * @param a Authorship with params to search for
	 * @return List of Authorships matching params
	 */
	List<Authorship> findAuthorshipsByFilter(Authorship a);

	/**
	 * Return list of Authorships matched by passed Authorship properties.
	 * Between all set properties AND conjunction is used. Return empty list if
	 * nothing found. Returned list can be sorted by sortParams.
	 *
	 * @param a Authorship with params to search for
	 * @param sortParam Specified way of sorting for result
	 * @return List of Authorships matching params
	 */
	List<Authorship> findAuthorshipsByFilter(Authorship a, SortParam sortParam);

	/**
	 * Return Authorship based on passed authorshipId
	 *
	 * @param id ID of Authorship to look for
	 * @return Founded Authorship or null if not found
	 */
	Authorship findAuthorshipById(Integer id);

	/**
	 * Find all Authorships, which are related to selected publication.
	 * Publication is decided by passed id.
	 *
	 * @param id ID of publication to look Authorships for
	 * @return List of related Authorships
	 */
	List<Authorship> findAuthorshipsByPublicationId(Integer id);

	/**
	 * Find all Authorships, which are related to selected user.
	 * User is decided by passed id.
	 *
	 * @param id ID of user to look Authorships for
	 * @return List of related Authorships
	 */
	List<Authorship> findAuthorshipsByUserId(Integer id);

	/**
	 * Updates existing Authorship in Perun. Proper Authorship is found by ID property.
	 * Other values are updated based on passed Authorship and can't be null.
	 *
	 * Throws exception if authorship not found or already exists
	 * (based on userId and publicationId combination).
	 *
	 * On success, former and new user's priorityCoeficient is recalculated.
	 *
	 * @param sess PerunSession
	 * @param report Authorship to update to
	 * @return Number of updated rows in DB (1 = success / 0 = not found / other = consistency error)
	 * @throws CabinetException
	 */
	int updateAuthorship(PerunSession sess, Authorship report) throws CabinetException, InternalErrorException;

	/**
	 * Delete Authorship from Perun based on passed ID. After success user's
	 * priorityCoeficient is recalculated.
	 *
	 * @param sess PerunSession
	 * @param id ID of Authorship to delete
	 * @return number of deleted rows in DB (1 = success / 0 = not found / other = consistency error)
	 * @throws CabinetException
	 */
	int deleteAuthorshipById(PerunSession sess, Integer id) throws CabinetException, InternalErrorException;

}
