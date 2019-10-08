package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Host;

/**
 * This exception is to be thrown in case we
 * are unable to look up the host computer in the Perun system.
 *
 * @author Michal Karm Babacek
 */
public class HostNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Host host;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public HostNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the host
	 * @param host the host that doesn't exist
	 */
	public HostNotExistsException(Host host) {
		super(host.toString());
		this.host = host;
	}

	/**
	 * Getter for the host
	 * @return the host that doesn't exist
	 */
	public Host getHost() {
		return this.host;
	}
}
