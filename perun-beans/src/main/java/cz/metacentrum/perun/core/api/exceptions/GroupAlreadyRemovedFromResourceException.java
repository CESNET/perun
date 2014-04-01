package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of GroupAlreadyRemovedFromResourceException.
 *
 * @author Michal Stava
 */
public class GroupAlreadyRemovedFromResourceException extends PerunException {
	static final long serialVersionUID = 0;

	public GroupAlreadyRemovedFromResourceException(String message) {
		super(message);
	}

	public GroupAlreadyRemovedFromResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupAlreadyRemovedFromResourceException(Throwable cause) {
		super(cause);
	}

}
