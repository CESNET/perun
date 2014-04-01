package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.rt.FacilityNotExistsRuntimeException;

/**
 * Checked version of MemberNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.FacilityNotExistsRuntimeException
 * @author Martin Kuba
 */
public class FacilityNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	public FacilityNotExistsException(FacilityNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
