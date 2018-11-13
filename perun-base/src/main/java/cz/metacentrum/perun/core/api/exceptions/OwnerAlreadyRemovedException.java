package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of OwnerAlreadyRemovedException.
 *
 * @author Slavek Licehammer
 */
public class OwnerAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public OwnerAlreadyRemovedException(String message) {
		super(message);
	}

	public OwnerAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwnerAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
