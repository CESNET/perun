package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.EnrichedSponsorship;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SponsorshipExpired extends AuditEvent implements EngineIgnoreEvent {
  private EnrichedSponsorship sponsorship;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public SponsorshipExpired() {
  }

  public SponsorshipExpired(EnrichedSponsorship sponsorship) {
    this.sponsorship = sponsorship;
    this.message = formatMessage("Sponsorship of member %s by sponsor %s has expired.",
        sponsorship.getSponsoredMember(), sponsorship.getSponsor());
  }

  public EnrichedSponsorship getSponsorship() {
    return sponsorship;
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
