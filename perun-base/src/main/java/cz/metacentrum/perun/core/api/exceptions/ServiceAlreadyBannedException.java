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

	public ServiceAlreadyBannedException(String message) {
		super(message);
	}

	public ServiceAlreadyBannedException(String message, Throwable cause) {
		super(message, cause);
	}

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
