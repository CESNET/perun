package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;

/**
 * This exception is thrown when creating an ExtSource which already exists
 *
 * @author Slavek Licehammer
 */
public class ExtSourceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the ExtSource
	 * @param extSource extSource that already exists
	 */
	public ExtSourceExistsException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	/**
	 * Getter for the ExtSource
	 * @return extSource that already exists
	 */
	public ExtSource getExtSource() {
		return this.extSource;
	}
}
