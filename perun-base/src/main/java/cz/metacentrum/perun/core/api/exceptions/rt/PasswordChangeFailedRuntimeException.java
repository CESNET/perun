package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of PasswordChangeFailedRuntimeException.
 *
 * @author Michal Prochazka
 */
public class PasswordChangeFailedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordChangeFailedRuntimeException(String message) {
		super(message);
	}

	public PasswordChangeFailedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordChangeFailedRuntimeException(Throwable cause) {
		super(cause);
	}
}
