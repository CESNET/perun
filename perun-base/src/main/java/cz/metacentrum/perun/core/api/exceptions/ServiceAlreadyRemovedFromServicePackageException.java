package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceAlreadyRemovedFromServicePackageException.
 *
 * @author Michal Stava
 */
public class ServiceAlreadyRemovedFromServicePackageException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceAlreadyRemovedFromServicePackageException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyRemovedFromServicePackageException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceAlreadyRemovedFromServicePackageException(Throwable cause) {
		super(cause);
	}

}
