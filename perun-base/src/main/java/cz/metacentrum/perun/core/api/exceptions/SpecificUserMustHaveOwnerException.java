package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception means that serviceUser can't exist without any assigned user.
 *
 * @author Michal Stava
 */
public class SpecificUserMustHaveOwnerException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SpecificUserMustHaveOwnerException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserMustHaveOwnerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserMustHaveOwnerException(Throwable cause) {
		super(cause);
	}
}
