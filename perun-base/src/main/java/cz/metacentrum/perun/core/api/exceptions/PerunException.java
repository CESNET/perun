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

	private final static Logger log = LoggerFactory.getLogger(PerunException.class);
	private String errorId = Long.toHexString(System.currentTimeMillis());

	public PerunException() {
		super();

		if (!(this instanceof InternalErrorException)) {
			log.warn("Exception {}: {}.", errorId, this);
			if (log.isDebugEnabled()) {
				log.debug("Exception detail:", this);
			}
		}
	}

	public PerunException(String message) {
		super(message);

		if (!(this instanceof InternalErrorException)) {
			log.warn("Exception {}: {}.", errorId, this);
			if (log.isDebugEnabled()) {
				log.debug("Exception detail:", this);
			}
		}
	}

	public PerunException(String message, Throwable cause) {
		super(message, cause);

		if (!(this instanceof InternalErrorException)) {
			log.warn("Exception {}: {}.", errorId, this);
			if (log.isDebugEnabled()) {
				log.debug("Exception detail:", this);
			}
		}
	}

	public PerunException(Throwable cause) {
		super(cause!=null?cause.getMessage():null,cause);
		if (!(this instanceof InternalErrorException)) {
			log.warn("Exception {}: {}.", errorId, this);
			if (log.isDebugEnabled()) {
				log.debug("Exception detail:", this);
			}
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
