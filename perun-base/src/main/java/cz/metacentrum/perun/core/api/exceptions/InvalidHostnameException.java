package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception raises when creating Host with invalid hostname.
 *
 * @author Metodej Klang
 */
public class InvalidHostnameException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public InvalidHostnameException(String message) {
		super(message);
	}
}
