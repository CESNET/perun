package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationCreated extends AuditEvent implements EngineIgnoreEvent {

  private Application app;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ApplicationCreated() {
  }

  public ApplicationCreated(Application app) {
    this.app = app;
    this.message = formatMessage("New %s created.", app);
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
