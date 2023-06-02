package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the form item does not exist in any application form
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class FormItemNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FormItemNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FormItemNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FormItemNotExistsException(Throwable cause) {
		super(cause);
	}
}
