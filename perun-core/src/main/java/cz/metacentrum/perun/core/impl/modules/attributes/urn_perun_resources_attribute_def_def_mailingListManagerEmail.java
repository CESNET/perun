/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

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
    
}
