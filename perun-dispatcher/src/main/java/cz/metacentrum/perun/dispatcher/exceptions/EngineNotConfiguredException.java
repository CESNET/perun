package cz.metacentrum.perun.dispatcher.exceptions;

import cz.metacentrum.perun.dispatcher.exceptions.DispatcherException;

/**
 * Checked version of EngineNotConfiguredException.
 * 
 * @author Michal Karm Babacek
 */
public class EngineNotConfiguredException extends DispatcherException {

	private static final long serialVersionUID = 1L;

	public EngineNotConfiguredException(String message) {
		super(message);
	}

	public EngineNotConfiguredException(String message, Throwable cause) {
		super(message, cause);
	}

	public EngineNotConfiguredException(Throwable cause) {
		super(cause);
	}
}
