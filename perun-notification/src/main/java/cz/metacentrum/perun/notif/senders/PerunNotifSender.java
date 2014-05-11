package cz.metacentrum.perun.notif.senders;

import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;

import java.util.List;
import java.util.Set;

/**
 * Defines sender which can handle sending messages to user
 *
 * User: tomastunkl Date: 23.11.12 Time: 23:04
 */
public interface PerunNotifSender {

	/**
	 * Method returns whether sender can handle given type of receiver
	 *
	 * @param typeOfReceiver
	 * @return
	 */
	public boolean canHandle(PerunNotifTypeOfReceiver typeOfReceiver);

	/**
	 * Method sends dtos which has receiver which sender canHandle
	 *
	 * @param dtosToSend
	 * @return
	 */
	public Set<Integer> send(List<PerunNotifMessageDto> dtosToSend);
}
