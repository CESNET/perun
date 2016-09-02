package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when password doesn't match expected strength requirements.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordStrengthFailedException extends PerunException {
	static final long serialVersionUID = 0;

	public PasswordStrengthFailedException(String message) {
		super(message);
	}

	public PasswordStrengthFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordStrengthFailedException(Throwable cause) {
		super(cause);
	}
}
