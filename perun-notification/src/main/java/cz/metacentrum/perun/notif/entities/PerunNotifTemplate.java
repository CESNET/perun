package cz.metacentrum.perun.notif.entities;

import cz.metacentrum.perun.notif.enums.PerunNotifNotifyTrigger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;

/**
 * Represents template for message send to receivers.
 * <p>
 * Table pn_template
 *
 * @author tomas.tunkl
 */
public class PerunNotifTemplate {

  private static final String MAP_DELIMITER = ";";
  private static final String LIST_DELIMITER = "/";
  public static final RowMapper<PerunNotifTemplate> PERUN_NOTIF_TEMPLATE = new RowMapper<PerunNotifTemplate>() {

    public PerunNotifTemplate mapRow(ResultSet rs, int i) throws SQLException {

      PerunNotifTemplate template = new PerunNotifTemplate();
      template.setId(rs.getInt("id"));
      template.setName(rs.getString("name"));
      template.setPrimaryProperties(parseMapWithList(rs.getString("primary_properties")));
      template.setNotifyTrigger(PerunNotifNotifyTrigger.resolve(rs.getString("notify_trigger")));
      template.setYoungestMessageTime(rs.getLong("youngest_message_time"));
      template.setOldestMessageTime(rs.getLong("oldest_message_time"));
      template.setSender(rs.getString("sender"));
      template.setReplyTo(rs.getString("reply_to"));

      return template;
    }
  };
  /**
   * Unique identifier of template
   * <p>
   * Column id Sequence pn_template_id_seq
   */
  private int id;
  /**
   * Name of template
   */
  private String name;
  /**
   * Regexes to recognize affiliation to this template, if notifyTrigger waits for all regexes to execute notify, the
   * regexes are taken from here.
   * <p>
   * Relation saved in pn_template_regex
   */
  private Set<PerunNotifRegex> matchingRegexs;
  /**
   * Receivers to which message is send
   * <p>
   * Relation saved in pn_template_receiver
   */
  private Set<PerunNotifReceiver> receivers;
  /**
   * Holds primary properties which are parsed from message These properties are same for every auditerMessage which we
   * collect Example: User is same in every message but content is different Properties are key which is className of
   * object and values, which are properties retrievable from class
   * <p>
   * Column primary_properties
   */
  private Map<String, List<String>> primaryProperties;
  /**
   * Notify trigger based on which processing is executed
   * <p>
   * Column notify_trigger
   */
  private PerunNotifNotifyTrigger notifyTrigger;
  /**
   * Contains freemarker templates which are used to create content of result message
   */
  private List<PerunNotifTemplateMessage> perunNotifTemplateMessages;
  /**
   * Defines millis of oldest message waiting to be sent. If message is older messages is always sent. Can be disabled,
   * based on notifyTrigger
   * <p>
   * Column oldest_message_time
   */
  private Long oldestMessageTime;
  /**
   * Time to the youngest message, if message is younger we expect another message soon and we wait so we can aggregate
   * <p>
   * Column youngest_message_time
   */
  private Long youngestMessageTime;
  /**
   * String value which is used as sender of message
   * <p>
   * Column sender
   */
  private String sender;
  /**
   * String value which is used as reply_to in the email
   * <p>
   * Column reply_to
   */
  private String replyTo;

  public PerunNotifTemplate() {

    perunNotifTemplateMessages = new ArrayList<PerunNotifTemplateMessage>();
    primaryProperties = new HashMap<String, List<String>>();
    receivers = new HashSet<PerunNotifReceiver>();
    matchingRegexs = new HashSet<PerunNotifRegex>();
  }

  private static Map<String, List<String>> parseMapWithList(String row) {

    if ((row == null) || (row.isEmpty())) {
      return null;
    }

    Map<String, List<String>> result = new HashMap<String, List<String>>();

    try {
      String[] splittedValue = row.split(PerunNotifTemplate.MAP_DELIMITER);
      for (String entry : splittedValue) {
        String key = entry.substring(0, entry.indexOf("="));
        String values = entry.substring(entry.indexOf("=") + 1);
        String[] parsedValues = values.split(PerunNotifTemplate.LIST_DELIMITER);

        result.put(key, Arrays.asList(parsedValues));
      }
    } catch (StringIndexOutOfBoundsException ex) {
      return null;
    }
    return result;
  }

  public void addPerunNotifRegex(PerunNotifRegex regex) {

    if (matchingRegexs == null) {
      matchingRegexs = new HashSet<PerunNotifRegex>();
    }

    matchingRegexs.add(regex);
  }

  public void addPerunNotifTemplateMessage(PerunNotifTemplateMessage message) {

    if (perunNotifTemplateMessages == null) {
      perunNotifTemplateMessages = new ArrayList<PerunNotifTemplateMessage>();
    }

    perunNotifTemplateMessages.add(message);
  }

  public void addReceiver(PerunNotifReceiver receiver) {

    if (receivers == null) {
      receivers = Collections.synchronizedSet(new HashSet<PerunNotifReceiver>());
    }

    receivers.add(receiver);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PerunNotifTemplate)) {
      return false;
    }

    PerunNotifTemplate that = (PerunNotifTemplate) o;

    if (id != that.id) {
      return false;
    }

    return true;
  }

  public int getId() {
    return id;
  }

  public Set<PerunNotifRegex> getMatchingRegexs() {
    return matchingRegexs;
  }

  public String getName() {
    return name;
  }

  public PerunNotifNotifyTrigger getNotifyTrigger() {
    return notifyTrigger;
  }

  public Long getOldestMessageTime() {
    return oldestMessageTime;
  }

  public List<PerunNotifTemplateMessage> getPerunNotifTemplateMessages() {
    return perunNotifTemplateMessages;
  }

  public Map<String, List<String>> getPrimaryProperties() {
    return primaryProperties;
  }

  public Set<PerunNotifReceiver> getReceivers() {
    return receivers;
  }

  public String getReplyTo() {
    if (replyTo == null || replyTo.isEmpty()) {
      return getSender();
    }
    return replyTo;
  }

  public String getSender() {
    return sender;
  }

  public String getSerializedPrimaryProperties() {

    return serializePropertiesMap(primaryProperties);
  }

  public Long getYoungestMessageTime() {
    return youngestMessageTime;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (matchingRegexs != null ? matchingRegexs.hashCode() : 0);
    result = 31 * result + (receivers != null ? receivers.hashCode() : 0);
    result = 31 * result + (primaryProperties != null ? primaryProperties.hashCode() : 0);
    result = 31 * result + (notifyTrigger != null ? notifyTrigger.hashCode() : 0);
    result = 31 * result + (perunNotifTemplateMessages != null ? perunNotifTemplateMessages.hashCode() : 0);
    result = 31 * result + (oldestMessageTime != null ? oldestMessageTime.hashCode() : 0);
    result = 31 * result + (youngestMessageTime != null ? youngestMessageTime.hashCode() : 0);
    return result;
  }

  private String serializePropertiesMap(Map<String, List<String>> map) {

    StringBuilder builder = new StringBuilder();
    for (Iterator<String> keyIterator = map.keySet().iterator(); keyIterator.hasNext(); ) {
      String key = keyIterator.next();
      builder.append(key + "=");
      List<String> list = map.get(key);
      for (Iterator<String> listIter = list.iterator(); listIter.hasNext(); ) {
        String listItem = listIter.next();
        builder.append(listItem);
        if (listIter.hasNext()) {
          builder.append(LIST_DELIMITER);
        }
      }
      if (keyIterator.hasNext()) {
        builder.append(MAP_DELIMITER);
      }
    }

    return builder.toString();
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setMatchingRegexs(Set<PerunNotifRegex> matchingRegexs) {
    this.matchingRegexs = matchingRegexs;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNotifyTrigger(PerunNotifNotifyTrigger notifyTrigger) {
    this.notifyTrigger = notifyTrigger;
  }

  public void setOldestMessageTime(Long oldestMessageTime) {
    this.oldestMessageTime = oldestMessageTime;
  }

  public void setPerunNotifTemplateMessages(List<PerunNotifTemplateMessage> perunNotifTemplateMessages) {
    this.perunNotifTemplateMessages = perunNotifTemplateMessages;
  }

  public void setPrimaryProperties(Map<String, List<String>> primaryProperties) {
    this.primaryProperties = primaryProperties;
  }

  public void setReceivers(List<? extends PerunNotifReceiver> receivers) {
    if (receivers == null) {
      this.receivers = null;
    } else {
      Set<PerunNotifReceiver> newReceivers = Collections.synchronizedSet(new HashSet<PerunNotifReceiver>());
      newReceivers.addAll(receivers);
      this.receivers = newReceivers;
    }
  }

  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public void setYoungestMessageTime(Long youngestMessageTime) {
    this.youngestMessageTime = youngestMessageTime;
  }

  @Override
  public String toString() {
    return "id: " + getId() + " primary properties: " + getSerializedPrimaryProperties() + " notify trigger: " +
           getNotifyTrigger() + " youngest message time: " + getYoungestMessageTime() + " oldest message time: " +
           getOldestMessageTime() + " sender: " + getSender() + " reply_to: " + getReplyTo() + " name: " + getName();
  }

  public void update(PerunNotifTemplate updatedTemplate) {

    this.matchingRegexs = updatedTemplate.getMatchingRegexs();
    this.receivers = updatedTemplate.getReceivers();
    this.primaryProperties = updatedTemplate.getPrimaryProperties();
    this.notifyTrigger = updatedTemplate.getNotifyTrigger();
    this.perunNotifTemplateMessages = updatedTemplate.getPerunNotifTemplateMessages();
    this.oldestMessageTime = updatedTemplate.getOldestMessageTime();
    this.youngestMessageTime = updatedTemplate.getYoungestMessageTime();
    this.sender = updatedTemplate.getSender();
    this.replyTo = updatedTemplate.getReplyTo();
  }
}
