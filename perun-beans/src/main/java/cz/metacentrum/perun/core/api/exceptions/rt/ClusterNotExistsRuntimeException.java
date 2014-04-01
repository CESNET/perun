package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * ClusterNotExistsRuntimeException
 *
 * This exception is to be thrown in case we
 * are unable to look up the cluster in the Perun system.
 *
 * @author Michal Karm Babacek
 */
@SuppressWarnings("serial")
public class ClusterNotExistsRuntimeException extends EntityNotExistsRuntimeException {

	public ClusterNotExistsRuntimeException() {
		super();
	}

	public ClusterNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

}
