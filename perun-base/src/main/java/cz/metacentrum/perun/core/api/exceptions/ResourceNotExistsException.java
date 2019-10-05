package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Resource;

/**
 * This exception is thrown when trying to get a resource which does not exist in the database
 *
 * @author Slavek Licehammer
 */
public class ResourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Resource resource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the resource
	 * @param resource resource that does not exist
	 */
	public ResourceNotExistsException(Resource resource) {
		super(resource.toString());
		this.resource = resource;
	}

	/**
	 * Getter for the resource
	 * @return resource that does not exist
	 */
	public Resource getResource() {
		return this.resource;
	}
}
