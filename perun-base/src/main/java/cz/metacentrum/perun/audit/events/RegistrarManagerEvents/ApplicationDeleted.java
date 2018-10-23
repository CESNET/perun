package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationDeleted implements AuditEvent {
	private Application app;
	private String name = this.getClass().getName();
	private String message;

	public ApplicationDeleted() {
	}

	public ApplicationDeleted(Application app) {
		this.app = app;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	@Override
	public String toString() {
		return "Application ID=" + app.getId() + " voID=" + app.getVo().getId() + ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : "") + " has been deleted";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
