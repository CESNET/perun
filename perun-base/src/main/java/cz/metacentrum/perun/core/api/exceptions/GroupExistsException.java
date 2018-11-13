package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of GroupExistsException.
 *
 * @author Martin Kuba
 */
public class GroupExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	public GroupExistsException(String message) {
		super(message);
	}

	public GroupExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupExistsException(Throwable cause) {
		super(cause);
	}
}
