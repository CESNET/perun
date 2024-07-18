package cz.metacentrum.perun.registrar.model;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class InvitationWithSender extends Invitation {
  private String senderName;
  private String senderEmail;

  public InvitationWithSender(int id, int voId, int groupId, int senderId, String receiverName, String receiverEmail,
                              String redirectUrl, Locale language, LocalDate expiration, String senderName,
                              String senderEmail) {
    super(id, voId, groupId, senderId, receiverName, receiverEmail, redirectUrl, language, expiration);
    this.senderName = senderName;
    this.senderEmail = senderEmail;
  }

  public InvitationWithSender(int id, int voId, int groupId, int senderId, String receiverName, String receiverEmail,
                              Locale language, LocalDate expiration, String senderName, String senderEmail) {
    super(id, voId, groupId, senderId, receiverName, receiverEmail, language, expiration);
    this.senderName = senderName;
    this.senderEmail = senderEmail;
  }

  public InvitationWithSender(int id, int voId, int groupId, Integer applicationId, int senderId, String receiverName,
                              String receiverEmail, String redirectUrl, UUID token, Locale language,
                              LocalDate expiration, InvitationStatus status, String senderName, String senderEmail) {
    super(id, voId, groupId, applicationId, senderId, receiverName, receiverEmail, redirectUrl, token, language,
        expiration, status);
    this.senderName = senderName;
    this.senderEmail = senderEmail;
  }

  public InvitationWithSender(int id, int voId, int groupId, Integer applicationId, int senderId, String receiverName,
                              String receiverEmail, String redirectUrl, UUID token, Locale language,
                              LocalDate expiration,
                              InvitationStatus status, String createdAt, String createdBy, String modifiedAt,
                              String modifiedBy, Integer createdByUid, Integer modifiedByUid, String senderName,
                              String senderEmail) {
    super(id, voId, groupId, applicationId, senderId, receiverName, receiverEmail, redirectUrl, token, language,
        expiration, status, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
    this.senderName = senderName;
    this.senderEmail = senderEmail;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getSenderEmail() {
    return senderEmail;
  }

  public void setSenderEmail(String senderEmail) {
    this.senderEmail = senderEmail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InvitationWithSender that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return Objects.equals(senderName, that.senderName) &&
               Objects.equals(senderEmail, that.senderEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), senderName, senderEmail);
  }

  @Override
  public String toString() {
    return "InvitationWithSender:[id='" + getId() + "', voId='" + getVoId() + "', groupId='" + getGroupId() +
               "', applicationId='" + getApplicationId() + "', senderId='" + getSenderId() + "', senderName='" +
               getSenderName() + "', senderEmail='" + getSenderEmail() + "', receiverName='" +
               getReceiverName() + "', receiverEmail='" + getReceiverEmail() + "', redirectUrl='" + getRedirectUrl() +
               "', token='" + getToken() + "', language='" + getLanguage() + "', expiration='" +
               getExpiration() + "', " + "status" + "='" + getStatus() + "']";
  }
}
