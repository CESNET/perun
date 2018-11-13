package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;

/**
 * Checked version of ExtSourceNotExistsException
 *
 * @author Slavek Licehammer
 */
public class ExtSourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	public ExtSourceNotExistsException(String message) {
		super(message);
	}

	public ExtSourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceNotExistsException(Throwable cause) {
		super(cause);
	}

	public ExtSourceNotExistsException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	public ExtSource getExtSource() {
		return this.extSource;
	}
}
