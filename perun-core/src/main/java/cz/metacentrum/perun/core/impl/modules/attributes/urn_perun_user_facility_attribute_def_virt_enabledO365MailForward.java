package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual attribute for Office 365 email forwarding.
 * If the mail forwarding is disabled the returned value is ''.
 * Otherwise, the value of o365MailForward Attribute is returned.
 */
public class urn_perun_user_facility_attribute_def_virt_enabledO365MailForward
    extends UserFacilityVirtualAttributesModuleAbstract implements UserFacilityVirtualAttributesModuleImplApi {

  private final static String A_U_F_DISABLE0365MAILFORWARD =
      AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":disableO365MailForward";
  private final static String A_U_F_O365MAILFORWARD = AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":o365MailForward";

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility,
                                     AttributeDefinition attributeDefinition) {
    Attribute attr = new Attribute(attributeDefinition);
    try {
      Attribute disableForwardAtrr =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, A_U_F_DISABLE0365MAILFORWARD);
      if (disableForwardAtrr.getValue() == null || !disableForwardAtrr.valueAsBoolean()) {
        Attribute o365MailForwardAttr =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, A_U_F_O365MAILFORWARD);
        String o365MailForward = o365MailForwardAttr.valueAsString();
        attr.setValue(o365MailForward == null ? "" : o365MailForward);
      } else {
        attr.setValue("");
      }
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
      throw new ConsistencyErrorException(ex);
    }
    return attr;
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(A_U_F_DISABLE0365MAILFORWARD);
    strongDependencies.add(A_U_F_O365MAILFORWARD);
    return strongDependencies;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("enabledO365MailForward");
    attr.setDisplayName("Enabled O365 mail to forward");
    attr.setType(String.class.getName());
    attr.setDescription("The mail to forward to if the forwarding is not disabled.");
    return attr;
  }
}
