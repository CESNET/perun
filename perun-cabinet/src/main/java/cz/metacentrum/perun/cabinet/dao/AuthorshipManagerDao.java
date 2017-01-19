package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface of DAO layer for handling Authorship entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface AuthorshipManagerDao {

	/**
	 * Creates new Authorship for Publication and User
	 *
	 * @param sess PerunSession
	 * @param authorship new Category object
	 * @return Created Authorship with ID set
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
	Authorship createAuthorship(PerunSession sess, Authorship authorship) throws InternalErrorException;

	/**
	 * Delete Authorship by its ID. After deletion users "priorityCoefficient" is recalculated.
	 *
	 * @param sess PerunSession
	 * @param authorship Authorship to delete by its ID
	 * @throws CabinetException When Authorship by ID doesn't exist
	 * @throws InternalErrorException When implementation fails
	 */
	@Transactional
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

}
