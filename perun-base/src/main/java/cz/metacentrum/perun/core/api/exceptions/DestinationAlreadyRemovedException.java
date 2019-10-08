package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Exception is thrown when the destination has already been removed from the facility and service.
 *
 * @author Slavek Licehammer
 */
public class DestinationAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	private Destination destination;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public DestinationAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the destination
	 * @param destination destination which has already been removed
	 */
	public DestinationAlreadyRemovedException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	/**
	 * Getter for the destination
	 * @return destination which has already been removed
	 */
	public Destination getDestination() {
		return this.destination;
	}

}
