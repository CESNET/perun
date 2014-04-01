package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceUserAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class ServiceUserAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public ServiceUserAlreadyRemovedException(String message) {
		super(message);
	}

	public ServiceUserAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUserAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
