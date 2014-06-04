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
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleImplApi;
import java.util.HashMap;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_def_accountExpirationTime extends FacilityUserAttributesModuleAbstract implements FacilityUserAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer accExpTime = (Integer) attribute.getValue();

		if (accExpTime == null) {
			throw new WrongAttributeValueException("account expiration time shouldn't be null");
		}
		Integer facilityAccExpTime = null;
		try {
			facilityAccExpTime = (Integer) perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, attribute.getName()).getValue();
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
		if(accExpTime > facilityAccExpTime) {
			throw new WrongAttributeValueException("this user_facility attribute cannot has higher value than same facility attribute");
		}
	}

	public Attribute fillAttribute(PerunSessionImpl perunSession, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute ret = new Attribute(attribute);
		List<Integer> resourcesExpTimes = new ArrayList<Integer>();
		Integer resourceExpTime = null;
		for (Resource r : perunSession.getPerunBl().getUsersManagerBl().getAllowedResources(perunSession, facility, user)) {
			try { //getting all resources at which user has access
				resourceExpTime = (Integer) perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, r, attribute.getName()).getValue();
				if (resourceExpTime != null) {
					resourcesExpTimes.add(resourceExpTime);
				}
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException(ex);
			}
		}
		Integer facilityAccExpTime = null;
		try {
			facilityAccExpTime = (Integer) perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, attribute.getName()).getValue();
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
		if (facilityAccExpTime != null) {
			resourcesExpTimes.add(facilityAccExpTime);
		}
		ret.setValue(Collections.min(resourcesExpTimes));
		return ret;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("accountExpirationTime");
		attr.setDisplayName("Account expiration");
		attr.setType(String.class.getName());
		attr.setDescription("Account expiration.");
		return attr;
	}
}
