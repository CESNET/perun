package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the user doesn't have the chosen attribute for mail set,
 * where the notification should be sent to.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 * @author Dominik František Bučík <bucik@ics.muni.cz>
 */
public class NotificationMemberMailNotExistsException extends PerunException {

	public NotificationMemberMailNotExistsException(String message) {
		super(message);
	}

	public NotificationMemberMailNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotificationMemberMailNotExistsException(Throwable cause) {
		super(cause);
	}
}
