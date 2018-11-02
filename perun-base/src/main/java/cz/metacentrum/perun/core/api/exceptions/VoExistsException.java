package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of VoExistsException.
 *
 * @author Martin Kuba
 */
public class VoExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Converts runtime version to checked version.
	 * @param rt runtime version of this exception
	 */

	public VoExistsException(String message) {
		super(message);
	}

	public VoExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public VoExistsException(Throwable cause) {
		super(cause);
	}
}
