package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of GroupAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class GroupAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public GroupAlreadyRemovedException(String message) {
		super(message);
	}

	public GroupAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
