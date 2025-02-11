package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualPersistentAttribute;

/**
 * Class for access def:lifescienceid-persistent-shadow attribute. It generates value if you call it for the first
 * time.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_lifescienceid_persistent
    extends UserVirtualPersistentAttribute {

  public static final String SHADOW =
      "urn:perun:user:attribute-def:def:login-namespace:lifescienceid-persistent-shadow";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:lifescienceid-persistent");
    attr.setDisplayName("Lifescienceid login");
    attr.setType(String.class.getName());
    attr.setDescription("Login to Lifescienceid. It is set automatically with first call.");
    return attr;
  }

  @Override
  public String getShadow() {
    return SHADOW;
  }
}
