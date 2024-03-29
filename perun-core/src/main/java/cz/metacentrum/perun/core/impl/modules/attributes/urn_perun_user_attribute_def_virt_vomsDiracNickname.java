package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.Collections;
import java.util.List;

/**
 * Dirac Nickname is defined like login in egi-ui, if not exists, then it is empty
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_vomsDiracNickname extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  private static final String A_U_D_loginNamespace_egiUi =
      AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:egi-ui";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("vomsDiracNickname");
    attr.setDisplayName("Voms Nickname for DIRAC");
    attr.setType(String.class.getName());
    attr.setDescription("It is login in egi-ui or empty if login not exists.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    try {
      Attribute loginInEgiui =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_loginNamespace_egiUi);
      Utils.copyAttributeToVirtualAttributeWithValue(loginInEgiui, attribute);
    } catch (AttributeNotExistsException ex) {
      //That means that egi-ui attribute not exists at all, return empty attribute
      return attribute;
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
    return attribute;
  }

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(A_U_D_loginNamespace_egiUi);
  }
}
