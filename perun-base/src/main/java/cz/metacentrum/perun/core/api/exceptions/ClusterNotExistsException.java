package cz.metacentrum.perun.core.api.exceptions;


/**
 * Checked version of ClusterNotExistsException.
 *
 * This exception is to be thrown in case we
 * are unable to look up the cluster in the Perun system.
 *
 * @author Michal Karm Babacek
 */
public class ClusterNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ClusterNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ClusterNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ClusterNotExistsException(Throwable cause) {
		super(cause);
	}
}
