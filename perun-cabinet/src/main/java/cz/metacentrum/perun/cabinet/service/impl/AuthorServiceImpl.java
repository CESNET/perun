package cz.metacentrum.perun.cabinet.service.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.dao.IAuthorshipDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.service.IAuthorService;

/**
 * Class for handling Authors in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AuthorServiceImpl implements IAuthorService {

	private IAuthorshipDao authorshipDao;

	// setter methods ==================================

	public void setAuthorshipDao(IAuthorshipDao authorshipDao) {
		this.authorshipDao = authorshipDao;
	}

	// service methods ==================================


	public boolean authorExists(Author a) {
		return authorshipDao.findAuthorByUserId(a.getId()) != null;
	}

	public Author findAuthorByUserId(Integer userId) {
		return authorshipDao.findAuthorByUserId(userId);
	}

	public List<Author> findAllAuthors() {
		return authorshipDao.findAllAuthors();
	}

	public int getAuthorsCount() {
		List<Integer> allAuthors = authorshipDao.findUniqueAuthorsIds();
		return allAuthors.size();
	}

	public List<Author> findAuthorsByPublicationId(Integer id) {
		return authorshipDao.findAuthorsByPublicationId(id);
	}

	public List<Integer> findUniqueAuthorsIds() {
		return authorshipDao.findUniqueAuthorsIds();
	}

}