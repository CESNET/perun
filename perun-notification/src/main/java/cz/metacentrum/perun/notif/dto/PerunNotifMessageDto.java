package cz.metacentrum.perun.notif.dto;

import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;

import java.util.Set;

/**
 * Dto holds data for messages which are going to be sent using type specified
 * in PerunNotifReceiver. User: tomastunkl Date: 23.11.12 Time: 22:37
 */
public class PerunNotifMessageDto {

	/**
	 * Receiver of messageToSend
	 */
	private PerunNotifReceiver receiver;

	/**
	 * Sender of messageToSend
	 */
	private String sender;

	/**
	 * Complete message send to receiver
	 */
	private String messageToSend;

	/**
	 * Subject to send
	 */
	private String subject;

	/**
	 * Ids of poolMessages used to create this message
	 */
	private Set<Integer> usedPoolIds;

	/**
	 * PoolMessage used to create message
	 */
	private PoolMessage poolMessage;

	/**
	 * Template used for this message
	 */
	private PerunNotifTemplate template;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public PerunNotifReceiver getReceiver() {
		return receiver;
	}

	public void setReceiver(PerunNotifReceiver receiver) {
		this.receiver = receiver;
	}

	public String getMessageToSend() {
		return messageToSend;
	}

	public void setMessageToSend(String messageToSend) {
		this.messageToSend = messageToSend;
	}

	public Set<Integer> getUsedPoolIds() {
		return usedPoolIds;
	}

	public void setUsedPoolIds(Set<Integer> usedPoolIds) {
		this.usedPoolIds = usedPoolIds;
	}

	public PoolMessage getPoolMessage() {
		return poolMessage;
	}

	public void setPoolMessage(PoolMessage poolMessage) {
		this.poolMessage = poolMessage;
	}

	public PerunNotifTemplate getTemplate() {
		return template;
	}

	public void setTemplate(PerunNotifTemplate template) {
		this.template = template;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
