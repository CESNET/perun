/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
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
 * @date 23.11.2011
 */
public class urn_perun_resource_attribute_def_def_accountExpirationTime extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_F_accountExpirationTime = AttributesManager.NS_FACILITY_ATTR + ":accountExpirationTime";

	public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}


	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer accExpTime = (Integer) attribute.getValue();
		if(accExpTime == null) {
			throw new WrongAttributeValueException("Attribute value shouldnt be null");
		}
		Facility fac = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
		Integer facilityAccExpTime = null;
		try {
			//FIXME this can't work (different namespace!!)
			facilityAccExpTime = (Integer) perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, fac, A_F_accountExpirationTime).getValue();
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
		if(facilityAccExpTime == null) {
			throw new WrongReferenceAttributeValueException("cant determine attribute value on underlying facility");
		}
		if(facilityAccExpTime < accExpTime) {
			throw new WrongAttributeValueException("value can be higher than same facility attribute");
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_F_accountExpirationTime);
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("accountExpirationTime");
		attr.setDisplayName("Account expiration time.");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unix account expiration time.");
		return attr;
	}
}
