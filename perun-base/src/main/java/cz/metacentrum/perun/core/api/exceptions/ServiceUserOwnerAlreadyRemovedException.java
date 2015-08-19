package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceUserOwnerAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class ServiceUserOwnerAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public ServiceUserOwnerAlreadyRemovedException(String message) {
		super(message);
	}

	public ServiceUserOwnerAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUserOwnerAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
