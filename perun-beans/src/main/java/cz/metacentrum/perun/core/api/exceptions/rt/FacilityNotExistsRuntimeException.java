package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class FacilityNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public FacilityNotExistsRuntimeException() {
		super();
	}

	public FacilityNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public FacilityNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public FacilityNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
