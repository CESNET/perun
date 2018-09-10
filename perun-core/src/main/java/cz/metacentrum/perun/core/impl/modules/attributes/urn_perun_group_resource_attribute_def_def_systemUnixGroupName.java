package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import java.util.ArrayList;

/**
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_resource_attribute_def_def_systemUnixGroupName extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final String A_GR_systemUnixGID = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGID";
	private static final String A_GR_systemIsUnixGroup = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isSystemUnixGroup";

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attributeDefinition);
	}

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{

		String groupName = (String) attribute.getValue();
		Attribute isSystemGroup = new Attribute();

		if(groupName==null) {

			try {
				isSystemGroup = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemIsUnixGroup);
			} catch(AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Not exist Attribute " + A_GR_systemIsUnixGroup +  " for group " + group,ex);
			} catch (GroupResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}

			if(isSystemGroup.getValue() != null && (Integer) isSystemGroup.getValue()==1) {
				throw new WrongReferenceAttributeValueException(attribute, "Attribute cant be null if " + group + " on " + resource + " is system unix group.");
			}
		} else if(groupName.matches("^[-_a-zA-Z0-9]*$")!=true) {
			throw new WrongAttributeValueException(attribute,"String with other chars than numbers, letters or symbols _ and - is not allowed value.");
		}

		//Get facility for the resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		//List of pairs (group and resource) which has the attribute with the value
		List<Pair<Group,Resource>> listGroupPairsResource =sess.getPerunBl().getGroupsManagerBl().getGroupResourcePairsByAttribute(sess, attribute);

		//Searching through all pairs and if is not checking group/resource/attribute, then try for being on the same facility, if yes then throw exception but only if these groups have not the same GID too.
		for(Pair<Group,Resource> p : listGroupPairsResource) {
			if(!p.getLeft().equals(group) || !p.getRight().equals(resource)) {
				Facility facilityForTest = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, p.getRight());

				Attribute group1GID = new Attribute();
				Attribute group2GID = new Attribute();

				try {
					group1GID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, A_GR_systemUnixGID);
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("Attribute "+ A_GR_systemUnixGID +" not exists for group " + group + " and resource " + resource,ex);
				} catch (GroupResourceMismatchException ex) {
					throw new InternalErrorException(ex);
				}

				try {

					group2GID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, p.getRight(), p.getLeft(), A_GR_systemUnixGID);
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("Attribute "+ A_GR_systemUnixGID +" not exists for group " + p.getLeft() + " and resource " + p.getRight() ,ex);
				} catch (GroupResourceMismatchException ex) {
					throw new InternalErrorException(ex);
				}

				if(facilityForTest.equals(facility) && (group1GID.getValue() != null ? (! group1GID.getValue().equals(group2GID.getValue())) : group2GID != null)) {
					throw new WrongAttributeValueException(attribute, "Group name " + groupName + "is allready used by another group-resource and these have not the same GID and GroupName.  " + p.getLeft() + " " + p.getRight());
				}
			}
		}

	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_GR_systemUnixGID);
		dependencies.add(A_GR_systemIsUnixGroup);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("systemUnixGroupName");
		attr.setDisplayName("Name of system unix group");
		attr.setType(String.class.getName());
		attr.setDescription("Name of the system unix group.");
		return attr;
	}
}
