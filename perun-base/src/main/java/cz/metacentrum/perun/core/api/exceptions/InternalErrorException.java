package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

/**
 * Checked version of InternalErrorException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException
 * @author Martin Kuba
 */
public class InternalErrorException extends PerunException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(InternalErrorException.class);

	public InternalErrorException(InternalErrorRuntimeException rt) {
		super(rt.getMessage(),rt);

		log.error("Internal Error Exception:", this);
	}

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
