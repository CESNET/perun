package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupExchangeMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupPreferredMailUrn;

/**
 * Storage for all spare mail aliases of a person at VŠUP.
 * Primary mail stored in O365 Exchange server is stored in user:def:vsupExchangeMail attribute !!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupExchangeMailAliases extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  // VŠUP ALIASES MAIL PATTERN !!
  public static final Pattern vsupAliasesMailPattern =
      Pattern.compile("^[-_A-Za-z0-9+']+(\\.[-_A-Za-z0-9+']+)*@(vsup|umprum)\\.cz$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {

    List<String> mails = (attribute.getValue() != null) ? (attribute.valueAsList()) : (new ArrayList<>());

    for (String mail : mails) {
      Matcher emailMatcher = vsupAliasesMailPattern.matcher(mail);
      if (!emailMatcher.find()) {
        throw new WrongAttributeValueException(attribute, user,
            "Following value of primary mail alias is not in a correct form: '" + mail + "'.");
      }
    }

    // TODO - check uniqueness within list of values ??

  }

  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    // list of reserved mails for user
    Attribute reservedMailsAttribute;
    ArrayList<String> reservedMailsAttributeValue;

    // other vsup mail attributes to get values from
    Attribute vsupMailAttribute;
    Attribute vsupPreferredMailAttribute;
    Attribute vsupExchangeMailAttribute;

    // output sets used for comparison
    Set<String> reservedMailsOfUser = new HashSet<>();
    Set<String> actualMailsOfUser = new HashSet<>();

    // get related attributes
    try {
      reservedMailsAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttributeForUpdate(session, user, usedMailsUrn);
      vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
      vsupPreferredMailAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupPreferredMailUrn);
      vsupExchangeMailAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailUrn);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException(e);
    }

    // if REMOVE action and reserved map is empty -> consistency error

    if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
      throw new ConsistencyErrorException(
          "User attribute 'urn:perun:user:attribute-def:def:usedMails' is empty, but we are removing 'vsupExchangeMailAliases' value, so there should have been entry in usedMails attribute.");
    }

    // get value from reserved mails attribute

    if (reservedMailsAttribute.getValue() == null) {
      reservedMailsAttributeValue = new ArrayList<>();
    } else {
      reservedMailsAttributeValue = reservedMailsAttribute.valueAsList();
    }

    // fill set for comparison
    reservedMailsOfUser.addAll(reservedMailsAttributeValue);

    if (vsupMailAttribute.getValue() != null) {
      actualMailsOfUser.add(vsupMailAttribute.valueAsString());
    }
    if (vsupPreferredMailAttribute.getValue() != null) {
      actualMailsOfUser.add(vsupPreferredMailAttribute.valueAsString());
    }
    if (vsupExchangeMailAttribute.getValue() != null) {
      actualMailsOfUser.add(vsupExchangeMailAttribute.valueAsString());
    }

    // Remove values, which are no longer set to any of user mail attributes
    for (String mail : reservedMailsOfUser) {
      if (!actualMailsOfUser.contains(mail)) {
        // Remove mail, which is not in attributes anymore
        reservedMailsAttributeValue.remove(mail);
      }
    }

    // if SET action and new mails are not present (prevent duplicates within the value)
    if (attribute.getValue() != null) {
      List<String> mails = attribute.valueAsList();
      for (String mail : mails) {
        if (!reservedMailsAttributeValue.contains(mail)) {
          reservedMailsAttributeValue.add(mail);
        }
      }
    }

    // save changes in reserved mails attribute
    try {
      // always set value to attribute, since we might start with null in attribute and empty list in variable !!
      reservedMailsAttribute.setValue(reservedMailsAttributeValue);
      session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, reservedMailsAttribute);
    } catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("vsupExchangeMailAliases");
    attr.setDisplayName("School mail aliases");
    attr.setType(ArrayList.class.getName());
    attr.setDescription(
        "Spare school mail aliases of a user. It contains all former addresses of user from @vsup.cz namespace. Values are filled manually.");
    return attr;
  }

}
