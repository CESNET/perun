package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;

public class FormItemUpdated extends AuditEvent implements EngineIgnoreEvent {

  private ApplicationForm form;
  private ApplicationFormItem item;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public FormItemUpdated() {
  }

  public FormItemUpdated(ApplicationForm form, ApplicationFormItem item) {
    this.form = form;
    this.item = item;
    this.message = formatMessage("Application form ID=%d voID=%d %s has had it itemID=%d updated.", form.getId(),
        form.getVo().getId(), ((form.getGroup() != null) ? " groupID=" + form.getGroup().getId() : ""), item.getId());
  }

  public ApplicationForm getForm() {
    return form;
  }

  public ApplicationFormItem getItem() {
    return item;
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
