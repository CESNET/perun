package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ActionType;

/**
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class ActionTypeNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ActionType actionType;

	public ActionTypeNotExistsException(String message) {
		super(message);
	}

	public ActionTypeNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionTypeNotExistsException(Throwable cause) {
		super(cause);
	}

	public ActionTypeNotExistsException(ActionType actionType) {
		super(actionType.toString());
		this.actionType = actionType;
	}

	public ActionType getActionType() {
		return this.actionType;
	}
}
