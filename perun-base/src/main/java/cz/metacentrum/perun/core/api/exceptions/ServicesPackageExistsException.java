package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.exceptions.rt.ServicesPackageExistsRuntimeException;

/**
 * ServicePackage already exists in underlaying data source.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ServicesPackageExistsRuntimeException
 * @author Slavek Licehammer
 */
public class ServicesPackageExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private ServicesPackage servicesPackage;

	public ServicesPackageExistsException(ServicesPackageExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ServicesPackageExistsException(String message) {
		super(message);
	}

	public ServicesPackageExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServicesPackageExistsException(Throwable cause) {
		super(cause);
	}

	public ServicesPackageExistsException(ServicesPackage servicePackage) {
		super(servicePackage.toString());
		this.servicesPackage = servicePackage;
	}

	public ServicesPackage getServicePackage() {
		return this.servicesPackage;
	}
}
