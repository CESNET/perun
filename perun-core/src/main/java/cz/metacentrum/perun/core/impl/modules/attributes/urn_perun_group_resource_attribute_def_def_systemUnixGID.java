package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.List;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
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

/**
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_resource_attribute_def_def_systemUnixGID extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final String A_GR_systemUnixGroupName = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGroupName";
	private static final String A_GR_systemIsUnixGroup = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isSystemUnixGroup";

	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attributeDefinition);
	}

	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Integer gid = (Integer) attribute.getValue();

		//Gid should not be null if is system unix group or if less than 1
		Attribute isSystemGroup = new Attribute();

		if(gid == null) {
			try {
				isSystemGroup = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemIsUnixGroup);
			} catch(AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Not exist Attribute " + A_GR_systemIsUnixGroup +  " for group " + group,ex);
			}

			if(isSystemGroup.getValue() != null && (Integer) isSystemGroup.getValue() == 1) {
				throw new WrongReferenceAttributeValueException(attribute, "Attribute cant be null if " + group + " on " + resource + " is system unix group.");
			}
		} else if(gid < 1) {
			throw new WrongAttributeValueException(attribute,"GID number less than 1 is not allowed value.");
		}


		//Get facility for the resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		//List of pairs (group and resource) which has the attribute with the value
		List<Pair<Group,Resource>> listGroupPairsResource = sess.getPerunBl().getGroupsManagerBl().getGroupResourcePairsByAttribute(sess, attribute);

		//Searching through all pairs and if is not checking group/resource/attribute, then try for being on the same facility, if yes then throw exception but only if these groups have not the same GroupName too.
		for(Pair<Group,Resource> p : listGroupPairsResource) {
			if(!p.getLeft().equals(group) || !p.getRight().equals(resource)) {
				Facility facilityForTest = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, p.getRight());

				Attribute group1GroupName = new Attribute();
				Attribute group2GroupName = new Attribute();

				try {
					group1GroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemUnixGroupName);
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("Attribute " + A_GR_systemUnixGroupName + " not exists for group " + group,ex);
				}

				try {
					group2GroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, p.getRight(), p.getLeft(), A_GR_systemUnixGroupName);
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("Attribute " + A_GR_systemUnixGroupName + " not exists for group " + p.getLeft(),ex);
				}

				if(facilityForTest.equals(facility) && !(group1GroupName.getValue().equals(group2GroupName.getValue()))) throw new WrongAttributeValueException(attribute, "Gid " + gid + "is allready used by another group-resource.  " + p.getLeft() + " " + p.getRight());
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<String>();
		dependecies.add(A_GR_systemUnixGroupName);
		return dependecies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("systemUnixGID");
		attr.setDisplayName("GID of system unix group");
		attr.setType(Integer.class.getName());
		attr.setDescription("GID of the system unix group.");
		return attr;
	}
}
