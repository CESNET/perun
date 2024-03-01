package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Group-resource vomGroupName attribute.
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_group_resource_attribute_def_def_vomsGroupName extends GroupResourceAttributesModuleAbstract
    implements GroupResourceAttributesModuleImplApi {
  private static final Pattern pattern = Pattern.compile("^[^<>&=]*$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    String vomsGroupName = attribute.valueAsString();

    if (vomsGroupName == null) {
      return;
    }

    Matcher matcher = pattern.matcher(vomsGroupName);

    if (!matcher.matches()) {
      throw new WrongAttributeValueException(attribute, group, resource,
          "Bad format of attribute vomsGroupName. It should not contain '<>&=' characters.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setFriendlyName("vomsGroupName");
    attr.setDisplayName("Voms group name");
    attr.setDescription("Name of voms group, if defined.");
    attr.setType(String.class.getName());
    attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
    return attr;
  }
}
