package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
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
 * Application module for EduGain purpose
 */
public class EduGain implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	/**
	 * All new members will be given role VOOBSERVER and TOPGROUPCREATOR
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, InternalErrorException, GroupNotExistsException, VoNotExistsException {

		if (Application.AppType.INITIAL.equals(app.getType())) {

			Vo vo = app.getVo();
			User user = app.getUser();

			AuthzResolver.setRole(session, user, vo, Role.TOPGROUPCREATOR);

			Group membersGroup = session.getPerun().getGroupsManager().getGroupByName(session, vo, "members");
			AuthzResolver.setRole(session, user, membersGroup, Role.GROUPADMIN);

		}

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

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
