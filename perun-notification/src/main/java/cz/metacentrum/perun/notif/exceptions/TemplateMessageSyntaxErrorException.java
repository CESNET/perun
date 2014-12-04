package cz.metacentrum.perun.notif.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import freemarker.core.ParseException;

/**
 * Exception thrown when there is a freemarker syntax error in a TemplateMessage.
 *
 * @author Jiri Mauritz (jirmauritz at gmail dot com)
 */
public class TemplateMessageSyntaxErrorException extends PerunException {

	public TemplateMessageSyntaxErrorException(Throwable cause) {
		super(cause);
	}

	public TemplateMessageSyntaxErrorException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public TemplateMessageSyntaxErrorException(String msg) {
		super(msg);
	}

	public TemplateMessageSyntaxErrorException(PerunNotifTemplateMessage message) {
		super("Syntax error in template message id: " + message.getId() + " message: " + message.getMessage());
	}

	public TemplateMessageSyntaxErrorException(PerunNotifTemplateMessage message, Throwable cause) {
		super("Syntax error in template message id: " + message.getId() + " message: " + message.getMessage(), cause);
	}
}
