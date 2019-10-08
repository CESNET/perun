package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Exception is thrown when the destination already exists
 *
 * @author Michal Prochazka
 */
public class DestinationExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;
	private Destination destination;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public DestinationExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the destination
	 * @param destination destination which already exists
	 */
	public DestinationExistsException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	/**
	 * Gets the destination for this instance.
	 *
	 * @return The destination
	 */
	public Destination getDestination()
	{
		return this.destination;
	}
}
