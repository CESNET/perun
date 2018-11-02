package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of EntityNotAssignedException. It represents parrent class for *NotAssignedException classes.
 *
 * @author Slavek Licehammer
 */
public class EntityNotAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityNotAssignedException(String message) {
		super(message);
	}

	public EntityNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotAssignedException(Throwable cause) {
		super(cause);
	}
}
