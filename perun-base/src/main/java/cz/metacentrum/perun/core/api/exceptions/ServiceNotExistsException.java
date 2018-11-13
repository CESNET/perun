package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * Service not exists in underlaying data source.
 *
 * @author Martin Kuba
 */
public class ServiceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceNotExistsException(String message) {
		super(message);
	}

	public ServiceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceNotExistsException(Throwable cause) {
		super(cause);
	}

	public ServiceNotExistsException(Service service) {
		super(service.toString());
		this.service = service;
	}

	public Service getService() {
		return this.service;
	}
}
