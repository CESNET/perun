package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Owner;

/**
 * This exception is thrown when the owner does not exist in the database
 *
 * @author Slavek Licehammer
 */
public class OwnerNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Owner owner;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public OwnerNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public OwnerNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public OwnerNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the owner
	 * @param owner owner that does not exist
	 */
	public OwnerNotExistsException(Owner owner) {
		super(owner.toString());
		this.owner = owner;
	}

	/**
	 * Getter for the owner
	 * @return owner that does not exist
	 */
	public Owner getOwner() {
		return this.owner;
	}
}
