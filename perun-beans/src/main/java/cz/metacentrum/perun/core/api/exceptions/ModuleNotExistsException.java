package cz.metacentrum.perun.core.api.exceptions;


/**
 * Module not exists.
 *
 * @author Slavek Licehammer
 */
public class ModuleNotExistsException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public ModuleNotExistsException(String message) {
		super(message);
	}

	public ModuleNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleNotExistsException(Throwable cause) {
		super(cause);
	}
}
