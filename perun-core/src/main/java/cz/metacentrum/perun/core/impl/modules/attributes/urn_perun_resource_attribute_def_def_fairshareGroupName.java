package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 * New module for attribute fairshareGroupName
 *
 *  Name must be unique, always has prefix 'G:' and the length without prefix is 12 character max (14 with prefix)
 *
 * @author Milan Stava <stavamichal@gmail.com>
 * @date 21.1.2015
 */
public class urn_perun_resource_attribute_def_def_fairshareGroupName extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[a-zA-Z]{1,12}$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Null is ok, it means this resource is not fairshare group
		if (attribute.getValue() == null) {
			return;
		}

		String gName = (String) attribute.getValue();

		//Test if gName matchers regex
		Matcher matcher = pattern.matcher(gName);
		if (!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, resource, "Wrong format of group fairshare name. Max length is 12, only letters are allowed.");
		}

		//On facility must be fairshare group name unique (between all resources of this facility)
		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
		List<Resource> facilityResources = perunSession.getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility);
		facilityResources.remove(resource);
		List<String> resourcesFairshareGroupNames = new ArrayList<>();
		for(Resource res: facilityResources) {
			try {
				Attribute resFairshareName = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, res, attribute.getName());
				if(resFairshareName.getValue() == null) continue;
				resourcesFairshareGroupNames.add((String) resFairshareName.getValue());
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		if(resourcesFairshareGroupNames.contains(gName)) throw new WrongAttributeValueException(attribute, resource, "This name is already taken (not unique). Choose another one.");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("fairshareGroupName");
		attr.setDisplayName("Fairshare group name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of fairshare group.");
		return attr;
	}
}
