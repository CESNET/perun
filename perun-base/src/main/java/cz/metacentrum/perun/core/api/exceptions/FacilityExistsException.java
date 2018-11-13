package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * Checked version of FacilityExistsException.
 *
 * @author Slavek Licehammer
 */
public class FacilityExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	public FacilityExistsException(String message) {
		super(message);
	}

	public FacilityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FacilityExistsException(Throwable cause) {
		super(cause);
	}

	public FacilityExistsException(Facility facility) {
		super(facility.toString());
		this.facility = facility;
	}
}
