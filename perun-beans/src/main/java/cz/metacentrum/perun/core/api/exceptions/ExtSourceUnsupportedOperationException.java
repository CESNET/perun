package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceUnsupportedOperationException.
 *
 * @author Michal Prochazka
 */
public class ExtSourceUnsupportedOperationException extends PerunException {
	static final long serialVersionUID = 0;

	public ExtSourceUnsupportedOperationException() {
	}

	public ExtSourceUnsupportedOperationException(String message) {
		super(message);
	}

	public ExtSourceUnsupportedOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceUnsupportedOperationException(Throwable cause) {
		super(cause);
	}
}
