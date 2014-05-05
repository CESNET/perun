package cz.metacentrum.perun.notif.mail;

/**
 * User: tomastunkl Date: 14.10.12 Time: 0:18
 */
public class PerunNotifPlainMessage extends MessagePreparator implements EmailMessage {

	public PerunNotifPlainMessage(String from, String fromText, String subject, String messageContent) {

		super(from, fromText, subject, messageContent, EmailType.PLAIN);
	}
}
