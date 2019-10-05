package cz.metacentrum.perun.core.api.exceptions;

/**
 * Certificate is not valid - for a lots of different reasons (like unsupported subject, expired etc.)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class InvalidCertificateException extends PerunException {

	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public InvalidCertificateException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public InvalidCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public InvalidCertificateException(Throwable cause) {
		super(cause);
	}
}
