package cz.metacentrum.perun.core.api.exceptions;

/**
 * Member is not flagged as sponsored, but somebody tries to add a sponsor to it..
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class MemberNotSponsoredException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MemberNotSponsoredException(String message) {
		super(message);
	}

}
