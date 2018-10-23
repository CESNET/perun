package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailSending extends AuditEvent {

	private ApplicationMail mail;
	private String message;
	private boolean enabled;

	@SuppressWarnings("unused") // used by jackson mapper
	public MailSending() {
	}

	public MailSending(ApplicationMail mail, boolean enabled) {
		this.mail = mail;
		this.enabled = enabled;
		this.message = formatMessage("Sending of Mail ID: %d %s.", mail.getId(),
				(enabled) ? " enabled." : " disabled.");
	}

	public ApplicationMail getMail() {
		return mail;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
