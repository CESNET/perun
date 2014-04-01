package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_group_resource_attribute_def_def_smb_group_is_share extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

    public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
        /*Integer value = (Integer) attribute.getValue();
        if (value == null) {
            throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
        }
        if (!(value == 1 || value == 0)) {
            throw new WrongAttributeValueException("0 and 1 are only allowed values");
        }*/
    }

    public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
       /*Attribute ret = new Attribute(attribute);
       ret.setValue(0);
       return ret;*/
       return new Attribute();
    }

    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("smb_group_is_share");
      attr.setType(Integer.class.getName());
      attr.setDescription("Group creates it's own share on Samba server.");
      return attr;
    }
}
