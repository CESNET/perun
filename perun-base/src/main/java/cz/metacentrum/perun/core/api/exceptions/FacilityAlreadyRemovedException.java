package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of FacilityAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class FacilityAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public FacilityAlreadyRemovedException(String message) {
		super(message);
	}

	public FacilityAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public FacilityAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
