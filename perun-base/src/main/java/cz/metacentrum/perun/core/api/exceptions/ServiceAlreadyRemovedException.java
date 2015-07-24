package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class ServiceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public ServiceAlreadyRemovedException(String message) {
		super(message);
	}

	public ServiceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
