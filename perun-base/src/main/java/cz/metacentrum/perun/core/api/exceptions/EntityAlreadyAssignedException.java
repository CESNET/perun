package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of EntityAllreadyAssignedException. It represents parrent class for *AllreadyAssignedException classes.
 *
 * @author Slavek Licehammer
 */
public class EntityAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityAlreadyAssignedException(String message) {
		super(message);
	}

	public EntityAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityAlreadyAssignedException(Throwable cause) {
		super(cause);
	}
}
