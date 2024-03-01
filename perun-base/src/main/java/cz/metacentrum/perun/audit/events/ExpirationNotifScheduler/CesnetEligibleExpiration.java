package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class CesnetEligibleExpiration extends AuditEvent implements EngineIgnoreEvent {

  private User user;
  private int daysToExpiration;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public CesnetEligibleExpiration() {
  }

  public CesnetEligibleExpiration(User user, int daysToExpiration) {
    this.user = user;
    this.daysToExpiration = daysToExpiration;
    this.message = formatMessage("%s isCesnetEligible expires in %s days.", user, daysToExpiration);
  }

  public CesnetEligibleExpiration(User user, int daysToExpiration, String expirationString) {
    this.user = user;
    this.daysToExpiration = daysToExpiration;
    this.message = formatMessage("%s isCesnetEligible expires %s.", user, expirationString);
  }

  public int getDaysToExpiration() {
    return daysToExpiration;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
