package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Invitation to a group sent via email. User accepting the invitation creates a pre-approved application.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class Invitation extends Auditable {
  private int voId;
  private int groupId;
  private Integer applicationId;
  private int senderId;
  private String receiverName;
  private String receiverEmail;
  private String redirectUrl;
  private UUID token;
  private Locale language;
  private LocalDate expiration;
  private InvitationStatus status = InvitationStatus.PENDING;


  public Invitation(int voId, int groupId, String receiverName, String receiverEmail, String redirectUrl,
                    Locale language, LocalDate expiration) {
    this.voId = voId;
    this.groupId = groupId;
    this.receiverName = receiverName;
    this.receiverEmail = receiverEmail;
    this.redirectUrl = redirectUrl;
    this.language = language;
    this.expiration = expiration;
  }

  // TODO add constructors based on the required fields from API
  public Invitation(int id, int voId, int groupId, int senderId, String receiverName, String receiverEmail,
                    String redirectUrl, Locale language, LocalDate expiration) {
    super(id);
    this.voId = voId;
    this.groupId = groupId;
    this.senderId = senderId;
    this.receiverName = receiverName;
    this.receiverEmail = receiverEmail;
    this.redirectUrl = redirectUrl;
    this.language = language;
    this.expiration = expiration;
  }

  public Invitation(int id, int voId, int groupId, int senderId, String receiverName, String receiverEmail,
                    Locale language, LocalDate expiration) {
    super(id);
    this.voId = voId;
    this.groupId = groupId;
    this.senderId = senderId;
    this.receiverName = receiverName;
    this.receiverEmail = receiverEmail;
    this.language = language;
    this.expiration = expiration;
  }

  public Invitation(int id, int voId, int groupId, Integer applicationId, int senderId, String receiverName,
                    String receiverEmail, String redirectUrl, UUID token, Locale language, LocalDate expiration,
                    InvitationStatus status) {
    super(id);
    this.voId = voId;
    this.groupId = groupId;
    this.applicationId = applicationId;
    this.senderId = senderId;
    this.receiverName = receiverName;
    this.receiverEmail = receiverEmail;
    this.redirectUrl = redirectUrl;
    this.token = token;
    this.language = language;
    this.expiration = expiration;
    this.status = status;
  }

  public Invitation(int id, int voId, int groupId, Integer applicationId, int senderId, String receiverName,
                    String receiverEmail, String redirectUrl, UUID token, Locale language, LocalDate expiration,
                    InvitationStatus status, String createdAt, String createdBy, String modifiedAt, String modifiedBy,
                    Integer createdByUid, Integer modifiedByUid) {
    super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
    this.voId = voId;
    this.groupId = groupId;
    this.applicationId = applicationId;
    this.senderId = senderId;
    this.receiverName = receiverName;
    this.receiverEmail = receiverEmail;
    this.redirectUrl = redirectUrl;
    this.token = token;
    this.language = language;
    this.expiration = expiration;
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
    Invitation that = (Invitation) o;
    return getVoId() == that.getVoId() && getGroupId() == that.getGroupId() &&
               Objects.equals(getApplicationId(), that.getApplicationId()) && getSenderId() == that.getSenderId() &&
               Objects.equals(getReceiverName(), that.getReceiverName()) &&
               Objects.equals(getReceiverEmail(), that.getReceiverEmail()) &&
               Objects.equals(getRedirectUrl(), that.getRedirectUrl()) && Objects.equals(getToken(), that.getToken()) &&
               Objects.equals(getLanguage(), that.getLanguage()) &&
               Objects.equals(getExpiration(), that.getExpiration()) && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getVoId(), getGroupId(), getApplicationId(), getSenderId(), getReceiverName(),
        getReceiverEmail(), getRedirectUrl(), getToken(), getLanguage(), getExpiration(), getStatus());
  }

  @Override
  public String serializeToString() {
    return this.getClass().getSimpleName() + ":[" + "id=<" + getId() + ">" + ", voId=<" + getVoId() + ">" +
               ", groupId=<" + getGroupId() + ">" + ", applicationId=<" +
               (getApplicationId() == null ? "\\0" : getApplicationId()) + ">" +
               ", senderId=<" + getSenderId() + ">" + ", receiverName=<" + getReceiverName() + ">" +
               ", receiverEmail=<" + getReceiverEmail() + ">" + ", redirectUrl=<" +
               BeansUtils.createEscaping(getRedirectUrl()) + ">" +
               ", tokn=<" + (getToken() == null ? "\\0" : getToken()) + ">" + ", language=<" + getLanguage() + ">" +
               ", expiration=<" + getExpiration() + ">" + ", status=<" + getStatus() + ">]";
  }

  @Override
  public String toString() {
    return "Invitation:[id='" + getId() + "', voId='" + getVoId() + "', groupId='" + getGroupId() +
               "', applicationId='" + getApplicationId() + "', senderId='" + getSenderId() + "', receiverName='" +
               getReceiverName() + "', receiverEmail='" + getReceiverEmail() + "', redirectUrl='" + getRedirectUrl() +
               "', token='" + getToken() + "', language='" + getLanguage() + "', expiration='" +
               getExpiration() + "', " + "status" + "='" + getStatus() + "']";
  }

  public int getVoId() {
    return voId;
  }

  public void setVoId(int voId) {
    this.voId = voId;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public int getSenderId() {
    return senderId;
  }

  public void setSenderId(int senderId) {
    this.senderId = senderId;
  }

  public String getReceiverName() {
    return receiverName;
  }

  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  public String getReceiverEmail() {
    return receiverEmail;
  }

  public void setReceiverEmail(String receiverEmail) {
    this.receiverEmail = receiverEmail;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public UUID getToken() {
    return token;
  }

  public void setToken(UUID token) {
    this.token = token;
  }

  public Locale getLanguage() {
    return language;
  }

  public void setLanguage(Locale language) {
    this.language = language;
  }

  public LocalDate getExpiration() {
    return expiration;
  }

  public void setExpiration(LocalDate expiration) {
    this.expiration = expiration;
  }

  public InvitationStatus getStatus() {
    return status;
  }

  public void setStatus(InvitationStatus status) {
    this.status = status;
  }
}
