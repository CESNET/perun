package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationCreated extends AuditEvent {

	private final Application app;
	private final String message;

	public ApplicationCreated(Application app) {
		this.app = app;
		this.message = String.format("New %s created.", app);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Application getApp() {
		return app;
	}

	@Override
	public String toString() {
		return message;
	}
}
