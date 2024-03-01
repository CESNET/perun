package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

import java.util.Arrays;

/**
 * Generic module which takes login-namespace attribute for a specific namespace and returns the unscoped value of the attribute.
 */
public class urn_perun_user_attribute_def_virt_unscopedLogin_namespace extends UserVirtualAttributesModuleAbstract {

  private static final String USER_DEF_BASE_NS = "urn:perun:user:attribute-def:def:login-namespace:";

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute unscopedLogin = new Attribute(attributeDefinition);
    String namespace = unscopedLogin.getFriendlyNameParameter();

    Attribute scopedLogin;
    try {
      scopedLogin = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, USER_DEF_BASE_NS + namespace);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      return unscopedLogin;
    }

    String scopedValue = scopedLogin.valueAsString();
    if (scopedValue == null || scopedValue.isEmpty()) {
      return unscopedLogin;
    }
    String[] scopedParts = scopedValue.split("@");
    if (scopedParts.length > 2) {
      String unscopedValue = String.join("@", Arrays.copyOfRange(scopedParts, 0, scopedParts.length - 1));
      unscopedLogin.setValue(unscopedValue);
    } else {
      unscopedLogin.setValue(scopedParts[0]);
    }

    return unscopedLogin;
  }
}
