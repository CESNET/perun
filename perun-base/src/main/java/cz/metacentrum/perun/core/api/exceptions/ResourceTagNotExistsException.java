package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;

/**
 * This exception is thrown when trying to get a resourceTag that does not exist in the database
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ResourceTagNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceTagNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the resourceTag
	 * @param resourceTag resourceTag that does not exist
	 */
	public ResourceTagNotExistsException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	/**
	 * Getter for the resourceTag
	 * @return resourceTag that does not exist
	 */
	public ResourceTag getResourceTag() {
		return this.resourceTag;
	}
}
