package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailForGroupIdAdded extends AuditEvent implements EngineIgnoreEvent {

  private ApplicationMail mail;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MailForGroupIdAdded() {
  }

  public MailForGroupIdAdded(ApplicationMail mail, Group group) {
    this.mail = mail;
    this.group = group;
    this.message = formatMessage("Mail ID: %d of Type: %s/%s added for Group ID: %d.", mail.getId(),
        mail.getMailType(), mail.getAppType(), group.getId());
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Group getGroup() {
    return group;
  }

  public ApplicationMail getMail() {
    return mail;
  }

  @Override
  public String toString() {
    return message;
  }
}
