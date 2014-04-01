package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.exceptions.rt.ExtSourceExistsRuntimeException;

/**
 * Checked version of ExtSourceExistsException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ExtSourceExistsRuntimeException
 * @author Slavek Licehammer
 */
public class ExtSourceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private ExtSource extSource;

	public ExtSourceExistsException(ExtSourceExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ExtSourceExistsException(String message) {
		super(message);
	}

	public ExtSourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtSourceExistsException(Throwable cause) {
		super(cause);
	}

	public ExtSourceExistsException(ExtSource extSource) {
		super(extSource.toString());
		this.extSource = extSource;
	}

	public ExtSource getExtSource() {
		return this.extSource;
	}
}
