package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception is thrown when the attribute does not exist in underlying data source.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributeNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AttributeNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeNotExistsException(Throwable cause) {
		super(cause);
	}

}
