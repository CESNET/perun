package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.service.SortParam;

/**
 * Interface of DAO layer for handling Authorship entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @version $Id$
 */
public interface IAuthorshipDao {

	int create(Authorship r);

	List<Authorship> findByFilter(Authorship filter, SortParam sortParam);

	Authorship findById(Integer id);

	Authorship findLastestOfUser(Integer userId);

	List<Authorship> findAll();

	int getCount();

	int getCountForUser(Integer userId);

	List<Authorship> findByFilter(Authorship filter);

	List<Authorship> findByPublicationId(Integer id);

	List<Authorship> findByUserId(Integer id);

	int update(Authorship report);

	int deleteById(Integer id);
	
	List<Integer> findUniqueAuthorsIds();
	
	Author findAuthorByUserId(Integer id);
	
	List<Author> findAuthorsByPublicationId(Integer id);
	
	List<Author> findAllAuthors();

}