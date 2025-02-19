package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualPersistentAttribute;

/**
 * Class for access def:eurofusion-persistent-shadow attribute. It generates value if you call it for the first time.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_eurofusion_persistent
    extends UserVirtualPersistentAttribute {

  public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:eurofusion-persistent-shadow";

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:eurofusion-persistent");
    attr.setDisplayName("EUROfusion login");
    attr.setType(String.class.getName());
    attr.setDescription("Login for EUROfusion. It is set automatically with first call.");
    return attr;
  }

  @Override
  public String getShadow() {
    return SHADOW;
  }
}
