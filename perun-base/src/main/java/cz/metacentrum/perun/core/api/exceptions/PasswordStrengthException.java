package cz.metacentrum.perun.core.api.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * This exception is thrown when password fails strength check required by the namespace
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordStrengthException extends PerunException {

	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordStrengthException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordStrengthException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordStrengthException(Throwable cause) {
		super(cause);
	}

}
