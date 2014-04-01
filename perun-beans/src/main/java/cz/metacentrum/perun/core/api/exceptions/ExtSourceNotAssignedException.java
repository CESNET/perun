package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.ExtSourceNotAssignedRuntimeException;

/**
 * Checked version of ExtSourceNotAssignedException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ExtSourceNotAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class ExtSourceNotAssignedException extends EntityNotAssignedException {
	static final long serialVersionUID = 0;

	public ExtSourceNotAssignedException(ExtSourceNotAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ExtSourceNotAssignedException(String message) {
		super(message);
	}

	public ExtSourceNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceNotAssignedException(Throwable cause) {
		super(cause);
	}
}
