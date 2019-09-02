package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * This exception is thrown when the facility contact entity already exists in the database.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class FacilityContactExistsException extends EntityExistsException {

	private Facility facility;
	private String contactName;
	private Object contactEntity;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FacilityContactExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityContactExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityContactExistsException(Throwable cause) {
		super(cause);
	}
	/**
	 * Constructor with facility, contact name and contact entity
	 * @param facility facility whose contact does not exist
	 * @param contactName contact name
	 * @param contactEntity the contact entity of type Object
	 */
	public FacilityContactExistsException(Facility facility, String contactName, Object contactEntity) {
		super("Facility contact for Facility " + facility + ", contact name " + contactName + " and entity " + contactEntity.toString() + " already exists.");
		this.facility = facility;
		this.contactEntity = contactEntity;
		this.contactName = contactName;
	}

	/**
	 * Getter for the facility
	 * @return facility whose contact already exists
	 */
	public Facility getFacility() {
		return facility;
	}

	/**
	 * Getter for the contact name
	 * @return contact name
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * Getter for the contact entity of the type Object
	 * @return contact entity
	 */
	public Object getContactEntity() {
		return contactEntity;
	}
}