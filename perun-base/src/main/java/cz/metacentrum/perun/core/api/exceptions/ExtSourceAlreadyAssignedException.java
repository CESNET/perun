package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;

/**
 * Checked version of ExtSourceAllreadyAssignedException.
 *
 * @author Slavek Licehammer
 */
public class ExtSourceAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	public ExtSourceAlreadyAssignedException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	public ExtSourceAlreadyAssignedException(String message) {
		super(message);
	}

	public ExtSourceAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceAlreadyAssignedException(Throwable cause) {
		super(cause);
	}
}
