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
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupExchangeMailAliasesUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupPreferredMailUrn;

/**
 * Attribute module for Primary school mail at VŠUP.
 * Expected format is "firstName.lastName[counter]@umprum.cz".
 * Artistic names have preference over normal: "u:d:artisticFirstName", "u:d:artisticLastName".
 * If firstName contains more names, only first is taken.
 * If lastName contains more names, space is replaced with dash.
 * Value can be empty or manually changed if necessary.
 * In case of users name change, attribute value must be changed manually !!
 * Value is filled/generated only for normal users (service users shouldn't have this attribute set)!!
 * <p>
 * On value change, map of usedMails in entityless attributes is checked and updated.
 * Also u:d:vsupPreferredMail is set to current value, if is empty or equals u:d:vsupMail.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupExchangeMail extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  // VŠUP EXCHANGE MAIL PATTERN !!
  private static final Pattern vsupExchangeMailPattern =
      Pattern.compile("^[-A-Za-z]+\\.[-A-Za-z]+([0-9])*@umprum\\.cz$");

  /**
   * Normalize string for the purpose of generating safe mail value.
   *
   * @return normalized string
   */
  public static String normalizeStringForMail(String toBeNormalized) {

    if (toBeNormalized == null || toBeNormalized.trim().isEmpty()) {
      return null;
    }

    toBeNormalized = toBeNormalized.toLowerCase();
    toBeNormalized = java.text.Normalizer.normalize(toBeNormalized, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    toBeNormalized = toBeNormalized.replaceAll("[^-a-zA-Z]+", "");

    // unable to fill login for users without name or with partial name
    if (toBeNormalized.isEmpty()) {
      return null;
    }

    return toBeNormalized;

  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    // can be empty
    if (attribute.getValue() == null) {
      return;
    }

    // if set, must match generic format
    Matcher emailMatcher = vsupExchangeMailPattern.matcher(attribute.valueAsString());
    if (!emailMatcher.find()) {
      throw new WrongAttributeValueException(attribute, user,
          "Primary mail alias is not in a correct form: \"firstName.lastName[counter]@umprum.cz\".");
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    // can be empty
    if (attribute.getValue() == null) {
      return;
    }

    // We must check uniqueness since vsupExchangeMail is filled by itself and filling function iterates until value is correct.

    try {

      // lock this logic
      Attribute reservedMailsAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttributeForUpdate(sess, user, usedMailsUrn);

      List<User> users =
          sess.getPerunBl().getUsersManagerBl().getUsersByAttributeValue(sess, usedMailsUrn, attribute.valueAsString());
      if (users.size() > 1) {
        String ids = ArrayUtils.toString(users.stream().map(User::getId).collect(Collectors.toList()));
        throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null,
            "VŠUP primary mail: '" + attribute.getValue() + "' is shared between users with IDs: " + ids + ".");
      }

      if (users.size() == 1) {
        User ownerUser = users.get(0);
        if (!Objects.equals(ownerUser.getId(), user.getId())) {
          throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null,
              "VŠUP primary mail: '" + attribute.getValue() + "' is already in use by User ID: " + ownerUser.getId() +
                  ".");
        }
      }

    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new ConsistencyErrorException("Attribute is not of user type.", ex);
    }

  }

  @Override
  public List<String> getDependencies() {
    return Collections.singletonList(usedMailsUrn);
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {

    if (user.isSpecificUser()) {
      // Do not fill value for service/sponsored users
      return new Attribute(attribute);
    }

    String firstName = user.getFirstName();
    String lastName = user.getLastName();
    Attribute filledAttribute = new Attribute(attribute);

    try {
      Attribute artFirstName = session.getPerunBl().getAttributesManagerBl()
          .getAttribute(session, user, "urn:perun:user:attribute-def:def:artisticFirstName");
      Attribute artLastName = session.getPerunBl().getAttributesManagerBl()
          .getAttribute(session, user, "urn:perun:user:attribute-def:def:artisticLastName");

      if (artFirstName.getValue() != null) {
        firstName = artFirstName.valueAsString();
      }
      if (artLastName.getValue() != null) {
        lastName = artLastName.valueAsString();
      }

    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException("Definition for artistic names of user doesn't exists.", e);
    }

    if (lastName == null || firstName == null) {
      return filledAttribute;
    }

    // if first name contains more names (divided by spaces), then take only first
    firstName = firstName.split(" ")[0];
    // if last name contains more names (divided by spaces), then replace space with dash
    lastName = lastName.replaceAll(" ", "-");

    // remove all diacritics marks from name
    String mail = normalizeStringForMail(firstName) + "." + normalizeStringForMail(lastName);

    // fill value - start as mail, mail2, mail3, ....
    int iterator = 1;
    while (iterator >= 1) {
      if (iterator > 1) {
        filledAttribute.setValue(mail + iterator + "@umprum.cz");
      } else {
        filledAttribute.setValue(mail + "@umprum.cz");
      }
      try {
        checkAttributeSemantics(session, user, filledAttribute);
        return filledAttribute;
      } catch (WrongReferenceAttributeValueException ex) {
        // continue in a WHILE cycle
        iterator++;
      }
    }

    return filledAttribute;

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
    Attribute vsupExchangeMailAliasesAttribute;

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
          "User attribute 'urn:perun:user:attribute-def:def:usedMails' is empty, but we are removing 'vsupExchangeMail' value, so there should have been entry in usedMails attribute.");
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
    if (vsupExchangeMailAliasesAttribute.getValue() != null) {
      actualMailsOfUser.addAll(vsupExchangeMailAliasesAttribute.valueAsList());
    }

    // Remove values, which are no longer set to any of user mail attributes
    for (String mail : reservedMailsOfUser) {
      if (!actualMailsOfUser.contains(mail)) {
        // Remove mail, which is not in attributes anymore
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
      throw new InternalErrorException(ex);
    }

    // if set, check u:d:vsupPreferredMail and set its value if is currently empty or equals vsupMail (in both domains @vsup.cz or @umprum.cz)
    if (attribute.getValue() != null) {

      String preferredMail = vsupPreferredMailAttribute.valueAsString();
      if (preferredMail == null || Objects.equals(preferredMail, vsupMailAttribute.valueAsString()) ||
          Objects.equals(preferredMail, vsupMailAttribute.valueAsString().replace("@vsup.cz", "@umprum.cz"))) {
        vsupPreferredMailAttribute.setValue(attribute.getValue());
        try {
          session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, vsupPreferredMailAttribute);
        } catch (WrongAttributeValueException | WrongAttributeAssignmentException e) {
          throw new InternalErrorException("Unable to store generated vsupExchangeMail to vsupPreferredMail.", e);
        }
      }
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("vsupExchangeMail");
    attr.setDisplayName("School mail (primary)");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Generated primary school mail in a \"name.surname[counter]@umprum.cz\" form. It is used by o365 Exchange server. Value can be empty. On users name change, attribute value must be fixed manually.");
    return attr;
  }

}
