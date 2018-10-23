package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.ApplicationForm;

public class FormUpdated implements AuditEvent {
	private ApplicationForm form;
	private String name = this.getClass().getName();
	private String message;

	public FormUpdated() {
	}

	public FormUpdated(ApplicationForm form) {
		this.form = form;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ApplicationForm getForm() {
		return form;
	}

	public void setForm(ApplicationForm form) {
		this.form = form;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Application form ID=" + form.getId() + " voID=" + form.getVo().getId() + ((form.getGroup() != null) ? (" groupID=" + form.getGroup().getId()) : "") + " has been updated.";
	}
}
