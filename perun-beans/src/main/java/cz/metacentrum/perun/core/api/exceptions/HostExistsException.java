package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Host;

/**
 * Checked version of HostExistsException.
 *
 * This exception is to be thrown if host already exists in cluster.
 *
 *
 * @author Slavek Licehammer
 */
public class HostExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Host host;

	public HostExistsException(String message) {
		super(message);
	}

	public HostExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public HostExistsException(Throwable cause) {
		super(cause);
	}

	public HostExistsException(Host host) {
		super(host.toString());
		this.host = host;
	}

	public Host getHost() {
		return this.host;
	}
}
