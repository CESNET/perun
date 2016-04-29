package cz.metacentrum.perun.core.api.exceptions;

/**
 * Class description.
 * Created on 15. 10. 2015.
 *
 * @author Oliver Mrázik
 */
public class GroupOperationsException extends PerunException {
	static final long serialVersionUID = 0;
	
	public GroupOperationsException() {
	}

	public GroupOperationsException(String message) {
		super(message);
	}

	public GroupOperationsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GroupOperationsException(Throwable cause) {
		super(cause);
	}
}