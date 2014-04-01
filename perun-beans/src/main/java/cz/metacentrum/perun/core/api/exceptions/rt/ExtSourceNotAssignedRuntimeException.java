package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ExtSourceNotAssignedRuntimeException extends EntityNotAssignedRuntimeException {
	private String userId;

	public ExtSourceNotAssignedRuntimeException() {
		super();
	}

	public ExtSourceNotAssignedRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public ExtSourceNotAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public ExtSourceNotAssignedRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
