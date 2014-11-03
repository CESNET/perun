package cz.metacentrum.perun.notif.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;

/**
 * Exception thrown when trying to create new Regex and a Regex with the same regular expression already exists.
 *
 * @author Jiri Mauritz (jirmauritz at gmail dot com)
 */
public class NotifRegexAlreadyExistsException extends PerunException {

	public NotifRegexAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public NotifRegexAlreadyExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NotifRegexAlreadyExistsException(String msg) {
		super(msg);
	}

	public NotifRegexAlreadyExistsException(PerunNotifRegex regex) {
		super("Regex with regular expression: " + regex.getRegex() + "already exists.");
	}
}
