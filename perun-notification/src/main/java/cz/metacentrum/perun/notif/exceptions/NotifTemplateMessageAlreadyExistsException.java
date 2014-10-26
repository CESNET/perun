package cz.metacentrum.perun.notif.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;

/**
 * Exception thrown when trying to create new template message and a template message with the same template id and locale already exists.
 *
 * @author Jiri Mauritz (jirmauritz at gmail dot com)
 */
public class NotifTemplateMessageAlreadyExistsException extends PerunException {

	public NotifTemplateMessageAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public NotifTemplateMessageAlreadyExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NotifTemplateMessageAlreadyExistsException(String msg) {
		super(msg);
	}

	public NotifTemplateMessageAlreadyExistsException(PerunNotifTemplateMessage message) {
		super("TemplateMessage with template id: " + message.getTemplateId() + " and locale: "
			+ message.getLocale() + " already exists.");
	}
}
