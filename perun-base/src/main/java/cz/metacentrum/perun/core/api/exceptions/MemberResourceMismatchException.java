package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the member and resource are not in the same VO
 * @author Zdenek Strmiska
 * @date 22.8.2017
 */

public class MemberResourceMismatchException extends PerunException {

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
    public MemberResourceMismatchException(Throwable cause) {
        super(cause);
    }

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
    public MemberResourceMismatchException(String message, Throwable cause) {
        super(message,cause);
    }

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
    public MemberResourceMismatchException(String message) {
        super(message);
    }

	/**
	 * Constructor with no arguments
	 */
	public MemberResourceMismatchException() {
    }
}
