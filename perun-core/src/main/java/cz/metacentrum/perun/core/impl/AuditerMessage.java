package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Wrapper for AuditEvent associating it with originating user session.
 * It is supposed to be used solely inside Auditer to store runtime state of events.
 *
 * @see Auditer
 * @see AuditEvent
 *
 * @author Pavel Zlámal
 * @author Vojtěch Sassmann
 */
public class AuditerMessage {

	private final AuditEvent event;
	private final PerunSession originatingSession;

	public AuditerMessage(PerunSession sess, AuditEvent event) {
		this.event = event;
		this.originatingSession = sess;
	}

	/**
	 * Get wrapped AuditEvent
	 *
	 * @return wrapped AuditEvent
	 */
	public AuditEvent getEvent() {
		return event;
	}

	/**
	 * Get originating user session responsible for the event.
	 *
	 * @return Origination user session
	 */
	public PerunSession getOriginatingSession() {
		return this.originatingSession;
	}

	@Override
	public String toString() {
		return AuditerMessage.class.getSimpleName() + ":[message='" + event.getMessage() + "']";
	}

}
