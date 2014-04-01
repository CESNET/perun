package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of HostAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class HostAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public HostAlreadyRemovedException(String message) {
		super(message);
	}

	public HostAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public HostAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
