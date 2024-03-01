package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public abstract class AbstractApplicationExpirationRulesModule<T extends PerunBean> extends AttributesModuleAbstract
    implements AttributesModuleImplApi {
  public static final String applicationWaitingForEmailVerificationKeyName = "emailVerification";
  public static final String applicationIgnoredByAdminKeyName = "ignoredByAdmin";
  private static final Pattern daysToExpirationPattern = Pattern.compile("^\\d+$");

  public void checkAttributeSyntax(PerunSessionImpl sess, T entity, Attribute attribute)
      throws WrongAttributeValueException {

    Map<String, String> attrValue = attribute.valueAsMap();

    if (attrValue == null || attrValue.isEmpty()) {
      return;
    }

    // If is not empty, so i will check if all keys and values are correct
    for (Map.Entry<String, String> entry : attrValue.entrySet()) {
      if (!isAllowedParameter(entry.getKey())) {
        throw new WrongAttributeValueException(attribute, "There is not allowed parameter value: " + entry.getKey());
      }
      Matcher daysToExpirationMatcher = daysToExpirationPattern.matcher(entry.getValue());
      if (!daysToExpirationMatcher.find()) {
        throw new WrongAttributeValueException(attribute,
            "There is not allowed value for parameter '" + entry.getKey() + "': " + entry.getValue());
      }
    }

  }

  /**
   * If parameter (key) is allowed in HashMap for applicationExpirationRules
   *
   * @param parameter String
   * @return true if parameter is allowed, false if is not
   */
  private boolean isAllowedParameter(String parameter) {
    if (parameter == null) {
      return false;
    }
    return parameter.equals(applicationWaitingForEmailVerificationKeyName) ||
        parameter.equals(applicationIgnoredByAdminKeyName);
  }
}
