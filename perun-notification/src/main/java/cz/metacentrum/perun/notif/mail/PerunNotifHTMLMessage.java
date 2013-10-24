package cz.metacentrum.perun.notif.mail;

/**
 * Implementation of html email message
 */
public class PerunNotifHTMLMessage extends MessagePreparator implements EmailMessage {

	public PerunNotifHTMLMessage(String from, String fromText, String subject, String messageContent) {

		super(from, fromText, subject, messageContent, EmailType.HTML);
	}
}
