package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sponsored member with list of his sponsors (for GUI)
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class MemberWithSponsors {
  private RichMember member;
  private List<Sponsor> sponsors;

  /**
   * Constructor
   */
  public MemberWithSponsors() {
  }

  /**
   * Constructor
   */
  public MemberWithSponsors(RichMember member) {
    this.member = member;
  }

  public Member getMember() {
    return member;
  }

  public void setMember(RichMember member) {
    this.member = member;
  }

  public List<Sponsor> getSponsors() {
    return sponsors;
  }

  public void setSponsors(List<Sponsor> sponsors) {
    this.sponsors = sponsors;
  }

  public void addSponsor(Sponsor sponsor) {
    if (this.sponsors == null) {
      this.sponsors = new ArrayList<>();
    }
    this.sponsors.add(sponsor);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MemberWithSponsors that = (MemberWithSponsors) o;
    return Objects.equals(member, that.member);
  }

  @Override
  public int hashCode() {
    return Objects.hash(member);
  }
}
