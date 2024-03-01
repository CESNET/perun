package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoApproveByGroupMembership extends DefaultRegistrarModule {
  private static final Logger log = LoggerFactory.getLogger(AutoApproveByGroupMembership.class);

  private static final String AUTOAPPROVEBYGROUPMEMBERSHIP_GROUP_ATTRNAME =
      AttributesManager.NS_GROUP_ATTR_DEF + ":autoApproveByGroupMembership";
  private static final String AUTOAPPROVEBYGROUPMEMBERSHIP_VO_ATTRNAME =
      AttributesManager.NS_VO_ATTR_DEF + ":autoApproveByGroupMembership";

  @Override
  public boolean autoApproveShouldBeForce(PerunSession sess, Application app) throws PerunException {
    PerunBl perun = (PerunBl) sess.getPerun();

    // force auto approval only for group application
    if (app.getGroup() == null || app.getVo() == null) {
      return false;
    }

    Member member;

    try {
      if (app.getUser() == null) {
        LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
        PerunPrincipal applicationPrincipal =
            new PerunPrincipal(app.getCreatedBy(), app.getExtSourceName(), app.getExtSourceType(),
                app.getExtSourceLoa(), additionalAttributes);

        User user = perun.getUsersManagerBl().getUserByExtSourceInformation(sess, applicationPrincipal);
        if (user != null) {
          member = perun.getMembersManager().getMemberByUser(sess, app.getVo(), user);
        } else {
          log.error(
              "[REGISTRAR] User is not member of VO, therefore it is not possible to auto-approve group application: " +
                  app);
          return false;
        }
      } else {
        member = perun.getMembersManager().getMemberByUser(sess, app.getVo(), app.getUser());
      }
    } catch (MemberNotExistsException | UserNotExistsException | UserExtSourceNotExistsException |
             ExtSourceNotExistsException ex) {
      log.error(
          "[REGISTRAR] User is not member of VO, therefore it is not possible to auto-approve group application: " +
              app);
      return false;
    }

    Attribute autoApproveGroupIds =
        perun.getAttributesManagerBl().getAttribute(sess, app.getGroup(), AUTOAPPROVEBYGROUPMEMBERSHIP_GROUP_ATTRNAME)
            .valueAsList() != null ?
            perun.getAttributesManagerBl()
                .getAttribute(sess, app.getGroup(), AUTOAPPROVEBYGROUPMEMBERSHIP_GROUP_ATTRNAME) :
            perun.getAttributesManagerBl().getAttribute(sess, app.getVo(), AUTOAPPROVEBYGROUPMEMBERSHIP_VO_ATTRNAME);

    if (autoApproveGroupIds.valueAsList() != null) {
      for (String id : autoApproveGroupIds.valueAsList()) {
        try {
          if (perun.getGroupsManagerBl().getTotalMemberGroupStatus(sess, member,
              perun.getGroupsManagerBl().getGroupById(sess, Integer.parseInt(id))) == MemberGroupStatus.VALID) {
            return true;
          }
        } catch (GroupNotExistsException ex) {
          if (perun.getAttributesManagerBl()
              .getAttribute(sess, app.getGroup(), AUTOAPPROVEBYGROUPMEMBERSHIP_GROUP_ATTRNAME).valueAsList() != null) {
            log.error("[REGISTRAR] Attribute autoApproveByGroupMembership for group " + app.getGroup() +
                " contains invalid group ID: " + id);
          } else {
            log.error("[REGISTRAR] Attribute autoApproveByGroupMembership for VO " + app.getVo() +
                " contains invalid group ID: " + id);
          }
        }
      }
    }

    return false;
  }
}
