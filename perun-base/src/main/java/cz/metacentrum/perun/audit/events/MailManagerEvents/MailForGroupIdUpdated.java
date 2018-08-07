package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailForGroupIdUpdated extends AuditEvent {

	private final ApplicationMail mail;
	private final Group group;
	private final String message;

	public MailForGroupIdUpdated(ApplicationMail mail, Group group) {
		this.mail = mail;
		this.group = group;
		this.message = String.format("Mail ID: %d of Type: %s/%s updated for Group ID: %d.", mail.getId(),
				mail.getMailType(), mail.getAppType(), group.getId());
	}

	@Override
	public String getMessage() {
		return message;
	}

	public ApplicationMail getMail() {
		return mail;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return message;
	}
}
