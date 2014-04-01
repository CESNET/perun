package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.EntityNotExistsRuntimeException;

/**
 * Checked version of EntityNotExistsException. It represents parrent class for *NotExistsException classes.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.EntityNotExistsRuntimeException
 * @author Slavek Licehammer
 */
public class EntityNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityNotExistsException(EntityNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public EntityNotExistsException(String message) {
		super(message);
	}

	public EntityNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotExistsException(Throwable cause) {
		super(cause);
	}
}
