package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Host;

/**
 * This exception is to be thrown if host already exists in cluster.
 *
 * @author Slavek Licehammer
 */
public class HostExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Host host;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public HostExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the host
	 * @param host the host that already exists
	 */
	public HostExistsException(Host host) {
		super(host.toString());
		this.host = host;
	}

	/**
	 * Getter for the host
	 * @return the host that already exists
	 */
	public Host getHost() {
		return this.host;
	}
}
