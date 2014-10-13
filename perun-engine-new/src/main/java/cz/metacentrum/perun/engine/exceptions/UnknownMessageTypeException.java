package cz.metacentrum.perun.engine.exceptions;

/**
 * Checked version of UnknownMessageTypeException.
 * 
 * @author Michal Karm Babacek
 */
public class UnknownMessageTypeException extends EngineException {

	private static final long serialVersionUID = 1L;

	public UnknownMessageTypeException(String message) {
		super(message);
	}

	public UnknownMessageTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownMessageTypeException(Throwable cause) {
		super(cause);
	}
}
