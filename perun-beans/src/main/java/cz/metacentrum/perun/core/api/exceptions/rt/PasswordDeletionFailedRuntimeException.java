package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of PasswordCreationFailedRuntimeException.
 *
 * @author Michal Prochazka
 */
public class PasswordDeletionFailedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordDeletionFailedRuntimeException(String message) {
		super(message);
	}

	public PasswordDeletionFailedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordDeletionFailedRuntimeException(Throwable cause) {
		super(cause);
	}
}
