package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when user tries to join two identities, which already belongs to him.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class IdentitiesAlreadyJoinedException extends PerunException {

	private static final long serialVersionUID = 1L;

	public IdentitiesAlreadyJoinedException(String message) {
		super(message);
	}

	public IdentitiesAlreadyJoinedException(String message, Throwable ex) {
		super(message, ex);
	}

}
