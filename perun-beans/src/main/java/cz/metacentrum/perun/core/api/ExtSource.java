package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import java.util.Map;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * External Source Bean
 * 
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSource extends Auditable {

  private String name;
  private String type;
  private Map<String, String> attributes;

  public ExtSource() {
    super();
  }

  public ExtSource(int id, String name, String type) {
    super(id);
    this.name = name;
    this.type = type;
  }

  public ExtSource(int id, String name, String type, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
    super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
    this.name = name;
    this.type = type;
  }

  public ExtSource(String name) {
    this();
    this.name = name;
  }

  public ExtSource(String name, String type) {
    this(name);
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String serializeToString() {
    return this.getClass().getSimpleName() +":[" +
        "id=<" + getId() + ">" +
            ", name=<" + (getName() == null ? "\\0" : BeansUtils.createEscaping(getName())) + ">" +
                ", type=<" + (getType() == null ? "\\0" : BeansUtils.createEscaping(getType())) + ">" +
                ']';
  }

  public String toString() {
    return this.getClass().getSimpleName()+":[" +
        "id='" + getId() + '\'' +
        ", name='" + name + '\'' +
        ", type='" + type + '\'' +
        ']';
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getId();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    // obj can be ExtSourceSql or ExtSourceLdap or whathever
    if (!(obj instanceof ExtSource))	return false;

    ExtSource other = (ExtSource) obj;
    if (getId() != other.getId())	return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (type == null) {
      if (other.type != null)	return false;
    } else if (!type.equals(other.type)) return false;
    return true;
  }

}
