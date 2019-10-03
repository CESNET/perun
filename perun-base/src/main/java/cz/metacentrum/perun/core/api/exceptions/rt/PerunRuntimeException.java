package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PerunRuntimeException extends RuntimeException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger("ultimate_logger");

	private String errorId = Long.toHexString(System.currentTimeMillis());

	public PerunRuntimeException() {

		super();

		if (!(this instanceof InternalErrorException)) {
			log.debug("Runtime Exception {}: {}.", errorId, this);
		}
	}

	public PerunRuntimeException(String err) {

		super(err);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Runtime Exception {}: {}.", errorId, this);
		}
	}

	public PerunRuntimeException(Throwable cause) {
		super(cause!=null?cause.getMessage():null, cause);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Runtime Exception {}: {}.", errorId, this);
		}
	}

	public PerunRuntimeException(String err, Throwable cause) {
		super(err, cause);

		if (!(this instanceof InternalErrorException)) {
			log.debug("Runtime Exception {}: {}.", errorId, this);
		}
	}

	public String getErrorId() {
		return errorId;
	}

	public String getType() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getMessage() {
		return "Error "+errorId+": "+super.getMessage();
	}
}
