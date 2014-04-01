package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class MemberNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public MemberNotExistsRuntimeException() {
		super();
	}

	public MemberNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public MemberNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public MemberNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
