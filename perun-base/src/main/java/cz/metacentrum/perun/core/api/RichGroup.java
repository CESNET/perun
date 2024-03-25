package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Group with list of all its attributes.
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class RichGroup extends Group {

  private List<Attribute> attributes;

  public RichGroup() {
  }

  public RichGroup(Group group, List<Attribute> attrs) {
    super(group.getId(), group.getName(), group.getDescription(), group.getCreatedAt(), group.getCreatedBy(),
        group.getModifiedAt(), group.getModifiedBy(), group.getParentGroupId(), group.getCreatedByUid(),
        group.getModifiedByUid());
    this.setVoId(group.getVoId());
    this.setUuid(group.getUuid());
    this.attributes = attrs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RichGroup other = (RichGroup) obj;
    if (getId() != other.getId()) {
      return false;
    }
    if (attributes == null) {
      return other.getAttributes() == null;
    }
    if (this.getAttributes().size() != other.getAttributes().size()) {
      return false;
    }

    List<Attribute> sortedThis = this.getAttributes().stream().sorted().toList();

    List<Attribute> sortedOther = other.getAttributes().stream().sorted().toList();

    return sortedThis.equals(sortedOther);
  }

  public List<Attribute> getAttributes() {
    return this.attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + Objects.hashCode(this.attributes);
    return hash;
  }

  @Override
  public String serializeToString() {
    StringBuilder str = new StringBuilder();

    String result;
    List<String> stringAttrs = new ArrayList<>();

    // serialize group attributes
    List<Attribute> attrs = this.getAttributes();
    if (attrs == null) {
      result = "\\0";
    } else {
      for (Attribute attr : attrs) {
        stringAttrs.add(attr.serializeToString());
      }
      result = stringAttrs.toString();
    }

    return str.append(this.getClass().getSimpleName()).append(":[").append("id=<").append(getId()).append(">")
        .append(", uuid=<").append(getUuid()).append(">").append(", parentGroupId=<")
        .append(getParentGroupId() == null ? "\\0" : getParentGroupId()).append(">").append(", name=<")
        .append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(", shortName=<")
        .append(getShortName() == null ? "\\0" : BeansUtils.createEscaping(getShortName())).append(">")
        .append(", description=<")
        .append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">")
        .append(", voId=<").append(getVoId()).append(">").append(", groupAttributes=<").append(result).append(">")
        .append(']').toString();
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder();
    ret.append(getClass().getSimpleName());
    ret.append(":[");
    ret.append("id='");
    ret.append(this.getId());
    ret.append("', uuid='");
    ret.append(getUuid());
    ret.append("', parentGroupId='");
    ret.append(getParentGroupId());
    ret.append("', name='");
    ret.append(this.getName());
    ret.append("', shortName='");
    ret.append(this.getShortName());
    ret.append("', description='");
    ret.append(this.getDescription());
    ret.append("', voId='");
    ret.append(this.getVoId());
    ret.append("', groupAttributes='");
    ret.append(this.getAttributes());
    ret.append("']");
    return ret.toString();
  }
}
