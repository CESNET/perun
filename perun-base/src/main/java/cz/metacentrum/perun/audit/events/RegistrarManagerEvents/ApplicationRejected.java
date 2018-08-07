package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationRejected extends AuditEvent {

	private final Application app;
	private final String message;

	public ApplicationRejected(Application application) {
		this.app = application;
		this.message = String.format("Application ID=%d voID=%d %s has been rejected.", app.getId(),
				app.getVo().getId(), ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : ""));
	}

	public Application getApplication() {
		return app;
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
