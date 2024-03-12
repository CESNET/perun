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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains login in the MU namespace concatenated with @muni.cz if it is available, null otherwise
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_scopedLogin_namespace_mu extends UserVirtualAttributesModuleAbstract {
  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_scopedLogin_namespace_mu.class);

  private static final String A_U_D_loginNamespace_mu = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("scopedLogin-namespace:mu");
    attr.setDisplayName("Login + @muni.cz in namespace: mu");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Contains an optional login (UCO) concatenated with domain (@muni.cz) in namespace mu if the user has it.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
    Attribute attr = new Attribute(attribute);
    try {
      Attribute defLogin =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, A_U_D_loginNamespace_mu);
      Utils.copyAttributeToVirtualAttributeWithValue(defLogin, attr);
    } catch (AttributeNotExistsException e) {
      // We log the non-existing attribute, but we don't throw an exception.
      LOG.warn("Attribute {} does not exist.", A_U_D_loginNamespace_mu);
    } catch (WrongAttributeAssignmentException e) {
      // It's OK, we just return attribute with value null
    }
    if (attr.getValue() != null) {
      attr.setValue(attr.getValue() + "@muni.cz");
    }
    return attr;
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(A_U_D_loginNamespace_mu);
    return strongDependencies;
  }
}
