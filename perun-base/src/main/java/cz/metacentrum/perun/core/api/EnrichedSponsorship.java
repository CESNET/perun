package cz.metacentrum.perun.core.api;

import java.time.LocalDate;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class EnrichedSponsorship {
  private User sponsor;
  private Member sponsoredMember;
  private LocalDate validityTo;
  private boolean active;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EnrichedSponsorship that = (EnrichedSponsorship) o;

    if (getSponsor() != null ? !getSponsor().equals(that.getSponsor()) : that.getSponsor() != null) {
      return false;
    }
    return getSponsoredMember() != null ? getSponsoredMember().equals(that.getSponsoredMember()) :
        that.getSponsoredMember() == null;
  }

  public User getSponsor() {
    return sponsor;
  }

  public void setSponsor(User sponsor) {
    this.sponsor = sponsor;
  }

  public Member getSponsoredMember() {
    return sponsoredMember;
  }

  public void setSponsoredMember(Member sponsoredMember) {
    this.sponsoredMember = sponsoredMember;
  }

  public LocalDate getValidityTo() {
    return validityTo;
  }

  public void setValidityTo(LocalDate validityTo) {
    this.validityTo = validityTo;
  }

  @Override
  public int hashCode() {
    int result = getSponsor() != null ? getSponsor().hashCode() : 0;
    result = 31 * result + (getSponsoredMember() != null ? getSponsoredMember().hashCode() : 0);
    return result;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return "EnrichedSponsorship[" + "sponsor=" + sponsor + ", sponsoredMember=" + sponsoredMember + ", validityTo=" +
           validityTo + ", active=" + active + ']';
  }
}
