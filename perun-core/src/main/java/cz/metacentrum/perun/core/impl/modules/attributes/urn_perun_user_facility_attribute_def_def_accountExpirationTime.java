/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl.modules.attributes;

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
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_def_accountExpirationTime extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	private static final String A_F_D_accountExpirationTime = AttributesManager.NS_FACILITY_ATTR_DEF + ":accountExpirationTime";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException {
		Integer accExpTime = (Integer) attribute.getValue();

		if (accExpTime == null) {
			throw new WrongAttributeValueException("account expiration time shouldn't be null");
		}
		Integer facilityAccExpTime;
		try {
			facilityAccExpTime = (Integer) perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_D_accountExpirationTime).getValue();
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
		if(accExpTime > facilityAccExpTime) {
			throw new WrongAttributeValueException("this user_facility attribute cannot has higher value than same facility attribute");
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_F_D_accountExpirationTime);
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute ret = new Attribute(attribute);
		List<Integer> resourcesExpTimes = new ArrayList<>();
		Integer resourceExpTime;
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
		Integer facilityAccExpTime;
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

	@Override
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
