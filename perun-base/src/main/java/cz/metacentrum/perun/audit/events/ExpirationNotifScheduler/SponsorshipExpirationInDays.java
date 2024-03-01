package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.EnrichedSponsorship;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SponsorshipExpirationInDays extends AuditEvent implements EngineIgnoreEvent {

  private EnrichedSponsorship sponsorship;
  private int days;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public SponsorshipExpirationInDays() {
  }

  public SponsorshipExpirationInDays(EnrichedSponsorship sponsorship, int days) {
    this.sponsorship = sponsorship;
    this.days = days;
    //TODO
    this.message = formatMessage("Sponsorship of member %s by sponsor %s will expire in %d days.",
        sponsorship.getSponsoredMember(), sponsorship.getSponsor(), days);
  }

  public EnrichedSponsorship getSponsorship() {
    return sponsorship;
  }

  public int getDays() {
    return days;
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
