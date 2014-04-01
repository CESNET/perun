package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class DestinationNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public DestinationNotExistsRuntimeException() {
		super();
	}

	public DestinationNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public DestinationNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public DestinationNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
