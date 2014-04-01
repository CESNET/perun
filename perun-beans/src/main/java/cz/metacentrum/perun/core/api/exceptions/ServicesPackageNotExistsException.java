package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.exceptions.rt.ServicesPackageNotExistsRuntimeException;

/**
 * ServicesPackage not exists in underlaying data source.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ServicesPackageNotExistsRuntimeException
 * @author Martin Kuba
 */
public class ServicesPackageNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ServicesPackage servicesPackage;

	public ServicesPackageNotExistsException(ServicesPackageNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ServicesPackageNotExistsException(String message) {
		super(message);
	}

	public ServicesPackageNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServicesPackageNotExistsException(Throwable cause) {
		super(cause);
	}

	public ServicesPackageNotExistsException(ServicesPackage servicesPackage) {
		super(servicesPackage.toString());
		this.servicesPackage = servicesPackage;
	}

	public ServicesPackage getServicesPackage() {
		return this.servicesPackage;
	}
}
