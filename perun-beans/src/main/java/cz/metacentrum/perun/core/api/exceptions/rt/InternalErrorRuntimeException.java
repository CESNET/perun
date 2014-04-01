package cz.metacentrum.perun.core.api.exceptions.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

@SuppressWarnings("serial")
public class InternalErrorRuntimeException extends PerunRuntimeException {

	private final static Logger log = LoggerFactory.getLogger(InternalErrorException.class);

	public InternalErrorRuntimeException(String err) {
		super(err);

		log.error("Runtime Internal Error Exception:", this);
	}

	public InternalErrorRuntimeException(Throwable cause) {
		super(cause);

		log.error("Runtime Internal Error Exception:", this);
	}

	public InternalErrorRuntimeException(String err, Throwable cause) {
		super(err, cause);

		log.error("Runtime Internal Error Exception:", this);
	}

}
