package cz.metacentrum.perun.notif.dto;

import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;

/**
 * Dto holds data for one email message.
 */
public class PerunNotifEmailMessageToSendDto {

	//Contains message send by email
	private String message;

	//Email address of receiver
	private String receiver;

	//Subject of email
	private String subject;

	//Sender of email message
	private String sender;

	private final PerunNotifTypeOfReceiver typeOfReceiver = PerunNotifTypeOfReceiver.EMAIL_USER;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}
