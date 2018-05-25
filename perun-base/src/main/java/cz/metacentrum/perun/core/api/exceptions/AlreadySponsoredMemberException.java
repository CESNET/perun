package cz.metacentrum.perun.core.api.exceptions;

/**
 * Member is already flagged as sponsored and something is trying to set it again.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class AlreadySponsoredMemberException extends PerunException {

	public AlreadySponsoredMemberException(String message) {
		super(message);
	}

}
