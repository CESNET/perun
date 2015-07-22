package cz.metacentrum.perun.engine.exceptions;

/**
 * Checked version of EngineNotConfiguredException.
 * 
 * @author Michal Karm Babacek
 */
public class EngineNotConfiguredException extends EngineException {

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
