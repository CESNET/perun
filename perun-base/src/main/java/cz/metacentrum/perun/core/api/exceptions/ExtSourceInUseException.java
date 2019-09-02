package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceInUseException.
 * Exception is thrown when the ExtSource is in use
 *
 * @author Michal Prochazka
 */
public class ExtSourceInUseException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceInUseException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceInUseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceInUseException(Throwable cause) {
		super(cause);
	}
}
