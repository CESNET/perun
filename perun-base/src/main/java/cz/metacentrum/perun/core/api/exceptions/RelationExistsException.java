package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * This exception means that some relation or dependency exists which prevents to execute the action.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException
 * @author Slavek Licehammer
 */
public class RelationExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public RelationExistsException(RelationExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public RelationExistsException(String message) {
		super(message);
	}

	public RelationExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public RelationExistsException(Throwable cause) {
		super(cause);
	}
}
