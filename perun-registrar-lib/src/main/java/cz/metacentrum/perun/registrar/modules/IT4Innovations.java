package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.model.Application;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module ensures, that all new VO members which goes through registrations are also added to common VO "e-INFRA
 * CZ".
 * <p>
 * Users marked with colliding accounts are prohibited from the registration!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class IT4Innovations extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(IT4Innovations.class);

  /**
   * Add approved VO members into e-INFRA CZ VO.
   */
  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws WrongReferenceAttributeValueException, WrongAttributeValueException {

    PerunBl perun = (PerunBl) session.getPerun();
    User user = app.getUser();
    Vo vo = app.getVo();

    // For INITIAL VO APPLICATIONS
    if (Application.AppType.INITIAL.equals(app.getType()) && app.getGroup() == null) {
      try {
        Vo einfraVo = perun.getVosManagerBl().getVoByShortName(session, "e-infra.cz");
        Member einfraMember = perun.getMembersManagerBl().createMember(session, einfraVo, user);
        LOG.debug("{} member added to \"e-INFRA CZ\": {}", vo.getName(), einfraMember);
        perun.getMembersManagerBl().validateMemberAsync(session, einfraMember);
      } catch (VoNotExistsException e) {
        LOG.warn("e-INFRA CZ VO doesn't exists, {} member can't be added into it.", vo.getName());
      } catch (AlreadyMemberException ignore) {
        // user is already in e-INFRA CZ
      } catch (ExtendMembershipException e) {
        // can't be member of e-INFRA CZ, shouldn't happen
        LOG.error("{} member can't be added to \"e-INFRA CZ\": {}", vo.getName(), e);
      }
    }

    return app;

  }

  @Override
  public void canBeApproved(PerunSession session, Application app) throws PerunException {
    if (isBlockedUser(session)) {
      throw new CantBeApprovedException(
          "Users account from application is in collision with existing account in IT4Innovations. It must be " +
          "resolved manually.");
    }
  }

  @Override
  public void canBeSubmitted(PerunSession session, Application.AppType appType, Map<String, String> params)
      throws PerunException {
    if (isBlockedUser(session)) {
      throw new CantBeSubmittedException(
          "Your existing user account is in collision with existing account in IT4Innovations. In order to register " +
          "please contact support at support@it4i.cz");
    }
  }

  private boolean isBlockedUser(PerunSession session)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {

    PerunBl perun = (PerunBl) session.getPerun();
    User user = session.getPerunPrincipal().getUser();

    if (user != null) {
      // Check if user is not prevented from registration.
      Attribute a = perun.getAttributesManagerBl()
          .getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":it4iBlockCollision");
      return (a.valueAsBoolean() != null) ? a.valueAsBoolean() : false;
    }
    return false;

  }

}
