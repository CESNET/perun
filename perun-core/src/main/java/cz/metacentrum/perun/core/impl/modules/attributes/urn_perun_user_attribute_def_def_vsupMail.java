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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attribute module for storing basic/backup school mail of persons at VŠUP.
 * It has to be "login@vsup.cz" and is set whenever u:d:login-namespace:vsup attribute is set/changed !!
 * <p>
 * Value can't be filled by this module, so we must allow NULL value in checkAttributeSemantics(), because when all mail
 * attributes are required and set at once, we can't ensure correct processing order of attributes and it might perform check on old
 * value, because of setRequiredAttributes() implementation uses in memory value instead of refreshing from DB.
 * <p>
 * On value change, map of usedMails in entityless attributes is checked and updated.
 * Also u:d:vsupPreferredMail is set to current value, if is empty.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupMail extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  // VŠUP MAIL PATTERN !! -> login@vsup.cz
  public static final Pattern emailPattern = Pattern.compile("^[-_A-Za-z0-9+']+(\\.[-_A-Za-z0-9+']+)*@vsup\\.cz$");
  public static final String usedMailsUrn = "urn:perun:user:attribute-def:def:usedMails";

  // all VŠUP mail attributes
  public static final String vsupMailUrn = "urn:perun:user:attribute-def:def:vsupMail";
  public static final String vsupPreferredMailUrn = "urn:perun:user:attribute-def:def:vsupPreferredMail";
  public static final String vsupExchangeMailUrn = "urn:perun:user:attribute-def:def:vsupExchangeMail";
  public static final String vsupExchangeMailAliasesUrn = "urn:perun:user:attribute-def:def:vsupExchangeMailAliases";

  private static final String A_U_D_loginNamespace_vsup = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:vsup";

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() != null) {
      Matcher emailMatcher = emailPattern.matcher(attribute.valueAsString());
      if (!emailMatcher.find()) {
        throw new WrongAttributeValueException(attribute, user,
            "School mail is not in a correct form: \"login@vsup.cz\".");
      }
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {

    // check only if not null
    if (attribute.getValue() != null) {

      // check that mail matches login
      try {
        Attribute login =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_loginNamespace_vsup);

        // if login and mail value is set, they must match !!
        if (login.getValue() != null && attribute.getValue() != null) {
          if (!Objects.equals(login.getValue() + "@vsup.cz", attribute.getValue())) {
            throw new WrongReferenceAttributeValueException(attribute, login, user, null, user, null,
                "VŠUP mail must match users login at VŠUP.");
          }
        }
        // if only one of them is set, it's OK, because:
        // - we need vsupMail to be required, but it doesn't support fill (by purpose).
        // - it's set in changedAttributeHook() of login-namespace:vsup attribute during same transaction.
        // - always requiring non-null value would cause setRequiredAttributes() to fail, because of above and method implementation.
      } catch (AttributeNotExistsException e) {
        throw new ConsistencyErrorException("Attribute for login-namespace: vsup doesn't exists.", e);
      }

      //if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "School mail can't be null.");

    }

    // We check uniqueness on all related attributes change, so we don't need to do it here.

  }

  @Override
  public List<String> getDependencies() {
    return Collections.singletonList(A_U_D_loginNamespace_vsup);
  }

  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    // list of reserved mails for user
    Attribute reservedMailsAttribute;
    ArrayList<String> reservedMailsAttributeValue;

    // other vsup mail attributes to get values from
    Attribute vsupPreferredMailAttribute;
    Attribute vsupExchangeMailAttribute;
    Attribute vsupExchangeMailAliasesAttribute;

    // output sets used for comparison
    Set<String> reservedMailsOfUser = new HashSet<>();
    Set<String> actualMailsOfUser = new HashSet<>();

    // get related attributes
    try {
      reservedMailsAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttributeForUpdate(session, user, usedMailsUrn);
      vsupPreferredMailAttribute =
          session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupPreferredMailUrn);
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
          "User attribute 'urn:perun:user:attribute-def:def:usedMails' is empty, but we are removing 'vsupMail' value, so there should have been entry in usedMails attribute.");
    }

    // get value from reserved mails attribute

    if (reservedMailsAttribute.getValue() == null) {
      reservedMailsAttributeValue = new ArrayList<>();
    } else {
      reservedMailsAttributeValue = reservedMailsAttribute.valueAsList();
    }

    // fill set for comparison
    reservedMailsOfUser.addAll(reservedMailsAttributeValue);

    if (vsupPreferredMailAttribute.getValue() != null) {
      actualMailsOfUser.add(vsupPreferredMailAttribute.valueAsString());
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

    // if setting non-empty value, process u:d:vsupExchangeMailAliases and u:d:vsupPreferredMail
    if (attribute.getValue() != null) {

      ArrayList<String> vals = vsupExchangeMailAliasesAttribute.valueAsList();
      if (vals == null) {
        vals = new ArrayList<>();
      }
      if (!vals.contains(attribute.valueAsString())) {

        // store value between u:d:vsupExchangeMailAliases if not yet present
        try {
          vals.add(attribute.valueAsString());
          vsupExchangeMailAliasesAttribute.setValue(vals);
          session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, vsupExchangeMailAliasesAttribute);
        } catch (WrongAttributeValueException | WrongAttributeAssignmentException e) {
          throw new InternalErrorException("Unable to store generated vsupMail between vsupExchangeMailAliases.", e);
        }

      }

      // store value with changed domain to @umprum.cz into u:d:vsupPreferredMail if it's still empty
      if (vsupPreferredMailAttribute.getValue() == null) {
        vsupPreferredMailAttribute.setValue(attribute.valueAsString().replace("@vsup.cz", "@umprum.cz"));
        try {
          session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, vsupPreferredMailAttribute);
        } catch (WrongAttributeValueException | WrongAttributeAssignmentException e) {
          throw new InternalErrorException(
              "Unable to store generated vsupMail (with modified domain to @umprum.cz) to vsupPreferredMail.", e);
        }
      }

    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("vsupMail");
    attr.setDisplayName("School mail");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Generated school mail in a \"login@vsup.cz\" form. Do not change value manually! Represents account in Zimbra.");
    return attr;
  }

}
