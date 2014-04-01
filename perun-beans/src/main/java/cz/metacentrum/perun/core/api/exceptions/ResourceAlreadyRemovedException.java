package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ResourceAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class ResourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public ResourceAlreadyRemovedException(String message) {
		super(message);
	}

	public ResourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
