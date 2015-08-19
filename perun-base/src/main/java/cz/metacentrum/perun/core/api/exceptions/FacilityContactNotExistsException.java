package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * Checked version of FacilityContactNotExistsException
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class FacilityContactNotExistsException extends EntityNotExistsException {

	private Facility facility;
	private String contactName;
	private Object contactEntity;

	public FacilityContactNotExistsException(String message) {
		super(message);
	}

	public FacilityContactNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FacilityContactNotExistsException(Throwable cause) {
		super(cause);
	}

	public FacilityContactNotExistsException(Facility facility, String contactName, Object contactEntity) {
		super("Facility contact for facility " + facility + ", contact name " + contactName + " and entity " + contactEntity.toString() + " not exists.");
		this.facility = facility;
		this.contactEntity = contactEntity;
		this.contactName = contactName;
	}

	public FacilityContactNotExistsException(Facility facility, String contactName) {
		super("Facility contact for facility " + facility + " and contact name " + contactName + " not exists.");
		this.facility = facility;
		this.contactName = contactName;
	}

	public Facility getFacility() {
		return facility;
	}

	public String getContactName() {
		return contactName;
	}

	public Object getContactEntity() {
		return contactEntity;
	}
}
