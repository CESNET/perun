package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the user is already a sponsor of the member.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class AlreadySponsorException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AlreadySponsorException(String message) {
		super(message);
	}

}
