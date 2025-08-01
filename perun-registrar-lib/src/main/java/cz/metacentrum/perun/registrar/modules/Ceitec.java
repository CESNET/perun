package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.modules.CeitecCrmConnector;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for CEITEC VO on e-INFRA CZ instance.
 * <p>
 * Only applications with CEITEC_ID param passed in URL are allowed to submit the application
 * during the migration period.
 * Valid CEITEC_ID is prefilled back to the registration form and later stored in attribute.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Ceitec extends DefaultRegistrarModule {

  static final Logger LOG = LoggerFactory.getLogger(Ceitec.class);
  private final CeitecCrmConnector ceitecCrmConnector = new CeitecCrmConnector();

  @Override
  public void canBeSubmitted(PerunSession session, Application.AppType appType, Map<String, String> federValues,
                             Map<String, List<String>> externalParams) throws PerunException {

    String ceitecId = null;
    if (externalParams != null) {
      List<String> ceitecIds = externalParams.get("ceitec_id");
      ceitecId = ceitecIds.get(0);
    }

    if (ceitecCrmConnector.isMigrationRunning() && Application.AppType.INITIAL.equals(appType)) {
      if (StringUtils.isBlank(ceitecId)) {
        throw new CantBeSubmittedException(
                "Registration in e-INFRA CZ instance of CEITEC is opened only for current CEITEC users." +
                        "Make sure 'ceitec_id' parameter is present in registration URL.",
                "NOT_ELIGIBLE_CEITEC", null, null);
      } else if (!ceitecCrmConnector.checkCrmUserExists(ceitecId)) {
        throw new CantBeSubmittedException(
                "Registration in e-INFRA CZ instance of CEITEC is opened only for current CEITEC users." +
                        "Provided 'ceitec_id' parameter is not valid.",
                "NOT_CEITEC_USER", null, null);
      }
    }

  }

  @Override
  public void processFormItemsWithData(PerunSession session, Application.AppType appType, ApplicationForm form,
                                       Map<String, List<String>> externalParams,
                                       List<ApplicationFormItemWithPrefilledValue> formItems) throws PerunException {

    String ceitecId = null;
    if (externalParams != null) {
      List<String> ceitecIds = externalParams.get("ceitec_id");
      ceitecId = ceitecIds.get(0);
    }

    if (!ceitecCrmConnector.isMigrationRunning()) {
      // TODO - outside migration will we check by EPPNs that there are no 2 users - if so show warning message?
      return;
    }
    if (StringUtils.isBlank(ceitecId)) {
      return;
    }
    if (!ceitecCrmConnector.checkCrmUserExists(ceitecId)) {
      return;
    }
    for (ApplicationFormItemWithPrefilledValue formItem : formItems) {
      if ("ceitec_id".equals(formItem.getFormItem().getShortname())) {
        formItem.setPrefilledValue(ceitecId);
      }
    }

  }

  @Override
  public Application beforeApprove(PerunSession session, Application app) throws CantBeApprovedException,
          RegistrarException, PrivilegeException {

    // we must store CEITEC ID before adding existing user to VO
    // to make sure it's not empty when auto-generating login/CN.

    if (!ceitecCrmConnector.isMigrationRunning()) {
      return app;
    }
    if (!Application.AppType.INITIAL.equals(app.getType())) {
      return app;
    }
    if (app.getUser() == null) {
      return app;
    }

    List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());
    for (ApplicationFormItemData item : data) {
      if ("ceitec_id".equals(item.getShortname()) && StringUtils.isNotBlank(item.getValue())) {
        getSetCeitecIdAttribute(session, app.getUser(), item.getValue());
      }
    }
    return app;

  }

  private void getSetCeitecIdAttribute(PerunSession session, User user, String ceitecId)
          throws CantBeApprovedException {

    PerunBl perun = (PerunBl) session.getPerun();
    try {
      Attribute a = perun.getAttributesManagerBl().getAttribute(session, user,
              AttributesManager.NS_USER_ATTR_DEF + ":ceitecId");
      if (a != null) {
        if (a.getValue() != null) {
          throw new WrongAttributeValueException("User " + user +
                  " already has CEITEC ID attribute.");
        }
        a.setValue(ceitecId);
        perun.getAttributesManagerBl().setAttribute(session, user, a);
      }
    } catch (WrongAttributeAssignmentException | WrongAttributeValueException |
             WrongReferenceAttributeValueException e) {
      throw new CantBeApprovedException("Can't get/set ceitecId attribute.", e);
    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException("Required attribute for CEITEC ID doesn't exist.");
    }

  }

}
