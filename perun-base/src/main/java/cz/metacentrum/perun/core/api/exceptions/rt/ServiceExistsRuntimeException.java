package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ServiceExistsRuntimeException extends EntityExistsRuntimeException {

	public ServiceExistsRuntimeException() {
		super();
	}

	public ServiceExistsRuntimeException(Throwable cause) {
		super(cause);
	}


}
