package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * HostNotExistsRuntimeException
 *
 * This exception is to be thrown in case we
 * are unable to look up the host computer in the Perun system.
 *
 * @author Michal Karm Babacek
 */
@SuppressWarnings("serial")
public class HostNotExistsRuntimeException extends EntityNotExistsRuntimeException {

	public HostNotExistsRuntimeException() {
		super();
	}

	public HostNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

}
