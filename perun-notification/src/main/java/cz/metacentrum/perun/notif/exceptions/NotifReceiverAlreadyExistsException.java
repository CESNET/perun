package cz.metacentrum.perun.notif.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;

/**
 * Exception thrown when trying to create new receiver and a receiver with the same target and locale already exists.
 *
 * @author Jiri Mauritz (jirmauritz at gmail dot com)
 */
public class NotifReceiverAlreadyExistsException extends PerunException {

	public NotifReceiverAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public NotifReceiverAlreadyExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NotifReceiverAlreadyExistsException(String msg) {
		super(msg);
	}

	public NotifReceiverAlreadyExistsException(PerunNotifReceiver receiver) {
		super("Receiver with target: " + receiver.getTarget() + " and locale: " +
					receiver.getLocale() + "already exists.");
	}
}
