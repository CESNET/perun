package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of LoginNotExistsException.
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
public class LoginExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public LoginExistsException(String message) {
		super(message);
	}

	public LoginExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginExistsException(Throwable cause) {
		super(cause);
	}
}
