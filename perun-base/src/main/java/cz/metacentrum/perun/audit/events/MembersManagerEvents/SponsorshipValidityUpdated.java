package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import java.time.LocalDate;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SponsorshipValidityUpdated extends AuditEvent {

  private Member sponsoredMember;
  private User sponsor;
  private String message;
  private LocalDate validity;

  @SuppressWarnings("unused") // used by jackson mapper
  public SponsorshipValidityUpdated() {
  }

  public SponsorshipValidityUpdated(Member sponsoredMember, User sponsor, LocalDate validityTo) {
    this.sponsoredMember = sponsoredMember;
    this.sponsor = sponsor;
    this.validity = validityTo;
    this.message = formatMessage("Validity of sponsorship of %s by %s changed to %s.",
        sponsoredMember, sponsor, (validityTo == null) ? "FOREVER" : validityTo.toString());
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Member getSponsoredMember() {
    return sponsoredMember;
  }

  public User getSponsor() {
    return sponsor;
  }

  public LocalDate getValidity() {
    return validity;
  }

  @Override
  public String toString() {
    return message;
  }

}
