package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This module checks if the regexes in the attribute are valid, making sure they can be used as a filter for domain
 * names.
 *
 * @author Matej Hakoš <492968@muni.cz>
 */
public class urn_perun_entityless_attribute_def_def_allowedMailDomains_mu extends EntitylessAttributesModuleAbstract
    implements EntitylessAttributesModuleImplApi {

  private static final String REGEX_DOMAIN_NAME = "\\/\\[@\\|\\\\\\.][a-z0-9-]+(\\\\.[a-z]{2,})*\\/i";

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute)
      throws WrongAttributeValueException {
    List<String> lines = attribute.valueAsList();
    if (lines == null || lines.isEmpty()) {
      return;
    }

    for (String regex : lines) {
      try {
        // parts[0] should be regex, other parts are values, not checked by this
        Pattern.compile(regex);
        // Check if the regex is a valid domain name filter
        if (!Pattern.matches(REGEX_DOMAIN_NAME, regex)) {
          throw new WrongAttributeValueException(attribute,
              "Regexp: \"" + regex + "\" is not a valid domain name filter");
        }
      } catch (PatternSyntaxException e) {
        throw new WrongAttributeValueException(attribute,
            "Regexp: \"" + regex + "\" syntax is not in the correct form");
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
    attr.setFriendlyName("allowedMailDomains:mu");
    attr.setDisplayName("List of allowed domains for MU");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("List of regexes which filters domain names in tscMails:mu.");
    return attr;
  }

}
