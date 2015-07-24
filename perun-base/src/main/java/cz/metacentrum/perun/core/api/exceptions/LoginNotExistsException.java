package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of LoginNotExistsException.
 *
 * @author Michal Prochazka
 */
public class LoginNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public LoginNotExistsException(String message) {
		super(message);
	}

	public LoginNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginNotExistsException(Throwable cause) {
		super(cause);
	}
}
