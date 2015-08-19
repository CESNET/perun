package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of LoginNotExistsRuntimeException.
 *
 * @author Michal Prochazka
 */
public class LoginNotExistsRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public LoginNotExistsRuntimeException(String message) {
		super(message);
	}

	public LoginNotExistsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}
}
