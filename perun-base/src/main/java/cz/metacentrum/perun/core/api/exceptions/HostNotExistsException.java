package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.exceptions.rt.HostNotExistsRuntimeException;

/**
 * Checked version of HostNotExistsException.
 *
 * This exception is to be thrown in case we
 * are unable to look up the host computer in the Perun system.
 *
 * @author Michal Karm Babacek
 */
public class HostNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Host host;

	public HostNotExistsException(HostNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public HostNotExistsException(String message) {
		super(message);
	}

	public HostNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public HostNotExistsException(Throwable cause) {
		super(cause);
	}

	public HostNotExistsException(Host host) {
		super(host.toString());
		this.host = host;
	}

	public Host getHost() {
		return this.host;
	}
}
