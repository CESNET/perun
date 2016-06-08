package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when some inconsistency in underlaying data sources occur.
 *
 * @author Slavek Licehammer
 */
public class ConsistencyErrorException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public ConsistencyErrorException(String message) {
		super(message);
	}

	public ConsistencyErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConsistencyErrorException(Throwable cause) {
		super(cause);
	}
}
