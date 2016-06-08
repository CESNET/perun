package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceUserOwnerAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class SpecificUserOwnerAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public SpecificUserOwnerAlreadyRemovedException(String message) {
		super(message);
	}

	public SpecificUserOwnerAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecificUserOwnerAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
