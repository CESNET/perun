package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface which provides Cabinet with ability to search through
 * external PS based on user's identity and PS namespace.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface CabinetManagerBl {

	/**
	 * Search for user's publications based on his identity in Perun.
	 * External PS is chosen by passed namespace. All params are required.
	 *
	 * @param userId User's ID in Perun
	 * @param yearSince Since which year should publications be (filter results)
	 * @param yearTill Until which year should publications be (filter results)
	 * @return List of user's publications in selected external PS
	 * @throws CabinetException
	 */
	List<Publication> findExternalPublicationsOfUser(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException;

	/**
	 * Searches for publications of given authorId in given publication system.
	 * The authorId is an internal id in given PublicationSystem, i.e. UCO in MU
	 * pub.sys. Note that the authorId can be obtained from user's UserExtSource
	 * in Perun and the PS through IPublicationSystemService. All params are required.
	 *
	 * @param authorId User's identification in external PS
	 * @param yearSince Since which year should publications be (filter results)
	 * @param yearTill Until which year should publications be (filter results)
	 * @param ps PublicationSystem to search throught
	 * @return List of user's publications
	 * @throws CabinetException
	 */
	List<Publication> findPublicationsInPubSys(String authorId, int yearSince, int yearTill, PublicationSystem ps) throws CabinetException;

	/**
	 * Updates priority coefficient for User
	 *
	 * @param sess session
	 * @param userId ID of user
	 * @param rank new value
	 * @throws CabinetException
	 */
	void updatePriorityCoefficient(PerunSession sess, Integer userId, Double rank) throws CabinetException;

	/**
	 * Calculate and set new value for user's publications thanks
	 *
	 * @param userId to set thanks to
	 * @throws CabinetException
	 */
	void setThanksAttribute(int userId) throws CabinetException;

}
