package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class UserExtSourceNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public UserExtSourceNotExistsRuntimeException() {
		super();
	}

	public UserExtSourceNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public UserExtSourceNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public UserExtSourceNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
