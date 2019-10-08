package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ServicesPackage;

/**
 * This exception is thrown when the servicePackage has not been found in the database
 *
 * @author Martin Kuba
 */
public class ServicesPackageNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ServicesPackage servicesPackage;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServicesPackageNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServicesPackageNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServicesPackageNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the servicesPackage
	 * @param servicesPackage servicesPackage that doesn't exist in the database
	 */
	public ServicesPackageNotExistsException(ServicesPackage servicesPackage) {
		super(servicesPackage.toString());
		this.servicesPackage = servicesPackage;
	}

	/**
	 * Getter for the servicesPackage
	 * @return servicesPackage that doesn't exist in the database
	 */
	public ServicesPackage getServicesPackage() {
		return this.servicesPackage;
	}
}
