package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

/**
 * This exception is thrown when principal is performing MFA-requiring action and the auth time is older than the limit defined in the config
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
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
