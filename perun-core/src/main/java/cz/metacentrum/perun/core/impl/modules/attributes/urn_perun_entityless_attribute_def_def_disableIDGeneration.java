package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;


/**
 * Disables automatic generation of login persistent shadow values in selected namespaces. This for situations when the
 * CUID is provided by other means (e.g. by SAML during registration)
 *
 * @author David Flor
 */
public class urn_perun_entityless_attribute_def_def_disableIDGeneration extends
    EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

  public static String A_D_login_namespace = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, String key, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
    AttributeDefinition persistentShadowLoginAttr;
    String persistentShadowLoginAttrName = A_D_login_namespace + ":" + key + "-persistent-shadow";
    try {
      persistentShadowLoginAttr = perunSession.getPerunBl().getAttributesManagerBl()
                                      .getAttributeDefinition(perunSession, persistentShadowLoginAttrName);
    } catch (AttributeNotExistsException e) {
      throw new WrongReferenceAttributeValueException(attribute, "Attribute " +
                                                                     persistentShadowLoginAttrName + " not found");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
    attr.setFriendlyName("disableIDGeneration");
    attr.setDisplayName("Disable persistent ID generation");
    attr.setType(Boolean.class.getName());
    attr.setDescription(
        "Configures for which namespaces the persistent shadow login ID generation will be disabled. E.g. 'einfraid'" +
            " disables 'login-namespace:einfraid-persistent-shadow' generation");
    return attr;
  }
}
