package cz.metacentrum.perun.core.impl.modules.attributes;

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
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_resource_attribute_def_def_systemUnixGroupName extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private static final String A_GR_systemUnixGID = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGID";
	private static final String A_GR_systemIsUnixGroup = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isSystemUnixGroup";
	private static final Pattern pattern = Pattern.compile("^[-_a-zA-Z0-9]*$");

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Group group, Resource resource, AttributeDefinition attributeDefinition) {
		return new Attribute(attributeDefinition);
	}

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{

		String groupName = (String) attribute.getValue();
		Attribute isSystemGroup;

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
			
			return;
		}

		if (groupName != null) {
			Matcher matcher = pattern.matcher(groupName);
			if (!matcher.matches())
				throw new WrongAttributeValueException(attribute, "String with other chars than numbers, letters or symbols _ and - is not allowed value.");
		}

		//Get facility for the resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		//List of pairs (group and resource) which has the attribute with the value
		List<Pair<Group,Resource>> listGroupPairsResource =sess.getPerunBl().getGroupsManagerBl().getGroupResourcePairsByAttribute(sess, attribute);

		//Searching through all pairs and if is not checking group/resource/attribute, then try for being on the same facility, if yes then throw exception but only if these groups have not the same GID too.
		for(Pair<Group,Resource> p : listGroupPairsResource) {
			if(!p.getLeft().equals(group) || !p.getRight().equals(resource)) {
				Facility facilityForTest = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, p.getRight());

				Attribute group1GID;
				Attribute group2GID;

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
