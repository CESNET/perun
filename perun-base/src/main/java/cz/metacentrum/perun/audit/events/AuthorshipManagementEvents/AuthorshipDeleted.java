package cz.metacentrum.perun.audit.events.AuthorshipManagementEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.cabinet.model.Authorship;

public class AuthorshipDeleted implements AuditEvent {
	private Authorship authorship;
	private String name = this.getClass().getName();
	private String message;

	public AuthorshipDeleted(Authorship authorship) {
		this.authorship = authorship;
	}

	public AuthorshipDeleted() {
	}

	public Authorship getAuthorship() {
		return authorship;
	}

	public void setAuthorship(Authorship authorship) {
		this.authorship = authorship;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("Authorship %s deleted.", authorship.serializeToString());
	}
}
