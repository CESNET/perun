package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.OwnerAlreadyAssignedRuntimeException;

/**
 * Checked version of OwnerAlreadyAssignedException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.OwnerAlreadyAssignedException
 * @author Slavek Licehammer
 */
public class OwnerAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public OwnerAlreadyAssignedException(OwnerAlreadyAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public OwnerAlreadyAssignedException(String message) {
		super(message);
	}

	public OwnerAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwnerAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

}
