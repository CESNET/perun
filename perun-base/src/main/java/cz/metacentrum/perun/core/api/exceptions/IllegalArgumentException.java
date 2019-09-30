package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

/**
 * This exception is thrown when there is an illegal value in the argument
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorException
 * @author Michal Šťava
 */
public class IllegalArgumentException extends InternalErrorException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(IllegalArgumentException.class);

	/**
	 * Constructor with InternalErrorRuntimeException
	 * @param rt the InternalErrorRuntimeException
	 */
	public IllegalArgumentException(InternalErrorRuntimeException rt) {
		super(rt.getMessage(),rt);

		log.error("Illegal Argument Exception:", this);
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public IllegalArgumentException(String message) {
		super(message);

		log.error("Illegal Argument Exception:", this);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public IllegalArgumentException(String message, Throwable cause) {
		super(message, cause);

		log.error("Illegal Argument Exception:", this);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public IllegalArgumentException(Throwable cause) {
		super(cause);

		log.error("Illegal Argument Exception:", this);
	}
}
