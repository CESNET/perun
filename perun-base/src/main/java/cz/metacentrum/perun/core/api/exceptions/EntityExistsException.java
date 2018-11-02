package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of EntityExistsException. It represents parrent class for *ExistsException classes.
 *
 * @author Slavek Licehammer
 */
public class EntityExistsException extends PerunException {
	static final long serialVersionUID = 0;

	public EntityExistsException(String message) {
		super(message);
	}

	public EntityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityExistsException(Throwable cause) {
		super(cause);
	}
}
