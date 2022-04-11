package cz.metacentrum.perun.audit.events.ConsentManager;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Consent;

public class ConsentCreated extends AuditEvent {

	private Consent consent;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public ConsentCreated(){

	}

	public ConsentCreated(Consent consent){
		this.consent = consent;
		this.message = formatMessage("Created consent: %s", consent);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Consent getConsent() {
		return consent;
	}

	@Override
	public String toString() {
		return message;
	}
}
