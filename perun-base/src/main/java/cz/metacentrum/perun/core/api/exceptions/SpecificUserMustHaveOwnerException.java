package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * This exception means that serviceUser can't exist without any assigned user.
 *
 * @author Michal Stava
 */
public class SpecificUserMustHaveOwnerException extends PerunException {
	static final long serialVersionUID = 0;

	public SpecificUserMustHaveOwnerException(String message) {
		super(message);
	}

	public SpecificUserMustHaveOwnerException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecificUserMustHaveOwnerException(Throwable cause) {
		super(cause);
	}
}
