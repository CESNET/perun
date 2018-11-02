package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceNotAssignedException.
 *
 * @author Slavek Licehammer
 */
public class ExtSourceNotAssignedException extends EntityNotAssignedException {
	static final long serialVersionUID = 0;

	public ExtSourceNotAssignedException(String message) {
		super(message);
	}

	public ExtSourceNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceNotAssignedException(Throwable cause) {
		super(cause);
	}
}
