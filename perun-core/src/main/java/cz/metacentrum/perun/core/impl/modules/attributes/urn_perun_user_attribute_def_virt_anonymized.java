package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

/**
 * Get user anonymization flag (user was anonymized).
 *
 * @author Ľuboslav Halama <halama@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_anonymized extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {
  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("anonymized");
    attr.setDisplayName("User anonymized");
    attr.setType(Boolean.class.getName());
    attr.setDescription("Anonymization flag (user was anonymized).");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    attribute.setValue(sess.getPerunBl().getUsersManagerBl().isUserAnonymized(sess, user));
    return attribute;
  }
}
