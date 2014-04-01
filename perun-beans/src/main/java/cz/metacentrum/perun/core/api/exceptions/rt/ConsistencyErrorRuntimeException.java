package cz.metacentrum.perun.core.api.exceptions.rt;


/**
 * Runtime version of ConsistencyErrorException
 *
 * @author Slavek Licehammer
 */
public class ConsistencyErrorRuntimeException extends InternalErrorRuntimeException {
	static final long serialVersionUID = 0;


	public ConsistencyErrorRuntimeException(String message) {
		super(message);
	}

	public ConsistencyErrorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConsistencyErrorRuntimeException(Throwable cause) {
		super(cause);
	}
}
