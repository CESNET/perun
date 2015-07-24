package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when in the name of entity is any special char.
 *
 * @author Michal Šťava
 */
public class SpecialCharsNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public SpecialCharsNotAllowedException(String message) {
		super(message);
	}

	public SpecialCharsNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecialCharsNotAllowedException(Throwable cause) {
		super(cause);
	}
}
