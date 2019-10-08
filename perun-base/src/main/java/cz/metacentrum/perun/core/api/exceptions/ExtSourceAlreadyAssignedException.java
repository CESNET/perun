package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;

/**
 * This exception is thrown when the ExtSource is already associated with the Vo/group.
 *
 * @author Slavek Licehammer
 */
public class ExtSourceAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	/**
	 * Constructor with the ExtSource
	 * @param extSource extSource that has already been assigned
	 */
	public ExtSourceAlreadyAssignedException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Getter for the ExtSource
	 * @return extSource that has already been assigned
	 */
	public ExtSource getExtSource() {
		return extSource;
	}
}
