package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents ban on resource with resource and member together with user/member attributes.
 *
 * @author Johana Supikova <supikova@ics.muni.cz>
 */
public class EnrichedBanOnResource {
  private RichMember member;
  private Resource resource;
  private BanOnResource ban;

  /**
   * Constructs a new instance.
   */
  public EnrichedBanOnResource() {
    super();
  }

  public EnrichedBanOnResource(RichMember member, Resource resource, BanOnResource ban) {
    this.ban = ban;
    this.member = member;
    this.resource = resource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedBanOnResource that = (EnrichedBanOnResource) o;
    return Objects.equals(getBan(), that.getBan());
  }

  public BanOnResource getBan() {
    return ban;
  }

  public void setBan(BanOnResource ban) {
    this.ban = ban;
  }

  public RichMember getMember() {
    return member;
  }

  public void setMember(RichMember member) {
    this.member = member;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBan());
  }

  @Override
  public String toString() {
    return "EnrichedBanOnResource{" + "member=" + member + ", resource=" + resource + ", ban=" + ban + '}';
  }
}
