package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class ExtSourceNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String userId;

	public ExtSourceNotExistsRuntimeException() {
		super();
	}

	public ExtSourceNotExistsRuntimeException(String userId) {
		super();
		this.userId = userId;
	}

	public ExtSourceNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public ExtSourceNotExistsRuntimeException(Throwable cause, String userId) {
		super(cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
