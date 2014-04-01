package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.Service;

public class ServiceNotAssignedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private Service service;

	public ServiceNotAssignedRuntimeException() {
		super();
	}

	public ServiceNotAssignedRuntimeException(Service service) {
		super(service.toString());
		this.service = service;
	}

	public ServiceNotAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public ServiceNotAssignedRuntimeException(Throwable cause, Service service) {
		super(service.toString(), cause);

		this.service = service;
	}

	public Service getUserId() {
		return service;
	}
}
