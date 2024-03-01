package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents consent from a user to process their information
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class Consent extends Auditable {
  private int userId;
  private ConsentStatus status = ConsentStatus.UNSIGNED;
  private ConsentHub consentHub;
  private List<AttributeDefinition> attributes;

  public Consent() {

  }

  public Consent(int id, int userId, ConsentHub consentHub, List<AttributeDefinition> attributes) {
    super(id);
    this.userId = userId;
    this.consentHub = consentHub;
    this.attributes = attributes;
  }

  public Consent(int id, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid,
                 Integer modifiedByUid, int userId, ConsentHub consentHub, List<AttributeDefinition> attributes) {
    super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
    this.userId = userId;
    this.consentHub = consentHub;
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    Consent that = (Consent) obj;
    return Objects.equals(that.getConsentHub(), getConsentHub()) && Objects.equals(that.getUserId(), getUserId()) &&
           Objects.equals(that.getAttributes(), getAttributes()) && Objects.equals(that.getStatus(), getStatus());
  }

  public List<AttributeDefinition> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributeDefinition> attributes) {
    this.attributes = attributes;
  }

  public ConsentHub getConsentHub() {
    return consentHub;
  }

  public void setConsentHub(ConsentHub consentHub) {
    this.consentHub = consentHub;
  }

  public ConsentStatus getStatus() {
    return status;
  }

  public void setStatus(ConsentStatus status) {
    this.status = status;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getUserId(), getConsentHub(), getAttributes(), getStatus());
  }

  @Override
  public String serializeToString() {
    StringBuilder str = new StringBuilder();

    List<String> attributes = new ArrayList<>();
    String attributesString;

    if (getAttributes() == null) {
      attributesString = "\\0";
    } else {
      for (AttributeDefinition att : getAttributes()) {
        attributes.add(att.serializeToString());
      }
      attributesString = attributes.toString();
    }
    return str.append(this.getClass().getSimpleName()).append(":[").append("id=<").append(getId()).append(">")
        .append(", userId=<").append(getUserId()).append(">").append(", consentHub=<")
        .append(getConsentHub().serializeToString()).append(">").append(", status=<").append(getStatus()).append(">")
        .append(", attributes=<").append(attributesString).append(">").append("]").toString();

  }

  @Override
  public String toString() {
    return "Consent:[id='" + getId() + "', userId='" + getUserId() + "', consentHub='" + getConsentHub() +
           "', status='" + getStatus() + "', attributes='" + getAttributes() + "']";
  }
}
