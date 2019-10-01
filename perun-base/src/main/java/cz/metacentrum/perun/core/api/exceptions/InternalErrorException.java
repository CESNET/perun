package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class representing an exception caused by an unexpected error.
 *
 * @author Martin Kuba
 */
public class InternalErrorException extends PerunRuntimeException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(InternalErrorException.class);

	public InternalErrorException(String message) {
		super(message);

		log.error("Internal Error Exception:", this);
	}

	public InternalErrorException(String message, Throwable cause) {
		super(message, cause);

		log.error("Internal Error Exception:", this);
	}

	public InternalErrorException(Throwable cause) {
		super(cause);

		log.error("Internal Error Exception:", this);
	}
}
