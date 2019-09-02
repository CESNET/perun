package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception raises when group is externally managed
 * which means that group membership is managed by external source (group synchronization) and thus manual changes to the group membership are prohibited.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 */
public class ExternallyManagedException extends PerunException {

	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExternallyManagedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExternallyManagedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExternallyManagedException(Throwable cause) {
		super(cause);
	}
}
