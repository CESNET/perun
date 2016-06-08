package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceInUseException.
 *
 * @author Michal Prochazka
 */
public class ExtSourceInUseException extends PerunException {
	static final long serialVersionUID = 0;

	public ExtSourceInUseException(String message) {
		super(message);
	}

	public ExtSourceInUseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceInUseException(Throwable cause) {
		super(cause);
	}
}
