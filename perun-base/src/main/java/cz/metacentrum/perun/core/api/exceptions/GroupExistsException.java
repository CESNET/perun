package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.GroupExistsRuntimeException;

/**
 * Checked version of GroupExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupExistsRuntimeException
 * @author Martin Kuba
 */
public class GroupExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;


	public GroupExistsException(GroupExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public GroupExistsException(String message) {
		super(message);
	}

	public GroupExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupExistsException(Throwable cause) {
		super(cause);
	}
}
