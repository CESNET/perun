package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ExtSourceExistsRuntimeException extends EntityExistsRuntimeException {
	private String userId;

	public ExtSourceExistsRuntimeException() {
		super();
	}

	public ExtSourceExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public ExtSourceExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public ExtSourceExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
