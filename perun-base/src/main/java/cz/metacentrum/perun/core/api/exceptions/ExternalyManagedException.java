package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception raises when group is externally managed.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 */
public class ExternalyManagedException extends PerunException {

	static final long serialVersionUID = 0;

	public ExternalyManagedException(String message) {
		super(message);
	}

	public ExternalyManagedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternalyManagedException(Throwable cause) {
		super(cause);
	}
}
