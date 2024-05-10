package cz.metacentrum.perun.registrar.modules;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registration module for admin-meta. It is used to pre-generate login on application form from einfra login.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class AdminMeta extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(AdminMeta.class);

  private static final String URN_USER_D_LOGIN_EINFRA = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:einfra";

  private String generateLogin(PerunSession session) {

    PerunBl perunBl = (PerunBl) session.getPerun();
    User user = session.getPerunPrincipal().getUser();
    if (user == null) {

      // try to get login from einfra login reservations
      // FIXME - not effective at all but works
      List<Application> apps = registrar. getApplicationsForUser(session);
      for (Application app : apps) {
        List<Pair<String, String>> reservedLogins = perunBl.getUsersManagerBl().getReservedLoginsByApp(session,
                app.getId());
        for (Pair<String, String> pair : reservedLogins) {
          if ("einfra".equals(pair.getLeft())) {
            // reserved login found
            return pair.getRight();
          }
        }

      }
      // no user / no login-found
      return null;
    }

    Attribute login;
    try {
      login = perunBl.getAttributesManagerBl().getAttribute(session, user, URN_USER_D_LOGIN_EINFRA);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      LOG.error("Cannot generate login.", e);
      return null;
    }

    if (login.getValue() == null) {
      LOG.error("Cannot generate login, the user doesn't have filled attribute " + URN_USER_D_LOGIN_EINFRA);
      return null;
    } else {
      return login.valueAsString();
    }
  }

  @Override
  public void processFormItemsWithData(PerunSession session, Application.AppType appType, ApplicationForm form,
                                       List<ApplicationFormItemWithPrefilledValue> formItems) throws PerunException {
    if (Application.AppType.INITIAL.equals(appType)) {
      formItems.stream().filter(item -> item.getFormItem().getType() == ApplicationFormItem.Type.USERNAME)
              .filter(item -> isEmpty(item.getPrefilledValue()))
              .filter(item -> isNotEmpty(item.getFormItem().getPerunDestinationAttribute())).forEach(item -> {
                item.setPrefilledValue(generateLogin(session));
                item.setGenerated(true);
              });
    }

  }
}
