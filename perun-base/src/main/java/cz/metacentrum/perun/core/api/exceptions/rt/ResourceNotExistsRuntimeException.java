package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ResourceNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public ResourceNotExistsRuntimeException() {
		super();
	}

	public ResourceNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public ResourceNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public ResourceNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
