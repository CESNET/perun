package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Interface for handling Thanks entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface ThanksManagerBl {

	int createThanks(PerunSession sess, Thanks t) throws CabinetException;

	boolean thanksExists(Thanks t);

	List<Thanks> findThanksByFilter(Thanks t);

	int deleteThanksById(PerunSession sess, Integer id) throws CabinetException;

	List<Thanks> findThanksByPublicationId(int id);

	List<ThanksForGUI> findRichThanksByPublicationId(int id);

	Thanks findThanksById(int id);

	List<ThanksForGUI> findAllRichThanksByUserId(Integer id);

}
