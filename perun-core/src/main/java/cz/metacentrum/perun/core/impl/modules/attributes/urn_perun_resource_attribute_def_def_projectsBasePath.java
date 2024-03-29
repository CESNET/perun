package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for projects directory base path
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_resource_attribute_def_def_projectsBasePath extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("^(/[-_a-zA-Z0-9]+)+$");

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, resource, null, "Attribute can't be empty.");
    }
  }

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
          "Bad format of attribute projectsBasePath (expected something like '/first/second').");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("projectsBasePath");
    attr.setDisplayName("Projects base path");
    attr.setType(String.class.getName());
    attr.setDescription("Path to base directory of projects.");
    return attr;
  }
}
