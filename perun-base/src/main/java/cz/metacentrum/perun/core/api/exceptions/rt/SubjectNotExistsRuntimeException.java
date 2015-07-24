package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class SubjectNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String subject;

	public SubjectNotExistsRuntimeException() {
		super();
	}

	public SubjectNotExistsRuntimeException(String subject) {
		super();
		this.subject = subject;
	}

	public SubjectNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public SubjectNotExistsRuntimeException(Throwable cause, String subject) {
		super(cause);
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}
}
