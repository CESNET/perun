package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class ExtSourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public ExtSourceAlreadyRemovedException(String message) {
		super(message);
	}

	public ExtSourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
