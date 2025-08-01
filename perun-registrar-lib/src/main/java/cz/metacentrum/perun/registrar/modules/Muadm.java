package cz.metacentrum.perun.registrar.modules;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registration module for mu-adm. It is used to pre-generate login on application form.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class Muadm extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(Muadm.class);

  private static final String URN_USER_D_LOGIN_MU = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";

  private String generateLogin(PerunSession session) {
    User user = session.getPerunPrincipal().getUser();
    if (user == null) {
      return null;
    }

    PerunBl perunBl = (PerunBl) session.getPerun();
    Attribute uco;
    try {
      uco = perunBl.getAttributesManagerBl().getAttribute(session, user, URN_USER_D_LOGIN_MU);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      LOG.error("Cannot generate login.", e);
      return null;
    }

    if (uco.getValue() == null) {
      LOG.error("Cannot generate login, the user doesn't have filled attribute " + URN_USER_D_LOGIN_MU);
      return null;
    } else {
      return uco.valueAsString() + "adm";
    }
  }

  @Override
  public void processFormItemsWithData(PerunSession session, Application.AppType appType, ApplicationForm form,
                                       Map<String, List<String>> externalParams,
                                       List<ApplicationFormItemWithPrefilledValue> formItems) throws PerunException {
    if (!Application.AppType.INITIAL.equals(appType) && !Application.AppType.EMBEDDED.equals(appType)) {
      return;
    }

    formItems.stream().filter(item -> item.getFormItem().getType() == ApplicationFormItem.Type.USERNAME)
        .filter(item -> isEmpty(item.getPrefilledValue()))
        .filter(item -> isNotEmpty(item.getFormItem().getPerunDestinationAttribute())).forEach(item -> {
          item.setPrefilledValue(generateLogin(session));
          item.setGenerated(true);
        });
  }
}
