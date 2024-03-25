package cz.metacentrum.perun.audit.events.ConsentManager;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Consent;

public class ChangedConsentStatus extends AuditEvent {

  private Consent consent;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ChangedConsentStatus() {

  }

  public ChangedConsentStatus(Consent consent) {
    this.consent = consent;
    this.message = formatMessage("%s status was changed.", consent);
  }

  public Consent getConsent() {
    return consent;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String toString() {
    return message;
  }

}
