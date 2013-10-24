package cz.metacentrum.perun.cabinet.service;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;

/**
 * Interface for handling Author entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public interface IAuthorService {

	/**
	 * Check if author exists in DB (user in Perun)
	 * 
	 * @param a author
	 * @return true if author exists
	 */
	boolean authorExists(Author a);

	/**
	 * Find author by it's userId property from Perun (with logins and authorships property filled)
	 *
	 * @param userId ID of user / author
	 * @return author
	 */
	Author findAuthorByUserId(Integer userId);

	/**
	 * Return all users from Perun as authors (with logins and authorships property filled)
	 * 
	 * @return list of all authors
	 */
	List<Author> findAllAuthors();

	/**
	 * Return count of all authors (users) in Perun
	 * 
	 * @return count of all authors
	 */
	int getAuthorsCount();
	
	List<Author> findAuthorsByPublicationId(Integer id);
	
	List<Integer> findUniqueAuthorsIds();
	
}