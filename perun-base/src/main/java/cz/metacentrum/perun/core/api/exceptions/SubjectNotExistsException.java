package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of SubjectNotExistsException.
 *
 * @author Martin Kuba
 */
public class SubjectNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private String subject;

	public SubjectNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubjectNotExistsException(Throwable cause) {
		super(cause);
	}

	public SubjectNotExistsException(String subject) {
		super(subject.toString());
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}
}
