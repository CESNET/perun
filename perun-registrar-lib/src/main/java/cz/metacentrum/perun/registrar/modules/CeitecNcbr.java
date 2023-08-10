package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for NCBR@MUNI VOs at CESNET instance.
 *
 * The module
 * 1. Allows (auto)approval of applications by users from IdP MUNI
 * 2. Enforce manual approval of applications by users from different IdPs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CeitecNcbr extends DefaultRegistrarModule {

	final static Logger log = LoggerFactory.getLogger(CeitecNcbr.class);

	private final static String IDP_MU = "https://idp2.ics.muni.cz/idp/shibboleth";

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		if (IDP_MU.equals(app.getExtSourceName())) return;
		throw new CantBeApprovedException("Application can't be approved automatically. User has not used IdP MUNI to log in. Please double check users identity before manual/force approval.", "", "", "", true, app.getId());

	}

}
