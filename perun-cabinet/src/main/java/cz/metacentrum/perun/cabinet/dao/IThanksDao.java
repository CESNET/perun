package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;

/**
 * Interface of DAO layer for handling Thanks entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface IThanksDao {

	int createThanks(Thanks t);

	List<Thanks> findThanksByFilter(Thanks t);

	Thanks findThanksById(Integer id);

	int deleteThanksById(Integer id);

	List<Thanks> findThanksByPublicationId(Integer id);

	List<ThanksForGUI> findRichThanksByPublicationId(Integer id);

	List<ThanksForGUI> findAllRichThanksByUserId(Integer id);

}
