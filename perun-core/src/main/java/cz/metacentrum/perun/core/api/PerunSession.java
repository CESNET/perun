package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates context of user's interaction with the Perun system.
 *
 */
public abstract class PerunSession {

	private PerunPrincipal principal;
	private PerunClient client;
	private Perun perun;
	final static Logger log = LoggerFactory.getLogger(PerunSessionImpl.class);

	/**
	 * Constructor.
	 *
	 * @param perun     Perun
	 * @param principal identification of the actor, who will perform operations.
	 * @param client	represents client who communicates with Perun.
	 * @throws InternalErrorException if any parametr is null
	 */
	public PerunSession(Perun perun, PerunPrincipal principal, PerunClient client) {
		if (perun == null) throw new InternalErrorException(new NullPointerException("perun is null"));
		if (principal == null) throw new InternalErrorException(new NullPointerException("principal is null"));
		if (client == null) throw new InternalErrorException(new NullPointerException("client is null"));
		this.principal = principal;
		this.perun = perun;
		this.client = client;
	}

	public PerunPrincipal getPerunPrincipal() {
		return principal;
	}

	public PerunClient getPerunClient() {
		return client;
	}

	public Perun getPerun() {
		return perun;
	}

	public void destroy() throws InternalErrorException {
		perun = null;
		principal = null;
	}

	public void validate() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[" +"principal='" + principal + "', client='"+client+"']";
	}

	public String getLogId() {
		return principal.getActor();
	}
}
