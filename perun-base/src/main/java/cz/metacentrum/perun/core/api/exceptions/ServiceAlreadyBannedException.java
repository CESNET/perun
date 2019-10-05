package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when the service is already blocked/banned on facility/destination.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ServiceAlreadyBannedException extends PerunException {

	static final long serialVersionUID = 0;

	private Service service;
	private Facility facility;
	private Destination destination;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceAlreadyBannedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyBannedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyBannedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the service and the facility
	 * @param service service that is already blocked/banned
	 * @param facility facility on which the service is blocked/banned
	 */
	public ServiceAlreadyBannedException(Service service, Facility facility) {
		this(service.toString() + " is already banned on " + facility.toString());
		this.service = service;
		this.facility = facility;
	}

	/**
	 * Constructor with the service and the facility
	 * @param service service that is already blocked/banned
	 * @param destination destination on which the service is blocked/banned
	 */
	public ServiceAlreadyBannedException(Service service, Destination destination) {
		this(service.toString() + " is already banned on " + destination.toString());
		this.service = service;
		this.destination = destination;
	}

	/**
	 * Getter for the service
	 * @return service that is already blocked/banned
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Getter for the facility
	 * @return facility on which the service is blocked/banned
	 */
	public Facility getFacility() {
		return facility;
	}

	/**
	 * Getter for the destination
	 * @return destination on which the service is blocked/banned
	 */
	public Destination getDestination() {
		return destination;
	}

}
