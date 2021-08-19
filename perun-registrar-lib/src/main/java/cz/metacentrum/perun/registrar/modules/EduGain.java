package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application module for EduGain purpose
 */
public class EduGain extends DefaultRegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

	/**
	 * All new members will be given role VOOBSERVER and TOPGROUPCREATOR
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, GroupNotExistsException, VoNotExistsException {

		if (Application.AppType.INITIAL.equals(app.getType())) {

			Vo vo = app.getVo();
			User user = app.getUser();

			try {
				AuthzResolver.setRole(session, user, vo, Role.TOPGROUPCREATOR);

				Group membersGroup = session.getPerun().getGroupsManager().getGroupByName(session, vo, "members");
				AuthzResolver.setRole(session, user, membersGroup, Role.GROUPADMIN);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}

		}

		return app;

	}

}
