package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Contains the ban on facility, the facility itself and (rich)user which has been baned
 */
public class EnrichedBanOnFacility {
  private RichUser user;
  private Facility facility;
  private BanOnFacility ban;

  public EnrichedBanOnFacility() {
    super();
  }

  public EnrichedBanOnFacility(RichUser user, Facility facility, BanOnFacility ban) {
    this.user = user;
    this.facility = facility;
    this.ban = ban;
  }

  public RichUser getUser() {
    return user;
  }

  public void setUser(RichUser user) {
    this.user = user;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public BanOnFacility getBan() {
    return ban;
  }

  public void setBan(BanOnFacility ban) {
    this.ban = ban;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedBanOnFacility that = (EnrichedBanOnFacility) o;
    return Objects.equals(getBan(), that.getBan());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBan());
  }

  @Override
  public String toString() {
    return "EnrichedBanOnFacility{" +
        "user=" + user +
        ", facility=" + facility +
        ", ban=" + ban +
        '}';
  }
}
