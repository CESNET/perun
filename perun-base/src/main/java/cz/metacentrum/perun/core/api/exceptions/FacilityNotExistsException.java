package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * This exception is thrown when trying to get a facility that does not exist in the database
 *
 * @author Martin Kuba
 */
public class FacilityNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FacilityNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the facility
	 * @param facility that does not exist
	 */
	public FacilityNotExistsException(Facility facility) {
		super(facility.toString());
		this.facility = facility;
	}

	/**
	 * Getter for the facility
	 * @return facility that does not exist
	 */
	public Facility getFacility() {
		return facility;
	}
}
