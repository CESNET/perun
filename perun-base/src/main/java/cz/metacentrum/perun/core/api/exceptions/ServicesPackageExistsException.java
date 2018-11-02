package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ServicesPackage;

/**
 * ServicePackage already exists in underlaying data source.
 *
 * @author Slavek Licehammer
 */
public class ServicesPackageExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private ServicesPackage servicesPackage;

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
