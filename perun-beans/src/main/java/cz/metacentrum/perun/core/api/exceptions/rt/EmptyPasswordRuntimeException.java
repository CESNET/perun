package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Checked version of EmptyPasswordRuntimeException.
 *
 * @author Michal Prochazka
 */
public class EmptyPasswordRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public EmptyPasswordRuntimeException(String message) {
		super(message);
	}

	public EmptyPasswordRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptyPasswordRuntimeException(Throwable cause) {
		super(cause);
	}
}
