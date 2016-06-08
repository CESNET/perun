package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.rt.ServiceNotAssignedRuntimeException;

/**
 * Service hasn't been assigned to the resource.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ServiceNotAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class ServiceNotAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceNotAssignedException(ServiceNotAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ServiceNotAssignedException(String message) {
		super(message);
	}

	public ServiceNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceNotAssignedException(Throwable cause) {
		super(cause);
	}

	public ServiceNotAssignedException(Service service) {
		super(service.toString());
		this.service = service;
	}

	public Service getService() {
		return service;
	}
}
