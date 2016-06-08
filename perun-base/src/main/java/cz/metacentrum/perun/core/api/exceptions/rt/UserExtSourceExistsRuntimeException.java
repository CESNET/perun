package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class UserExtSourceExistsRuntimeException extends EntityExistsRuntimeException {
	private String userId;

	public UserExtSourceExistsRuntimeException() {
		super();
	}

	public UserExtSourceExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public UserExtSourceExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public UserExtSourceExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
