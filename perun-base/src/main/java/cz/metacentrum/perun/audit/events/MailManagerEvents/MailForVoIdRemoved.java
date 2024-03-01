package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailForVoIdRemoved extends AuditEvent implements EngineIgnoreEvent {

  private ApplicationMail mail;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MailForVoIdRemoved() {
  }

  public MailForVoIdRemoved(ApplicationMail mail, Vo vo) {
    this.mail = mail;
    this.vo = vo;
    this.message = formatMessage("Mail ID: %d of Type: %s/%s removed for VO ID: %d.", mail.getId(),
        mail.getMailType(), mail.getAppType(), vo.getId());
  }

  @Override
  public String getMessage() {
    return message;
  }

  public ApplicationMail getMail() {
    return mail;
  }

  public Vo getVo() {
    return vo;
  }

  @Override
  public String toString() {
    return message;
  }
}
