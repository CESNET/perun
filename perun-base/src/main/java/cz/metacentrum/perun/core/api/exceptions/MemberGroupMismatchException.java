package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the member and group are not in the same VO
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MemberGroupMismatchException extends PerunException {

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberGroupMismatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberGroupMismatchException(String message, Throwable cause) {
		super(message,cause);
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MemberGroupMismatchException(String message) {
		super(message);
	}

	/**
	 * Constructor with no arguments
	 */
	public MemberGroupMismatchException() {
	}

}
