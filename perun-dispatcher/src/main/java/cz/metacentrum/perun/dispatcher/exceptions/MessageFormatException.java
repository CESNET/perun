package cz.metacentrum.perun.dispatcher.exceptions;

/**
 * Checked version of MessageFormatException.
 * 
 * @author Michal Karm Babacek
 */
public class MessageFormatException extends DispatcherException {

	private static final long serialVersionUID = 1L;

	public MessageFormatException(String message) {
		super(message);
	}

	public MessageFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageFormatException(Throwable cause) {
		super(cause);
	}
}
