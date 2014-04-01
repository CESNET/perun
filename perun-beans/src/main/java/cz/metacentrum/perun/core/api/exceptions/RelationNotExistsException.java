package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * This exception means that some relation or dependency not exists which prevents to execute the action.
 *
 * @author Michal Stava
 */
public class RelationNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public RelationNotExistsException(String message) {
		super(message);
	}

	public RelationNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public RelationNotExistsException(Throwable cause) {
		super(cause);
	}
}
