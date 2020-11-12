package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the user doesn't have the chosen attribute for mail set,
 * where password reset link should be send to.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class PasswordResetMailNotExistsException extends PerunException {

	public PasswordResetMailNotExistsException(String message) {
		super(message);
	}

	public PasswordResetMailNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordResetMailNotExistsException(Throwable cause) {
		super(cause);
	}
}
