package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the operation is not supported for this kind of ExtSource
 *
 * @author Michal Prochazka
 */
public class ExtSourceUnsupportedOperationException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Constructor with no parameters
	 */
	public ExtSourceUnsupportedOperationException() {
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceUnsupportedOperationException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceUnsupportedOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceUnsupportedOperationException(Throwable cause) {
		super(cause);
	}
}
