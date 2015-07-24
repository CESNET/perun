package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when some parsing problem occur (regex, matcher, pattern, etc.)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ParserException extends InternalErrorException {
	static final long serialVersionUID = 0;
	private String parsedValue;

	public ParserException(String message) {
		super(message);
	}
	
	public ParserException(String message, String parsedValue) {
		super(message);
		this.parsedValue = parsedValue;
	}

	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParserException(String message, Throwable cause, String parsedValue) {
		super(message, cause);
		this.parsedValue = parsedValue;
	}

	public ParserException(Throwable cause) {
		super(cause);
	}

	public ParserException(Throwable cause, String parsedValue) {
		super(cause);
		this.parsedValue = parsedValue;
	}

	public String getParsedValue() {
		return parsedValue;
	}
}
