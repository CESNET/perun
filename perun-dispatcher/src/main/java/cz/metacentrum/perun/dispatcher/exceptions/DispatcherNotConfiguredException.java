package cz.metacentrum.perun.dispatcher.exceptions;

import cz.metacentrum.perun.dispatcher.exceptions.DispatcherException;

/**
 * Checked version of DispatcherNotConfiguredException.
 * 
 * @author Michal Karm Babacek
 */
public class DispatcherNotConfiguredException extends DispatcherException {

	private static final long serialVersionUID = 1L;

	public DispatcherNotConfiguredException(String message) {
		super(message);
	}

	public DispatcherNotConfiguredException(String message, Throwable cause) {
		super(message, cause);
	}

	public DispatcherNotConfiguredException(Throwable cause) {
		super(cause);
	}
}
