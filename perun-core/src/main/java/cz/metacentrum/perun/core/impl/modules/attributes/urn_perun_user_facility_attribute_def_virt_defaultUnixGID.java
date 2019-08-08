package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGID extends UserFacilityVirtualAttributesModuleAbstract implements UserFacilityVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attr = new Attribute(attributeDefinition);
		try {
			//first phase: if attribute UF:D:defaultUnixGID is set, it has top priority
			Attribute defaultUnixGID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
			if (defaultUnixGID.getValue() != null) {
				Utils.copyAttributeToVirtualAttributeWithValue(defaultUnixGID, attr);
				return attr;
			}
			//second phase: UF:D:defaultUnixGID is not set, module will select unix group name from preffered list
			String gidNamespace = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace").getValue();
			if (gidNamespace == null) {
				return attr;
			}

			String groupNameNamespace = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace").getValue();
			if (groupNameNamespace == null) {
				return attr;
			}

			Attribute userPreferredGroupNames = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + groupNameNamespace);
			List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
			if (userPreferredGroupNames.getValue() != null) {
				Map<String, Resource> resourcesWithName = new HashMap<>();
				for (Resource resource : resources) {
					String groupNameForTest = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + groupNameNamespace).getValue();
					if (groupNameForTest != null) {
						resourcesWithName.put(groupNameForTest, resource);
					}
				}

				List<Member> userMembers = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
				Map<String, Group> groupsWithName = new HashMap<>();
				for (Resource resource : resources) {
					List<Group> groupsFromResource = sess.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource);
					for (Group group : groupsFromResource) {
						List<Member> groupMembers = sess.getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
						groupMembers.retainAll(userMembers);
						if (!groupMembers.isEmpty()) {
							String groupNamesForTest = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + groupNameNamespace).getValue();
							if (groupNamesForTest != null) {
								groupsWithName.put(groupNamesForTest, group);
							}
						}
					}
				}

				for (String pGroupName : (List<String>) userPreferredGroupNames.getValue()) {
					if (resourcesWithName.containsKey(pGroupName)) {
						Resource resource = resourcesWithName.get(pGroupName);
						Attribute resourceUnixGID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + gidNamespace);
						if(resourceUnixGID.getValue() != null) {
							Utils.copyAttributeToViAttributeWithoutValue(userPreferredGroupNames, attr);
							attr.setValue(resourceUnixGID.getValue());
							return attr;
						}
					}
					if (groupsWithName.containsKey(pGroupName)) {
						Group group = groupsWithName.get(pGroupName);
						Attribute groupUnixGID = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + gidNamespace);
						if(groupUnixGID.getValue() != null) {
							Utils.copyAttributeToViAttributeWithoutValue(userPreferredGroupNames, attr);
							attr.setValue(groupUnixGID.getValue());
							return attr;
						}
					}
				}
			}
			//third phase: Preferred unix name is not on the facility and it is choosen basicDefaultGID
			Attribute basicGid = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID");
			Utils.copyAttributeToVirtualAttributeWithValue(basicGid, attr);
			return attr;


		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, facility, "Attribute can't be null.");
		try {
			Attribute defaultUnixGID = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
			defaultUnixGID.setValue(attribute.getValue());
			perunSession.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(perunSession, facility, user, defaultUnixGID);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws InternalErrorException {
		try {
			Attribute attributeToSet = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
			attributeToSet.setValue(attribute.getValue());
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, facility, user, attributeToSet);

		} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
		strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
		strongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:*");
		strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:*");
		strongDependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:*");
		strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:*");
		strongDependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:*");
		strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID");
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("defaultUnixGID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Computed unix group id from user preferrences");
		return attr;
	}
}
