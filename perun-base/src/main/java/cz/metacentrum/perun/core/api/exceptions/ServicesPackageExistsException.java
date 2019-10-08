package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ServicesPackage;

/**
 * This exception is thrown when trying to create servicePackage with a name that has already been used
 *
 * @author Slavek Licehammer
 */
public class ServicesPackageExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private ServicesPackage servicesPackage;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServicesPackageExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServicesPackageExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServicesPackageExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the servicePackage
	 * @param servicePackage servicePackage that already exists
	 */
	public ServicesPackageExistsException(ServicesPackage servicePackage) {
		super(servicePackage.toString());
		this.servicesPackage = servicePackage;
	}

	/**
	 * Getter for the servicePackage
	 * @return servicePackage that already exists
	 */
	public ServicesPackage getServicePackage() {
		return this.servicesPackage;
	}
}
