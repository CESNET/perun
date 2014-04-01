package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of PasswordCreationFailedRuntimeException.
 *
 * @author Michal Prochazka
 */
public class PasswordCreationFailedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordCreationFailedRuntimeException(String message) {
		super(message);
	}

	public PasswordCreationFailedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordCreationFailedRuntimeException(Throwable cause) {
		super(cause);
	}
}
