package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when the service has not been found in the database
 *
 * @author Martin Kuba
 */
public class ServiceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Service service;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the service
	 * @param service service that has not been found in the database
	 */
	public ServiceNotExistsException(Service service) {
		super(service.toString());
		this.service = service;
	}

	/**
	 * Getter for the service
	 * @return service that has not been found in the database
	 */
	public Service getService() {
		return this.service;
	}
}
