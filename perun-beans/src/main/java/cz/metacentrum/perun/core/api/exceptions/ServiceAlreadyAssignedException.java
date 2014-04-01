package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.rt.ServiceAlreadyAssignedRuntimeException;

/**
 * Service is already assigned to the resource.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ServiceAlreadyAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class ServiceAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceAlreadyAssignedException(ServiceAlreadyAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ServiceAlreadyAssignedException(String message) {
		super(message);
	}

	public ServiceAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	public ServiceAlreadyAssignedException(Service service) {
		super(service.toString());
		this.service = service;
	}

	public Service getService() {
		return service;
	}
}
