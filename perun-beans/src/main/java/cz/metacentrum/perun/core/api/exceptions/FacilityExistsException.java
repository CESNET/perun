package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.rt.FacilityExistsRuntimeException;

/**
 * Checked version of FacilityExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.FacilityExistsRuntimeException
 * @author Slavek Licehammer
 */
public class FacilityExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private Facility facility;

	/**
	 * Converts runtime version to checked version.
	 * @param rt runtime version of this exception
	 */
	public FacilityExistsException(FacilityExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
