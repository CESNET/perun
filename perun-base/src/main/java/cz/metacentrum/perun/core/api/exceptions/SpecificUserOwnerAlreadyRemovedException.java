package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when trying to remove specificUser's owner who has already been removed as an owner
 *
 * @author Michal Stava
 */
public class SpecificUserOwnerAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SpecificUserOwnerAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserOwnerAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserOwnerAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
