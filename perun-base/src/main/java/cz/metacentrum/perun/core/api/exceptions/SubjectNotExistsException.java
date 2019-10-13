package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when the subject with the specific login has not been found in the database
 *
 * @author Martin Kuba
 */
public class SubjectNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private String subject;

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SubjectNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SubjectNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the subject
	 * @param subject that does not exist
	 */
	public SubjectNotExistsException(String subject) {
		super(subject.toString());
		this.subject = subject;
	}

	/**
	 * Getter for the subject
	 * @return subject that does not exist
	 */
	public String getSubject() {
		return this.subject;
	}
}
