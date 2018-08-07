package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationVerified extends AuditEvent {

	private final Application app;
	private final String message;

	public ApplicationVerified(Application app) {
		this.app = app;
		this.message = String.format("Application ID=%d voID=%d %s has been verified.", app.getId(),
				app.getVo().getId(), ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : ""));
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
