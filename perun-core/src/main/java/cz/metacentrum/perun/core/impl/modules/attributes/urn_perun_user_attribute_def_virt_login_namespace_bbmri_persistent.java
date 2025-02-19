package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualPersistentAttribute;

/**
 * Class for access def:bbmri-persistent-shadow attribute. It generates value if you call it for the first time.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 * @author Jakub Hruska <jhruska@mail.muni.cz>
 * @date 07.11.2016
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_bbmri_persistent
    extends UserVirtualPersistentAttribute {

  public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:bbmri-persistent-shadow";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:bbmri-persistent");
    attr.setDisplayName("BBMRI login");
    attr.setType(String.class.getName());
    attr.setDescription("Login to BBMRI. It is set automatically with first call.");
    return attr;
  }

  @Override
  public String getShadow() {
    return SHADOW;
  }
}
