package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception raises when group is externally managed.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 */
public class ExternallyManagedException extends PerunException {

	static final long serialVersionUID = 0;

	public ExternallyManagedException(String message) {
		super(message);
	}

	public ExternallyManagedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternallyManagedException(Throwable cause) {
		super(cause);
	}
}
