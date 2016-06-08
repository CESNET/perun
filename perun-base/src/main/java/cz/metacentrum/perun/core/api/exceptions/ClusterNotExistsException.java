package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.ClusterNotExistsRuntimeException;


/**
 * Checked version of ClusterNotExistsException.
 *
 * This exception is to be thrown in case we
 * are unable to look up the cluster in the Perun system.
 *
 * @author Michal Karm Babacek
 */
public class ClusterNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	public ClusterNotExistsException(ClusterNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ClusterNotExistsException(String message) {
		super(message);
	}

	public ClusterNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClusterNotExistsException(Throwable cause) {
		super(cause);
	}
}
