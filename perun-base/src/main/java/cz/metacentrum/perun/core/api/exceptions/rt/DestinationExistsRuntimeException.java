package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class DestinationExistsRuntimeException extends EntityExistsRuntimeException {
	private String userId;

	public DestinationExistsRuntimeException() {
		super();
	}

	public DestinationExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public DestinationExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public DestinationExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
