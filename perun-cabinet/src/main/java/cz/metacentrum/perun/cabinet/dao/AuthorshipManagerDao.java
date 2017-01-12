package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.bl.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;
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
