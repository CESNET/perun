package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when the service has already been assigned to the resource.
 *
 * @author Slavek Licehammer
 */
public class ServiceAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private Service service;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the service
	 * @param service service that has already been assigned to the resource
	 */
	public ServiceAlreadyAssignedException(Service service) {
		super(service.toString());
		this.service = service;
	}

	/**
	 * Getter for the service
	 * @return service that has already been assigned to the resource
	 */
	public Service getService() {
		return service;
	}
}
