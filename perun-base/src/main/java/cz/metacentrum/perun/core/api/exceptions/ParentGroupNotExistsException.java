package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.ParentGroupNotExistsRuntimeException;

/**
 * Checked version of GroupExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupExistsRuntimeException
 * @author Martin Kuba
 */
public class ParentGroupNotExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;


	public ParentGroupNotExistsException(ParentGroupNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ParentGroupNotExistsException(String message) {
		super(message);
	}

	public ParentGroupNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParentGroupNotExistsException(Throwable cause) {
		super(cause);
	}
}
