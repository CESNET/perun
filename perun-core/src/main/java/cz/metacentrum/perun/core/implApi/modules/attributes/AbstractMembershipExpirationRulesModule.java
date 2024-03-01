package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public abstract class AbstractMembershipExpirationRulesModule<T extends PerunBean> extends AttributesModuleAbstract
    implements AttributesModuleImplApi {

  public static final String MEMBERSHIP_GRACE_PERIOD_KEY_NAME = "gracePeriod";
  public static final String MEMBERSHIP_PERIOD_KEY_NAME = "period";
  public static final String MEMBERSHIP_DO_NOT_EXTEND_LOA_KEY_NAME = "doNotExtendLoa";
  public static final String MEMBERSHIP_PERIOD_LOA_KEY_NAME = "periodLoa";
  public static final String MEMBERSHIP_DO_NOT_ALLOW_LOA_KEY_NAME = "doNotAllowLoa";
  public static final String AUTO_EXTENSION_LAST_LOGIN_PERIOD = "autoExtensionLastLoginPeriod";
  public static final String AUTO_EXTENSION_EXT_SOURCES = "autoExtensionExtSources";
  public static final String EXPIRE_SPONSORED_MEMBERS = "expireSponsoredMembers";
  private static final Pattern EXTENSION_DATE_PATTERN = Pattern.compile("^[+][0-9]+([dmy])$");
  private static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]+([dmy])$");
  private static final Pattern LOA_PATTERN = Pattern.compile("^(([0-9]+,)|([0-9]+,[ ]))*[0-9]+$");
  private static final Pattern PERIOD_LOA_PATTERN =
      Pattern.compile("^[0-9]+[|](([0-9]+[.][0-9]+[.])|([+][0-9]+([dmy])))[.]?$");
  private static final Pattern EXT_SOURCES_PATTER = Pattern.compile("^(\\d+)(,\\d+)*$");
  private static final Pattern EXPIRE_SPONSORED_MEMBERS_PATTERN = Pattern.compile("^(true)|(false)$");

  public void checkAttributeSemantics(PerunSessionImpl sess, T entity, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    Map<String, String> attrValue;

    //For no value is correct (it means no rules)
    if (attribute.getValue() == null) {
      return;
    }

    //save value to map attrValue
    attrValue = attribute.valueAsMap();

    //Same for empty HashList
    if (attrValue.isEmpty()) {
      return;
    }

    if (attrValue.containsKey(AUTO_EXTENSION_EXT_SOURCES)) {
      String[] extSourceIds = attrValue.get(AUTO_EXTENSION_EXT_SOURCES).split(",");
      for (String extSourceId : extSourceIds) {
        try {
          sess.getPerunBl().getExtSourcesManagerBl().getExtSourceById(sess, Integer.parseInt(extSourceId));
        } catch (ExtSourceNotExistsException e) {
          throw new WrongReferenceAttributeValueException("There is no extSource with given id: " + extSourceId, e);
        }
      }
    }
  }

  public void checkAttributeSyntax(PerunSessionImpl sess, T entity, Attribute attribute)
      throws WrongAttributeValueException {
    Map<String, String> attrValue;

    //For no value is correct (it means no rules)
    if (attribute.getValue() == null) {
      return;
    }

    //save value to map attrValue
    attrValue = attribute.valueAsMap();

    //Same for empty HashList
    if (attrValue.isEmpty()) {
      return;
    }

    //If is not empty, so i will check if all keys are correct first
    Set<String> keys;
    keys = attrValue.keySet();

    //Only possibilities: period, doNotExtendLoa, gracePeriod, periodLoa
    for (String k : keys) {
      if (!isAllowedParameter(k)) {
        throw new WrongAttributeValueException(attribute, "There is not allowed parameter value: " + k);
      }
    }

    //If all possibilities are correct, so check their values

    //For period (only date like 1.1. or 29.4. without year) or (+xy where x is number and y is d/m/y - +35m or +80d)
    String parameter = MEMBERSHIP_PERIOD_KEY_NAME;
    if (keys.contains(parameter)) {
      DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);
      try {
        //Check if given date is valid date (2000 is a leap year)
        LocalDate.parse(attrValue.get(parameter) + "2000", dateTimeFormatter);
      } catch (DateTimeParseException ex) {
        //It's not date in format d.M. (or dd.MM.) so test for next option, for example: "+18m"
        Matcher extensionDateMatcher = EXTENSION_DATE_PATTERN.matcher(attrValue.get(parameter));
        if (!extensionDateMatcher.find()) {
          throw new WrongAttributeValueException(attribute,
              "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
        }
      }
    }

    //For gracePeriod (xy where x is number and y is d/m/y - 35m or 80d)
    parameter = MEMBERSHIP_GRACE_PERIOD_KEY_NAME;
    if (keys.contains(parameter)) {
      Matcher dateMatcher = DATE_PATTERN.matcher(attrValue.get(parameter));
      if (!dateMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }

    //For doNotExtendLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
    parameter = MEMBERSHIP_DO_NOT_EXTEND_LOA_KEY_NAME;
    if (keys.contains(parameter)) {
      Matcher loaMatcher = LOA_PATTERN.matcher(attrValue.get(parameter));
      if (!loaMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }

    //For doNotAllowLoa (exmp: '3,4,5' or '3, 4 ,5' or '325, 324,336')
    parameter = MEMBERSHIP_DO_NOT_ALLOW_LOA_KEY_NAME;
    if (keys.contains(parameter)) {
      Matcher loaMatcher = LOA_PATTERN.matcher(attrValue.get(parameter));
      if (!loaMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }

    //For periodLoa x|y. or x|y where x is loa format and y is period format and symbol '.' is not mandatory
    parameter = MEMBERSHIP_PERIOD_LOA_KEY_NAME;
    if (keys.contains(parameter)) {
      Matcher periodLoaMatcher = PERIOD_LOA_PATTERN.matcher(attrValue.get(parameter));
      if (!periodLoaMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
      String value = attrValue.get(parameter);
      for (int i = 0; i < value.length(); i++) {
        if (value.charAt(i) == '|') {
          value = value.substring(i + 1);
        }
      }
      if (value.charAt(0) != '+') {
        if (value.contains("..")) {
          value = value.substring(0, value.length() - 1);
        }
        DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);
        try {
          LocalDate.parse(value + "2000", dateTimeFormatter);
        } catch (DateTimeParseException ex) {
          throw new WrongAttributeValueException(attribute,
              "There is not allowed value (bad date format) for parameter '" + parameter + "': " +
              attrValue.get(parameter));
        }
      }
    }

    parameter = AUTO_EXTENSION_LAST_LOGIN_PERIOD;
    if (keys.contains(parameter)) {
      Matcher dateMatcher = DATE_PATTERN.matcher(attrValue.get(AUTO_EXTENSION_LAST_LOGIN_PERIOD));
      if (!dateMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }

    parameter = AUTO_EXTENSION_EXT_SOURCES;
    if (keys.contains(parameter)) {
      Matcher extSourcesMatcher = EXT_SOURCES_PATTER.matcher(attrValue.get(AUTO_EXTENSION_EXT_SOURCES));
      if (!extSourcesMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }

    parameter = EXPIRE_SPONSORED_MEMBERS;
    if (keys.contains(parameter)) {
      Matcher expireSponsoredMemberMatcher = EXPIRE_SPONSORED_MEMBERS_PATTERN.matcher(attrValue.get(parameter));
      if (!expireSponsoredMemberMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + parameter + "': " + attrValue.get(parameter));
      }
    }
  }

  /**
   * If parameter (key) is allowed in HashMap for membershipExpirationRules
   *
   * @param parameter String
   * @return true if parameter is allowed, false if is not
   */
  protected abstract boolean isAllowedParameter(String parameter);
}
