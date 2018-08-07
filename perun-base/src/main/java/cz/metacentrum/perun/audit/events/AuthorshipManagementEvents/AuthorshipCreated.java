package cz.metacentrum.perun.audit.events.AuthorshipManagementEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.cabinet.model.Authorship;

public class AuthorshipCreated extends AuditEvent {

	private final Authorship authorship;
	private final String message;

	public AuthorshipCreated(Authorship authorship) {
		this.authorship = authorship;
		this.message = String.format("Authorship %s created.", authorship.serializeToString());
	}

	public Authorship getAuthorship() {
		return authorship;
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
