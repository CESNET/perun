package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base of Perun checked exceptions.
 *
 * @author Martin Kuba
 */
public abstract class PerunException extends Exception {
	static final long serialVersionUID = 0;
ss
	private final static Logger log = LoggerFactory.getLogger("ultimate_logger");
	private String errorId = Long.toHexString(System.currentTimeMillis());

	public PerunException() {
		super();

		if (!(this instanceof InternalErrorException)) {
			log.debug("Exception {}: {}.", errorId, this);
		}
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PerunException(String message) {
		super(message);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Exception {}: {}.", errorId, this);
		}

	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PerunException(String message, Throwable cause) {
		super(message, cause);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Exception {}: {}.", errorId, this);
		}

	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PerunException(Throwable cause) {

		super(cause!=null?cause.getMessage():null,cause);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Exception {}: {}.", errorId, this);
		}
	}

	@Override
	public String getMessage() {
		return "Error "+errorId+": "+super.getMessage();
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}
}
