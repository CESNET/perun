package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Custom logic for VO eduroam.
 *
 * @author Jan Zverina <zverina@cesnet.cz>
 */
public class Eduroam implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(DuSoft.class);

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	@Override
	public Application approveApplication(PerunSession session, Application app) throws VoNotExistsException, UserNotExistsException, PrivilegeException, MemberNotExistsException, InternalErrorException, GroupNotExistsException, AlreadyMemberException, ExternallyManagedException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException {
		// Add new member to groups eduroam-admin and eduroam-announce just if its initial application form
		if (Application.AppType.INITIAL.equals(app.getType())) {
			// Get perun and beans from session
			Perun perun = session.getPerun();
			Vo vo = app.getVo();
			User user = app.getUser();
			Member member = perun.getMembersManager().getMemberByUser(session, vo, user);

			// Get the groups in which the new member of VO will be automatically added
			Group eduroamAdmin = perun.getGroupsManager().getGroupByName(session, vo, "eduroam-admin");
			Group eduroamAnnounce = perun.getGroupsManager().getGroupByName(session, vo, "eduroam-announce");

			// Add member to these groups
			perun.getGroupsManager().addMember(session, eduroamAdmin, member);
			perun.getGroupsManager().addMember(session, eduroamAnnounce, member);
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
