package cz.metacentrum.perun.core.api;

import java.time.LocalDate;

/**
 * Class representing relationship between a sponsor (User id)
 * and sponsored member (Member id).
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Sponsorship {
  int sponsoredId;
  int sponsorId;
  LocalDate validityTo;
  boolean active;

  public int getSponsoredId() {
    return sponsoredId;
  }

  public void setSponsoredId(int sponsoredId) {
    this.sponsoredId = sponsoredId;
  }

  public int getSponsorId() {
    return sponsorId;
  }

  public void setSponsorId(int sponsorId) {
    this.sponsorId = sponsorId;
  }

  public LocalDate getValidityTo() {
    return validityTo;
  }

  public void setValidityTo(LocalDate validityTo) {
    this.validityTo = validityTo;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
	  if (this == o) {
		  return true;
	  }
	  if (o == null || getClass() != o.getClass()) {
		  return false;
	  }

    Sponsorship that = (Sponsorship) o;

	  if (getSponsoredId() != that.getSponsoredId()) {
		  return false;
	  }
    return getSponsorId() == that.getSponsorId();
  }

  @Override
  public int hashCode() {
    int result = (int) (getSponsoredId() ^ (getSponsoredId() >>> 32));
    result = 31 * result + (int) (getSponsorId() ^ (getSponsorId() >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Sponsorship[" +
        "sponsoredId=" + sponsoredId +
        ", sponsorId=" + sponsorId +
        ", validityTo=" + validityTo +
        ", active=" + active +
        ']';
  }
}
