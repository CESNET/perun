package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ExtSourceAlreadyAssignedRuntimeException extends EntityAlreadyAssignedRuntimeException {
	private String userId;

	public ExtSourceAlreadyAssignedRuntimeException() {
		super();
	}

	public ExtSourceAlreadyAssignedRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public ExtSourceAlreadyAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public ExtSourceAlreadyAssignedRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
