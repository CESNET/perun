package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when some parsing problem occur (regex, matcher, pattern, etc.)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ParserException extends InternalErrorException {
	static final long serialVersionUID = 0;
	private String parsedValue;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ParserException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and parsed value
	 * @param message message with details about the cause
	 * @param parsedValue the value that is to be parsed
	 */
	public ParserException(String message, String parsedValue) {
		super(message);
		this.parsedValue = parsedValue;
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a message, parsed value and the cause
	 * @param message message with details about the cause
	 * @param parsedValue the value that is to be parsed
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ParserException(String message, Throwable cause, String parsedValue) {
		super(message, cause);
		this.parsedValue = parsedValue;
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ParserException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with a Throwable object and parsed value
	 * @param cause Throwable that caused throwing of this exception
	 * @param parsedValue the value that is to be parsed
	 */
	public ParserException(Throwable cause, String parsedValue) {
		super(cause);
		this.parsedValue = parsedValue;
	}

	/**
	 * Getter for the parsed value
	 * @return the value that is to be parsed
	 */
	public String getParsedValue() {
		return parsedValue;
	}
}
