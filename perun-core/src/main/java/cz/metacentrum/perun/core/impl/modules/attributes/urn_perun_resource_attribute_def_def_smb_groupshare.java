package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_resource_attribute_def_def_smb_groupshare extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

   
    public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
        /*Attribute attr = new Attribute(attribute);
        attr.setValue(0);
        return attr;*/
        return new Attribute();
    }

    
    public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
         /*Integer value = (Integer) attribute.getValue();
        if(value == null) {
             throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
        }
        if(!(value == 1 || value == 0)) {
            throw new WrongAttributeValueException("0 and 1 are only allowed values");
        }*/
    }

    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("smb_groupshare");
      attr.setType(Integer.class.getName());
      attr.setDescription("Groups on resource represents Samba shares.");
      return attr;
    }
}
