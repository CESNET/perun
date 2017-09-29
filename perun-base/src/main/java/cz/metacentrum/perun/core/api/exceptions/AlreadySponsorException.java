package cz.metacentrum.perun.core.api.exceptions;

/**
 * User already is a sponsor of a member.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class AlreadySponsorException extends PerunException {

	public AlreadySponsorException(String message) {
		super(message);
	}

}
