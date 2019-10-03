package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

/**
 * Service is already blocked/banned on facility.
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

	public ServiceAlreadyBannedException(Service service, Facility facility) {
		this(service.toString() + " is already banned on " + facility.toString());
		this.service = service;
		this.facility = facility;
	}

	public ServiceAlreadyBannedException(Service service, Destination destination) {
		this(service.toString() + " is already banned on " + destination.toString());
		this.service = service;
		this.destination = destination;
	}

	public Service getService() {
		return service;
	}

	public Facility getFacility() {
		return facility;
	}

	public Destination getDestination() {
		return destination;
	}

}
