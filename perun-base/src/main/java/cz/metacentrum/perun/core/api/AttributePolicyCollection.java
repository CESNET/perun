package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents a policy collection of an attribute.
 * <p>
 * User has rights to perform an action (READ/WRITE) on the attribute, if he satisfies all policies in at least one of
 * the attribute policy collections with given action.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AttributePolicyCollection {
  private int id;
  private int attributeId;
  private AttributeAction action;
  private List<AttributePolicy> policies;

  public AttributePolicyCollection() {
  }

  public AttributePolicyCollection(int id, int attributeId, AttributeAction action, List<AttributePolicy> policies) {
    this.id = id;
    this.attributeId = attributeId;
    this.action = action;
    this.policies = policies;
  }

  public void addPolicy(AttributePolicy policy) {
    policies.add(policy);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributePolicyCollection that = (AttributePolicyCollection) o;
    return getId() == that.getId() && getAttributeId() == that.getAttributeId() && getAction() == that.getAction() &&
           Objects.equals(getPolicies(), that.getPolicies());
  }

  public AttributeAction getAction() {
    return action;
  }

  public void setAction(AttributeAction action) {
    this.action = action;
  }

  public int getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(int attributeId) {
    this.attributeId = attributeId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<AttributePolicy> getPolicies() {
    return policies;
  }

  public void setPolicies(List<AttributePolicy> policies) {
    this.policies = policies;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getAttributeId(), getAction(), getPolicies());
  }

  @Override
  public String toString() {
    return "AttributePolicyCollection{" + "id=" + id + ", attributeId=" + attributeId + ", action=" + action +
           ", policies=" + policies + '}';
  }
}
