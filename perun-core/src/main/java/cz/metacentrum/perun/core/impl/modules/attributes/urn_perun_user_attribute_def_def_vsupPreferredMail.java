package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.impl.Utils.EMAIL_PATTERN;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupExchangeMailAliasesUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupExchangeMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;

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
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Attribute module for storing preferred mail of a user at VŠUP.
 * <p>
 * It is by default filled from u:d:vsupExchangeMail or u:d:vsupMail (changing domain in value to @umprum.cz), but it
 * can be set manually to any value (even outside of @vsup.cz and @umprum.cz domains) or can be empty.
 * <p>
 * If it is empty, value might be set by setting source attributes u:d:vsupMail or u:d:vsupExchangeMail.
 * <p>
 * On value change, map of usedMails in entityless attributes is checked and updated. Also, value is copied to
 * u:d:preferredMail so admin can see preferred mail in old GUI.
 * <p>
 * Since filled value by this module might be NULL at the time of processing, we must allow NULL value in
 * checkAttributeSemantics(), because when all mail attributes are required and set at once, we can't ensure correct
 * processing order of related attributes and it might perform check on old value, because of setRequiredAttributes()
 * implementation uses in memory value instead of refreshing from DB.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupPreferredMail extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    // list of reserved mails for user
    Attribute reservedMailsAttribute;
    ArrayList<String> reservedMailsAttributeValue;

    // other vsup mail attributes to get values from
    Attribute vsupMailAttribute;
    Attribute vsupExchangeMailAttribute;
    Attribute vsupExchangeMailAliasesAttribute;

    // output sets used for comparison
    Set<String> reservedMailsOfUser = new HashSet<>();
    Set<String> actualMailsOfUser = new HashSet<>();

    // get related attributes
    try {
      reservedMailsAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttributeForUpdate(session, user, usedMailsUrn);
      vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
      vsupExchangeMailAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailUrn);
      vsupExchangeMailAliasesAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailAliasesUrn);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException(e);
    }

    // if REMOVE action and reserved map is empty -> consistency error

    if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
      throw new ConsistencyErrorException(
          "User attribute 'urn:perun:user:attribute-def:def:usedMails' is empty, but we are removing " +
          "'vsupPreferredMail' value, so there should have been entry in usedMails attribute.");
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
    if (vsupExchangeMailAttribute.getValue() != null) {
      actualMailsOfUser.add(vsupExchangeMailAttribute.valueAsString());
    }
    if (vsupExchangeMailAliasesAttribute.getValue() != null) {
      actualMailsOfUser.addAll(vsupExchangeMailAliasesAttribute.valueAsList());
    }

    // Remove values, which are no longer set to any of user mail attributes
    for (String mail : reservedMailsOfUser) {
      if (!actualMailsOfUser.contains(mail)) {
        // modify attribute value, not our comparison set
        reservedMailsAttributeValue.remove(mail);
        // since this attribute holds single value, we can break the cycle here
        break;
      }
    }

    // if SET action and new mail is not present (prevent duplicates within the value)
    if (attribute.getValue() != null && !reservedMailsAttributeValue.contains(attribute.valueAsString())) {
      reservedMailsAttributeValue.add(attribute.valueAsString());
    }

    // save changes in reserved mails attribute
    try {
      // always set value to attribute, since we might start with null in attribute and empty list in variable !!
      reservedMailsAttribute.setValue(reservedMailsAttributeValue);
      session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, reservedMailsAttribute);
    } catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
      // FIXME - špatná hodnota znamená že není unikátní !!
      throw new InternalErrorException(ex);
    }

    // update user:preferredMail so admin can see users preferred mail in GUI.
    try {
      if (attribute.getValue() != null) {

        Attribute userPreferredMail = session.getPerunBl().getAttributesManagerBl()
            .getAttribute(session, user, "urn:perun:user:attribute-def:def:preferredMail");
        if (!Objects.equals(userPreferredMail.getValue(), attribute.getValue())) {
          // if preferred mail is different, update user:preferredMail
          userPreferredMail.setValue(attribute.getValue());
          session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);
        }
      }
    } catch (WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {

    // we must allow null, since when setting required attributes all at once, value might not be filled yet
    // if vsupMail or vsupExchangeMail is empty, but it's checked by method impl.

    if (attribute.getValue() == null) {
      return; // throw new WrongAttributeValueException(attribute, user, "Preferred mail can't be null.");
    }

    // standard email pattern !!
    Matcher emailMatcher = EMAIL_PATTERN.matcher((String) attribute.getValue());
    if (!emailMatcher.find()) {
      throw new WrongAttributeValueException(attribute, user, "School Preferred Mail is not in a correct form.");
    }

    // We check uniqueness on all related attributes change, so we don't need to do it here.

  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {

    // Fill attribute preferably from u:d:vsupExchangeMail or u:d:vsupMail as backup
    Attribute resultAttribute = new Attribute(attribute);
    try {
      Attribute vsupExchangeMail =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailUrn);
      if (vsupExchangeMail.getValue() != null) {
        resultAttribute.setValue(vsupExchangeMail.getValue());
      } else {
        Attribute vsupMail = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
        if (vsupMail.getValue() != null) {
          // We modify base mail domain, as we want to preferably use "@umprum.cz"
          resultAttribute.setValue(vsupMail.valueAsString().replace("@vsup.cz", "@umprum.cz"));
        }
      }

    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException("Related VŠUP mail attributes not exists.", e);
    }
    return resultAttribute;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("vsupPreferredMail");
    attr.setDisplayName("School preferred mail");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Preferred mail used for VŠUP internal services. Pre-filled from vsupMailAlias or vsupMail, but can be custom" +
        " value.");
    return attr;
  }

}
