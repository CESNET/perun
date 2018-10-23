package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailForGroupIdRemoved implements AuditEvent {
	private ApplicationMail mail;
	private Group group;
	private String name = this.getClass().getName();
	private String message;

	public MailForGroupIdRemoved(ApplicationMail mail, Group group) {
		this.mail = mail;
		this.group = group;
	}

	public MailForGroupIdRemoved() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ApplicationMail getMail() {
		return mail;
	}

	public void setMail(ApplicationMail mail) {
		this.mail = mail;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Mail ID: " + mail.getId() + " of Type: " + mail.getMailType() + "/" + mail.getAppType() + " removed for Group ID: " + group.getId() + ".";
	}
}
