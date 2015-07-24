package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

/**
 * Exception, which is using for bad range of messages count for audit messages.
 *
 * @author Michal Šťava
 */
public class WrongRangeOfCountException extends IllegalArgumentException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(IllegalArgumentException.class);

	public WrongRangeOfCountException(String message) {
		super(message);

		log.error("Illegal Argument Exception:", this);
	}

	public WrongRangeOfCountException(String message, Throwable cause) {
		super(message, cause);

		log.error("Illegal Argument Exception:", this);
	}

	public WrongRangeOfCountException(Throwable cause) {
		super(cause);

		log.error("Illegal Argument Exception:", this);
	}
}
