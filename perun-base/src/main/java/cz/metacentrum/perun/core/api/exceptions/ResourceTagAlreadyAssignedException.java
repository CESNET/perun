package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;
/**
 * This exception is thrown when the resourceTag has already been assigned to the resource
 *
 * @author Stava Michal <stavamichal@gmail.com>
 */
public class ResourceTagAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceTagAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceTagAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the resourceTag
	 * @param resourceTag that has already been assigned to the resource
	 */
	public ResourceTagAlreadyAssignedException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	/**
	 * Constructor with a message and the resourceTag
	 * @param message message with details about the cause
	 * @param resourceTag that has already been assigned to the resource
	 */
	public ResourceTagAlreadyAssignedException(String message, ResourceTag resourceTag) {
		super(message);
		this.resourceTag = resourceTag;
	}

	/**
	 * Getter for the resourceTag
	 * @return resourceTag that has already been assigned to the resource
	 */
	public ResourceTag getResourceTag() {
		return resourceTag;
	}
}
