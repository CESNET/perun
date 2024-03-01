package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents member of group assigned to resource
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class AssignedMember {
  private RichMember richMember;
  private GroupResourceStatus status;

  public AssignedMember(RichMember richMember, GroupResourceStatus status) {
    this.richMember = richMember;
    this.status = status;
  }


  public RichMember getRichMember() {
    return richMember;
  }

  public void setRichMember(RichMember richMember) {
    this.richMember = richMember;
  }

  public GroupResourceStatus getStatus() {
    return status;
  }

  public void setStatus(GroupResourceStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssignedMember that = (AssignedMember) o;
    return Objects.equals(getRichMember(), that.getRichMember()) && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRichMember(), getStatus());
  }

  @Override
  public String toString() {
    return "AssignedMember{" +
        "richMember= " + richMember +
        ", status=" + status +
        '}';
  }
}
