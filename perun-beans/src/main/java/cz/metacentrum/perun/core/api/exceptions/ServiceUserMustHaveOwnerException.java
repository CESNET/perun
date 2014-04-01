package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * This exception means that serviceUser can't exist without any assigned user.
 *
 * @author Michal Stava
 */
public class ServiceUserMustHaveOwnerException extends PerunException {
	static final long serialVersionUID = 0;

	public ServiceUserMustHaveOwnerException(String message) {
		super(message);
	}

	public ServiceUserMustHaveOwnerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUserMustHaveOwnerException(Throwable cause) {
		super(cause);
	}
}
