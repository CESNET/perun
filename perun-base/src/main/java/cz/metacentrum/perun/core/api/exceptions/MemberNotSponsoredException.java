package cz.metacentrum.perun.core.api.exceptions;

/**
 * Member is not flagged as sponsored, but somedy tries to add a sponsor to it..
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class MemberNotSponsoredException extends PerunException {

	public MemberNotSponsoredException(String message) {
		super(message);
	}

}
