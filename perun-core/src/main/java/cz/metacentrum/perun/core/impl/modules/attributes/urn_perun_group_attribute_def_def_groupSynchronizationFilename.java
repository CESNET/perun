package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group synchronization filename Defines file inside the directory configured in the extsource definition.
 *
 * @author Johana Supikova <supikova@ics.muni.cz>
 */
public class urn_perun_group_attribute_def_def_groupSynchronizationFilename extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {
  private static final Logger log =
      LoggerFactory.getLogger(urn_perun_group_attribute_def_def_groupSynchronizationFilename.class);

  private static final Pattern pattern = Pattern.compile("^[-\\w.]+$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    String key = attribute.valueAsString();
    if (key == null) {
      return;
    }

    Matcher match = pattern.matcher(key);

    if (!match.matches()) {
      throw new WrongAttributeValueException(attribute, group,
          "Bad format of attribute groupSynchronizationFilename (no slash '/' characters are allowed).");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("groupSynchronizationFilename");
    attr.setDisplayName("Group synchronization filename");
    attr.setType(String.class.getName());
    attr.setDescription("");
    return attr;
  }
}
