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
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 27.4.2011
 */
public class urn_perun_user_facility_attribute_def_def_homeMountPoint extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$*");

	@Override
	public void checkAttributeSemantics(PerunSessionImpl session, User user, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		List<Resource> usersResources;
		usersResources = session.getPerunBl().getUsersManagerBl().getAllowedResources(session, facility, user);

		List<String> homeMntPointsOnAllResources = new ArrayList<>();
		for (Resource res : usersResources) {
			Attribute resAttribute;
			try {
				resAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, res, AttributesManager.NS_RESOURCE_ATTR_DEF + ":homeMountPoints");
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("no homemountpoints found on underlying resources", ex);
			}
			List<String> homeMntPoint = (List<String>) resAttribute.getValue();
			if (homeMntPoint != null) {
				homeMntPointsOnAllResources.addAll(homeMntPoint);
			}
		}
		if (homeMntPointsOnAllResources.isEmpty()) {
			throw new WrongReferenceAttributeValueException("No homeMountPoints set on associated resources.");
		}
		if (!homeMntPointsOnAllResources.contains(attribute.valueAsString())) {
			throw new WrongAttributeValueException(attribute, user, facility, "User's home mount point is invalid. Valid mount points: " + homeMntPointsOnAllResources);
		}

		Matcher match = pattern.matcher((String) attribute.getValue());
		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, "Attribute has wrong format");
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, User user, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute returnAttribute = new Attribute(attribute);
		List<Resource> usersResources;
		usersResources = session.getPerunBl().getUsersManagerBl().getAllowedResources(session, facility, user);
		for (Resource res : usersResources) {
			Attribute resAttribute;
			try {
				resAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, res, AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultHomeMountPoint");
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("no homemountpoints found on underlying user's  resources", ex);
			}
			if (resAttribute.getValue() != null) {
				returnAttribute.setValue(resAttribute.getValue());
				return returnAttribute;
			}
		}
		return returnAttribute;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":homeMountPoints");
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("homeMountPoint");
		attr.setDisplayName("Home mount point");
		attr.setType(String.class.getName());
		attr.setDescription("Home mount point.");
		return attr;
	}
}
