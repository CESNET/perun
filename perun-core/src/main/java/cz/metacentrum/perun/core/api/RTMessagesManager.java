package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;

/**
 * RTMessage manager can create a new message and send it to RT like predefined service user.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface RTMessagesManager {

	/**
	 * This method get needed information and by predefined service user send message to RT which try to create new ticket
	 * For basic usage this method is for error msg from users of perun.
	 *
	 * @param sess
	 * @param member member which send message to RT
	 * @param queue in which queue will be message send
	 * @param subject subject of message
	 * @param text text of message
	 * @return RTMessage with information about success or not
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException if the Member not exist in the system
	 */
	@Deprecated
	RTMessage sendMessageToRT(PerunSession sess, Member member, String queue, String subject, String text) throws InternalErrorException, MemberNotExistsException;

	/**
	 * This method get needed information and by predefined service user send message to RT which try to create new ticket
	 * For basic usage this method is for error msg from users of perun.
	 *
	 * queue define concrete queue in RT
	 *
	 * @param sess
	 * @param queue in which queue will be message send
	 * @param subject subject of message
	 * @param text text of message
	 * @return RTMessage with information about success or not
	 * @throws InternalErrorException
	 */
	RTMessage sendMessageToRT(PerunSession sess, String queue, String subject, String text) throws InternalErrorException;

	/**
	 * This method get needed information and by predefined service user send message to RT which try to create new ticket
	 * For basic usage this method is for error msg from users of perun.
	 *
	 * voId is there especially for searching voAttribute rtVoQueue
	 * queue define concrete queue in RT
	 *
	 * @param sess (also to get user from it)
	 * @param voId id of vo, where user is member
	 * @param queue in which queue will be message send
	 * @param subject subject of message
	 * @param text text of message
	 * @return RTMessage with information about success or not
	 * @throws InternalErrorException
	 */
	RTMessage sendMessageToRT(PerunSession sess, int voId, String queue, String subject, String text) throws InternalErrorException;

	/**
	 * This method get needed information and by predefined service user send message to RT which try to create new ticket
	 * For basic usage this method is for error msg from users of perun.
	 *
	 * voId is there especially for searching voAttribute rtVoQueue
	 *
	 * @param sess (also to get user from it)
	 * @param voId id of vo, where user is member
	 * @param subject subject of message
	 * @param text text of message
	 * @return RTMessage with information about success or not
	 * @throws InternalErrorException
	 */
	RTMessage sendMessageToRT(PerunSession sess, int voId, String subject, String text) throws InternalErrorException;
}
