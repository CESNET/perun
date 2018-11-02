package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of EntityNotExistsException. It represents parrent class for *NotExistsException classes.
 *
 * @author Slavek Licehammer
 */
public class EntityNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityNotExistsException(String message) {
		super(message);
	}

	public EntityNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotExistsException(Throwable cause) {
		super(cause);
	}
}
