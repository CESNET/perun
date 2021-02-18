package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception raises when creating invalid Destination.
 *
 * @author Metodej Klang
 */
public class InvalidDestinationException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public InvalidDestinationException(String message) {
		super(message);
	}
}
