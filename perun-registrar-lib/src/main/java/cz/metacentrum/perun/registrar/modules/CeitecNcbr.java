package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Module for CEITEC@MUNI and NCBR@MUNI VOs at CESNET instance of Perun.
 *
 * The module
 * 1. Allows (auto)approval of applications with LoA = 2.
 * 2. Enforce manual approval of applications with LoA = 0, 1.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CeitecNcbr implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(CeitecNcbr.class);

	@Override
	public void setRegistrar(RegistrarManager registrar) {
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	@Override
	public Application approveApplication(PerunSession session, Application app) {
		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) {
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		if (app.getExtSourceLoa() == 2) return;
		throw new CantBeApprovedException("Application can't be approved automatically. LoA is: "+app.getExtSourceLoa()+". Please double check users identity before manual/force approval.", "", "", "", true);

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
