package cz.metacentrum.perun.core.api;

@Deprecated
public enum ActionType {

  WRITE("write"),
  WRITE_VO("write_vo"),
  WRITE_PUBLIC("write_public"),
  READ("read"),
  READ_VO("read_vo"),
  READ_PUBLIC("read_public");

  private final String actionType;

  ActionType(String actionType) {
    this.actionType = actionType;
  }

  public String getActionType() {
    return actionType;
  }
}
