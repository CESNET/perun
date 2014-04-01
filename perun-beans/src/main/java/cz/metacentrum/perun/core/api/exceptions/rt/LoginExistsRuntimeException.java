package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of LoginExistsRuntimeException.
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
public class LoginExistsRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public LoginExistsRuntimeException(String message) {
		super(message);
	}

	public LoginExistsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginExistsRuntimeException(Throwable cause) {
		super(cause);
	}
}
