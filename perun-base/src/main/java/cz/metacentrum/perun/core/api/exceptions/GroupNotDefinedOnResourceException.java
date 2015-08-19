package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.GroupNotDefinedOnResourceRuntimeException;

/**
 * Checked version of GroupNotDefinedOnResourceRuntimeException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupNotDefinedOnResourceRuntimeException
 * @author Martin Kuba
 */
public class GroupNotDefinedOnResourceException extends PerunException {
	static final long serialVersionUID = 0;


	public GroupNotDefinedOnResourceException(GroupNotDefinedOnResourceRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public GroupNotDefinedOnResourceException(String message) {
		super(message);
	}

	public GroupNotDefinedOnResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupNotDefinedOnResourceException(Throwable cause) {
		super(cause);
	}
}
