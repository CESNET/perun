package cz.metacentrum.perun.engine.model;

import java.io.Serializable;

/**
 * Intended to retrieve commands via the JMS messaging, not yet implemented
 *
 * @author Michal Karm Babacek
 */
public class Command implements Serializable {

  public static final String SEND_STATS = "SEND_STATS";
  public static final String REFRESH_PROCESSING_RULES = "REFRESH_PROCESSING_RULES";
  public static final String SWITCH_OFF = "SWITCH_OFF";
  public static final String FORCE_SERVICE_PROPAGATION = "FORCE_SERVICE_PROPAGATION";
  private static final long serialVersionUID = -4022586349130091226L;
  private String command;
  private String[] parameters;

  public String getCommand() {
    return command;
  }

  public String[] getParameters() {
    return parameters;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

}
