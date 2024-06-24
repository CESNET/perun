package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.model.Application;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users marked with colliding accounts are prohibited from the registration!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class IT4Innovations extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(IT4Innovations.class);

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
