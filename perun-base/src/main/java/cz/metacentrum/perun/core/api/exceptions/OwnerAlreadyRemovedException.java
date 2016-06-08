package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.OwnerAlreadyRemovedRuntimeException;

/**
 * Checked version of OwnerAlreadyRemovedException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.OwnerAlreadyRemovedException
 * @author Slavek Licehammer
 */
public class OwnerAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public OwnerAlreadyRemovedException(OwnerAlreadyRemovedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public OwnerAlreadyRemovedException(String message) {
		super(message);
	}

	public OwnerAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwnerAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
