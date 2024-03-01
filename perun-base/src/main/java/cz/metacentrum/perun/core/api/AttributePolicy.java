package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents a policy of an attribute.
 * It specifies the role, that users need to have, and the object, upon which the role is set.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AttributePolicy {
  private int id;
  private String role;
  private RoleObject object;
  private int policyCollectionId;

  public AttributePolicy() {
  }

  public AttributePolicy(int id, String role, RoleObject object, int policyCollectionId) {
    this.id = id;
    this.role = role;
    this.object = object;
    this.policyCollectionId = policyCollectionId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public RoleObject getObject() {
    return object;
  }

  public void setObject(RoleObject object) {
    this.object = object;
  }

  public int getPolicyCollectionId() {
    return policyCollectionId;
  }

  public void setPolicyCollectionId(int policyCollectionId) {
    this.policyCollectionId = policyCollectionId;
  }

  @Override
  public boolean equals(Object o) {
	  if (this == o) {
		  return true;
	  }
	  if (o == null || getClass() != o.getClass()) {
		  return false;
	  }
    AttributePolicy that = (AttributePolicy) o;
    return getId() == that.getId() && getPolicyCollectionId() == that.getPolicyCollectionId()
        && Objects.equals(getRole(), that.getRole()) && getObject() == that.getObject();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getRole(), getObject(), getPolicyCollectionId());
  }

  @Override
  public String toString() {
    return "AttributePolicy{" +
        "id=" + id +
        ", role='" + role + '\'' +
        ", object=" + object +
        ", policyCollectionId=" + policyCollectionId +
        '}';
  }
}
