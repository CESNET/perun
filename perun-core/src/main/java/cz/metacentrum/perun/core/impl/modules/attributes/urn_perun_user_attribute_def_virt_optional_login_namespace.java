package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribute, that contains login, only if it is available in the namespace.
 *
 * @author Matej Hakoš
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_optional_login_namespace extends UserVirtualAttributesModuleAbstract {
  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_optional_login_namespace.class);

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace");
    attr.setDisplayName("Optional login in namespace");
    attr.setType(String.class.getName());
    attr.setDescription("Contains an optional login if the user has it.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
    Attribute attr = new Attribute(attribute);
    String namespace = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + attribute.getFriendlyNameParameter();
    try {
      Attribute defLogin =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, namespace);
      Utils.copyAttributeToVirtualAttributeWithValue(defLogin, attr);
    } catch (AttributeNotExistsException e) {
      // We log the non-existing attribute, but we don't throw an exception.
      LOG.warn("Attribute {} does not exist.", namespace);
    } catch (WrongAttributeAssignmentException e) {
      // It's OK, we just return attribute with value null
    }
    return attr;
  }
}
