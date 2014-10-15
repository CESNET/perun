package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Application module for EduGain purpose
 */
public class EduGain implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	/**
	 * All new members will be given role VOOBSERVER and TOPGROUPCREATOR
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

		if (Application.AppType.INITIAL.equals(app.getType())) {

			Vo vo = app.getVo();
			User user = app.getUser();

			AuthzResolver.setRole(session, user, vo, Role.VOOBSERVER);
			AuthzResolver.setRole(session, user, vo, Role.TOPGROUPCREATOR);

		}

		return app;

	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

}
