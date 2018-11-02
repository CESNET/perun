package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * Checked version of MemberNotExistsException.
 *
 * @author Martin Kuba
 */
public class FacilityNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	public FacilityNotExistsException(String message) {
		super(message);
	}

	public FacilityNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FacilityNotExistsException(Throwable cause) {
		super(cause);
	}

	public FacilityNotExistsException(Facility facility) {
		super(facility.toString());
		this.facility = facility;
	}

	public Facility getFacility() {
		return facility;
	}
}
