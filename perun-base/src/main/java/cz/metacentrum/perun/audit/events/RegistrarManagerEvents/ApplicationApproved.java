package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationApproved extends AuditEvent implements EngineIgnoreEvent {

  private Application app;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ApplicationApproved() {
  }

  public ApplicationApproved(Application app) {
    this.app = app;
    this.message = formatMessage("Application ID=%d voID=%d %s was approved.", app.getId(),
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
