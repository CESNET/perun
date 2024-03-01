package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.registrar.model.ApplicationForm;

public class FormItemsUpdated extends AuditEvent implements EngineIgnoreEvent {

  private ApplicationForm form;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public FormItemsUpdated() {
  }

  public FormItemsUpdated(ApplicationForm form) {
    this.form = form;
    this.message = formatMessage("Application form ID=%d voID=%d %s has had its items updated.", form.getId(),
        form.getVo().getId(), ((form.getGroup() != null) ? " groupID=" + form.getGroup().getId() : ""));
  }

  public ApplicationForm getForm() {
    return form;
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
