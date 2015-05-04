package cz.metacentrum.perun.engine.exceptions;

/**
 * Checked version of UnknownCommandException.
 * 
 * @author Michal Karm Babacek
 */
public class UnknownCommandException extends EngineException {

	private static final long serialVersionUID = 1L;

	public UnknownCommandException(String message) {
		super(message);
	}

	public UnknownCommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownCommandException(Throwable cause) {
		super(cause);
	}
}
