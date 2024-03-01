package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Registration module used in "Elixir" VO on Elixir Perun instance. It is used to pre-generate available user login on
 * application form for new users.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz/>
 */
public class Elixir extends DefaultRegistrarModule {

  static final Logger LOG = LoggerFactory.getLogger(Elixir.class);

  private static String URN_USER_DISPLAY_NAME = AttributesManager.NS_USER_ATTR_CORE + ":" + "displayName";
  private static String URN_USER_PREFERRED_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":" + "preferredMail";

  /**
   * Retrieves specific attribute value from form items (first occurrence)
   *
   * @param formItems                 form items to search in
   * @param perunDestinationAttribute destination attribute
   * @return value of first found form item mapped to perunDestinationAttribute
   */
  private String fetchFormValue(List<ApplicationFormItemWithPrefilledValue> formItems,
                                String perunDestinationAttribute) {

    for (ApplicationFormItemWithPrefilledValue item : formItems) {
      if (perunDestinationAttribute.equals(item.getFormItem().getPerunDestinationAttribute())) {
        return item.getPrefilledValue();
      }
    }
    return null;

  }

  /**
   * Generates new login for input data
   *
   * @param session   PerunSession
   * @param formItems Whole form data
   * @return
   */
  private String generateLogin(PerunSession session, ApplicationFormItemWithPrefilledValue loginItem,
                               List<ApplicationFormItemWithPrefilledValue> formItems) {

    String displayName = fetchFormValue(formItems, URN_USER_DISPLAY_NAME);
    PerunBl perun = (PerunBl) session.getPerun();

    User user = null;
    try {
      user = Utils.parseUserFromCommonName(displayName, false);
    } catch (Exception ex) {

      LOG.warn("We couldn't parse commonName/displayName into User object");

      String mail = fetchFormValue(formItems, URN_USER_PREFERRED_MAIL);
      if (mail != null) {
        mail = mail.split("@")[0];
        user = new User(0, null, mail, null, null, null);
      }

    }

    if (user != null) {

      ModulesUtilsBlImpl.LoginGenerator generator = new ModulesUtilsBlImpl.LoginGenerator();

      String login = generator.generateLogin(user, new ModulesUtilsBlImpl.LoginGenerator.LoginGeneratorFunction() {
        @Override
        public String generateLogin(String firstName, String lastName) {

          String wholeLogin = "";
          if (firstName != null && !firstName.isEmpty()) {
            wholeLogin = firstName;
          }
          if (lastName != null && !lastName.isEmpty()) {
            wholeLogin = wholeLogin + lastName;
          }
          return wholeLogin;

        }
      });

      if (StringUtils.isEmpty(login)) {
        return null;
      }

      String checkedLogin = login;

      // fill value (with incremental number on conflict)
      int iterator = 0;
      while (iterator >= 0) {

        if (iterator > 0) {
          int iteratorLength = String.valueOf(iterator).length();
          if (login.length() + iteratorLength > 20) {
            // if login+iterator > 20 => crop login & reset iterator
            checkedLogin = login.substring(0, login.length() - 1);
            iterator = 0;
          } else {
            checkedLogin = login + iterator;
          }
        } else {
          // checked login is used
        }

        try {

          AttributeDefinition def = perun.getAttributesManagerBl()
              .getAttributeDefinition(session, loginItem.getFormItem().getPerunDestinationAttribute());
          Attribute checkAttribute = new Attribute(def, checkedLogin);
          perun.getAttributesManagerBl().checkAttributeSemantics(session, user, checkAttribute);
          return checkedLogin;

        } catch (WrongReferenceAttributeValueException ex) {
          // continue in a WHILE cycle - generated login was used
          iterator++;
        } catch (AttributeNotExistsException ex) {
          // we couldn't pre-fill login, its mapped to non-existing attribute
          LOG.warn("We couldn't generate new login, since its mapped to non-exisitng attribute {}., {}",
              loginItem.getFormItem().getPerunDestinationAttribute(), ex);
          return null;
        } catch (WrongAttributeAssignmentException | InternalErrorException e) {
          LOG.warn("We couldn't generate new login, because of exception.", e);
          return null;
        }
      }

    } else {
      LOG.error("We couldn't create arbitrary User object with name from form items in order to generate login.");
    }

    return null;

  }

  @Override
  public void processFormItemsWithData(PerunSession session, Application.AppType appType, ApplicationForm form,
                                       List<ApplicationFormItemWithPrefilledValue> formItems) throws PerunException {

    // generate login only on initial application
    if (!Application.AppType.INITIAL.equals(appType)) {
      return;
    }

    for (ApplicationFormItemWithPrefilledValue item : formItems) {
      if (Objects.equals(ApplicationFormItem.Type.USERNAME, item.getFormItem().getType())) {

        // skip if user already has login pre-filled from perun or federation
        if (!StringUtils.isEmpty(item.getPrefilledValue())) {
          continue;
        }

        // do not generate login if destination attribute is not set (won't be stored)
        if (StringUtils.isEmpty(item.getFormItem().getPerunDestinationAttribute())) {
          continue;
        }

        // set new generated value
        item.setPrefilledValue(generateLogin(session, item, formItems));
        // mark value as generated so the GUI allows editing and on submit server process new login
        item.setGenerated(true);

      }
    }

  }

}
