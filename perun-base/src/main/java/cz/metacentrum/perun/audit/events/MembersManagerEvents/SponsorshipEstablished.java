package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import java.time.LocalDate;

public class SponsorshipEstablished extends AuditEvent {

  private Member sponsoredMember;
  private User sponsor;
  private String message;
  private LocalDate validity;

  @SuppressWarnings("unused") // used by jackson mapper
  public SponsorshipEstablished() {
  }

  public SponsorshipEstablished(Member sponsoredMember, User sponsor, LocalDate validityTo) {
    this.sponsoredMember = sponsoredMember;
    this.sponsor = sponsor;
    this.validity = validityTo;
    this.message = formatMessage("Sponsorship of %s by %s established with validity to %s.", sponsoredMember, sponsor,
        (validityTo == null) ? "FOREVER" : validityTo.toString());
  }

  @Override
  public String getMessage() {
    return message;
  }

  public User getSponsor() {
    return sponsor;
  }

  public Member getSponsoredMember() {
    return sponsoredMember;
  }

  public LocalDate getValidity() {
    return validity;
  }

  @Override
  public String toString() {
    return message;
  }
}
