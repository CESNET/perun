package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.rt.ServiceExistsRuntimeException;

/**
 * Service already exists in underlaying data source.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ServiceExistsRuntimeException
 * @author Slavek Licehammer
 */
public class ServiceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceExistsException(ServiceExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
