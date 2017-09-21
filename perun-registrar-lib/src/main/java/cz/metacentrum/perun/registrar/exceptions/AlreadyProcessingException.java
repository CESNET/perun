package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Custom exception thrown by Registrar when user submits same application, which is already
 * processed by another thread. Prevents multiple application creation when code doesn't see
 * uncommitted data in DB and passes inner tests.
 *
 * This exception is silently skipped in GUI
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class AlreadyProcessingException extends PerunException {

	private static final long serialVersionUID = 1L;

	public AlreadyProcessingException(String message) {
		super(message);
	}

	public AlreadyProcessingException(String message, Throwable ex) {
		super(message, ex);
	}

}
