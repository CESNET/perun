package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when operation with password in backend exceeds expected limit.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordOperationTimeoutException extends PerunException {
	static final long serialVersionUID = 0;

	public PasswordOperationTimeoutException(String message) {
		super(message);
	}

	public PasswordOperationTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordOperationTimeoutException(Throwable cause) {
		super(cause);
	}
}
