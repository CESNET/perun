package cz.metacentrum.perun.core.api.exceptions;

/**
 * Pattern is not well-formed.
 *
 * Is to be thrown when the generative pattern of hostname e.g. local[00-12]domain has a wrong syntax.
 *
 * @author Jirka Mauritz <jirmauritz@gmail.com>
 */
public class WrongPatternException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public WrongPatternException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongPatternException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongPatternException(Throwable cause) {
		super(cause);
	}
}
