package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.EntityAlreadyAssignedRuntimeException;

/**
 * Checked version of EntityAllreadyAssignedException. It represents parrent class for *AllreadyAssignedException classes.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.EntityAlreadyAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class EntityAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityAlreadyAssignedException(EntityAlreadyAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public EntityAlreadyAssignedException(String message) {
		super(message);
	}

	public EntityAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityAlreadyAssignedException(Throwable cause) {
		super(cause);
	}
}
