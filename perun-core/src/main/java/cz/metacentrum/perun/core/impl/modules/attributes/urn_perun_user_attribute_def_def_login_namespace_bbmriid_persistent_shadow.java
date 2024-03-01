package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Random;

/**
 * Class for filling bbmriid shadow value. It is only storage! Use module login bbmriid_persistent for access the
 * value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_bbmriid_persistent_shadow
    extends urn_perun_user_attribute_def_def_login_namespace {
  private static final String attrFriendlyName = "login-namespace:bbmriid-persistent-shadow";
  private static final int lowestId = 2000;
  private static final int randomBound = 10000;
  private static final Random random = new Random();

  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {

    Attribute filledAttribute = new Attribute(attribute);

    if (attribute.getFriendlyName().equals(attrFriendlyName)) {
      String computedValue = Long.toString(System.nanoTime() + random.nextLong(lowestId, randomBound));
      filledAttribute.setValue(computedValue);
    }
    return filledAttribute;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName(attrFriendlyName);
    attr.setDisplayName("BBMRI Computed ID");
    attr.setType(String.class.getName());
    attr.setDescription("BBMRI Computed ID used in proxy and BBMRI services.");
    return attr;
  }
}
