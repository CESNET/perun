/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_resources_attribute_def_def_mailingListManagerEmail {
    
    public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException{
        if (attribute.getValue() == null) {
            throw new WrongAttributeValueException(attribute, "Attribute value is null.");
        }
        
        perunSession.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(perunSession, (String)attribute.getValue());
        
        
    }
    
    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("mailingListManagerEmail");
      attr.setType(String.class.getName());
      attr.setDescription("Email of owner of mailing list");
      return attr;
    }
}
