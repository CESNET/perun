package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of FacilityAlreadyRemovedException.
 * This exception is thrown when the facility has already been removed
 *
 * @author Michal Stava
 */
public class FacilityAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FacilityAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
