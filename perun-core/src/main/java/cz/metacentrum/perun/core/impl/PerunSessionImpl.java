package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session - you need it for almost all operation. It holds your priviledges. You get get managers from it.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 */
public class PerunSessionImpl extends PerunSession {

	final static Logger log = LoggerFactory.getLogger(PerunSessionImpl.class);

	public PerunSessionImpl(Perun perun, PerunPrincipal principal, PerunClient client) {
		super(perun, principal, client);
	}


	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[perunPrincipal='"+ getPerunPrincipal() +"']";
	}

	public PerunBl getPerunBl() {
		return (PerunBl) super.getPerun();
	}
}
