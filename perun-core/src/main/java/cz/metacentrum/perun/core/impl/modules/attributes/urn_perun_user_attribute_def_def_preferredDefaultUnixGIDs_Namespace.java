package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.List;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_attribute_def_def_preferredDefaultUnixGIDs_Namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {
    
    public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
    List<String> preferedGIDs = (List<String>) attribute.getValue();
    if(preferedGIDs != null && !preferedGIDs.isEmpty()) {
        try {
            sess.getPerunBl().getModulesUtilsBl().checkIfListOfGIDIsWithinRange(sess, user, attribute);
        } catch (AttributeNotExistsException ex) {
            throw new WrongAttributeValueException(ex);
        }
    }  
    }
  public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
      attr.setFriendlyName("preferredUnixGIDs");
      attr.setType(List.class.getName());
      attr.setDescription("User preferred unix group ids, ordered by user's personal preferrences.");
      return attr;
  }
}

