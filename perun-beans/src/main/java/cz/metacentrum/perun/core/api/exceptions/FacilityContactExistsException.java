package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * Checked version of FacilityContactExistsException
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class FacilityContactExistsException extends EntityExistsException {

	private Facility facility;
	private String contactName;
	private Object contactEntity;

	public FacilityContactExistsException(String message) {
		super(message);
	}

	public FacilityContactExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FacilityContactExistsException(Throwable cause) {
		super(cause);
	}

	public FacilityContactExistsException(Facility facility, String contactName, Object contactEntity) {
		super("Facility contact for Facility " + facility + ", contact name " + contactName + " and entity " + contactEntity.toString() + " already exists.");
		this.facility = facility;
		this.contactEntity = contactEntity;
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
