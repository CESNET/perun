package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * This exception is thrown when trying to create/update a facility with a name that is already used
 *
 * @author Slavek Licehammer
 */
public class FacilityExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FacilityExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the facility
	 * @param facility that already exists
	 */
	public FacilityExistsException(Facility facility) {
		super(facility.toString());
		this.facility = facility;
	}
}
