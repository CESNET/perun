package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class UserNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public UserNotExistsRuntimeException() {
		super();
	}

	public UserNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public UserNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public UserNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
