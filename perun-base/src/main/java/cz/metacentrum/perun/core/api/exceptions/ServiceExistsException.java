package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * Service already exists in underlaying data source.
 *
 * @author Slavek Licehammer
 */
public class ServiceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceExistsException(String message) {
		super(message);
	}

	public ServiceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceExistsException(Throwable cause) {
		super(cause);
	}

	public ServiceExistsException(Service service) {
		super(service.toString());
		this.service = service;
	}

	public Service getService() {
		return this.service;
	}
}
