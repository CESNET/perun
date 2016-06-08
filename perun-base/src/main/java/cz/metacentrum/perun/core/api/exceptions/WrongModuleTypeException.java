package cz.metacentrum.perun.core.api.exceptions;


/**
 * Raised when workning with module which doesn't have expected type.
 *
 * @author Slavek Licehammer
 */
public class WrongModuleTypeException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public WrongModuleTypeException(String message) {
		super(message);
	}

	public WrongModuleTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongModuleTypeException(Throwable cause) {
		super(cause);
	}
}
