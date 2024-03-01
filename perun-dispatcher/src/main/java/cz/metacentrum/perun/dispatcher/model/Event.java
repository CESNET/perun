package cz.metacentrum.perun.dispatcher.model;

import cz.metacentrum.perun.audit.events.AuditEvent;
import java.util.Date;
import java.util.Objects;

/**
 * Event object is used to pass audited events from Perun to Dispatcher through EventQueue.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.processing.AuditerListener
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 */
public class Event {

  private String header;
  private AuditEvent data;
  private long timeStamp;

  /**
   * Get event data
   *
   * @return event data
   */
  public AuditEvent getData() {
    return data;
  }

  /**
   * Set event data
   *
   * @param data event data
   */
  public void setData(AuditEvent data) {
    this.data = data;
  }

  /**
   * Get event timestamp
   *
   * @return timestamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  /**
   * Set event timestamp
   *
   * @param timeStamp timestamp
   */
  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * Get event header
   *
   * @return header
   */
  public String getHeader() {
    return header;
  }

  /**
   * Set event header
   *
   * @param header header
   */
  public void setHeader(String header) {
    this.header = header;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return timeStamp == event.timeStamp &&
        Objects.equals(header, event.header) &&
        Objects.equals(data, event.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(header, data, timeStamp);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("[").append(new Date(timeStamp).toString())
        .append("][").append(header).append("][").append(data).append("]");
    return str.toString();
  }

}
