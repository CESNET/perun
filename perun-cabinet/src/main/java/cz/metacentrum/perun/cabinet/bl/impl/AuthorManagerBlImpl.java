package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.dao.AuthorshipManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.bl.AuthorManagerBl;

/**
 * Class for handling Authors in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AuthorManagerBlImpl implements AuthorManagerBl {

	private AuthorshipManagerDao authorshipManagerDao;

	// setter methods ==================================

	public void setAuthorshipManagerDao(AuthorshipManagerDao authorshipManagerDao) {
		this.authorshipManagerDao = authorshipManagerDao;
	}

	// service methods ==================================


	public boolean authorExists(Author a) {
		return authorshipManagerDao.findAuthorByUserId(a.getId()) != null;
	}

	public Author findAuthorByUserId(Integer userId) {
		return authorshipManagerDao.findAuthorByUserId(userId);
	}

	public List<Author> findAllAuthors() {
		return authorshipManagerDao.findAllAuthors();
	}

	public int getAuthorsCount() {
		List<Integer> allAuthors = authorshipManagerDao.findUniqueAuthorsIds();
		return allAuthors.size();
	}

	public List<Author> findAuthorsByPublicationId(Integer id) {
		return authorshipManagerDao.findAuthorsByPublicationId(id);
	}

	public List<Integer> findUniqueAuthorsIds() {
		return authorshipManagerDao.findUniqueAuthorsIds();
	}

}
