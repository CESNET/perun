package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of MemberAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class MemberAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public MemberAlreadyRemovedException(String message) {
		super(message);
	}

	public MemberAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemberAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
