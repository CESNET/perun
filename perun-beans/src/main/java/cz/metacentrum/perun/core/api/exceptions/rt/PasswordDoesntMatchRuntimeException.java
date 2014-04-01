package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of PasswordDoesntMatchRuntimeException.
 *
 * @author Michal Prochazka
 */
public class PasswordDoesntMatchRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public PasswordDoesntMatchRuntimeException(String message) {
		super(message);
	}

	public PasswordDoesntMatchRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordDoesntMatchRuntimeException(Throwable cause) {
		super(cause);
	}
}
