package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dao layer for work with perunNotifPoolMessage in db
 *
 * @author tomas.tunkl
 *
 */
public interface PerunNotifPoolMessageDao {

	/**
	 * Saves perunNotifPoolMessage to db, keyAttributes of poolMessage are
	 * serialized to string and values are URLEncoded
	 *
	 * @param poolMessage
	 */
	public void savePerunNotifPoolMessage(
		PerunNotifPoolMessage poolMessage);

	/**
	 * Gets all perunNotifPoolMessages from db Integer is templateId,
	 * perunNotifPoolMessageProcessDto holds messages with same templateId
	 * and keyAttributes
	 *
	 * @return templateId = list<PoolMessage>
	 */
	public Map<Integer, List<PoolMessage>> getAllPoolMessagesForProcessing();

	/**
	 * Sets all created to now for all pool messages. Is used after start of
	 * application
	 */
	public void setAllCreatedToNow();

	/**
	 * Removes all pool messages from db containing given ids.
	 *
	 * @param proccessedIds
	 */
	public void removeAllPoolMessages(Set<Integer> proccessedIds);
}
