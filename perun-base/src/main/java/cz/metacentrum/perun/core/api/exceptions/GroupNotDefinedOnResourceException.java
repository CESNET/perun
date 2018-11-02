package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of GroupNotDefinedOnResourceRuntimeException.
 *
 * @author Martin Kuba
 */
public class GroupNotDefinedOnResourceException extends PerunException {
	static final long serialVersionUID = 0;

	public GroupNotDefinedOnResourceException(String message) {
		super(message);
	}

	public GroupNotDefinedOnResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupNotDefinedOnResourceException(Throwable cause) {
		super(cause);
	}
}
