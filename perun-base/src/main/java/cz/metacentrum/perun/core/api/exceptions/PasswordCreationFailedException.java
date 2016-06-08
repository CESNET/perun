package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of PasswordCreationFailedException.
 *
 * @author Michal Prochazka
 */
public class PasswordCreationFailedException extends PerunException {
	static final long serialVersionUID = 0;

	public PasswordCreationFailedException(String message) {
		super(message);
	}

	public PasswordCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordCreationFailedException(Throwable cause) {
		super(cause);
	}
}
