package cz.metacentrum.perun.core.api.exceptions.rt;

public class AlreadyAdminRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private String userId;

	public AlreadyAdminRuntimeException() {
		super();
	}

	public AlreadyAdminRuntimeException(String userId) {
		super(userId);
		this.userId = userId;
	}

	public AlreadyAdminRuntimeException(Throwable cause) {
		super(cause);
	}

	public AlreadyAdminRuntimeException(Throwable cause, String userId) {
		super(userId, cause);

		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
