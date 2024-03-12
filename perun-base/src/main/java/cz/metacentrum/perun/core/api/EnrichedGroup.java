package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents group with its attributes.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class EnrichedGroup {
  private Group group;
  private List<Attribute> attributes;

  public EnrichedGroup(Group group, List<Attribute> attributes) {
    this.group = group;
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedGroup that = (EnrichedGroup) o;
    return getGroup().equals(that.getGroup());
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getGroup());
  }

  @Override
  public String toString() {
    return "EnrichedGroup{" + "group=" + group + ", attributes=" + attributes + '}';
  }
}
