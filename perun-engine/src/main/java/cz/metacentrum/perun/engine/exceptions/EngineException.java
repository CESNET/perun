package cz.metacentrum.perun.engine.exceptions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base of Perun-Engine checked exceptions.
 * 
 * @author Michal Karm Babacek
 */
public abstract class EngineException extends Exception {
	static final long serialVersionUID = 0;

	static Logger logger = LoggerFactory.getLogger(EngineException.class);
	private String errorId = Long.toHexString(System.currentTimeMillis());

	public EngineException() {
		super();
		logger.error("Error ID: " + errorId, this);
	}

	public EngineException(String message) {
		super(message);
		logger.error("Error ID: " + errorId, this);
	}

	public EngineException(String message, Throwable cause) {
		super(message, cause);
		logger.error("Error ID: " + errorId, this);
	}

	public EngineException(Throwable cause) {
		super(cause != null ? cause.getMessage() : null, cause);
		logger.error("Error ID: " + errorId, this);
	}

	@Override
	public String getMessage() {
		return "Error " + errorId + ": " + super.getMessage();
	}

	public String getErrorId() {
		return errorId;
	}
}
