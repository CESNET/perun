package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to get a policy which does not exist in the PerunPoliciesContainer
 *
 * @author Peter Balčirák
 */
public class PolicyNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PolicyNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PolicyNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PolicyNotExistsException(Throwable cause) {
		super(cause);
	}
}