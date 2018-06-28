package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for attribute freeipaGroupName
 *
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 * @date 16.5.2016
 */
public class urn_perun_group_resource_attribute_def_def_freeipaGroupName extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.][a-zA-Z0-9_.-]{0,252}[a-zA-Z0-9_.$-]?$");
	private static final String A_GR_freeipaGroupName = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":freeipaGroupName";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		//prepare group name and check its format
		String groupName = (String) attribute.getValue();
		if (groupName == null) {
			throw new WrongAttributeValueException(attribute, group, "Attribute cannot be null.");
		}

		Matcher match = pattern.matcher(groupName);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, group, "Bad format of attribute freeipaGroupName. It has to match pattern ^[a-zA-Z0-9_.][a-zA-Z0-9_.-]{0,252}[a-zA-Z0-9_.$-]?$");
		}

		//Get facility for the resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		// Get all resources from the facility
		List<Resource> facilityResources =  sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);

		//For each resource get all groups
		for(Resource rs : facilityResources){
			List<Group> resourceGroups =  sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, rs);

			//Remove our group from list of groups
			if (rs.getId() == resource.getId()){
				resourceGroups.remove(group);
			}

			//For all groups get name and check uniqueness
			for(Group gr : resourceGroups){
				Attribute freeipaGroupNameAttribute = new Attribute();

				try{
					freeipaGroupNameAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, rs, gr, A_GR_freeipaGroupName);
				} catch(AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("Attribute "+ A_GR_freeipaGroupName +" does not exists for group " + gr + " and resource " + rs ,ex);
				} catch (GroupResourceMismatchException ex) {
					throw new InternalErrorException(ex);
				}

				if (freeipaGroupNameAttribute.getValue() != null){
					String name = (String) freeipaGroupNameAttribute.getValue();

					if (name.toLowerCase().equals(groupName.toLowerCase())){
						throw new WrongAttributeValueException(attribute, group, "Attribute has to be unique within one facility (case insensitive).");
					}
				}
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_GR_freeipaGroupName);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("freeipaGroupName");
		attr.setDisplayName("freeipa Group Name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of freeipa Group ");
		return attr;
	}
}
