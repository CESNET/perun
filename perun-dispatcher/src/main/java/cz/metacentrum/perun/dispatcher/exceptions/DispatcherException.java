package cz.metacentrum.perun.dispatcher.exceptions;

import org.slf4j.LoggerFactory;

/**
 * The base of Perun-Dispatcher checked exceptions.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public abstract class DispatcherException extends Exception {

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(DispatcherException.class);

	static final long serialVersionUID = 0;

	private String errorId = Long.toHexString(System.currentTimeMillis());

	public DispatcherException() {
		super();
		logger.error("Error ID: " + errorId, this);
	}

	public DispatcherException(String message) {
		super(message);
		logger.error("Error ID: " + errorId, this);
	}

	public DispatcherException(String message, Throwable cause) {
		super(message, cause);
		logger.error("Error ID: " + errorId, this);
	}

	public DispatcherException(Throwable cause) {
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
