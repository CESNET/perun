package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.PerunSession;

public class AuditerMessage {
	private final AuditEvent event;
	private final PerunSession originaterPerunSession;

	public AuditerMessage(PerunSession sess, AuditEvent event) {
		this.event = event;
		this.originaterPerunSession = sess;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public PerunSession getOriginaterPerunSession() {
		return this.originaterPerunSession;
	}

	@Override
	public String toString() {
		return "AuditerMessage:[message='" + event.getMessage() + "']";
	}

}
