package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;

/**
 * Custom logic for VO eduroam.
 *
 * @author Jan Zverina <zverina@cesnet.cz>
 */
public class Eduroam extends DefaultRegistrarModule {

	@Override
	public Application approveApplication(PerunSession session, Application app) throws VoNotExistsException, UserNotExistsException, PrivilegeException, MemberNotExistsException, GroupNotExistsException, AlreadyMemberException, ExternallyManagedException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException {
		// Add new member to groups eduroam-admin and eduroam-announce just if its initial application form
		if (Application.AppType.INITIAL.equals(app.getType())) {
			// Get perun and beans from session
			PerunBl perun = (PerunBl)session.getPerun();
			Vo vo = app.getVo();
			User user = app.getUser();
			Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

			// Get the groups in which the new member of VO will be automatically added
			Group eduroamAdmin = perun.getGroupsManagerBl().getGroupByName(session, vo, "eduroam-admin");
			Group eduroamAnnounce = perun.getGroupsManagerBl().getGroupByName(session, vo, "eduroam-announce");

			// Add member to these groups
			perun.getGroupsManager().addMember(session, eduroamAdmin, member);
			perun.getGroupsManager().addMember(session, eduroamAnnounce, member);
		}

		return app;
	}

}
