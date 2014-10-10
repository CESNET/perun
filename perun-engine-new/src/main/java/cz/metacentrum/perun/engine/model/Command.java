package cz.metacentrum.perun.engine.model;

import java.io.Serializable;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class Command implements Serializable {

	private static final long serialVersionUID = -4022586349130091226L;

	public static final String SEND_STATS = "SEND_STATS";
	public static final String REFRESH_PROCESSING_RULES = "REFRESH_PROCESSING_RULES";
	public static final String SWITCH_OFF = "SWITCH_OFF";
	public static final String FORCE_SERVICE_PROPAGATION = "FORCE_SERVICE_PROPAGATION";

	private String command;
	private String[] parameters;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

}
