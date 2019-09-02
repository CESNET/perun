package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception is thrown when the candidate does not exist in the ExtSource
 * It is thrown when search for candidates in ExtSource returned a unique identifier, for which no additional data was found in the ExtSource
 *
 * @author Martin Kuba
 */
public class CandidateNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public CandidateNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public CandidateNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public CandidateNotExistsException(String message) {
		super(message);
	}
}
