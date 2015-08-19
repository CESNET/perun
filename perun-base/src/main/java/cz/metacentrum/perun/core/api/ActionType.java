package cz.metacentrum.perun.core.api;

public enum ActionType {

	WRITE ("write"),
	READ ("read");

	private final String actionType;

	ActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}
}
