package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceAlreadyRemovedFromServicePackageException.
 *
 * @author Michal Stava
 */
public class ServiceAlreadyRemovedFromServicePackageException extends PerunException {
	static final long serialVersionUID = 0;

	public ServiceAlreadyRemovedFromServicePackageException(String message) {
		super(message);
	}

	public ServiceAlreadyRemovedFromServicePackageException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceAlreadyRemovedFromServicePackageException(Throwable cause) {
		super(cause);
	}

}
