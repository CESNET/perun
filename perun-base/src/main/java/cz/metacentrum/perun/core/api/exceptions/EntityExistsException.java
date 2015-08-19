package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.EntityExistsRuntimeException;

/**
 * Checked version of EntityExistsException. It represents parrent class for *ExistsException classes.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.EntityExistsRuntimeException
 * @author Slavek Licehammer
 */
public class EntityExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityExistsException(EntityExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public EntityExistsException(String message) {
		super(message);
	}

	public EntityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityExistsException(Throwable cause) {
		super(cause);
	}
}
