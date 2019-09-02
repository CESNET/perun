package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;

/**
 * This exception is thrown if the ExtSource does not exist
 *
 * @author Slavek Licehammer
 */
public class ExtSourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the ExtSource
	 * @param extSource extSource that does not exist
	 */
	public ExtSourceNotExistsException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	/**
	 * Getter for the ExtSource
	 * @return extSource that does not exist
	 */
	public ExtSource getExtSource() {
		return this.extSource;
	}
}
