package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;

/**
 * This exception is thrown when trying to remove a resourceTag which has already been removed
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ResourceTagNotAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceTagNotAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagNotAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the resourceTag
	 * @param resourceTag resourceTag that has already been removed
	 */
	public ResourceTagNotAssignedException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	/**
	 * Getter for the resourceTag
	 * @return resourceTag that has already been removed
	 */
	public ResourceTag getResourceTag() {
		return resourceTag;
	}
}
