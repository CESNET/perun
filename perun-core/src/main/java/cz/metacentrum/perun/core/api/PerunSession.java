package cz.metacentrum.perun.core.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Encapsulates context of user's interaction with the Perun system.
 *
 */
public abstract class PerunSession {

	private PerunPrincipal principal;
	private Perun perun;
	final static Logger log = LoggerFactory.getLogger(PerunSessionImpl.class);

	/**
	 * Constructor.
	 *
	 * @param perun     Perun
	 * @param principal identification of the actor, who will perform operations.
	 * @throws InternalErrorRuntimeException if any parametr is null
	 */
	public PerunSession(Perun perun, PerunPrincipal principal) {
		if (perun == null) throw new InternalErrorRuntimeException(new NullPointerException("perun is null"));
		if (principal == null) throw new InternalErrorRuntimeException(new NullPointerException("principal is null"));
		this.principal = principal;
		this.perun = perun;
	}

	public PerunPrincipal getPerunPrincipal() {
		return principal;
	}

	public Perun getPerun() {
		return perun;
	}

	public void destroy() throws InternalErrorException {
		perun = null;
		principal = null;
	}

	public void validate() throws InternalErrorException {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" +
			"principal='" + principal +
			"']";
	}
}
