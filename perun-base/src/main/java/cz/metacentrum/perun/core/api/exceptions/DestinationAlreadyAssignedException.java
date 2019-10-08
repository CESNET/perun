package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Exception is thrown when the destination has already been assigned to the facility and service.
 *
 * @author Slavek Licehammer
 */
public class DestinationAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	private Destination destination;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public DestinationAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the destination
	 * @param destination destination which has already been assigned
	 */
	public DestinationAlreadyAssignedException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	/**
	 * Getter for the destination
	 * @return destination which has already been assigned
	 */
	public Destination getDestination() {
		return this.destination;
	}



}
