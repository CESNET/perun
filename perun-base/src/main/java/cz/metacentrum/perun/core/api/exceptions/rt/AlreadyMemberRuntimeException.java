package cz.metacentrum.perun.core.api.exceptions.rt;

public class AlreadyMemberRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private String userId;

	public AlreadyMemberRuntimeException() {
		super();
	}

	public AlreadyMemberRuntimeException(String userId) {
		super(userId);
		this.userId = userId;
	}

	public AlreadyMemberRuntimeException(Throwable cause) {
		super(cause);
	}

	public AlreadyMemberRuntimeException(Throwable cause, String userId) {
		super(userId, cause);

		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
