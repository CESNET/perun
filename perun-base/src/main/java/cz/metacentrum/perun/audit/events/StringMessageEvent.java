package cz.metacentrum.perun.audit.events;

/**
 * DO NOT USE THIS EVENT IN NEW LOGS!!!
 * <p>
 * Meaning of this event is to support resolved messages from attribute modules until their methods are changed to
 * return list of {@link AuditEvent}
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class StringMessageEvent extends AuditEvent {

  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public StringMessageEvent() {
  }

  public StringMessageEvent(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
