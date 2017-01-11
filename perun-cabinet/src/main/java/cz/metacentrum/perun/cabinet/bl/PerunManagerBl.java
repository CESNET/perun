package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Encapsulates the Perun itself.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface PerunManagerBl {

	/**
	 * Returns all Owners from Perun
	 *
	 * @param sess session
	 * @return list of Owners
	 * @throws CabinetException
	 */
	List<Owner> findAllOwners(PerunSession sess) throws CabinetException ;

	/**
	 * Finds owner by id in Perun
	 *
	 * @param sess session
	 * @param id id of owner
	 * @return owner founded by id
	 * @throws CabinetException
	 */
	Owner findOwnerById(PerunSession sess, Integer id) throws CabinetException ;

	/**
	 * Finds user in Perun according userId
	 *
	 * @param sess session
	 * @param userId ID of user
	 * @return user
	 * @throws CabinetException
	 */
	User findUserById(PerunSession sess, Integer userId) throws CabinetException ;

	/**
	 * Finds all users(authors) in Perun with filled logins
	 *
	 * @param sess session
	 * @return list of users
	 * @throws CabinetException
	 */
	List<User> findAllUsers(PerunSession sess) throws CabinetException ;

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


	/**
	 * Return list of attributes with user's logins
	 *
	 * @param sess session
	 * @param user user to get logins for
	 * @return list of user ext sources
	 * @throws CabinetException
	 */
	public List<UserExtSource> getUsersLogins(PerunSession sess, User user) throws CabinetException;

	/**
	 * Return count of users in Perun
	 *
	 * @param sess session
	 * @return count of users in perun
	 * @throws CabinetException
	 */
	public int getUsersCount(PerunSession sess) throws CabinetException;
	// FIXME - should use direct count() in DB

}
