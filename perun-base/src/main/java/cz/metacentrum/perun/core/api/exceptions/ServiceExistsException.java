package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when trying to create a service with a name that has already been used
 *
 * @author Slavek Licehammer
 */
public class ServiceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Service service;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the service
	 * @param service service that already exists
	 */
	public ServiceExistsException(Service service) {
		super(service.toString());
		this.service = service;
	}

	/**
	 * Getter for the service
	 * @return service that already exists
	 */
	public Service getService() {
		return this.service;
	}
}
