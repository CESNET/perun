package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when in the name of entity is any diacritic symbol.
 *
 * @author Michal Šťava
 */
public class DiacriticNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public DiacriticNotAllowedException(String message) {
		super(message);
	}

	public DiacriticNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiacriticNotAllowedException(Throwable cause) {
		super(cause);
	}
}
