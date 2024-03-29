package cz.metacentrum.perun.notif.dto;

import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;

/**
 * Dto holds data for one email message.
 */
public class PerunNotifEmailMessageToSendDto {

  private final PerunNotifTypeOfReceiver typeOfReceiver = PerunNotifTypeOfReceiver.EMAIL_USER;
  //Contains message send by email
  private String message;
  //Email address of receiver
  private String receiver;
  //Subject of email
  private String subject;
  //Sender of email message
  private String sender;

  public String getMessage() {
    return message;
  }

  public String getReceiver() {
    return receiver;
  }

  public String getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
