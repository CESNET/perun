package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when trying to remove service from resource but the service hasn't been assigned to the resource.
 *
 * @author Slavek Licehammer
 */
public class ServiceNotAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private Service service;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceNotAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceNotAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the service
	 * @param service service that is not assigned to resource
	 */
	public ServiceNotAssignedException(Service service) {
		super(service.toString());
		this.service = service;
	}

	/**
	 * Getter for the service
	 * @return service that is not assigned to resource
	 */
	public Service getService() {
		return service;
	}
}
