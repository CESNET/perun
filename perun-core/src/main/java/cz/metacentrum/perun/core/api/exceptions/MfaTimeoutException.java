package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

public class MfaTimeoutException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public MfaTimeoutException(String message) {
		super(message);
	}

	public MfaTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public MfaTimeoutException(Throwable cause) {
		super(cause);
	}
}
