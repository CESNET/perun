package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeChangedForMultipleUsers extends AuditEvent {

  private Attribute attribute;
  private List<User> users;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeChangedForMultipleUsers() {
  }

  public AttributeChangedForMultipleUsers(Attribute attribute, List<User> users) {
    this.attribute = attribute;
    this.users = users;
    String serializedUsers = users.stream().map(User::serializeToString).collect(Collectors.joining(","));
    serializedUsers = "[" + serializedUsers + "]";
    this.message = formatMessage("%s changed for %s.", attribute, serializedUsers);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public List<User> getUsers() {
    return users;
  }

  @Override
  public String toString() {
    return message;
  }
}
