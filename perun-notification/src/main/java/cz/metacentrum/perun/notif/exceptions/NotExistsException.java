package cz.metacentrum.perun.notif.exceptions;

/**
 * Exception is thrown when requested object is not found User: tomastunkl Date:
 * 21.10.12 Time: 14:22
 */
public class NotExistsException extends RuntimeException {

	public NotExistsException() {
		super();
	}

	public NotExistsException(String message) {
		super(message);
	}
}
