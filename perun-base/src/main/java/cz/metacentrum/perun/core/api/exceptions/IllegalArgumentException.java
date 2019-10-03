package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Exception, which is using for illegal value in some argument
 *
 * @author Michal Šťava
 */
public class IllegalArgumentException extends InternalErrorException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(IllegalArgumentException.class);

	public IllegalArgumentException(String message) {
		super(message);

		log.error("Illegal Argument Exception:", this);
	}

	public IllegalArgumentException(String message, Throwable cause) {
		super(message, cause);

		log.error("Illegal Argument Exception:", this);
	}

	public IllegalArgumentException(Throwable cause) {
		super(cause);

		log.error("Illegal Argument Exception:", this);
	}
}
