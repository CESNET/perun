package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of PasswordDoesntMatchException.
 *
 * @author Michal Prochazka
 */
public class PasswordDoesntMatchException extends PerunException {
	static final long serialVersionUID = 0;

	public PasswordDoesntMatchException(String message) {
		super(message);
	}

	public PasswordDoesntMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordDoesntMatchException(Throwable cause) {
		super(cause);
	}
}
