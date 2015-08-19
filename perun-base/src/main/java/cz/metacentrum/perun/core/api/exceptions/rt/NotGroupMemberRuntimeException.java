package cz.metacentrum.perun.core.api.exceptions.rt;

public class NotGroupMemberRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private String userId;

	public NotGroupMemberRuntimeException() {
		super();
	}

	public NotGroupMemberRuntimeException(String userId) {
		super(userId);
		this.userId = userId;
	}

	public NotGroupMemberRuntimeException(Throwable cause) {
		super(cause);
	}

	public NotGroupMemberRuntimeException(Throwable cause, String userId) {
		super(userId, cause);

		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
