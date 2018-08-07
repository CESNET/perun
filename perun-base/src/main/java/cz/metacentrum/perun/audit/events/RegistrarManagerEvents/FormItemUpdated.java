package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;

public class FormItemUpdated extends AuditEvent {

	private final ApplicationForm form;
	private final ApplicationFormItem item;
	private final String message;

	public FormItemUpdated(ApplicationForm form, ApplicationFormItem item) {
		this.form = form;
		this.item = item;
		this.message = String.format("Application form ID=%d voID=%d %s has had it itemID=%d updated.", form.getId(),
				form.getVo().getId(), ((form.getGroup() != null) ? " groupID=" + form.getGroup().getId() : ""), item.getId());
	}

	@Override
	public String getMessage() {
		return message;
	}

	public ApplicationForm getForm() {
		return form;
	}

	public ApplicationFormItem getItem() {
		return item;
	}

	@Override
	public String toString() {
		return message;
	}
}
