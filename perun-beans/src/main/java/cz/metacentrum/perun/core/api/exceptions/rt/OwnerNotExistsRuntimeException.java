package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class OwnerNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public OwnerNotExistsRuntimeException() {
		super();
	}

	public OwnerNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public OwnerNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public OwnerNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
