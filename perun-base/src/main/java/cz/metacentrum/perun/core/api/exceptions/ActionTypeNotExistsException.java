package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ActionType;

/**
 * @author Michal Šťava <stavamichal@gmail.com>
 *
 * This exception is thrown when ActionType doesn't exist.
 */
public class ActionTypeNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ActionType actionType;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ActionTypeNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ActionTypeNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ActionTypeNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with an ActionType
	 * @param actionType ActionType that doesn't exist
	 */
	public ActionTypeNotExistsException(ActionType actionType) {
		super(actionType.toString());
		this.actionType = actionType;
	}

	/**
	 * Getter for ActionType
	 * @return ActionType which doesn't exist
	 */
	public ActionType getActionType() {
		return this.actionType;
	}
}
