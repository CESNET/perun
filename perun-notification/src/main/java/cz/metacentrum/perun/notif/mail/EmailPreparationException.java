package cz.metacentrum.perun.notif.mail;

/**
 * Exception is thrown when error occurs during preparing of mail from template
 *
 * @author tomas.tunkl
 *
 */
public class EmailPreparationException extends RuntimeException {

	private static final long serialVersionUID = 195888069765206588L;

	public EmailPreparationException() {
		super();
	}

	public EmailPreparationException(String msg) {
		super(msg);
	}

	public EmailPreparationException(Throwable cause) {
		super(cause);
	}

	public EmailPreparationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
