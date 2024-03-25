package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationRejected extends AuditEvent implements EngineIgnoreEvent {

  private Application app;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ApplicationRejected() {
  }

  public ApplicationRejected(Application application) {
    this.app = application;
    this.message = formatMessage("Application ID=%d voID=%d %s has been rejected.", app.getId(), app.getVo().getId(),
        ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : ""));
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
