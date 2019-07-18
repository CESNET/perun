package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Module for WeNMR VO at CESNET instance of Perun.
 *
 * The module
 * 1. Allows (auto)approval of applications submitted by users with IdP identity https://www.structuralbiology.eu/idp/shibboleth
 * 2. Enforce manual approval of applications submitted by users without IdP identity https://www.structuralbiology.eu/idp/shibboleth
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class WeNMR implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(WeNMR.class);

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

		// check if submitted from trusted IdP
		if (!Objects.equals("https://www.structuralbiology.eu/idp/shibboleth", app.getExtSourceName())) {

			// submitted by untrusted IdP
			PerunBl perun = (PerunBl) session.getPerun();
			User user;

			// check if user is known
			if (app.getUser() != null) {
				user = app.getUser();
			} else {
				try {
					user = perun.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(session, app.getExtSourceName(), app.getCreatedBy());
				} catch (Exception ex) {
					// unable to find user -> untrusted IdP
					throw new CantBeApprovedException("Application can't be approved automatically. User doesn't have identity from \"www.structuralbiology.eu\". Please check users identity before manual/force approval.", "", "", "", true);
				}
			}

			List<UserExtSource> ueses = perun.getUsersManagerBl().getUserExtSources(session, user);
			for (UserExtSource ues : ueses) {
				if (Objects.equals("https://www.structuralbiology.eu/idp/shibboleth", ues.getExtSource().getName())) {
					// user has trusted identity
					return;
				}
			}
			throw new CantBeApprovedException("Application can't be approved automatically. User doesn't have identity from \"www.structuralbiology.eu\". Please check users identity before manual/force approval.", "", "", "", true);

		}

		// submitted from trusted IdP

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
