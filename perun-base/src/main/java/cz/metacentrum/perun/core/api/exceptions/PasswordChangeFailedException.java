package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of PasswordChangeFailedException.
 *
 * @author Michal Prochazka
 */
public class PasswordChangeFailedException extends PerunException {
	static final long serialVersionUID = 0;

	public PasswordChangeFailedException(String message) {
		super(message);
	}

	public PasswordChangeFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordChangeFailedException(Throwable cause) {
		super(cause);
	}
}
