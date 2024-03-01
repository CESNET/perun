package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for VO "vo.esc.pithia.eu" and admins subgroups of organizations
 * <p>
 * This module is used by the "admins" subgroups within the VO in order make its members also "the members" of
 * organization.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EscPithiaOrganizationAdmins extends DefaultRegistrarModule {

  static final Logger LOG = LoggerFactory.getLogger(EscPithiaOrganizationAdmins.class);

  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws UserNotExistsException, PrivilegeException, AlreadyAdminException, GroupNotExistsException,
      VoNotExistsException, MemberNotExistsException, AlreadyMemberException, ExternallyManagedException,
      WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException,
      WrongReferenceAttributeValueException, RegistrarException, ExtendMembershipException, ExtSourceNotExistsException,
      NotGroupMemberException {

    // works only for initial group applications
    if (Application.AppType.INITIAL.equals(app.getType()) && app.getGroup() != null) {

      PerunBl perun = (PerunBl) session.getPerun();
      Group group = app.getGroup();
      Vo vo = app.getVo();
      User user = app.getUser();
      Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

      Group organizationGroup = null;
      try {
        organizationGroup = perun.getGroupsManagerBl().getParentGroup(session, group);
      } catch (ParentGroupNotExistsException e) {
        throw new ConsistencyErrorException("Parent group of our group doesn't exist!", e);
      }

      Group membersGroup =
          perun.getGroupsManagerBl().getGroupByName(session, vo, organizationGroup.getName() + ":members");

      try {
        // make sure admin is also member of the organization
        perun.getGroupsManagerBl().addMember(session, membersGroup, member);
      } catch (AlreadyMemberException e) {
        // ignore
      }

    }

    return app;

  }

}
