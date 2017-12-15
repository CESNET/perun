package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest {

		private static urn_perun_user_facility_attribute_def_virt_defaultUnixGID classInstance;
		private static PerunSessionImpl session;
		private static String namespace;
		private static Attribute dDefaultUnixGid;
		private static Attribute prefferedUnixGroupName;
		private static List<Resource> allowedResource;
		private static String resourceGroupName;
		private static List<Member> memberByUser;
		private static List<Group> assignedGroupsToResource;
		private static List<Member> groupMembers;
		private static String groupGroupName;
		private static Integer resourceUnixGID;
		private static Integer groupUnixGID;
		private static Attribute basicDefaultGID;
		private static Facility facility;
		private static User user;
		private static AttributeDefinition attrDef;
		private static Attribute prefferedUnixGroupNameSelectedValueGID;
		private static Resource resource;
		private static Member member;
		private static Group group;

		public urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest() {
		}

		@Before
		public void setUp() throws Exception{
				classInstance = new urn_perun_user_facility_attribute_def_virt_defaultUnixGID();
				session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
				attrDef = session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "defaultUnixGID");
				dDefaultUnixGid = new Attribute();
				namespace = "test";
				prefferedUnixGroupName = new Attribute();
				allowedResource = new ArrayList<>();
				memberByUser = new ArrayList<>();
				assignedGroupsToResource = new ArrayList<>();
				groupMembers = new ArrayList<>();
				basicDefaultGID = new Attribute();
				facility = new Facility(1, "testFacility");
				user = new User(1, "name", "surname", "middlename", "title", "title");
				prefferedUnixGroupNameSelectedValueGID = new Attribute(attrDef);
				resource = new Resource(1, "testName", "desc", 1);
				member = new Member(1, 1);
				group = new Group("name", "desc");
				allowedResource.add(resource);
				memberByUser.add(member);
				assignedGroupsToResource.add(group);
				groupMembers.add(member);
				resourceGroupName = "test1";
				groupGroupName = "test2";
				resourceUnixGID = 1;
				groupUnixGID = 2;

		}

		@Test
		public void getValueDefaultUGidIsSetTest ()throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.getValueDefaultUGidIsSetTest ()");
				//setup
				dDefaultUnixGid.setValue(123);
				//phase one
				//UF:D:DefaultUnixGid attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(dDefaultUnixGid);
				//phase two
				//namespace String
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
				//prefferedUnixGroupName attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + namespace))).thenReturn(prefferedUnixGroupName);
				//allowedResource list<resource>
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResource);
				//R:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(resourceGroupName);
				//getMemberByUser list<members>
				when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(memberByUser);
				//getAssignedGroupsToResource list<group>
				when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(assignedGroupsToResource);
				//getGroupMembers list<members>
				when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(groupMembers);
				//G:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(groupGroupName);
				//R:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(resourceUnixGID);
				//G:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(groupUnixGID);
				//phase three
				//UF:D:basicDefaultGID attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID"))).thenReturn(basicDefaultGID);

				Attribute testAttr = classInstance.getAttributeValue(session, facility, user, attrDef);
				assertEquals(testAttr,dDefaultUnixGid);
		}

		@Test
		public void getValueWithoutAnyAttrTest ()throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.getValueWithoutAnyAttrTest ()");
				//setup
				//phase one
				//UF:D:DefaultUnixGid attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(dDefaultUnixGid);
				//phase two
				//namespace String
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
				//prefferedUnixGroupName attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + namespace))).thenReturn(prefferedUnixGroupName);
				//allowedResource list<resource>
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResource);
				//R:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(resourceGroupName);
				//getMemberByUser list<members>
				when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(memberByUser);
				//getAssignedGroupsToResource list<group>
				when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(assignedGroupsToResource);
				//getGroupMembers list<members>
				when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(groupMembers);
				//G:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(groupGroupName);
				//R:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(resourceUnixGID);
				//G:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(groupUnixGID);
				//phase three
				//UF:D:basicDefaultGID attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID"))).thenReturn(basicDefaultGID);

				Attribute testAttr = classInstance.getAttributeValue(session, facility, user, attrDef);
				assertEquals(testAttr, new Attribute(attrDef));
		}

		@Test
		public void getValuePrefferedGroupNameIsSetTest ()throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.getValuePrefferedGroupNameIsSetTest ()");
				//setup
				String[] array = { "test2", "test3"};
				List<String> list = Arrays.asList(array);
				prefferedUnixGroupName.setValue(list);
				prefferedUnixGroupNameSelectedValueGID.setValue(2);
				//phase one
				//UF:D:DefaultUnixGid attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(dDefaultUnixGid);
				//phase two
				//namespace String
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
				//prefferedUnixGroupName attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + namespace))).thenReturn(prefferedUnixGroupName);
				//allowedResource list<resource>
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResource);
				//R:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(resourceGroupName);
				//getMemberByUser list<members>
				when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(memberByUser);
				//getAssignedGroupsToResource list<group>
				when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(assignedGroupsToResource);
				//getGroupMembers list<members>
				when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(groupMembers);
				//G:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(groupGroupName);
				//R:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(resourceUnixGID);
				//G:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(groupUnixGID);
				//phase three
				//UF:D:basicDefaultGID attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID"))).thenReturn(basicDefaultGID);

				Attribute testAttr = classInstance.getAttributeValue(session, facility, user, attrDef);
				assertEquals(testAttr, prefferedUnixGroupNameSelectedValueGID);
		}

		@Test
		public void getValuebasicDefaultUGidIsSetTest ()throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_virt_defaultUnixGID.getValuebasicDefaultUGidIsSetTest ()");
				//setup
				basicDefaultGID.setValue(123);
				//phase one
				//UF:D:DefaultUnixGid attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(dDefaultUnixGid);
				//phase two
				//namespace String
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
				//prefferedUnixGroupName attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + namespace))).thenReturn(prefferedUnixGroupName);
				//allowedResource list<resource>
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResource);
				//R:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(resourceGroupName);
				//getMemberByUser list<members>
				when(session.getPerunBl().getMembersManagerBl().getMembersByUser(any(PerunSessionImpl.class), any(User.class))).thenReturn(memberByUser);
				//getAssignedGroupsToResource list<group>
				when(session.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(any(PerunSessionImpl.class), any(Resource.class))).thenReturn(assignedGroupsToResource);
				//getGroupMembers list<members>
				when(session.getPerunBl().getGroupsManagerBl().getGroupMembers(any(PerunSessionImpl.class), any(Group.class))).thenReturn(groupMembers);
				//G:D:unixGroupName-namespace:namespace string
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + namespace)).getValue()).thenReturn(groupGroupName);
				//R:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(resourceUnixGID);
				//G:D:unixGID-namespace:namespace int
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Group.class), eq(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(groupUnixGID);
				//phase three
				//UF:D:basicDefaultGID attr
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGID"))).thenReturn(basicDefaultGID);

				Attribute testAttr = classInstance.getAttributeValue(session, facility, user, attrDef);
				assertEquals(testAttr, basicDefaultGID);
		}

}
