package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceUserAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class SpecificUserAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public SpecificUserAlreadyRemovedException(String message) {
		super(message);
	}

	public SpecificUserAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecificUserAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
