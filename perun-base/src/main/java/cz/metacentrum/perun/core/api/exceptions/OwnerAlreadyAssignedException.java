package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of OwnerAlreadyAssignedException.
 *
 * @author Slavek Licehammer
 */
public class OwnerAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public OwnerAlreadyAssignedException(String message) {
		super(message);
	}

	public OwnerAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwnerAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

}
