package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Resource;

/**
 * This exception is thrown when the resource could not be created/updated because there is already a resource with the same name
 *
 * @author Oliver Mrázik
 */
public class ResourceExistsException extends PerunException {

	private static final long serialVersionUID = -255958501797585251L;

	private Resource resource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the resource
	 * @param resource resource that already exists
	 */
	public ResourceExistsException(Resource resource) {
		super("Resource with name \"" + resource.getName() + "\" already exists in Facility and Vo.");
		this.resource = resource;
	}

	/**
	 * Getter for the resource
	 * @return resource that already exists
	 */
	public Resource getResource() {
		return resource;
	}
}
