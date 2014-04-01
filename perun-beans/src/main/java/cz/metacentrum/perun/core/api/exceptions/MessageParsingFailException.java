package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of MessageParsingFailException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.MessageParsingFailException
 * @author Michal Šťava
 */
public class MessageParsingFailException extends PerunException {
	static final long serialVersionUID = 0;

	private String log;

	public MessageParsingFailException(String message) {
		super(message);
	}

	public MessageParsingFailException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageParsingFailException(Throwable cause) {
		super(cause);
	}

	public MessageParsingFailException(String message, String log) {
		super(message);
		this.log = log;
	}

	public String getLog() {
		return log;
	}
}
