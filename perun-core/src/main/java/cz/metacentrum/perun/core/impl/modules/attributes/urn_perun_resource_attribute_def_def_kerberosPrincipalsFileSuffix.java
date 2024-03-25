package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for suffix of Kerberos principals file
 *
 * @author Šárka Palkovičová <sarka.palkovicova@gmail.com>
 * @date 16.2.2022
 */
public class urn_perun_resource_attribute_def_def_kerberosPrincipalsFileSuffix extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("^[-_a-zA-Z0-9]+$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    String path = attribute.valueAsString();
    if (path == null) {
      return;
    }

    Matcher match = pattern.matcher(path);

    if (!match.matches()) {
      throw new WrongAttributeValueException(attribute, resource,
          "Bad format of attribute kerberosPrincipalsFileSuffix. Allowed characters: A-Za-z0-9_-");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("kerberosPrincipalsFileSuffix");
    attr.setDisplayName("Kerberos principals file suffix");
    attr.setType(String.class.getName());
    attr.setDescription("Suffix of Kerberos principals file.");
    return attr;
  }
}
