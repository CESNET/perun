package cz.metacentrum.perun.notif.dto;

import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import java.util.Set;

/**
 * Dto holds data for messages which are going to be sent using type specified in PerunNotifReceiver. User: tomastunkl
 * Date: 23.11.12 Time: 22:37
 */
public class PerunNotifMessageDto {

  /**
   * Receiver of messageToSend
   */
  private PerunNotifReceiver receiver;

  /**
   * Reply_to field of messageToSend
   */
  private String replyTo;

  /**
   * Sender of messageToSend
   */
  private String sender;

  /**
   * Complete message send to receiver
   */
  private String messageToSend;

  /**
   * Subject to send
   */
  private String subject;

  /**
   * Ids of poolMessages used to create this message
   */
  private Set<Integer> usedPoolIds;

  /**
   * PoolMessage used to create message
   */
  private PoolMessage poolMessage;

  /**
   * Template used for this message
   */
  private PerunNotifTemplate template;

  public String getMessageToSend() {
    return messageToSend;
  }

  public PoolMessage getPoolMessage() {
    return poolMessage;
  }

  public PerunNotifReceiver getReceiver() {
    return receiver;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public String getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }

  public PerunNotifTemplate getTemplate() {
    return template;
  }

  public Set<Integer> getUsedPoolIds() {
    return usedPoolIds;
  }

  public void setMessageToSend(String messageToSend) {
    this.messageToSend = messageToSend;
  }

  public void setPoolMessage(PoolMessage poolMessage) {
    this.poolMessage = poolMessage;
  }

  public void setReceiver(PerunNotifReceiver receiver) {
    this.receiver = receiver;
  }

  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setTemplate(PerunNotifTemplate template) {
    this.template = template;
  }

  public void setUsedPoolIds(Set<Integer> usedPoolIds) {
    this.usedPoolIds = usedPoolIds;
  }
}
