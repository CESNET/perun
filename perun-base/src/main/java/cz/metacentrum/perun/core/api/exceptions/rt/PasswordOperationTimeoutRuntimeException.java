package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Thrown when operation with password in backend exceeds expected limit.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordOperationTimeoutRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordOperationTimeoutRuntimeException(String message) {
		super(message);
	}

	public PasswordOperationTimeoutRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordOperationTimeoutRuntimeException(Throwable cause) {
		super(cause);
	}
}
