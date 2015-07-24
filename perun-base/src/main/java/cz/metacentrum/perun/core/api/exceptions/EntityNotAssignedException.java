package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.EntityNotAssignedRuntimeException;

/**
 * Checked version of EntityNotAssignedException. It represents parrent class for *NotAssignedException classes.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.EntityNotAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class EntityNotAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityNotAssignedException(EntityNotAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public EntityNotAssignedException(String message) {
		super(message);
	}

	public EntityNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotAssignedException(Throwable cause) {
		super(cause);
	}
}
