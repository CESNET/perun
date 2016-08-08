package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ResourceExistsException.
 *
 * @author Oliver Mrázik
 */
public class ResourceExistsException extends PerunException {

	private static final long serialVersionUID = -255958501797585251L;

	public ResourceExistsException(String message) {
		super(message);
	}

	public ResourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceExistsException(Throwable cause) {
		super(cause);
	}
}
