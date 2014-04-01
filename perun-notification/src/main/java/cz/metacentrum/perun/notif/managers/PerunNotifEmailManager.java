package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;

import java.util.List;

/**
 * Manager for sending email messages, allows sending emails in batch
 *
 * @author tomas.tunkl
 *
 */
public interface PerunNotifEmailManager {

	/**
	 * Sends all messages in one batch
	 *
	 * @param list
	 * @return
	 */
	public void sendMessages(List<PerunNotifEmailMessageToSendDto> list);
}
