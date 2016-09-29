package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Thrown when password doesn't match expected strength requirements.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordStrengthFailedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordStrengthFailedRuntimeException(String message) {
		super(message);
	}

	public PasswordStrengthFailedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordStrengthFailedRuntimeException(Throwable cause) {
		super(cause);
	}
}
