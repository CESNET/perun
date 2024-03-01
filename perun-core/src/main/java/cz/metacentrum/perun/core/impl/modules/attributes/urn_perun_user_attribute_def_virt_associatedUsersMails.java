package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for virtual user attribute associatedUsersMails. Get list of preferred mails of all associated users.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_associatedUsersMails extends UserVirtualAttributesModuleAbstract {

  private static final String A_U_D_preferredMail = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("associatedUsersMails");
    attr.setDisplayName("Mails of associated users");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("List of preferred mails of all associated users.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    List<User> associatedUsers = sess.getPerunBl().getUsersManagerBl().getUsersBySpecificUser(sess, user);
    List<String> mails = new ArrayList<>();

    try {
      for (User associatedUser : associatedUsers) {
        Attribute userPreferredMail =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, associatedUser, A_U_D_preferredMail);
        if (userPreferredMail.getValue() != null) {
          mails.add(userPreferredMail.valueAsString());
        }
      }
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }

    // sort mails alphabetically
    mails.sort(Collator.getInstance());
    attribute.setValue(mails);
    return attribute;
  }
}
