package cz.metacentrum.perun.core.entry;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import cz.metacentrum.perun.core.api.ResourcesManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.LinkedList;
import java.util.Set;
import javax.print.attribute.SetOfIntegerSyntax;
import org.mockito.internal.matchers.Any;

/**
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AttributesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	/*
	 * Test is divided into groups by the type of methods
	 *
	 * 1. getAttributes (testing on core attributes - comparing some attr.value with object.attribute.value)
	 * 2. setAttributes (testing on opt attributes manually made - comparing with attribute objects)
	 * 3. getAttribute (by name) (testing on core attribute - typically ID)
	 * 4. getAttributeDefinition (& its variations)
	 * 5. getAttributeById (testing on own opt attributes by ID comparison)
	 * 6. setAttribute
	 * 7. createAttribute / deleteAttribute
	 * 8. getRequiredAttributes
	 * 9. ==fillAttribute==
	 * 10. ==checkAttributeValue==
	 * 11. removeAttribute/s / removeAllAttributes
	 * 12. rest check methods
	 *
	 */

	// these are in DB only when setUp"Type"() and must be used in correct (this) order
	private AttributesManager attributesManager;
	private ResourcesManager resourcesManager;
	private Vo vo;
	private Member member;
	private Facility facility;
	private Resource resource;
	private List<Attribute> attributes; // always have just 1 attribute we setUp"AttrType"()
	private Service service;
	private Group group;
	private Host host;

	//World's variables
	private Vo vo1;
	private Vo vo2;
	private User user1;
	private User user2;
	private User user3;
	private Group membersGroupOfVo1;
	private Group membersGroupOfVo2;
	private Group group1InVo1;
	private Group group2InVo1;
	private Group group1InVo2;
	private Group group2InVo2;
	private Member member1OfUser1;
	private Member member2OfUser1;
	private Member member1OfUser2;
	private Member member2OfUser2;
	private Member member1OfUser3;
	private Member member2OfUser3;
	private Resource resource1InVo1;
	private Resource resource2InVo1;
	private Resource resource1InVo2;
	private Resource resource2InVo2;
	private Facility facility1;
	private Facility facility2;
	private Facility facility3;
	private Host host1OnFacility1;
	private Host host2OnFacility1;
	private Host host1OnFacility2;
	private Host host2OnFacility2;
	private Host host1OnFacility3;
	private Host host2OnFacility3;
	private String key;

	@Before
	public void setUp() throws Exception {

		attributesManager = perun.getAttributesManager();
		resourcesManager = perun.getResourcesManager();
		this.setUpWorld();

	}

	/**
	 * How the world look: "->" means "have a binding, connection with"
	 *
	 * vo1 -> member1OfUser1, member2OfUser2, member1OfUser3 && group1InVo1, group2InVo1, membersGroupOfVo1 && resource1InVo1, resource2InVo1
	 * vo2 -> member2OfUser1, member1OfUser2, member2OfUser3 && group1InVo2, group2InVo2, membersGroupOfVo2 && resource1InVo2, resource2InVo2
	 *
	 * user1 -> member1OfUser1, member2OfUser1 && userExtSource1
	 * user2 -> member1OfUser2, member2OfUser2 && userExtSource2
	 * user3 -> member1OfUser3, member2OfUser3 && userExtSource3
	 *
	 * member1OfUser1 IS allowed
	 * member2OfUser1 IS disallowed
	 * member1OfUser2 IS allowed
	 * member2OfUser2 IS disallowed
	 * member1OfUser3 IS allowed
	 * member2OfUser3 IS allowed
	 *
	 * group1InVo1 -> member1OfUser1, member2OfUser2, member1OfUser3
	 * group2InVo1 -> member1OfUser1, member2OfUser2
	 * group1InVo2 -> member2OfUser1, member1OfUser2
	 * group2InVo2 -> member2OfUser1, member1OfUser2, member2OfUser3
	 *
	 * facility1 -> host1OnFacility1, host2OnFacility1
	 * facility2 -> host1OnFacility2, host2OnFacility2
	 * facility3 -> host1OnFacility3, host2OnFacility3
	 *
	 * resource1InVo1 ->  facility1 && group1InVo1, group2InVo1
	 * resource2InVo1 ->  facility2 && group2InVo1
	 * resource1InVo2 ->  facility2 && group1InVo2, group2InVo2
	 * resource2InVo2 ->  facility3 && group2InVo2
	 */
	public void setUpWorld() throws Exception {
		//Create VO
		vo1 = perun.getVosManagerBl().createVo(sess, new Vo(0, "vo1Test", "v1T"));
		vo2 = perun.getVosManagerBl().createVo(sess, new Vo(0, "vo2Test", "v2T"));

		//Create Groups(members groups in vos), Members and Users from Candidates
		Candidate can1 = new Candidate();
		can1.setFirstName("user1");
		can1.setId(0);
		can1.setMiddleName("");
		can1.setLastName("Test");
		can1.setTitleBefore("");
		can1.setTitleAfter("");
		UserExtSource userExtSource1 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user1TestLogin");
		UserExtSource userExtSource2 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user2TestLogin");
		UserExtSource userExtSource3 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user3TestLogin");
		can1.setUserExtSource(userExtSource1);
		can1.setAttributes(new HashMap<String,String>());
		member1OfUser1 = perun.getMembersManagerBl().createMemberSync(sess, vo1, can1);
		user1 = perun.getUsersManagerBl().getUserByMember(sess, member1OfUser1);
		member2OfUser1 = perun.getMembersManagerBl().createMember(sess, vo2, user1);
		can1.setFirstName("user2");
		can1.setUserExtSource(userExtSource2);
		member1OfUser2 = perun.getMembersManagerBl().createMemberSync(sess, vo2, can1);
		user2 = perun.getUsersManagerBl().getUserByMember(sess, member1OfUser2);
		member2OfUser2 = perun.getMembersManagerBl().createMember(sess, vo1, user2);
		can1.setFirstName("user3");
		can1.setUserExtSource(userExtSource3);
		member1OfUser3 = perun.getMembersManagerBl().createMemberSync(sess, vo1, can1);
		user3 = perun.getUsersManagerBl().getUserByMember(sess, member1OfUser3);
		member2OfUser3 = perun.getMembersManagerBl().createMember(sess, vo2, user3);

		//Validate members
		member1OfUser1 = perun.getMembersManagerBl().validateMember(sess, member1OfUser1);
		member2OfUser1 = perun.getMembersManagerBl().validateMember(sess, member2OfUser1);
		member1OfUser2 = perun.getMembersManagerBl().validateMember(sess, member1OfUser2);
		member2OfUser2 = perun.getMembersManagerBl().validateMember(sess, member2OfUser2);
		member1OfUser3 = perun.getMembersManagerBl().validateMember(sess, member1OfUser3);
		member2OfUser3 = perun.getMembersManagerBl().validateMember(sess, member2OfUser3);

		//Invalidate some members to Disallowed them
		perun.getMembersManagerBl().invalidateMember(sess, member2OfUser1);
		perun.getMembersManagerBl().invalidateMember(sess, member2OfUser2);

		//Create groups and add members to them
		membersGroupOfVo1 = perun.getGroupsManagerBl().getGroupByName(sess, vo1, VosManager.MEMBERS_GROUP);
		membersGroupOfVo2 = perun.getGroupsManagerBl().getGroupByName(sess, vo2, VosManager.MEMBERS_GROUP);
		group1InVo1 = perun.getGroupsManagerBl().createGroup(sess, vo1, new Group("testGroup1InVo1", ""));
		group2InVo1 = perun.getGroupsManagerBl().createGroup(sess, vo1, new Group("testGroup2InVo1", ""));
		group1InVo2 = perun.getGroupsManagerBl().createGroup(sess, vo2, new Group("testGroup1InVo2", ""));
		group2InVo2 = perun.getGroupsManagerBl().createGroup(sess, vo2, new Group("testGroup2InVo2", ""));
		perun.getGroupsManagerBl().addMember(sess, group1InVo1, member1OfUser1);
		perun.getGroupsManagerBl().addMember(sess, group2InVo1, member1OfUser1);
		perun.getGroupsManagerBl().addMember(sess, group1InVo1, member2OfUser2);
		perun.getGroupsManagerBl().addMember(sess, group2InVo1, member2OfUser2);
		perun.getGroupsManagerBl().addMember(sess, group1InVo2, member2OfUser1);
		perun.getGroupsManagerBl().addMember(sess, group2InVo2, member2OfUser1);
		perun.getGroupsManagerBl().addMember(sess, group1InVo2, member1OfUser2);
		perun.getGroupsManagerBl().addMember(sess, group2InVo2, member1OfUser2);
		perun.getGroupsManagerBl().addMember(sess, group1InVo1, member1OfUser3);
		perun.getGroupsManagerBl().addMember(sess, group2InVo2, member2OfUser3);

		//Create Facility
		facility1 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "testFacility1"));
		facility2 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "testFacility2"));
		facility3 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "testFacility3"));

		//Create Host on Facilities
		host1OnFacility1 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost1OnFacility1"), facility1);
		host2OnFacility1 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost2OnFacility1"), facility1);
		host1OnFacility2 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost1OnFacility2"), facility2);
		host2OnFacility2 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost2OnFacility2"), facility2);
		host1OnFacility3 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost1OnFacility3"), facility3);
		host2OnFacility3 = perun.getFacilitiesManagerBl().addHost(sess, new Host(0, "testHost2OnFacility3"), facility3);

		//Create resources and assing group to them
		resource1InVo1 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource1InVo1", "", facility1.getId(), vo1.getId()), vo1, facility1);
		resource2InVo1 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource2InVo1", "", facility2.getId(), vo1.getId()), vo1, facility2);
		resource1InVo2 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource1InVo2", "", facility2.getId(), vo2.getId()), vo2, facility2);
		resource2InVo2 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource2InVo2", "", facility3.getId(), vo2.getId()), vo2, facility3);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group1InVo1, resource1InVo1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2InVo1, resource1InVo1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2InVo1, resource2InVo1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group1InVo2, resource1InVo2);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2InVo2, resource1InVo2);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2InVo2, resource2InVo2);
	}

	// ==============  1. GET ATTRIBUTES ================================

	@Test
	public void setGroupNameWillProduceSettingMoreThanOneGIDAtOnce() throws Exception {
		System.out.println("attributesManager.setGroupNameWillProduceSettingMoreThanOneGIDAtOnce");

		//special variables
		String namespaceAAA = "AAA";
		String namespaceBBB = "BBB";

		//create attribute group_name in namespace aaa
		AttributeDefinition g_gn_AAA_def = new AttributeDefinition();
		g_gn_AAA_def.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		g_gn_AAA_def.setDescription("groupName in namespace AAA");
		g_gn_AAA_def.setFriendlyName("unixGroupName-namespace:" + namespaceAAA);
		g_gn_AAA_def.setType(String.class.getName());
		g_gn_AAA_def = perun.getAttributesManagerBl().createAttribute(sess, g_gn_AAA_def);
		Attribute g_gn_AAA = new Attribute(g_gn_AAA_def);
		g_gn_AAA.setValue("testGroupName");

		//create attribute group_name in namespace aaa
		AttributeDefinition g_gn_BBB_def = new AttributeDefinition();
		g_gn_BBB_def.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		g_gn_BBB_def.setDescription("groupName in namespace BBB");
		g_gn_BBB_def.setFriendlyName("unixGroupName-namespace:" + namespaceBBB);
		g_gn_BBB_def.setType(String.class.getName());
		g_gn_BBB_def = perun.getAttributesManagerBl().createAttribute(sess, g_gn_BBB_def);

		//create attribute gid in namespace aaa
		AttributeDefinition g_gid_AAA_def = new AttributeDefinition();
		g_gid_AAA_def.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		g_gid_AAA_def.setDescription("gid in namespace AAA");
		g_gid_AAA_def.setFriendlyName("unixGID-namespace:" + namespaceAAA);
		g_gid_AAA_def.setType(Integer.class.getName());
		g_gid_AAA_def = perun.getAttributesManagerBl().createAttribute(sess, g_gid_AAA_def);

		//create attribute gid in namespace bbb
		AttributeDefinition g_gid_BBB_def = new AttributeDefinition();
		g_gid_BBB_def.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		g_gid_BBB_def.setDescription("gid in namespace BBB");
		g_gid_BBB_def.setFriendlyName("unixGID-namespace:" + namespaceBBB);
		g_gid_BBB_def.setType(Integer.class.getName());
		g_gid_BBB_def = perun.getAttributesManagerBl().createAttribute(sess, g_gid_BBB_def);

		//create attribute group_name in namespace aaa
		AttributeDefinition r_gn_AAA_def = new AttributeDefinition();
		r_gn_AAA_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gn_AAA_def.setDescription("groupName in namespace AAA");
		r_gn_AAA_def.setFriendlyName("unixGroupName-namespace:" + namespaceAAA);
		r_gn_AAA_def.setType(String.class.getName());
		r_gn_AAA_def = perun.getAttributesManagerBl().createAttribute(sess, r_gn_AAA_def);

		//create attribute group_name in namespace aaa
		AttributeDefinition r_gn_BBB_def = new AttributeDefinition();
		r_gn_BBB_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gn_BBB_def.setDescription("groupName in namespace BBB");
		r_gn_BBB_def.setFriendlyName("unixGroupName-namespace:" + namespaceBBB);
		r_gn_BBB_def.setType(String.class.getName());
		r_gn_BBB_def = perun.getAttributesManagerBl().createAttribute(sess, r_gn_BBB_def);

		//create attribute gid in namespace aaa
		AttributeDefinition r_gid_AAA_def = new AttributeDefinition();
		r_gid_AAA_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gid_AAA_def.setDescription("gid in namespace AAA");
		r_gid_AAA_def.setFriendlyName("unixGID-namespace:" + namespaceAAA);
		r_gid_AAA_def.setType(Integer.class.getName());
		r_gid_AAA_def = perun.getAttributesManagerBl().createAttribute(sess, r_gid_AAA_def);

		//create attribute gid in namespace bbb
		AttributeDefinition r_gid_BBB_def = new AttributeDefinition();
		r_gid_BBB_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gid_BBB_def.setDescription("gid in namespace BBB");
		r_gid_BBB_def.setFriendlyName("unixGID-namespace:" + namespaceBBB);
		r_gid_BBB_def.setType(Integer.class.getName());
		r_gid_BBB_def = perun.getAttributesManagerBl().createAttribute(sess, r_gid_BBB_def);

		//Create special enviroment
		Vo v1 = new Vo(0, "TestingVo01", "TestingVo01");
		v1 = perun.getVosManagerBl().createVo(sess, v1);

		Facility f1 = new Facility(0, "Facility01_test");
		f1 = perun.getFacilitiesManagerBl().createFacility(sess, f1);
		Facility f2 = new Facility(0, "Facility02_test");
		f2 = perun.getFacilitiesManagerBl().createFacility(sess, f2);

		Resource r1 = new Resource(0, "TestingResource01", "TestingResource01", f1.getId(), v1.getId());
		r1 = perun.getResourcesManagerBl().createResource(sess, r1, v1, f1);
		Resource r2 = new Resource(0, "TestingResource02", "TestingResource02", f2.getId(), v1.getId());
		r2 = perun.getResourcesManagerBl().createResource(sess, r2, v1, f2);

		Group g1 = new Group("Testing_group01", "Testing group01");
		g1 = perun.getGroupsManagerBl().createGroup(sess, v1, g1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, g1, r1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, g1, r2);

		//Create minGID and maxGID for new namespace
		AttributeDefinition maxGIDAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:entityless:attribute-def:def:namespace-maxGID");
		Attribute maxGIDAAA = new Attribute(maxGIDAttrDef);
		maxGIDAAA.setValue(10000);
		Attribute maxGIDBBB = new Attribute(maxGIDAttrDef);
		maxGIDBBB.setValue(10000);
		AttributeDefinition minGIDAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:entityless:attribute-def:def:namespace-minGID");
		Attribute minGIDAAA = new Attribute(minGIDAttrDef);
		minGIDAAA.setValue(100);
		Attribute minGIDBBB = new Attribute(minGIDAttrDef);
		minGIDBBB.setValue(100);
		perun.getAttributesManagerBl().setAttribute(sess, namespaceAAA, minGIDAAA);
		perun.getAttributesManagerBl().setAttribute(sess, namespaceBBB, minGIDBBB);
		perun.getAttributesManagerBl().setAttribute(sess, namespaceAAA, maxGIDAAA);
		perun.getAttributesManagerBl().setAttribute(sess, namespaceBBB, maxGIDBBB);

		//set new namespace for facility (gid and groupName)
		AttributeDefinition groupNameNamespaceForFacilitiesAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:facility:attribute-def:def:unixGroupName-namespace");
		Attribute groupNameNamespaceForFacilities = new Attribute(groupNameNamespaceForFacilitiesAttrDef);
		groupNameNamespaceForFacilities.setValue(namespaceAAA);
		perun.getAttributesManagerBl().setAttribute(sess, f1, groupNameNamespaceForFacilities);
		perun.getAttributesManagerBl().setAttribute(sess, f2, groupNameNamespaceForFacilities);
		AttributeDefinition GIDNamespaceForFacilitiesAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:facility:attribute-def:def:unixGID-namespace");
		Attribute GIDNamespaceForFacilities = new Attribute(GIDNamespaceForFacilitiesAttrDef);
		GIDNamespaceForFacilities.setValue(namespaceAAA);
		perun.getAttributesManagerBl().setAttribute(sess, f1, GIDNamespaceForFacilities);
		GIDNamespaceForFacilities.setValue(namespaceBBB);
		perun.getAttributesManagerBl().setAttribute(sess, f2, GIDNamespaceForFacilities);

		//create new service and assigne it to resources
		Service s1 = new Service(0, "testService01");
		Owner o1 = new Owner(0,"testOwner01", "test@test.test", OwnerType.administrative);
		o1 = perun.getOwnersManagerBl().createOwner(sess, o1);
		s1 = perun.getServicesManagerBl().createService(sess, s1, o1);
		perun.getResourcesManagerBl().assignService(sess, r1, s1);
		perun.getResourcesManagerBl().assignService(sess, r2, s1);

		//create other required attributes and add them to the service
		AttributeDefinition f_v_maxGID = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:facility:attribute-def:virt:maxGID");
		AttributeDefinition f_v_minGID = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:facility:attribute-def:virt:minGID");
		AttributeDefinition g_v_gn = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:group_resource:attribute-def:virt:unixGroupName");
		AttributeDefinition g_v_gid = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:group_resource:attribute-def:virt:unixGID");
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, groupNameNamespaceForFacilitiesAttrDef);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, GIDNamespaceForFacilitiesAttrDef);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, f_v_maxGID);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, f_v_minGID);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, g_v_gn);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, g_v_gid);

		//set group_name to group g1
		perun.getAttributesManagerBl().setAttribute(sess, g1, g_gn_AAA);

		Attribute groupGIDInAAA = perun.getAttributesManagerBl().getAttribute(sess, g1, g_gid_AAA_def.getName());
		Attribute groupGIDInBBB = perun.getAttributesManagerBl().getAttribute(sess, g1, g_gid_BBB_def.getName());

		assertEquals(new Integer(100), (Integer) groupGIDInAAA.getValue());
		assertEquals(new Integer(100), (Integer) groupGIDInBBB.getValue());
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndMember() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<Resource, Member>(resource1InVo1, member1OfUser1, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);
		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("the Vo is vo1", vo1.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndGroup() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<Resource, Group>(resource1InVo1, group1InVo1, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);
		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo1", vo1.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUserAndFacility() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUserAndFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<User, Facility>(user2, facility2, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo2", vo2.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromGroup() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(group2InVo2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo2", vo2.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromMember() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(member2OfUser2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("Return no vo.", listOfRichAttributes.size() == 0);
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResource() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResource");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(resource2InVo2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo2", vo2.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUser() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUser");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(user1);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo1", vo1.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromHost() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromHost");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(host1OnFacility2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return at least 2 vos", listOfRichAttributes.size() >= 2);
		assertTrue("return maximum of 3 vos", listOfRichAttributes.size() <= 3);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		List<Vo> returnedVos = new ArrayList<Vo>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo1", returnedVos.contains(vo1));
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromFacility() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(facility3);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return at least 1 vos", listOfRichAttributes.size() >= 1);
		assertTrue("return max 2 vos", listOfRichAttributes.size() <= 2);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		List<Vo> returnedVos = new ArrayList<Vo>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
		assertTrue("returned vos not contains vo1", !returnedVos.contains(vo1));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromVo() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromVo");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(vo2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return 1 specific vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("returned vos contains vo2", listOfRichAttributes.get(0).getPrimaryHolder().equals(vo2));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromKey() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetVosFromKey");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder("String");

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return at least 2 vos", listOfRichAttributes.size() > 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		List<Vo> returnedVos = new ArrayList<Vo>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
		assertTrue("returned vos contains vo1", returnedVos.contains(vo1));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndMember() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, group1InVo2, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(resource1InVo2);
		richAttr.setSecondaryHolder(member2OfUser1);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("Return no group.", listOfRichAttributes.size() == 0);
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndGroup() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(resource1InVo2);
		richAttr.setSecondaryHolder(group2InVo2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return only one group", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Group);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("richObject have in primaryAttribute our group", listOfRichAttributes.get(0).getPrimaryHolder().equals(group2InVo2));
		assertTrue("richObject have in Attribute our attribute, which was set before", listOfRichAttributes.get(0).getAttribute().equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromUserAndFacility() throws Exception {
		System.out.println("attributesManager.getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromUserAndFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR);
		perun.getAttributesManagerBl().setAttribute(sess, group1InVo2, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(user2);
		richAttr.setSecondaryHolder(facility2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return two groups", listOfRichAttributes.size() == 2);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Group);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		List<Group> groups = new ArrayList<Group>();
		for(RichAttribute ra: listOfRichAttributes) {
			groups.add((Group) ra.getPrimaryHolder());
		}
		assertTrue("groups contains group1InVo2", groups.contains(group1InVo2));
		assertTrue("groups contains group2InVo2", groups.contains(group2InVo2));
		assertTrue("richObject have in Attribute our attribute, which was set before", listOfRichAttributes.get(0).getAttribute().equals(attribute));
	}

	//TODO Another TESTS for getRichAttributesWithHolders

	@Test
	public void getAllSimilarAttributeNames() throws Exception {
		System.out.println("attributesManager.getAllSimilarAttributeNames");

		List<String> similarAttrNames = new ArrayList<String>();
		String name = "urn:perun:user:attribute-def:def:login-namespace";
		similarAttrNames = perun.getAttributesManagerBl().getAllSimilarAttributeNames(sess, name);
		assertTrue("returned less than 0 names",similarAttrNames.size() >= 0);
	}

	@Test
	public void getFacilityAttributes() throws Exception {
		System.out.println("attributesManager.getFacilityAttributes");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributesManager.setAttribute(sess, facility, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
		assertNotNull("unable to get facility attributes", retAttr);

		assertTrue("our atttribute not returned",retAttr.contains(attributes.get(0)));
		assertTrue("returned less than 4 attributes",retAttr.size() >= 3);
		// 2 core + 1 opt

	}

	@Test (expected=FacilityNotExistsException.class)
		public void getFacilityAttributesWhenFacilityNotExists() throws Exception {
			System.out.println("attributesManager.getFacilityAttributesWhenFacilityNotExists");

			attributesManager.getAttributes(sess, new Facility());
			// shouldn't find facility

		}

	@Test
	public void getAllGroupAttributesStartWithNameWithoutNullValue() throws Exception {
		System.out.println("attributesManager.getAllAttributesStartWithNameWithoutNullValue");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttributes();

		for(Attribute a: attributes) {
			attributesManager.setAttribute(sess, group, a);
		}

		List<Attribute> retAttr = attributesManager.getAllAttributesStartWithNameWithoutNullValue(sess, group, AttributesManager.NS_GROUP_ATTR_OPT + ":group_test_uniqueattribute");

		assertNotNull("unable to get group attributes", retAttr);


		assertTrue("our atttributes not returned",attributes.containsAll(retAttr));
		assertTrue("returned 3 attributes",retAttr.size() == 3);
	}

	@Test
	public void getAllResourceAttributesStartWithNameWithoutNullValue() throws Exception {
		System.out.println("attributesManager.getAllAttributesStartWithNameWithoutNullValue");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttributes();

		for(Attribute a: attributes) {
			attributesManager.setAttribute(sess, resource, a);
		}

		List<Attribute> retAttr = attributesManager.getAllAttributesStartWithNameWithoutNullValue(sess, resource, AttributesManager.NS_RESOURCE_ATTR_OPT + ":resource_test_uniqueattribute:");
		assertNotNull("unable to get resource attributes", retAttr);

		assertTrue("our atttributes not returned",attributes.containsAll(retAttr));
		assertTrue("returned 3 attributes",retAttr.size() == 3);
	}

	@Test
	public void getVoAttributes() throws Exception {
		System.out.println("attributesManager.getVoAttributes");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributesManager.setAttribute(sess, vo, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
		assertNotNull("unable to get vo attributes", retAttr);

		assertTrue("our atttribute not returned",retAttr.contains(attributes.get(0)));
		assertTrue("returned less than 4 attributes",retAttr.size() >= 4);
		// 3 core + 1 opt

	}

	@Test
	public void testOfAllGetMethods() throws Exception {
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		member = setUpMember();
		List<Host> hosts = setUpHost();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		service = setUpService();
		List<Attribute> attribute = new ArrayList<Attribute>();
		attributes = setUpFacilityAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, facility, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, facility, attributes.get(0).getName()));
		attributes = setUpGroupAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, group, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, group, attributes.get(0).getName()));
		attributes = setUpHostAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, hosts.get(0), attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, hosts.get(0), attributes.get(0).getName()));
		attributes = setUpMemberAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, member, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, member, attributes.get(0).getName()));
		attributes = setUpResourceAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, resource, attributes.get(0).getName()));
		attributes = setUpEntitylessAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, "klic", attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, "klic", attributes.get(0).getName()));
		attributes = setUpUserAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, user, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, user, attributes.get(0).getName()));
		attributes = setUpVoAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, vo, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, vo, attributes.get(0).getName()));
		attributes = setUpFacilityUserAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, facility, user, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, facility, user, attributes.get(0).getName()));
		attributes = setUpGroupResourceAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, group, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, resource, group, attributes.get(0).getName()));
		attributes = setUpMemberResourceAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, member, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, resource, member, attributes.get(0).getName()));
		attributes = setUpUserLargeAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, user, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, user, attributes.get(0).getName()));
		attributes = setUpResourceLargeAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, resource, attributes.get(0).getName()));
	}

	@Test
	public void getEntitylessAttributes() throws Exception {
		System.out.println("attributesManager.getEntitylessAttributes");

		attributes = setUpEntitylessAttribute();
		String key = "Test Attributu Michal";
		attributesManager.setAttribute(sess, key, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, key);
		assertNotNull("unable to get entityless attributes", retAttr);

		assertTrue("our atttribute not returned",retAttr.contains(attributes.get(0)));
		assertEquals("We expected 1 and we get "+retAttr.size(), 1, retAttr.size());

	}
	
	@Test
	public void getEntitylessAttributeForUpdateWithListValue() throws Exception {
		System.out.println("attributesManager.getEntitylessAttributeForUpdate");

		List<Attribute> attributes = setUpEntitylessAttributeWithListValue();
		perun.getAttributesManagerBl().setAttribute(sess, "test1", attributes.get(0));
		perun.getAttributesManagerBl().setAttribute(sess, "test2", attributes.get(0));
		
		Attribute attr1 = perun.getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, "test1", attributes.get(0).getName());
		Attribute attr2 = perun.getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, "test2", attributes.get(0).getName());
		
		List<String> attr1Value = (List<String>) attr1.getValue();
		List<String> attr2Value = (List<String>) attr2.getValue();
		
		assertTrue("Values must be equals", attr1Value.equals(attributes.get(0).getValue()));
		assertTrue("Values must be equals", attr2Value.equals(attributes.get(0).getValue()));
		assertTrue("Attributes are the same", attr1.equals(attr2));
		assertTrue("Attributes are the same", attr1.equals(attributes.get(0)));
		assertTrue("Attributes are the same", attr2.equals(attributes.get(0)));
	}
	
	@Test
	public void getEntitylessAttributeForUpdateWithMapValue() throws Exception {
		System.out.println("attributesManager.getEntitylessAttributeForUpdate");

		List<Attribute> attributes = setUpEntitylessAttributeWithMapValue();
		
		perun.getAttributesManagerBl().setAttribute(sess, "test1", attributes.get(0));
		perun.getAttributesManagerBl().setAttribute(sess, "test2", attributes.get(0));
		
		Attribute testAttr1 = perun.getAttributesManagerBl().getAttribute(sess, "test1", attributes.get(0).getName());
		Attribute testAttr2 = perun.getAttributesManagerBl().getAttribute(sess, "test2", attributes.get(0).getName());
		
		Attribute attr1 = perun.getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, "test1", attributes.get(0).getName());
		Attribute attr2 = perun.getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, "test2", attributes.get(0).getName());
		
		Map<String, String> attr1Value = (Map<String, String>) attr1.getValue();
		Map<String, String> attr2Value = (Map<String, String>) attr2.getValue();
		
		assertTrue("Values must be equals", attr1Value.equals(attributes.get(0).getValue()));
		assertTrue("Values must be equals", attr2Value.equals(attributes.get(0).getValue()));
		assertTrue("Attributes are the same", attr1.equals(attr2));
		assertTrue("Attributes are the same", attr1.equals(attributes.get(0)));
		assertTrue("Attributes are the same", attr2.equals(attributes.get(0)));
	}
	
	@Test
	public void getEntitylessKeys() throws Exception {
		System.out.println("attributesManager.getEntitylessKeys");

		attributes = setUpEntitylessAttribute();
		String key = "Test Attributu Michal2";
		attributesManager.setAttribute(sess, key, attributes.get(0));

		List<String> entStr = attributesManager.getEntitylessKeys(sess, attributesManager.getAttributeDefinition(sess, attributes.get(0).getName()));
		assertNotNull("unable to get entityless attributes", entStr);

		assertTrue("our String not returned",entStr.contains(key));
		assertEquals("We expected 1 and we get "+entStr.size(), 1, entStr.size());

	}

	/*@Test
		public void checkAttributeDependenciesForAllAttributesInMap() throws Exception {
		System.out.println("attributesManager.checkAttributeDependenciesForAllAttributesInMap");
		AttributesManagerBlImpl attributesManagerBlImpl = mock(AttributesManagerBlImpl.class, RETURNS_DEEP_STUBS); //RETURNS_DEEP_STUBS = budeme mockovat nekolik vnorenych volani metod
//spy(attributesManagerBlImpl).checkAttributeValue(sess, resource, null);
//when(attributesManagerBlImpl.checkAttributeValue(any(PerunSession.class), any(Resource.class), any(Attribute.class)))
}*/

@Test (expected=VoNotExistsException.class)
	public void getVoAttributesWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.getVoAttributesWhenVoNotExists");

		attributesManager.getAttributes(sess, new Vo());
		// shouldn't find VO

	}

@Test
public void getResourceAttributes() throws Exception {
	System.out.println("attributesManager.getResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();
	attributesManager.setAttribute(sess, resource, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
	assertNotNull("unable to get resource attributes", retAttr);

	assertTrue("our atttribute not returned",retAttr.contains(attributes.get(0)));
	assertTrue("returned less than 4 attributes",retAttr.size() >= 4);
	// 3 core + 1 opt

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceAttributesWhenResourceNotExists");

		attributesManager.getAttributes(sess, new Resource());
		// shouldn't find resource

	}

@Test
public void getResourceMemberAttributes() throws Exception {
	System.out.println("attributesManager.getResourceMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpMemberResourceAttribute();
	attributesManager.setAttribute(sess, resource, member, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member);
	assertNotNull("unable to get member-resource attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

// výjímky pro Member a Resource Not Exists nenastanou,
// protože si to bere data z předaných objektů a ne z databáze


@Test
public void getResourceMemberAttributesForUser() throws Exception {
	System.out.println("attributesManager.getResourceMemberAttributesForUser");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributesManager.setAttribute(sess, user, attributes.get(0));

	// return members and users attributes from resources members
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member, true);
	assertNotNull("unable to get member-resource(work with user) attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceMemberAttributesForUserWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceMemberAttributesForUserWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttributes(sess, new Resource(), member, true);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getResourceMemberAttributesForUserWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getResourceMemberAttributesForUserWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributes(sess, resource, new Member(), true);
		// shouldn't find member

	}

@Test
public void getMemberAttributes() throws Exception {
	System.out.println("attributesManager.getMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	assertNotNull("unable to get member attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=MemberNotExistsException.class)
	public void getMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributesWhenMemberNotExists");

		attributesManager.getAttributes(sess, new Member());
		// shouldn't find member

	}

@Test
public void getMemberAttributesByListOfNames1() throws Exception {
	System.out.println("attributesManager.getMemberAttributesByListOfNames");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:member:attribute-def:opt");
	attr.setFriendlyName("member_test_for_list_of_names_attribute");
	attr.setType(String.class.getName());
	attr.setValue("MemberAttributeForList");
	attributesManager.createAttribute(sess, attr);
	attributesManager.setAttribute(sess, member, attr);

	List<String> attrNames = new ArrayList<String>();
	attrNames.add(attributes.get(0).getName());
	attrNames.add(attr.getName());

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member, attrNames);
	assertNotNull("unable to get member attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));
	assertTrue("our attribute was not returned",retAttr.contains(attr));
}

@Test
public void getMemberAttributesByListOfNames2() throws Exception {
	System.out.println("attributesManager.getMemberAttributesByListOfNames");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:member:attribute-def:opt");
	attr.setFriendlyName("member_test_for_list_of_names_attribute");
	attr.setType(String.class.getName());
	attr.setValue("MemberAttributeForList");
	attributesManager.createAttribute(sess, attr);
	attributesManager.setAttribute(sess, member, attr);

	List<String> attrNames = new ArrayList<String>();
	attrNames.add(attr.getName());

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member, attrNames);
	assertNotNull("unable to get member attributes", retAttr);
	assertFalse("our attribute was not returned",retAttr.contains(attributes.get(0)));
	assertTrue("our attribute was not returned",retAttr.contains(attr));
}

@Test
public void getFacilityUserAttributes() throws Exception {
	System.out.println("attributesManager.getFacilityUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpFacilityUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);

	attributesManager.setAttributes(sess, facility, user, attributes);
	// set facility-user attribute

	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, user);
	assertNotNull("unable to get facility-user attributes",retAttr);
	assertTrue("returned incorrect facility-user",retAttr.contains(attributes.get(0)));

}

@Test (expected=FacilityNotExistsException.class)
	public void getFacilityUserAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributesWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributes(sess, new Facility(), user);
		// shouldn't find facility;

	}

@Test (expected=UserNotExistsException.class)
	public void getFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributesWhenUserNotExists");

		facility = setUpFacility();

		attributesManager.getAttributes(sess, facility, new User());
		// shouldn't find user;

	}

@Test
public void getUserAttributes() throws Exception {
	System.out.println("attributesManager.getUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributesManager.setAttribute(sess, user, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertNotNull("unable to get user attributes",retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=UserNotExistsException.class)
	public void getUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getUserAttributesWhenUserNotExists");

		attributesManager.getAttributes(sess, new User());
		// souldn't find user

	}

@Test
public void getUserLargeAttributes() throws Exception {
	System.out.println("attributesManager.getUserAttributes - large attributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpUserLargeAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributesManager.setAttribute(sess, user, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertNotNull("unable to get user attributes",retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));
}

@Test
public void getGroupAttributes() throws Exception {
	System.out.println("attributesManager.getGroupAttributes");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();
	attributesManager.setAttribute(sess, group, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
	assertNotNull("unable to get group attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=GroupNotExistsException.class)
	public void getGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getGroupAttributesWhenGroupNotExists");

		attributesManager.getAttributes(sess, new Group());
		// shouldn't find group

	}

@Test
public void getGroupResourceAttributes() throws Exception {
	System.out.println("attributesManager.getGroupResourceAttributes");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpGroupResourceAttribute();
	attributesManager.setAttribute(sess, resource, group, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
	assertNotNull("unable to get group-resource attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void getGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void getGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributes(sess, resource, new Group());
		// shouldn't find member

	}

@Test
public void getHostAttributes() throws Exception {
	System.out.println("attributesManager.getHostAttributes");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttribute(sess, host, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
	assertNotNull("unable to get host attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=HostNotExistsException.class)
	public void getHostAttributesWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.getHostAttributesWhenHostNotExists");

		attributesManager.getAttributes(sess, new Host());
		// shouldn't find host

	}

@Test
public void getAttributesByAttributeDefinition() throws Exception {
	System.out.println("attributesManager.getAttributesByAttributeDefinition");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttribute(sess, host, attributes.get(0));

	List<Attribute> retAttr = attributesManager.getAttributesByAttributeDefinition(sess, attributes.get(0));
	assertNotNull("unable to get attributes", retAttr);
	assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

}






// ==============  2.  SET ATTRIBUTES ================================




@Test
public void setFacilityAttributes() throws Exception {
	System.out.println("attributesManager.setFacilityAttributes");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();

	attributesManager.setAttributes(sess, facility, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);

	assertTrue("unable to set/or return facility attribute we created",retAttr.contains(attributes.get(0)));

}

@Test (expected=FacilityNotExistsException.class)
	public void setFacilityAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityAttributesWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();

		attributesManager.setAttributes(sess, new Facility(), attributes);
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void setFacilityAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityAttributesWhenAttrbuteNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object no existing in db by setting it's ID to 0
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setFacilityAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println("attributesManager.setFacilityAttributesWhenWrongAttrAssigment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		// create Vo attribute instead Facility attribute to raise exception
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't set wrong attribute

	}

@Test (expected=InternalErrorException.class)
	public void setFacilityAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setFacilityAttributesWhenTypeMismatch");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setVoAttributes() throws Exception {
	System.out.println("attributesManager.setVoAttributes");

	vo = setUpVo();
	attributes = setUpVoAttribute();

	attributesManager.setAttributes(sess, vo, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
	assertTrue("unable to set/or return vo attribute we created",retAttr.contains(attributes.get(0)));

}

@Test (expected=VoNotExistsException.class)
	public void setVoAttributesWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.setVoAttributesWhenVoNotExists");

		attributes = setUpVoAttribute();

		attributesManager.setAttributes(sess, new Vo(), attributes);
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void setVoAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setVoAttributes");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		// make valid attribute into not existing by setting ID = 0
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setVoAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println("attributesManager.setVoAttributesWhenWrongAttrAssigment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		// create Facility attribute instead Vo attribute to raise exception
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't set wrong attribute

	}

@Test (expected=InternalErrorException.class)
	public void setVoAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setVoAttributesWhenTypeMismatch");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setResourceAttributes() throws Exception {
	System.out.println("attributesManager.setResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();

	attributesManager.setAttributes(sess, resource, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
	assertTrue("unable to set/or return resource attribute we created",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void setResourceAttributesWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.setResourceAttributesWhenVoNotExists");

		attributes = setUpResourceAttribute();

		attributesManager.setAttributes(sess, new Resource(), attributes);
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void setResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setVoAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute into not existing by setting ID = 0
		attributesManager.setAttributes(sess, resource, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setResourceAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println("attributesManager.setResourceAttributesWhenWrongAttrAssigment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();
		// create Vo attribute instead Resource attribute to raise exception
		attributesManager.setAttributes(sess, resource, attributes);
		// shouldn't set wrong attribute

	}

@Test (expected=InternalErrorException.class)
	public void setResourceAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setResourceAttributesWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, resource, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setMemberResourceAttributes() throws Exception {
	System.out.println("attributesManager.setMemberResourceAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpMemberResourceAttribute();

	attributesManager.setAttributes(sess, resource, member, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member);
	assertNotNull("unable to get member-resource attributes", retAttr);
	assertTrue("unable to set/or return our member-resource attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void setMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setResourceMemberAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, new Resource(), member, attributes);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void setMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, resource, new Member(), attributes);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void setMemberResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0
		attributesManager.setAttributes(sess, resource, member, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberResourceAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributes = setUpVoAttribute();
		// set up wrong attribute - vo instead of member-resource
		attributesManager.setAttributes(sess, resource, member, attributes);
		// shouldn't set attribute

	}

@Test (expected=InternalErrorException.class)
	public void setMemberResourceAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, resource, member, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setUserAttributesForMemberResource() throws Exception {
	System.out.println("attributesManager.setUserAttributesForMemberResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpUserAttribute();

	attributesManager.setAttributes(sess, resource, member, attributes, true);

	// return users attributes from resource member
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member, true);
	assertNotNull("unable to set or get member-resource(work with user) attributes", attributes);
	assertTrue("our attribute was not set/returned",retAttr.contains(attributes.get(0)));

}

@Test (expected=MemberNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributesForMemberResourceWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, resource, new Member(), attributes, true);
		// shouldn't find member

	}

@Test (expected=ResourceNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributesForMemberResourceWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, new Resource(), member, attributes, true);
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributesForMemberResourceWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object not existing in DB

		attributesManager.setAttributes(sess, resource, member, attributes, true);
		// shouldn't find attribute

	}

@Test (expected=InternalErrorException.class)
	public void setUserAttributesForMemberResourceWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setUserAttributesForMemberResourceWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpUserAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, resource, member, attributes, true);
		// shouldn't set wrong attribute

	}

@Test
public void setMemberAttributes() throws Exception {
	System.out.println("attributesManager.setMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();

	attributesManager.setAttributes(sess, member, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	assertNotNull("unable to get members attributes", retAttr);
	assertTrue("our attribute is not set or returned from member", retAttr.contains(attributes.get(0)));

}

@Test (expected=MemberNotExistsException.class)
	public void setMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setMemberAttributesWhenMemberNotExists");

		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, new Member(), attributes);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void setMemberAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setMemberAttributesWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object not existing in DB

		attributesManager.setAttributes(sess, member, attributes);
		// shouldn't find attributes

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setMemberAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();
		// create user attribute instead of member attr to raise exception
		attributesManager.setAttributes(sess, member, attributes);
		// shoudln't add attribute

	}

@Test (expected=InternalErrorException.class)
	public void setMemberAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setMemberAttributesWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, member, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setUserAttributes() throws Exception {
	System.out.println("attributesManager.setUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpUserAttribute();

	attributesManager.setAttributes(sess, user, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertNotNull("unable to get user attributes", retAttr);
	assertTrue("our attribute is not set or returned from user", retAttr.contains(attributes.get(0)));

}

@Test (expected=UserNotExistsException.class)
	public void setUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributesWhenUserNotExists");

		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, new User(), attributes);
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributesWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0

		attributesManager.setAttributes(sess, user, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setUserAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setUserAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, user, attributes);
		// shouldn't add attribute

	}

@Test (expected=InternalErrorException.class)
	public void setUserAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setUserAttributesWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.setAttributes(sess, user, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setGroupAttributes() throws Exception {
	System.out.println("attributesManager.setGroupAttributes");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();

	attributesManager.setAttributes(sess, group, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
	assertNotNull("unable to get group attributes", retAttr);
	assertTrue("our attribute is not set or returned from group", retAttr.contains(attributes.get(0)));

}

@Test (expected=GroupNotExistsException.class)
	public void setGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.setGroupAttributesWhenGroupNotExists");

		attributes = setUpGroupAttribute();

		attributesManager.setAttributes(sess, new Group(), attributes);
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void setGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0

		attributesManager.setAttributes(sess, group, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setGroupAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setGroupAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, group, attributes);
		// shouldn't add attribute

	}

@Test (expected=InternalErrorException.class)
	public void setGroupAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setGroupAttributesWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, group, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setMemberWorkWithUserAttributes() throws Exception {
	System.out.println("attributesManager.setMemberWorkWithUserAttributes");
	vo = setUpVo();
	member = setUpMember();
	List<Attribute> attributes_member = setUpMemberAttribute();
	User user =sess.getPerun().getUsersManager().getUserByMember(sess, member);
	List<Attribute> attributes_user = setUpUserAttribute();
	attributes = new ArrayList<Attribute>();
	attributes.addAll(attributes_member);
	attributes.addAll(attributes_user);

	attributesManager.setAttributes(sess, member, attributes, true);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member, true);
	assertNotNull("unable to get member attributes", retAttr);
	assertTrue("unable to set/or return our member attribute",retAttr.contains(attributes.get(0)));
	assertTrue("unable to set/or return our member attribute",retAttr.contains(attributes.get(1)));
}

@Test
public void setMemberWorkWithoutUserAttributes() throws Exception {
	System.out.println("attributesManager.setMemberWorkWithoutUserAttributes");
	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();

	attributesManager.setAttributes(sess, member, attributes, false);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member, false);
	assertNotNull("unable to get member attributes", retAttr);
	assertTrue("unable to set/or return our member attribute",retAttr.contains(attributes.get(0)));
}

@Test
public void setGroupResourceAttributes() throws Exception {
	System.out.println("attributesManager.setGroupResourceAttributes");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpGroupResourceAttribute();

	attributesManager.setAttributes(sess, resource, group, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
	assertNotNull("unable to get group-resource attributes", retAttr);
	assertTrue("unable to set/or return our group-resource attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void setGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, new Resource(), group, attributes);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void setGroupResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, new Group(), attributes);
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void setGroupResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0
		attributesManager.setAttributes(sess, resource, group, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setGroupResourceAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		attributes = setUpVoAttribute();
		// set up wrong attribute - vo instead of group-resource
		attributesManager.setAttributes(sess, resource, group, attributes);
		// shouldn't set attribute

	}

@Test (expected=InternalErrorException.class)
	public void setGroupResourceAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributesWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, resource, member, attributes);
		// shouldn't set wrong attribute

	}

@Test
public void setHostAttributes() throws Exception {
	System.out.println("attributesManager.setHostAttributes");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();

	attributesManager.setAttributes(sess, host, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
	assertNotNull("unable to get host attributes", retAttr);
	assertTrue("our attribute is not set or returned from group", retAttr.contains(attributes.get(0)));

}

@Test (expected=HostNotExistsException.class)
	public void setHostAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.setHostAttributesWhenHostNotExists");

		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, new Host(), attributes);
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void setHostAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setHostAttributesWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0

		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setHostAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setHostAttributesWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't add attribute

	}

@Test (expected=InternalErrorException.class)
	public void setHostAttributesWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setHostAttributesWhenTypeMismatch");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't set wrong attribute

	}






// ==============  3.  GET ATTRIBUTE (by name) ================================
//
// attribute name is namespace:friendlyName (urn:perun:facility:attribute-def:core:id)



@Test
public void getFacilityAttribute() throws Exception {
	System.out.println("attributesManager.getFacilityAttribute");

	facility = setUpFacility();

	Attribute retAttr = attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:id");
	assertNotNull("unable to get core attribute facility id", retAttr);
	assertEquals("returned core attr value is not correct",retAttr.getValue(),facility.getId());

}

@Test (expected=FacilityNotExistsException.class)
	public void getFacilityAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeWhenFacilityNotExists");

		attributesManager.getAttribute(sess, new Facility(), "urn:perun:facility:attribute-def:core:id");
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void getFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();

		attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();

		attributesManager.getAttribute(sess, facility, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on facility

	}

@Test
public void getVoAttribute() throws Exception {
	System.out.println("attributesManager.getVoAttribute");

	vo = setUpVo();

	Attribute retAttr = attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:core:id");
	assertNotNull("unable to get core attribute vo id", retAttr);
	assertEquals("returned core attr value is not correct",retAttr.getValue(),vo.getId());

}

@Test (expected=VoNotExistsException.class)
	public void getVoAttributeWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.getVoAttributeWhenVoNotExists");

		attributesManager.getAttribute(sess, new Vo(), "urn:perun:vo:attribute-def:core:id");
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void getVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getVoAttributeWhenAttributeNotExists");

		vo = setUpVo();

		attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();

		attributesManager.getAttribute(sess, vo, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on vo

	}

@Test
public void getResourceAttribute() throws Exception {
	System.out.println("attributesManager.getResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();

	Attribute retAttr = attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:core:id");
	assertNotNull("unable to get core attribute resource id", retAttr);
	assertEquals("returned core attr value is not correct",retAttr.getValue(),resource.getId());

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceAttributeWhenResourceNotExists");

		attributesManager.getAttribute(sess, new Resource(), "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void getResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, "urn:perun:vo:attribute-def:core:id");
		// shouldn't find vo attribute on resource

	}

@Test
public void getMemberResourceAttribute() throws Exception {
	System.out.println("attributesManager.getMemberResourceAttribute");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpMemberResourceAttribute();

	attributesManager.setAttributes(sess, resource, member, attributes);

	Attribute retAttr = attributesManager.getAttribute(sess, resource, member,"urn:perun:member_resource:attribute-def:opt:member_resource_test_attribute");
	assertNotNull("unable to get opt member resource attribute ", retAttr);
	assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

}

@Test (expected=ResourceNotExistsException.class)
	public void getMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, resource, member, attributes);

		attributesManager.getAttribute(sess, new Resource(), member, "urn:perun:member_resource:attribute-def:opt:member_resource_test_attribute");
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeWhenMemberNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, resource, member, attributes);

		attributesManager.getAttribute(sess, resource, new Member(), "urn:perun:member_resource:attribute-def:opt:member_resource_test_attribute");
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void getMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, member, "urn:perun:member_resource:attribute-def:opt:nesmysl");
		// shouldn't find member resource attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, member, "urn:perun:resource:attribute-def:opt:member_resource_test_attribute");
		// shouldn't find resource attribute instead of member-resource

	}

@Test
public void getMemberAttribute() throws Exception {
	System.out.println("attributesManager.getMemberAttribute");

	vo = setUpVo();
	member = setUpMember();

	Attribute retAttr = attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:core:id");
	assertNotNull("unable to get core attribute member id", retAttr);
	assertEquals("returned core attr value is not correct",retAttr.getValue(),member.getId());

}

@Test (expected=MemberNotExistsException.class)
	public void getMemberAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributeWhenMemberNotExists");

		attributesManager.getAttribute(sess, new Member(), "urn:perun:member:attribute-def:core:id");
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void getMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on member

	}

@Test
public void getFacilityUserAttribute() throws Exception {
	System.out.println("attributesManager.getFacilityUserAttribute");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	attributes = setUpFacilityUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);

	attributesManager.setAttributes(sess, facility, user, attributes);

	Attribute retAttr = attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:opt:user_facility_test_attribute");
	assertNotNull("unable to get opt user_facility attribute ", retAttr);
	assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

}

@Test (expected=FacilityNotExistsException.class)
	public void getFacilityUserAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, facility, user, attributes);

		attributesManager.getAttribute(sess, new Facility(), user, "urn:perun:user_facility:attribute-def:opt:user_facility_test_attribute");
		// shouldn't find facility

	}

@Test (expected=UserNotExistsException.class)
	public void getFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeWhenUserNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, facility, user, attributes);

		attributesManager.getAttribute(sess, facility, new User(), "urn:perun:user_facility:attribute-def:opt:user_facility_test_attribute");
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void getFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, facility, user, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on user-facility

	}

@Test
public void getUserAttribute() throws Exception {
	System.out.println("attributesManager.getUserAttribute");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);

	attributesManager.setAttributes(sess, user, attributes);

	Attribute retAttr = attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:core:id");
	assertNotNull("unable to get core attribute user id", retAttr);
	assertEquals("returned core attr value is not correct",retAttr.getValue(),user.getId());

}

@Test (expected=UserNotExistsException.class)
	public void getUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getUserAttributeWhenUserNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, user, attributes);

		attributesManager.getAttribute(sess, new User(), "urn:perun:user:attribute-def:core:id");
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void getUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, user, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on user

	}

@Test
public void getGroupAttribute() throws Exception {
	System.out.println("attributesManager.getGroupAttribute");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();

	attributesManager.setAttribute(sess, group, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:group_test_attribute");
	assertNotNull("unable to get opt group attribute", retAttr);
	assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

}

@Test
public void getGroupAttributesFromList() throws Exception {
	System.out.println("attributesManager.getAttributes");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttributes();
	attributesManager.setAttributes(sess, group, attributes);

	List<String> attNames = new ArrayList<>();
	for (Attribute a : attributes) {
		attNames.add(a.getName());
	}

	List<Attribute> retAttributes = attributesManager.getAttributes(sess, group, attNames);
	assertNotNull("unable to get group attributes", retAttributes);
	assertTrue("returned opt attributes are not correct", attributes.equals(retAttributes));
}

@Test (expected=GroupNotExistsException.class)
	public void getGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getGroupAttributeWhenGroupNotExists");

		attributesManager.getAttribute(sess, new Group(), "urn:perun:group:attribute-def:opt:group_test_attribute");
		// shouldn't find groups

	}

@Test (expected=AttributeNotExistsException.class)
	public void getGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:nesmysl");
		// shouldn't find opt attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttribute(sess, group, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on group

	}

@Test
public void getGroupResourceAttribute() throws Exception {
	System.out.println("attributesManager.getGroupResourceAttribute");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpGroupResourceAttribute();

	attributesManager.setAttributes(sess, resource, group, attributes);

	Attribute retAttr = attributesManager.getAttribute(sess, resource, group,"urn:perun:group_resource:attribute-def:opt:group_resource_test_attribute");
	assertNotNull("unable to get opt group resource attribute ", retAttr);
	assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

}

@Test (expected=ResourceNotExistsException.class)
	public void getGroupResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, group, attributes);

		attributesManager.getAttribute(sess, new Resource(), group, "urn:perun:group_resource:attribute-def:opt:group_resource_test_attribute");
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void getGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeWhenGroupNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, group, attributes);

		attributesManager.getAttribute(sess, resource, new Group(), "urn:perun:group_resource:attribute-def:opt:group_resource_test_attribute");
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void getGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, group, "urn:perun:group_resource:attribute-def:opt:nesmysl");
		// shouldn't find member resource attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, group, "urn:perun:resource:attribute-def:opt:group_resource_test_attribute");
		// shouldn't find resource attribute instead of member-resource

	}

@Test
public void getHostAttribute() throws Exception {
	System.out.println("attributesManager.getHostAttribute");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();

	attributesManager.setAttributes(sess, host, attributes);

	Attribute retAttr = attributesManager.getAttribute(sess, host,"urn:perun:host:attribute-def:opt:host_test_attribute");
	assertNotNull("unable to get opt host attribute ", retAttr);
	assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

}

@Test (expected=HostNotExistsException.class)
	public void getHostAttributeWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.getHostAttributeWhenHostNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, host, attributes);

		attributesManager.getAttribute(sess, new Host(), "urn:perun:host:attribute-def:opt:host_test_attribute");
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void getHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);

		attributesManager.getAttribute(sess, host, "urn:perun:host:attribute-def:opt:nesmysl");
		// shouldn't find host attribute "nesmysl"

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);

		attributesManager.getAttribute(sess, host, "urn:perun:resource:attribute-def:opt:host_test_attribute");
		// shouldn't find resource attribute instead of host

	}









// ==============  4.  GET ATTRIBUTE DEFINITION ================================


@Test
public void getAttributeDefinition() throws Exception {
	System.out.println("attributesManager.getAttributeDefinition");

	AttributeDefinition attrDef = attributesManager.getAttributeDefinition(sess, "urn:perun:vo:attribute-def:core:id");
	assertNotNull("unable to get attribute definition by name",attrDef);
	assertTrue("returned wrong attr def by name",attrDef.getName().equals("urn:perun:vo:attribute-def:core:id"));

}

@Test
public void getAttributeDefinitionWithRights() throws Exception {
	System.out.println("attributesManager.getAttributeDefinitionWithRights");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	member = setUpMember();

	perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);
	perun.getGroupsManagerBl().addMember(sess, group, member);

	Attribute attr = setUpSpecificMemberResourceAttribute(member, resource);

	List<PerunBean> perunBeans = new ArrayList<PerunBean>();
	perunBeans.add(member);
	perunBeans.add(resource);

	List<AttributeDefinition> attrDefs = attributesManager.getAttributesDefinitionWithRights(sess, perunBeans);
	List<AttributeDefinition> allAttrDef = attributesManager.getAttributesDefinition(sess);

	assertFalse(attrDefs.isEmpty());
	assertFalse(attrDefs.containsAll(allAttrDef));
	assertTrue(allAttrDef.containsAll(attrDefs));
	assertTrue(attrDefs.contains(attr));

	for(AttributeDefinition ad: attrDefs) {
		assertTrue(attributesManager.isFromNamespace(sess, ad, AttributesManager.NS_MEMBER_ATTR) ||
				attributesManager.isFromNamespace(sess, ad, AttributesManager.NS_RESOURCE_ATTR) ||
				attributesManager.isFromNamespace(sess, ad, AttributesManager.NS_MEMBER_RESOURCE_ATTR));
		assertTrue(ad.getWritable());
	}

}

@Test (expected=AttributeNotExistsException.class)
	public void getAttributeDefinitionWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getAttributeDefinitionWhenAttributeNotExists");

		attributesManager.getAttributeDefinition(sess, "urn:perun:vo:attribute-def:core:nesmysl");
		// shouldn't find vo attribute "nesmysl"

	}

@Test
public void getAttributesDefinition() throws Exception {
	System.out.println("attributesManager.getAttributesDefinition");

	List<AttributeDefinition> attrDef = attributesManager.getAttributesDefinition(sess);
	assertNotNull("unable to get attributes definition",attrDef);
	assertTrue("there should be some attributes definition",attrDef.size() > 0);

}

@Test
public void getAttributeDefinitionById() throws Exception {
	System.out.println("attributesManager.getAttributesDefinitionById");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setDescription("attributesManagerTestAttrDef");
	attrDef.setFriendlyName("attrDef");
	attrDef.setNamespace("urn:perun:member:attribute-def:opt");
	attrDef.setType(String.class.getName());

	attributesManager.createAttribute(sess, attrDef);
	// store attr definition in DB

	AttributeDefinition retAttrDef = attributesManager.getAttributeDefinitionById(sess, attrDef.getId());
	assertNotNull("unable to get attribute definition",retAttrDef);
	assertTrue("returned wrong attr definition",retAttrDef.getName().equals(attrDef.getName()));

}

@Test (expected=AttributeNotExistsException.class)
	public void getAttributeDefinitionByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getAttributesDefinitionByIdWhenAttributeNotExists");

		attributesManager.getAttributeDefinitionById(sess, 0);
		// shouldn't find attribute definition

	}

@Test
public void getAttributesDefinitionByNamespace() throws Exception {
	System.out.println("attributesManager.getAttributesDefinitionByNamespace");

	List<AttributeDefinition> attrDef = attributesManager.getAttributesDefinitionByNamespace(sess, "urn:perun:member:attribute-def:core");
	assertNotNull("unable to get attributes definition",attrDef);
	assertTrue("there should be some attributes definition",attrDef.size() > 0);

}










// ==============  5.  GET ATTRIBUTE BY ID ================================




@Test
public void getFacilityAttributeById() throws Exception {
	System.out.println("attributesManager.getFacilityAttributeById");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();
	attributesManager.setAttributes(sess, facility, attributes);

	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, facility, id);
	assertNotNull("unable to get facility attribute by id",retAttr);
	assertEquals("returned attribute is not same as we stored",retAttr,attributes.get(0));

}

@Test (expected=FacilityNotExistsException.class)
	public void getFacilityAttributeByIdWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeByIdWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Facility(), id);
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void getFacilityAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeByIdWhenAttributeNotExists");

		facility = setUpFacility();

		attributesManager.getAttributeById(sess, facility, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getFacilityAttributeByIdWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, facility, id);
		// shouldn't return facility attribute when ID belong to different type of attribute

	}

@Test
public void getVoAttributeById() throws Exception {
	System.out.println("attributesManager.getVoAttributeById");

	vo = setUpVo();
	attributes = setUpVoAttribute();
	attributesManager.setAttributes(sess, vo, attributes);

	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, vo, id);
	assertNotNull("unable to get vo attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=VoNotExistsException.class)
	public void getVoAttributeByIdWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.getVoAttributeByIdWhenVoNotExists");

		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Vo(), id);
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void getVoAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getVoAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();

		attributesManager.getAttributeById(sess, vo, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getVoAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getVoAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, vo, id);
		// shouldn't return vo attribute when ID belong to different type of attribute

	}

@Test
public void getResourceAttributeById() throws Exception {
	System.out.println("attributesManager.getResourceAttributeById");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();
	attributesManager.setAttributes(sess, resource, attributes);

	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, resource, id);
	assertNotNull("unable to get resource attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceAttributeByIdWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceAttributeByIdWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Resource(), id);
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void getResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, resource, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getResourceAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, resource, id);
		// shouldn't return resource attribute when ID belong to different type of attribute

	}

@Test
public void getMemberResourceAttributeById() throws Exception {
	System.out.println("attributesManager.getMemberResourceAttributeById");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpMemberResourceAttribute();
	attributesManager.setAttributes(sess, resource, member, attributes);

	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, resource, member, id);
	assertNotNull("unable to get resource member attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=ResourceNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeByIdWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Resource(), member, id);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeByIdWhenMemberNotExists");

		vo = setUpVo();
		facility= setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess,resource, new Member(), id);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, resource, member, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberResourceAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, resource, member, id);
		// shouldn't return member resource attribute when ID belong to different type of attribute

	}

@Test
public void getMemberAttributeById() throws Exception {
	System.out.println("attributesManager.getMemberAttributeById");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttributes(sess, member, attributes);
	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, member, id);
	assertNotNull("unable to get member attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=MemberNotExistsException.class)
	public void getMemberAttributeByIdWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Member(), id);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void getMemberAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttributeById(sess, member, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, id);
		// shouldn't return member attribute when ID belong to different type of attribute

	}

@Test
public void getFacilityUserAttributeById() throws Exception {
	System.out.println("attributesManager.getFacilityUserAttributeById");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityUserAttribute();
	attributesManager.setAttributes(sess, facility, user, attributes);
	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, facility, user, id);
	assertNotNull("unable to get facility-user attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=FacilityNotExistsException.class)
	public void getFacilityUserAttributeByIdWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeByIdWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityUserAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Facility(), user, id);
		// shouldn't find facility
	}

@Test (expected=UserNotExistsException.class)
	public void getFacilityUserAttributeByIdWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeByIdWhenUserNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, facility, new User(), id);
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void getFacilityUserAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributeById(sess, facility, user, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityUserAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getFacilityUserAttributeByIdWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, facility, user, id);
		// shouldn't return facility-user attribute for member attribute id

	}

@Test
public void getUserAttributeById() throws Exception {
	System.out.println("attributesManager.getUserAttributeById");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributesManager.setAttributes(sess, user, attributes);
	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, user, id);
	assertNotNull("unable to get user attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=UserNotExistsException.class)
	public void getUserAttributeByIdWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getUserAttributeByIdWhenMemberNotExists");

		attributes = setUpUserAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new User(), id);
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void getUserAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributeById(sess, user, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, user, id);
		// shouldn't return user attribute when ID belong to different type of attribute

	}

// TODO - není implementace pro getAttributeById(sess, group, id)
// až bude metoda potřeba doplní se i test

@Test
public void getGroupResourceAttributeById() throws Exception {
	System.out.println("attributesManager.getGroupResourceAttributeById");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpGroupResourceAttribute();
	attributesManager.setAttributes(sess, resource, group, attributes);
	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, resource, group, id);
	assertNotNull("unable to get user attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=GroupNotExistsException.class)
	public void getGroupResourceAttributeByIdWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeByIdWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, resource, new Group(), id);
		// shouldn't find group

	}

@Test (expected=ResourceNotExistsException.class)
	public void getGroupResourceAttributeByIdWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeByIdWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Resource(), group, id);
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void getGroupResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getGroupResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, resource, group, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getMemberAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, resource, group, id);
		// shouldn't return group_resource attribute when ID belong to different type of attribute

	}

@Test
public void getHostAttributeById() throws Exception {
	System.out.println("attributesManager.getHostAttributeById");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttributes(sess, host, attributes);
	int id = attributes.get(0).getId();

	Attribute retAttr = attributesManager.getAttributeById(sess, host, id);
	assertNotNull("unable to get host attribute by id",retAttr);
	assertEquals("returned attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=HostNotExistsException.class)
	public void getHostAttributeByIdWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.getHostAttributeByIdWhenHostNotExists");

		attributes = setUpHostAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Host(), id);
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void getHostAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.getHostAttributeByIdWhenAttributeNotExists");

		host = setUpHost().get(0);

		attributesManager.getAttributeById(sess, host, 0);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void getHostAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.getHostAttributeByIdWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, host, id);
		// shouldn't return host attribute when ID belong to different type of attribute

	}








// ==============  6. SET ATTRIBUTE ================================


@Test
public void setFacilityAttribute() throws Exception {
	System.out.println("attributesManager.setFacilityAttribute");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();

	attributesManager.setAttribute(sess, facility, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:opt:facility_test_attribute");
	assertNotNull("unable to get facility attribute by name",retAttr);
	assertEquals("returned facility attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=FacilityNotExistsException.class)
	public void setFacilityAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityAttributeWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();

		attributesManager.setAttribute(sess, new Facility(), attributes.get(0));
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void setFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't add vo attribute into facility

	}

@Test (expected=InternalErrorException.class)
	public void setFacilityAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setFacilityAttributeWhenTypeMismatch");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setFacilityUserAttribute() throws Exception {
	System.out.println("attributesManager.setFacilityUserAttribute");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	attributes = setUpFacilityUserAttribute();
	User user = perun.getUsersManager().getUserByMember(sess, member);

	attributesManager.setAttribute(sess, facility, user, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:opt:user_facility_test_attribute");
	assertNotNull("unable to get facility-user attribute by name",retAttr);
	assertEquals("returned facility-user attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=FacilityNotExistsException.class)
	public void setFacilityUserAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityUserAttributeWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityUserAttribute();

		attributesManager.setAttribute(sess, new Facility(), user, attributes.get(0));
		// shouldn't find facility

	}

@Test (expected=UserNotExistsException.class)
	public void setFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityUserAttributeWhenUserNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();

		attributesManager.setAttribute(sess, facility, new User(), attributes.get(0));
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void setFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setFacilityUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, facility, user, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setFacilityUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setFacilityUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		facility = setUpFacility();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, facility, user, attributes.get(0));
		// shouldn't add vo attribute into facility

	}

@Test (expected=InternalErrorException.class)
	public void setFacilityUserAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setFacilityUserAttributeWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, facility, user, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setVoAttribute() throws Exception {
	System.out.println("attributesManager.setVoAttribute");

	vo = setUpVo();
	attributes = setUpVoAttribute();

	attributesManager.setAttribute(sess, vo, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:opt:vo_test_attribute");
	assertNotNull("unable to get vo attribute by name",retAttr);
	assertEquals("returned vo attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=VoNotExistsException.class)
	public void setVoAttributeWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.setVoAttributeWhenVoNotExists");

		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, new Vo(), attributes.get(0));
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void setVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setVoAttributeWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't add facility attribute into vo

	}

@Test (expected=InternalErrorException.class)
	public void setVoAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setVoAttributeWhenTypeMismatch");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setResourceAttribute() throws Exception {
	System.out.println("attributesManager.setResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();

	attributesManager.setAttribute(sess, resource, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:opt:resource_test_attribute");
	assertNotNull("unable to get resource attribute by name",retAttr);
	assertEquals("returned resource attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=ResourceNotExistsException.class)
	public void setResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setResourceAttributeWhenResourceNotExists");

		attributes = setUpResourceAttribute();

		attributesManager.setAttribute(sess, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void setResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, resource, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, resource, attributes.get(0));
		// shouldn't add vo attribute into resource

	}

@Test (expected=InternalErrorException.class)
	public void setResourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, resource, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setMemberResourceAttribute() throws Exception {
	System.out.println("attributesManager.setMemberResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	member = setUpMember();
	attributes = setUpMemberResourceAttribute();

	attributesManager.setAttribute(sess, resource, member, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, resource, member, "urn:perun:member_resource:attribute-def:opt:member_resource_test_attribute");
	assertNotNull("unable to get member-resource attribute by name",retAttr);
	assertEquals("returned member-resource attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=ResourceNotExistsException.class)
	public void setMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttribute(sess, new Resource(), member, attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void setMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributeWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttribute(sess, resource, new Member(), attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void setMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, resource, member, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, resource, member, attributes.get(0));
		// shouldn't add vo attribute into member-resource

	}

@Test (expected=InternalErrorException.class)
	public void setMemberResourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setMemberResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, resource, member, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setMemberAttribute() throws Exception {
	System.out.println("attributesManager.setMemberAttribute");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();

	attributesManager.setAttribute(sess, member, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:opt:member_test_attribute");
	assertNotNull("unable to get member attribute by name",retAttr);
	assertEquals("returned member attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=MemberNotExistsException.class)
	public void setMemberAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.setMemberAttributeWhenMemberNotExists");

		attributes = setUpMemberAttribute();

		attributesManager.setAttribute(sess, new Member(), attributes.get(0));
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void setMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setMemberAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, member, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, member, attributes.get(0));
		// shouldn't add vo attribute into member

	}

@Test (expected=InternalErrorException.class)
	public void setMemberAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setMemberAttributeWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, member, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setUserAttribute() throws Exception {
	System.out.println("attributesManager.setUserAttribute");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpUserAttribute();

	attributesManager.setAttribute(sess, user, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:opt:user_test_attribute");
	assertNotNull("unable to get user attribute by name",retAttr);
	assertEquals("returned user attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=UserNotExistsException.class)
	public void setUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributeWhenUserNotExists");

		attributes = setUpUserAttribute();

		attributesManager.setAttribute(sess, new User(), attributes.get(0));
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, user, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, user, attributes.get(0));
		// shouldn't add vo attribute into user

	}

@Test (expected=InternalErrorException.class)
	public void setUserAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setUserAttributeWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, user, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setGroupAttribute() throws Exception {
	System.out.println("attributesManager.setGroupAttribute");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();

	attributesManager.setAttribute(sess, group, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:group_test_attribute");
	assertNotNull("unable to get group attribute by name",retAttr);
	assertEquals("returned group attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=GroupNotExistsException.class)
	public void setGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.setGroupAttributeWhenGroupNotExists");

		attributes = setUpGroupAttribute();

		attributesManager.setAttribute(sess, new Group(), attributes.get(0));
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void setGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, group, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, group, attributes.get(0));
		// shouldn't add vo attribute into group

	}

@Test (expected=InternalErrorException.class)
	public void setGroupAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, group, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setGroupResourceAttribute() throws Exception {
	System.out.println("attributesManager.setGroupResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	attributes = setUpGroupResourceAttribute();

	attributesManager.setAttribute(sess, resource, group, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, resource, group, "urn:perun:group_resource:attribute-def:opt:group_resource_test_attribute");
	assertNotNull("unable to get group-resource attribute by name",retAttr);
	assertEquals("returned group-resource attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=ResourceNotExistsException.class)
	public void setGroupResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttribute(sess, new Resource(), group, attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void setGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributeWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttribute(sess, resource, new Group(), attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void setGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, resource, group, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setGroupResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, resource, group, attributes.get(0));
		// shouldn't add vo attribute into group-resource

	}

@Test (expected=InternalErrorException.class)
	public void setGroupResourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setGroupResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, resource, group, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

@Test
public void setHostAttribute() throws Exception {
	System.out.println("attributesManager.setHostAttribute");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();

	attributesManager.setAttribute(sess, host, attributes.get(0));

	Attribute retAttr = attributesManager.getAttribute(sess, host, "urn:perun:host:attribute-def:opt:host_test_attribute");
	assertNotNull("unable to get host attribute by name",retAttr);
	assertEquals("returned host attribute is not same as stored",retAttr,attributes.get(0));

}

@Test (expected=HostNotExistsException.class)
	public void setHostAttributeWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.setHostAttributeWhenHostNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttribute(sess, new Host(), attributes.get(0));
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void setHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.setHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void setHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.setHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't add vo attribute into host

	}

@Test (expected=InternalErrorException.class)
	public void setHostAttributeWhenTypeMismatch() throws Exception {
		System.out.println("attributesManager.setHostAttributeWhenTypeMismatch");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}










// ==============  7. CREATE ATTRIBUTE / DELETE ATTRIBUTE ================================



@Test
public void createAttribute() throws Exception {
	System.out.println("attributesManager.createAttribute");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setFriendlyName("attr_def_facility_tests_attr");
	attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
	attrDef.setDescription("poznamka");
	attrDef.setType(String.class.getName());
	// create attr definition
	attributesManager.createAttribute(sess, attrDef);
	// store attr def in DB acording namespace

	AttributeDefinition retAttrDef = attributesManager.getAttributeDefinition(sess, "urn:perun:facility:attribute-def:opt:attr_def_facility_tests_attr");

	assertNotNull("unable to get attr definition by name",retAttrDef);
	assertEquals("returned attr definition is not same as stored",attrDef,retAttrDef);

}

@Test (expected=AttributeExistsException.class)
	public void createAttributeWhenAttributeExists() throws Exception {
		System.out.println("attributesManager.createAttributeWhenAttributeExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_def_facility_tests_attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		// create attr definition
		attributesManager.createAttribute(sess, attrDef);
		// store attr def in DB acording namespace
		attributesManager.createAttribute(sess, attrDef);
		// shouldn't add attr def twice

	}

@Test (expected=AttributeNotExistsException.class)
	public void deleteAttribute() throws Exception {
		System.out.println("attributesManager.deleteAttribute");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_def_facility_tests_attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		assertNotNull("unable to create attribute before deletion",attributesManager.createAttribute(sess, attrDef));

		attributesManager.deleteAttribute(sess, attrDef);

		attributesManager.getAttributeDefinition(sess, "urn:perun:facility:attribute-def:opt:attr_def_facility_tests_attr");
		// shouldn't find attribute definition in db

	}

@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.deleteAttributeWhenAttributeNotExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_def_facility_tests_attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());

		attributesManager.deleteAttribute(sess, attrDef);
		// shouldn't find attribute

	}

	@Ignore
	@Test (expected=RelationExistsException.class)
	public void deleteAttributeWhenRelationExists() throws Exception {
		System.out.println("attributesManager.deleteAttributeWhenRelationExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// setting particular attribute to facility to make relation
		attributesManager.deleteAttribute(sess, attributes.get(0));
		// shouldn't delete assigned attribute
		// FIXME nevytváří Relation - v delete attribute chybí část pro service required attributes
		// výchozí je použití force delete i pro klasický delete

	}

// FIXME - deleteAttributeForce - not yet implemented
	@Ignore
	@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeForce() throws Exception {
		System.out.println("attributesManager.deleteAttributeForce");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_def_facility_tests_attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		//
		attributesManager.createAttribute(sess, attrDef);
		assertNotNull("unable to create attribute before deletion",attributesManager.createAttribute(sess, attrDef));

		attributesManager.deleteAttribute(sess, attrDef, true);

		attributesManager.getAttributeDefinition(sess, "urn:perun:facility:attribute-def:opt:attr_def_facility_tests_attr");
		// shouldn't find attribute definition in db

	}

@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeForceWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.deleteAttributeForceWhenAttributeNotExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_def_facility_tests_attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());

		attributesManager.deleteAttribute(sess, attrDef, true);
		// shouldn't find attribute

	}

// FIXME - deleteAttributeForce - not yet implemented
	@Ignore
	@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeForceWhenRelationExists() throws Exception {
		System.out.println("attributesManager.deleteAttributeForceWhenRelationExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// setting particular attribute to facility to make relation
		attributesManager.deleteAttribute(sess, attributes.get(0), true);
		// delete assigned attribute
		attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:opt:facility_test_attribute");
		// shouldn't find attribute because force deleted

	}


























// ==============  8. GET REQUIRED ATTRIBUTES ================================

@Test
public void getRequiredFacilityAttributesForItsServices() throws Exception {
	System.out.println("attributesManager.getRequiredFacilityAttributesForItsServices");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);
	attributesManager.setAttribute(sess, facility, attributes.get(0));

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, facility);
	assertNotNull("unable to get required facility attributes for its services",reqAttr);
	assertTrue("should have only 1 req facility attribute",reqAttr.size() == 1);

}

@Test
public void setRequiredAttributesForMemberResourceFacilityUser() throws Exception {
	System.out.println("attributesManager.setRequiredAttributesForMemberResourceFacilityUser");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManagerBl().getUserByMember(sess, member);
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.addAll(setUpMemberAttribute());
	attributes.addAll(setUpUserAttribute());
	attributes.addAll(setUpMemberResourceAttribute());
	attributes.addAll(setUpFacilityUserAttribute());
	perun.getResourcesManager().assignService(sess, resource, service);

	perun.getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member);
}

@Test
public void getResourceRequiredGroupResourceAndGroupAttributesForItsServices() throws Exception {
	System.out.println("attributesManager.getRequiredFacilityAttributesForItsServices");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);
	for(Attribute a: attributes) {
		if(attributesManager.isFromNamespace(sess, a, AttributesManager.NS_GROUP_ATTR)) {
			attributesManager.setAttribute(sess, group, a);
		} else if(attributesManager.isFromNamespace(sess, a, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
			attributesManager.setAttribute(sess, resource, group, a);
		}
	}

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, resource, group, true);

	assertNotNull("unable to get required group_resource and group attributes for its services",reqAttr);
	assertTrue("should have only 2 req group_resource and group attributes",reqAttr.size() == 2);

}

@Test (expected=FacilityNotExistsException.class)
	public void getRequiredFacilityAttributesForItsServicesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredFacilityAttributesForItsServicesWhenFacilityNotExists");

		attributesManager.getRequiredAttributes(sess, new Facility());
		// shouldn't find facility

	}

@Test
public void getRequiredResourceAttributesForItsServices() throws Exception {
	System.out.println("attributesManager.getRequiredResourceAttributesForItsServices");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);
	attributesManager.setAttribute(sess, resource, attributes.get(3));

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, resource);
	assertNotNull("unable to get required resource attributes for its services",reqAttr);
	assertTrue("should have only 1 req resource attribute",reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getRequiredResourceAttributesForItsServicesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredResourceAttributesForItsServicesWhenResourceNotExists");

		attributesManager.getRequiredAttributes(sess, new Resource());
		// shouldn't find Resource

	}

@Test
public void getResourceRequiredMemberResourceAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredMemberResourceAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, resource, member);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("should have only 1 req member resource attribute",reqAttr.size() == 1);

}

@Test
public void getServiceRequiredResourceAttributes() throws Exception {
	System.out.println("attributesManager.getServiceRequiredResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);
	
	Service service2 = setUpService2();
	Attribute attr = setUpResourceRequiredAttributeForService(service2);
	perun.getResourcesManager().assignService(sess, resource, service2);
	
	List<Service> serviceList = new ArrayList<>();
	serviceList.add(service);
	
	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, serviceList, resource);
	assertNotNull("unable to get required resource attributes for its services",reqAttr);
	assertTrue("should have only 1 req resource attribute",reqAttr.size() == 1);
	
	serviceList.add(service2);
	reqAttr = attributesManager.getRequiredAttributes(sess, serviceList, resource);
	assertNotNull("unable to get required resource attributes for its services",reqAttr);
	assertTrue("should have only 1 req resource attribute",reqAttr.size() == 2);
}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), resource,  member);
		// shouldn't find resource

	}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWhenSecondResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWhenSecondResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Resource(),  member);
		// shouldn't find resource

	}

@Test
public void getResourceRequiredMemberResourceAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWhenFakeResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	Resource fakeResource = new Resource();
	fakeResource.setName("AttrManTestResource2");
	fakeResource.setDescription("fake resource");

	perun.getResourcesManager().createResource(sess, fakeResource, vo, facility);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, resource, member);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, fakeResource, member);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource and no value set",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, fakeResource, member);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Should return 1 attribute (but with no value)",reqAttr.size() == 1);

}


@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, resource, new Member());
		// shouldn't find member

	}

@Test
public void getResourceRequiredMemberResourceAttributesWorkWithUserAttributes() throws Exception {
	System.out.println("attributesManager.getRequiredMemberResourceAttributesWorkWithUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, resource, member, true);
	assertNotNull("unable to get required member resource (work with user) attributes for its services",reqAttr);
	assertTrue("should have more than 1 req attribute",reqAttr.size() >= 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWorkWithUserWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), resource, member, true);
		// shouldn't find resource

	}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenSecondResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWorkWithUserWhenSecondResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Resource(), member, true);
		// shouldn't find resource

	}

@Test
public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredMemberResourceAttributesWorkWithUserWhenFakeResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	Resource fakeResource = new Resource();
	fakeResource.setName("AttrManTestResource2");
	fakeResource.setDescription("fake resource");

	perun.getResourcesManager().createResource(sess, fakeResource, vo, facility);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, resource, member, true);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, fakeResource, member, true);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource and no value set",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, fakeResource, member, true);
	assertNotNull("unable to get required member resource attributes for its services",reqAttr);
	assertTrue("Should return 4 attributes (but with no value)",reqAttr.size() == 4);
	// member_resource, user_facility, user, member

}

@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesWorkWithUserWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, resource, new Member(), true);
		// shouldn't find member
	}

@Test
public void getResourceRequiredFacilityUserAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredFacilityUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);
	group = setUpGroup();
	perun.getResourcesManager().assignGroupToResource(sess, group, resource);
	perun.getGroupsManager().addMember(sess, group, member);

	User user = perun.getUsersManager().getUserByMember(sess, member);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, facility, user);
	assertNotNull("unable to get required facility user attributes for its services",reqAttr);
	assertTrue("should have only 1 req facility user attribute",reqAttr.size() == 1);

}

@Test (expected=FacilityNotExistsException.class)
	public void getResourceRequiredFacilityUserAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredFacilityUserAttributesWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getResourceRequiredAttributes(sess, resource, new Facility(), user);
		// shouldn't find Facility

	}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredFacilityUserAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredFacilityUserAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), facility, user);
		// shouldn't find resource

	}

@Test (expected=UserNotExistsException.class)
	public void getResourceRequiredFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredFacilityUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, facility, new User());
		// shouldn't find user

	}

@Test
public void getResourceRequiredFacilityUserAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredFacilityUserAttributesWhenFakeResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	Resource fakeResource = new Resource();
	fakeResource.setName("AttrManTestResource2");
	fakeResource.setDescription("fake resource");

	perun.getResourcesManager().createResource(sess, fakeResource, vo, facility);

	User user = perun.getUsersManager().getUserByMember(sess, member);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, facility, user);
	assertNotNull("unable to get required facility user attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

}

@Test
public void getResourceRequiredMemberAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member);
	assertNotNull("Unable to get member required attributes for resource", reqAttr);
	assertTrue("There should be only one required attribute", reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredMemberAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member());
		// shouldn't find member

	}

@Test
public void getResourceRequiredMemberAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredMemberAttributesWhenFakeResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	Resource fakeResource = setUpResource(); // without service

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member);
	assertNotNull("unable to get required member attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

}

@Test
public void getResourceRequiredUserAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	User user = perun.getUsersManager().getUserByMember(sess, member);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, user);
	assertNotNull("Unable to get user required attributes for resource", reqAttr);
	assertTrue("There should be only one required attribute", reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredUserAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredUserAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), user);
		// shouldn't find resource

	}

@Test (expected=UserNotExistsException.class)
	public void getResourceRequiredUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new User());
		// shouldn't find member

	}

@Test
public void getResourceRequiredUserAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredUserAttributesWhenFakeResource");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	Resource fakeResource = setUpResource(); // without service

	User user = perun.getUsersManager().getUserByMember(sess, member);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, user);
	assertNotNull("unable to get required user attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

}

@Test
public void getResourceRequiredGroupResourceAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredGroupResourceAttributes");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, resource, group);
	assertNotNull("unable to get required group resource attributes for its services",reqAttr);
	assertTrue("should have only 1 req group resource attribute",reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), resource,  group);
		// shouldn't find resource

	}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredGroupResourceAttributesWhenSecondResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredGroupResourceAttributesWhenSecondResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Resource(),  group);
		// shouldn't find resource

	}

@Test
public void getResourceRequiredGroupResourceAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredGroupResourceAttributesWhenFakeResource");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	Resource fakeResource = new Resource();
	fakeResource.setName("AttrManTestResource2");
	fakeResource.setDescription("fake resource");

	perun.getResourcesManager().createResource(sess, fakeResource, vo, facility);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, resource, group);
	assertNotNull("unable to get required group resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, fakeResource, group);
	assertNotNull("unable to get required group resource attributes for its services",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource and no value set",reqAttr.size() == 0);

	reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, fakeResource, group);
	assertNotNull("unable to get required group resource attributes for its services",reqAttr);
	assertTrue("Should return 1 attribute (but with no value)",reqAttr.size() == 1);

}


@Test (expected=GroupNotExistsException.class)
	public void getResourceRequiredGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, resource, new Group());
		// shouldn't find group

	}

@Test
public void getResourceRequiredGroupAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredGroupAttributes");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, group);
	assertNotNull("Unable to get group required attributes for resource", reqAttr);
	assertTrue("There should be only one required attribute", reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredGroupAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredGroupAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void getResourceRequiredGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredGroupAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Group());
		// shouldn't find group

	}

@Test
public void getResourceRequiredGroupAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredGroupAttributesWhenFakeResource");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	Resource fakeResource = setUpResource(); // without service

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, group);
	assertNotNull("unable to get required group attributes for resource",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

}

@Test
public void getResourceRequiredHostAttributes() throws Exception {
	System.out.println("attributesManager.getResourceRequiredHostAttributes");

	vo = setUpVo();
	host = setUpHost().get(0); // also creates Facility

	// create resource
	Resource resource = new Resource();
	resource.setName("AttrTestResource");
	perun.getResourcesManager().createResource(sess, resource, vo, facility);

	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, host);
	assertNotNull("Unable to get host required attributes for resource", reqAttr);
	assertTrue("There should be only one required attribute", reqAttr.size() == 1);

}

@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredHostAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredHostAttributesWhenResourceNotExists");

		host = setUpHost().get(0);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), host);
		// shouldn't find resource

	}

@Test (expected=HostNotExistsException.class)
	public void getResourceRequiredHostAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getResourceRequiredHostAttributesWhenHostNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Host());
		// shouldn't find host

	}

@Test
public void getResourceRequiredHostAttributesWhenFakeResource() throws Exception {
	System.out.println("attributesManager.getResourceRequiredHostAttributesWhenFakeResource");

	host = setUpHost().get(0); // also creates cluster type facility
	vo = setUpVo();
	Resource fakeResource = setUpResource(); // without service

	List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, host);
	assertNotNull("unable to get required host attributes for resource",reqAttr);
	assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

}

// TODO - doplnit testy na:
/*
 * v API chybí varinata group_resource work with group attributes:
 *
 * getResourceRequiredAttributes(sess, resource, resource, group, boolean);
 *
 */

@Test
public void getRequiredAttributesDefinition() throws Exception {
	System.out.println("attributesManager.getRequiredAttributesDefinition");

	service = setUpService();
	attributes = setUpRequiredAttributes();

	List<AttributeDefinition> reqAttr = attributesManager.getRequiredAttributesDefinition(sess, service);
	assertNotNull("unable to get required services attribute definition",reqAttr);
	assertTrue("should have at least 7 req attribute definitions",reqAttr.size() >= 7);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredAttributesDefinitionWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredAttributesDefinitionWhenServiceNotExists");

		attributesManager.getRequiredAttributesDefinition(sess, new Service());
		// shouldn't find service

	}

@Test
public void getRequiredFacilityAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredFacilityAttributesFromOneService");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, facility);
	assertNotNull("unable to get required facility attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredFacilityAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredFacilityAttributesFromOneServiceWhenServiceNotExists");

		attributesManager.getRequiredAttributes(sess, new Service(), facility);
		// shouldn't find service

	}

@Test (expected=FacilityNotExistsException.class)
	public void getRequiredFacilityAttributesFromOneServiceWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredFacilityAttributesFromOneServiceWhenFacilityNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Facility());
		// shouldn't find service

	}

@Test
public void getRequiredResourceAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredResourceAttributesFromOneService");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource);
	assertNotNull("unable to get required resource attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredResourceAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, new Service(), resource);
		// shouldn't find service

	}

@Test (expected=ResourceNotExistsException.class)
	public void getRequiredResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Resource());
		// shouldn't find resource

	}

@Test
public void getRequiredMemberResourceAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneService");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource, member);
	assertNotNull("unable to get required resource-member attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test
public void getRequiredMembersResourceAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredMembersResourceAttributesFromOneService");

	vo = setUpVo();
	member = setUpMember();
	List<Member> members = new ArrayList<>();
	members.add(member);
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	group = setUpGroup(vo, member);
	resourcesManager.assignGroupToResource(sess, group, resource);
	perun.getResourcesManager().assignService(sess, resource, service);

	HashMap<Member, List<Attribute>> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource, members);
	assertNotNull("unable to get required resource-member attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute", reqAttr.size() == 1);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), resource, member);
		// shouldn't find service

	}

@Test (expected=ResourceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, service, new Resource(), member);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWhenMemberNotExists");


		vo = setUpVo();
		member = setUpMember();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, resource, new Member());
		// shouldn't find member

	}

@Test
public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUser() throws Exception {
	System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWorkWithUser");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource, member, true);
	assertNotNull("unable to get required resource-member attributes for one service",reqAttr);
	assertTrue("should have at least 4 req attribute",reqAttr.size() >= 4);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), resource, member, true);
		// shouldn't find service

	}

@Test (expected=ResourceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, service, new Resource(), member, true);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenMemberNotExists");


		vo = setUpVo();
		member = setUpMember();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, resource, new Member(), true);
		// shouldn't find member

	}

	@Test
	public void getRequiredMembersAttributesFromOneService() throws Exception {
		System.out.println("attributesManager.getRequiredMemberAttributesFromOneService");

		vo = setUpVo();
		member = setUpMember();
		List<Member> members = new ArrayList<>();
		members.add(member);
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		group = setUpGroup(vo, member);
		resourcesManager.assignGroupToResource(sess, group, resource);
		perun.getResourcesManager().assignService(sess, resource, service);

		HashMap<Member, List<Attribute>> reqAttr = attributesManager.getRequiredAttributes(sess, resource, service, members);
		assertNotNull("unable to get required member attributes for one service",reqAttr);
		assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

	}

@Test
public void getRequiredMemberAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredMemberAttributesFromOneService");

	vo = setUpVo();
	member = setUpMember();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, member);
	assertNotNull("unable to get required member attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test
public void getRequiredMemberAndUserAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredMemberAndUserAttributesFromOneService");

	vo = setUpVo();
	member = setUpMember();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();

	this.setUpMemberToResource();

	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, member, true);
	assertNotNull("unable to get required member attributes for one service",reqAttr);
	assertEquals("getRequiredAtributes(sess, member, true) returns wrong count of attributes", 2, reqAttr.size());


}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredMemberAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), member);
		// shouldn't find service

	}

@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberAttributesFromOneServiceWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredMemberAttributesFromOneServiceWhenMemberNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Member());
		// shouldn't find member

	}

@Test
public void getRequiredGroupResourceAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredGroupResourceAttributesFromOneService");

	vo = setUpVo();
	group = setUpGroup();
	facility = setUpFacility();
	resource = setUpResource();
	service = setUpService();
	attributes = setUpRequiredAttributes();
	perun.getResourcesManager().assignService(sess, resource, service);

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource,group);
	assertNotNull("unable to get required resource-group attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredGroupResourceAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredGroupResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, new Service(), resource, group);
		// shouldn't find service

	}

@Test (expected=ResourceNotExistsException.class)
	public void getRequiredGroupResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredGroupResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, service, new Resource(), member);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void getRequiredGroupResourceAttributesFromOneServiceWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredGroupResourceAttributesFromOneServiceWhenGroupNotExists");


		vo = setUpVo();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, resource, new Group());
		// shouldn't find group

	}

@Test
public void getRequiredGroupAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredGroupAttributesFromOneService");

	vo = setUpVo();
	group = setUpGroup();
	service = setUpService();
	attributes = setUpRequiredAttributes();

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, group);
	assertNotNull("unable to get required group attributes for one service",reqAttr);
	assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

}

@Test (expected=GroupNotExistsException.class)
	public void getRequiredGroupAttributesFromOneServiceWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredGroupAttributesFromOneServiceWhenGroupNotExists");

		service = setUpService();
		attributes = setUpRequiredAttributes();

		attributesManager.getRequiredAttributes(sess, service, new Group());
		// shouldn't find group

	}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredGroupAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredGroupAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, new Service(), group);
		// shouldn't find service

	}

// TODO - není metoda na získání pouze req. user atributů z 1 service

@Test
public void getRequiredHostAttributesFromOneService() throws Exception {
	System.out.println("attributesManager.getRequiredHostAttributesFromOneService");

	host = setUpHost().get(0);  // also creates cluster type facility
	service = setUpService();
	attributes = setUpRequiredAttributes();

	List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, host);
	assertNotNull("Unable to get required host attributes for one service",reqAttr);
	assertTrue("There should be 1 required host attribute",reqAttr.size() == 1);

}

@Test (expected=ServiceNotExistsException.class)
	public void getRequiredHostAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredHostAttributesFromOneServiceWhenServiceNotExists");

		host = setUpHost().get(0);  // also creates cluster type facility

		attributesManager.getRequiredAttributes(sess, new Service(), host);
		// shouldn't find service

	}

@Test (expected=HostNotExistsException.class)
	public void getRequiredHostAttributesFromOneServiceWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.getRequiredHostAttributesFromOneServiceWhenHostNotExists");

		host = setUpHost().get(0);  // also creates cluster type facility
		int id = host.getId();      // store ID
		host.setId(0);              // make host not existing in DB
		service = setUpService();
		attributes = setUpRequiredAttributes();
		try {
			attributesManager.getRequiredAttributes(sess, service, host);
			// shouldn't find service
		} catch (HostNotExistsException ex) {
			host.setId(id);
			throw ex;
		}
	}



// ==============  9. FILL ATTRIBUTE/S  ================================

// already tested in lower layer - package: cz.metacentrum.perun.core.impl.modules.attributes

// ==============  10. CHECK ATTRIBUTE VALUE  ================================

// already tested in lower layer - package: cz.metacentrum.perun.core.impl.modules.attributes





// ==============  11. REMOVE ATTRIBUTE/S / REMOVE ALL ATTRIBUTES ================================

@Test
public void removeFacilityAttribute() throws Exception {
	System.out.println("attributesManager.removeFacilityAttribute");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();
	attributesManager.setAttribute(sess, facility, attributes.get(0));
	// create facility and set attribute with value
	attributesManager.removeAttribute(sess, facility, attributes.get(0));
	// remove attribute from facility (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
	assertFalse("our facility shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=FacilityNotExistsException.class)
	public void removeFacilityAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributeWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, new Facility(), attributes.get(0));
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, facility, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, facility, attributes.get(0));
		// shouldn't find vo attribute on facility

	}

@Test
public void removeFacilityAttributes() throws Exception {
	System.out.println("attributesManager.removeFacilityAttributes");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();
	attributesManager.setAttribute(sess, facility, attributes.get(0));
	// create facility and set attribute with value
	attributesManager.removeAttributes(sess, facility, attributes);
	// remove attributes from facility (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
	assertFalse("our facility shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test
public void removeUserMemberResourceToMemberAndUserToFacilityAttributes() throws Exception {
	System.out.println("attributesManager.removeUserMemberResourceToMemberAndUserToFacilityAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = sess.getPerun().getUsersManager().getUserByMember(sess, member);
	resource = setUpResource();

	List<Attribute> attributes_user = setUpUserAttribute();
	List<Attribute> attributes_member = setUpMemberAttribute();
	List<Attribute> attributes_user_facility = setUpFacilityUserAttribute();
	List<Attribute> attributes_member_resource = setUpMemberResourceAttribute();

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.addAll(attributes_user);
	attributes.addAll(attributes_member);
	attributes.addAll(attributes_user_facility);
	attributes.addAll(attributes_member_resource);

	attributesManager.removeAttributes(sess, facility, resource, user, member, attributes);
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, resource, user, member);

	retAttr.retainAll(attributes);
	assertEquals("Excepted empty array list of Attributes.", new ArrayList<Attribute>(), retAttr);

}

@Test
public void removeEntitylessAttribute() throws Exception {
	System.out.println("attributesManager.removeEntitylessAttribute");
	attributes = setUpEntitylessAttribute();
	String key = "Test123456";
	attributesManager.setAttribute(sess, key, attributes.get(0));
	attributesManager.removeAttribute(sess, key, attributes.get(0));
	List<Attribute> retAttr = attributesManager.getAttributes(sess, key);
	assertFalse("There should not been set this entityless attribute, because it was removed.",retAttr.contains(attributes.get(0)));
}

@Test (expected=FacilityNotExistsException.class)
	public void removeFacilityAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributesWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, new Facility(), attributes);
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributesWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, facility, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeFacilityAttributesWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		attributesManager.removeAttributes(sess, facility, attributes);
		// shouldn't find vo attribute on facility

	}

@Test
public void removeAllGroupResourceAndGroupAttributes() throws Exception {
	System.out.println("attributesManager.removeAllGroupResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();

	attributes = setUpGroupResourceAttribute();
	attributes.addAll(setUpGroupAttribute());

	attributesManager.setAttributes(sess, resource, group, attributes, true);
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group, true);
	for(Attribute a: attributes) {
		assertTrue("our group or group and resource has set this attribute", retAttr.contains(a));
	}

	//remove all of them
	attributesManager.removeAllAttributes(sess, resource, group, true);
	retAttr = attributesManager.getAttributes(sess, resource, group, true);
	for(Attribute a: attributes) {
		assertFalse("our group or group and resource has not set this attribute", retAttr.contains(a));
	}
}

@Test
public void removeGroupResourceAndGroupAttributes() throws Exception {
	System.out.println("attributesManager.removeAllGroupResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();

	attributes = setUpGroupResourceAttribute();
	attributes.addAll(setUpGroupAttribute());

	attributesManager.setAttributes(sess, resource, group, attributes, true);
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group, true);
	for(Attribute a: attributes) {
		assertTrue("our group or group and resource has set this attribute", retAttr.contains(a));
	}

	//remove all of them
	attributesManager.removeAttributes(sess, resource, group, attributes, true);
	retAttr = attributesManager.getAttributes(sess, resource, group, true);
	for(Attribute a: attributes) {
		assertFalse("our group or group and resource has not set this attribute", retAttr.contains(a));
	}
}

@Test
public void removeAllFacilityAttributes() throws Exception {
	System.out.println("attributesManager.removeAllFacilityAttributes");

	facility = setUpFacility();
	attributes = setUpFacilityAttribute();
	attributesManager.setAttribute(sess, facility, attributes.get(0));
	// create facility and set attribute with value
	attributesManager.removeAllAttributes(sess, facility);
	// remove all attributes from facility (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
	assertFalse("our facility shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our facility should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:id")));

}

@Test
public void removeAllFacilityAttributesWithUserFacilityAttributes() throws Exception {
	System.out.println("attributesManager.removeAllFacilityAttributesExceptUserFacilityAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityAttribute();
	attributesManager.setAttribute(sess, facility, attributes.get(0));
	attributes.addAll(setUpFacilityUserAttribute());
	attributesManager.setAttribute(sess, facility, user, attributes.get(1));
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
	retAttr.addAll(attributesManager.getAttributes(sess, facility, user));
	assertTrue("our facility should have set our facility attribute", retAttr.contains(attributes.get(0)));
	assertTrue("our facility should have set our user-facility attribute", retAttr.contains(attributes.get(1)));

	// remove all attributes from facility (definition or attribute)
	attributesManager.removeAllAttributes(sess, facility, true);
	retAttr.clear();
	retAttr.addAll(attributesManager.getAttributes(sess, facility));
	retAttr.addAll(attributesManager.getAttributes(sess, facility, user));
	assertFalse("our facility should not have set our facility attribute", retAttr.contains(attributes.get(0)));
	assertFalse("our facility should not have set our user-facility attribute", retAttr.contains(attributes.get(1)));
	assertTrue("our facility should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:id")));
}

@Test
public void removeAllFacilityAttributesWithoutUserFacilityAttributes() throws Exception {
	System.out.println("attributesManager.removeAllFacilityAttributesExceptUserFacilityAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityAttribute();
	attributesManager.setAttribute(sess, facility, attributes.get(0));
	attributes.addAll(setUpFacilityUserAttribute());
	attributesManager.setAttribute(sess, facility, user, attributes.get(1));
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);
	retAttr.addAll(attributesManager.getAttributes(sess, facility, user));
	assertTrue("our facility should have set our facility attribute", retAttr.contains(attributes.get(0)));
	assertTrue("our facility should have set our user-facility attribute", retAttr.contains(attributes.get(1)));

	// remove all attributes from facility (definition or attribute)
	attributesManager.removeAllAttributes(sess, facility, false);
	retAttr.clear();
	retAttr.addAll(attributesManager.getAttributes(sess, facility));
	retAttr.addAll(attributesManager.getAttributes(sess, facility, user));
	assertFalse("our facility should not have set our facility attribute", retAttr.contains(attributes.get(0)));
	assertTrue("our facility should not have set our user-facility attribute", retAttr.contains(attributes.get(1)));
	assertTrue("our facility should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:id")));
}

@Test (expected=FacilityNotExistsException.class)
	public void removeAllFacilityAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeAllFacilityAttributesWhenFacilityNotExists");

		attributesManager.removeAllAttributes(sess, new Facility());
		// shouldn't find facility

	}

@Test
public void removeVoAttribute() throws Exception {
	System.out.println("attributesManager.removeVoAttribute");

	vo = setUpVo();
	attributes = setUpVoAttribute();
	attributesManager.setAttribute(sess, vo, attributes.get(0));
	// create vo and set attribute with value
	attributesManager.removeAttribute(sess, vo, attributes.get(0));
	// remove attribute from vo (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
	assertFalse("our vo shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=VoNotExistsException.class)
	public void removeVoAttributeWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.removeVoAttributeWhenVoNotExists");

		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, new Vo(), attributes.get(0));
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeVoAttributeWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, vo, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, vo, attributes.get(0));
		// shouldn't find facility attribute on vo

	}

@Test
public void removeVoAttributes() throws Exception {
	System.out.println("attributesManager.removeVoAttributes");

	vo = setUpVo();
	attributes = setUpVoAttribute();
	attributesManager.setAttribute(sess, vo, attributes.get(0));
	// create vo and set attribute with value
	attributesManager.removeAttributes(sess, vo, attributes);
	// remove attributes from vo (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
	assertFalse("our vo shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=VoNotExistsException.class)
	public void removeVoAttributesWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.removeVoAttributesWhenVoNotExists");

		attributes = setUpVoAttribute();
		attributesManager.removeAttributes(sess, new Vo(), attributes);
		// shouldn't find vo

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeVoAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeVoAttributesWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, vo, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeVoAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeVoAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, vo, attributes);
		// shouldn't find facility attribute on vo

	}

@Test
public void removeAllVoAttributes() throws Exception {
	System.out.println("attributesManager.removeAllVoAttributes");

	vo = setUpVo();
	attributes = setUpVoAttribute();
	attributesManager.setAttribute(sess, vo, attributes.get(0));
	// create vo and set attribute with value
	attributesManager.removeAllAttributes(sess, vo);
	// remove all attributes from vo (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
	assertFalse("our vo shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our vo should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:core:id")));

}

@Test (expected=VoNotExistsException.class)
	public void removeAllVoAttributesWhenVoNotExists() throws Exception {
		System.out.println("attributesManager.removeAllVoAttributesWhenVoNotExists");

		attributesManager.removeAllAttributes(sess, new Vo());
		// shouldn't find vo

	}

@Test
public void removeResourceAttribute() throws Exception {
	System.out.println("attributesManager.removeResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();
	attributesManager.setAttribute(sess, resource, attributes.get(0));
	// create resource and set attribute with value
	attributesManager.removeAttribute(sess, resource, attributes.get(0));
	// remove attribute from resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
	assertFalse("our resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeResourceAttributeWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		attributesManager.removeAttribute(sess, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, resource, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, resource, attributes.get(0));
		// shouldn't find facility attribute on resource

	}

@Test
public void removeResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();
	attributesManager.setAttribute(sess, resource, attributes.get(0));
	// create resource and set attribute with value
	attributesManager.removeAttributes(sess, resource, attributes);
	// remove attributes from resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
	assertFalse("our resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeResourceAttributesWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		attributesManager.removeAttributes(sess, new Resource(), attributes);
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, resource, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeResourceAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeResourceAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, resource, attributes);
		// shouldn't find facility attribute on resource

	}

@Test
public void removeAllResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeAllResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	attributes = setUpResourceAttribute();
	attributesManager.setAttribute(sess, resource, attributes.get(0));
	// create resource and set attribute with value
	attributesManager.removeAllAttributes(sess, resource);
	// remove all attributes from resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
	assertFalse("our resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our resource should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:core:id")));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeAllResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeAllResourceAttributesWhenResourceNotExists");

		attributesManager.removeAllAttributes(sess, new Resource());
		// shouldn't find resource

	}

@Test
public void removeMemberResourceAttribute() throws Exception {
	System.out.println("attributesManager.removeMemberResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	member = setUpMember();
	attributes = setUpMemberResourceAttribute();
	attributesManager.setAttribute(sess, resource, member, attributes.get(0));
	// create member-resource and set attribute with value
	attributesManager.removeAttribute(sess, resource, member, attributes.get(0));
	// remove attribute from member-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member);
	assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributeWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttribute(sess, new Resource(), member, attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void removeMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributeWhenMemberNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttribute(sess, resource, new Member(), attributes.get(0));
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, resource, member, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, resource, member, attributes.get(0));
		// shouldn't find facility attribute on member-resource

	}

@Test
public void removeMemberResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeMemberResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	member = setUpMember();
	attributes = setUpMemberResourceAttribute();
	attributesManager.setAttribute(sess, resource, member, attributes.get(0));
	// create member-resource and set attribute with value
	attributesManager.removeAttributes(sess, resource, member, attributes);
	// remove attributes from member-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member);
	assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributesWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttributes(sess, new Resource(), member, attributes);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void removeMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributesWhenMemberNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttributes(sess, resource, new Member(), attributes);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeMemberResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, resource, member, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberResourceAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeMemberResourceAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, resource, member, attributes);
		// shouldn't find facility attribute on member-resource

	}

@Test
public void removeAllMemberResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeAllMemberResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	member = setUpMember();
	attributes = setUpMemberResourceAttribute();
	attributesManager.setAttribute(sess, resource, member, attributes.get(0));
	// create member-resource and set attribute with value
	attributesManager.removeAllAttributes(sess, resource, member);
	// remove all attributes from member-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, member);
	assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	// member-resource don't have core attributes ??

}

@Test (expected=ResourceNotExistsException.class)
	public void removeAllMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeAllMemberResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.removeAllAttributes(sess, new Resource(), member);
		// shouldn't find resource

	}

@Test (expected=MemberNotExistsException.class)
	public void removeAllMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeAllMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.removeAllAttributes(sess, resource, new Member());
		// shouldn't find member

	}

@Test
public void removeMemberAttribute() throws Exception {
	System.out.println("attributesManager.removeMemberAttribute");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));
	// create member and set attribute with value
	attributesManager.removeAttribute(sess, member, attributes.get(0));
	// remove attribute from member (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	assertFalse("our member shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=MemberNotExistsException.class)
	public void removeMemberAttributeWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberAttributeWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		attributesManager.removeAttribute(sess, new Member(), attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, member, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, member, attributes.get(0));
		// shouldn't find facility attribute on member

	}

@Test
public void removeMemberAttributesWorkWithUserAttributes() throws Exception {
	System.out.println("attributesManager.removeMemberAttributesWorkWithUserAttributes");
	vo = setUpVo();
	member = setUpMember();
	User user = sess.getPerun().getUsersManager().getUserByMember(sess, member);
	attributes = setUpMemberAttribute();
	attributesManager.setAttributes(sess, member, attributes);
	List<Attribute> userAttrs = setUpUserAttribute();
	attributesManager.setAttributes(sess, user, userAttrs);
	attributes.addAll(userAttrs);
	attributesManager.removeAttributes(sess, member, true, attributes);

	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	retAttr.addAll(attributesManager.getAttributes(sess,user));
	for(Attribute attr:attributes){
		assertFalse("our member and user (who we getted from this member) shouldn't have set our attribute",retAttr.contains(attr));
	}

}

@Test
public void removeMemberAttributesWorkWithoutUserAtributes() throws Exception {
	System.out.println("attributesManager.removeMemberAttributesWorkWithoutUserAtributes");
	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttributes(sess, member, attributes);
	attributesManager.removeAttributes(sess, member, false, attributes);
	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	for(Attribute attr:attributes){
		assertFalse("our member shouldn't have set our attribute",retAttr.contains(attr));
	}
}

@Test
public void removeMemberAttributes() throws Exception {
	System.out.println("attributesManager.removeMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));
	// create member and set attribute with value
	attributesManager.removeAttributes(sess, member, attributes);
	// remove attributes from member (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	assertFalse("our member shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=MemberNotExistsException.class)
	public void removeMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberAttributesWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		attributesManager.removeAttributes(sess, new Member(), attributes);
		// shouldn't find member

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeMemberAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeMemberAttributesWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, member, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeMemberAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, member, attributes);
		// shouldn't find facility attribute on member

	}

@Test
public void removeAllMemberAttributes() throws Exception {
	System.out.println("attributesManager.removeAllMemberAttributes");

	vo = setUpVo();
	member = setUpMember();
	attributes = setUpMemberAttribute();
	attributesManager.setAttribute(sess, member, attributes.get(0));
	// create member and set attribute with value
	attributesManager.removeAllAttributes(sess, member);
	// remove all attributes from member (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
	assertFalse("our member shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our member should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:core:id")));

}

@Test (expected=MemberNotExistsException.class)
	public void removeAllMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println("attributesManager.removeAllMemberAttributesWhenMemberNotExists");

		attributesManager.removeAllAttributes(sess, new Member());
		// shouldn't find member

	}

@Test
public void removeFacilityUserAttribute() throws Exception {
	System.out.println("attributesManager.removeFacilityUserAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityUserAttribute();
	attributesManager.setAttributes(sess, facility, user, attributes);
	// create facility-user and set attribute with value
	attributesManager.removeAttribute(sess, facility, user, attributes.get(0));
	// remove attribute from facility-user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, user);
	assertFalse("our facility-user shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=FacilityNotExistsException.class)
	public void removeFacilityUserAttributeWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserResourceAttributeWhenFacilityNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.removeAttribute(sess, new Facility(), user, attributes.get(0));
		// shouldn't find facility

	}

@Test (expected=UserNotExistsException.class)
	public void removeFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributeWhenUserNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		attributesManager.removeAttribute(sess, facility, new User(), attributes.get(0));
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityUserAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, facility, user, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, facility, user, attributes.get(0));
		// shouldn't find vo attribute on facility-user

	}

@Test
public void removeFacilityUserAttributes() throws Exception {
	System.out.println("attributesManager.removeFacilityUserAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityUserAttribute();
	attributesManager.setAttributes(sess, facility, user, attributes);
	// create facility user and set attribute with value
	attributesManager.removeAttributes(sess, facility, user, attributes);
	// remove attributes from facility user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, user);
	assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=FacilityNotExistsException.class)
	public void removeFacilityUserAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributesWhenFacilityNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.removeAttributes(sess, new Facility(), user, attributes);
		// shouldn't find facility

	}

@Test (expected=UserNotExistsException.class)
	public void removeFacilityUserResourceAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributesWhenUserNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		attributesManager.removeAttributes(sess, facility, new User(), attributes);
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityUserAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, facility, user, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityUserAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeFacilityUserAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpVoAttribute();
		attributesManager.removeAttributes(sess, facility, user, attributes);
		// shouldn't find vo attribute on facility-user

	}

@Test
public void removeAllFacilityUserAttributes() throws Exception {
	System.out.println("attributesManager.removeAllFacilityUserAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpFacilityUserAttribute();
	attributesManager.setAttributes(sess, facility, user, attributes);
	// create facility user and set attribute with value
	attributesManager.removeAllAttributes(sess, facility, user);
	// remove all attributes from facility user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, user);
	assertFalse("our facility-user shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	// facility-user don't have core attributes ??

}

@Test (expected=FacilityNotExistsException.class)
	public void removeAllFacilityUserAttributesWhenFacilityNotExists() throws Exception {
		System.out.println("attributesManager.removeAllFacilityUserAttributesWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.removeAllAttributes(sess, new Facility(), user);
		// shouldn't find facility

	}

@Test (expected=UserNotExistsException.class)
	public void removeAllFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeAllFacilityUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();

		attributesManager.removeAllAttributes(sess, facility, new User());
		// shouldn't find user

	}

@Test
public void removeUserAttribute() throws Exception {
	System.out.println("attributesManager.removeUserAttribute");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpUserAttribute();
	attributesManager.setAttributes(sess, user, attributes);
	// create user and set attribute with value
	attributesManager.removeAttribute(sess, user, attributes.get(0));
	// remove attribute from user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertFalse("our user shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=UserNotExistsException.class)
	public void removeUserAttributeWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeUserAttributeWhenUserNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttribute(sess, new User(), attributes.get(0));
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, user, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, user, attributes.get(0));
		// shouldn't find facility attribute on user

	}

@Test
public void removeUserAttributes() throws Exception {
	System.out.println("attributesManager.removeUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpUserAttribute();
	attributesManager.setAttributes(sess, user, attributes);
	// create user and set attribute with value
	attributesManager.removeAttributes(sess, user, attributes);
	// remove attributes from user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertFalse("our user shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=UserNotExistsException.class)
	public void removeUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeUserAttributesWhenUserNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttributes(sess, new User(), attributes);
		// shouldn't find user

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeUserAttributesWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, user, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeUserAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeUserAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, user, attributes);
		// shouldn't find facility attribute on user

	}

@Test
public void removeAllUserAttributes() throws Exception {
	System.out.println("attributesManager.removeAllUserAttributes");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);
	attributes = setUpUserAttribute();
	attributesManager.setAttributes(sess, user, attributes);
	// create user and set attribute with value
	attributesManager.removeAllAttributes(sess, user);
	// remove all attributes from user (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
	assertFalse("our user shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our user should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:core:id")));

}

@Test (expected=UserNotExistsException.class)
	public void removeAllUserAttributesWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.removeAllUserAttributesWhenUserNotExists");

		attributesManager.removeAllAttributes(sess, new User());
		// shouldn't find user

	}

@Test
public void removeGroupAttribute() throws Exception {
	System.out.println("attributesManager.removeGroupAttribute");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();
	attributesManager.setAttribute(sess, group, attributes.get(0));
	// create group and set attribute with value
	attributesManager.removeAttribute(sess, group, attributes.get(0));
	// remove attribute from group (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
	assertFalse("our group shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=GroupNotExistsException.class)
	public void removeGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupAttributeWhenGroupNotExists");

		attributes = setUpGroupAttribute();
		attributesManager.removeAttribute(sess, new Group(), attributes.get(0));
		// shouldn't find facility

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, group, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, group, attributes.get(0));
		// shouldn't find vo attribute on group

	}

@Test
public void removeGroupAttributes() throws Exception {
	System.out.println("attributesManager.removeGroupAttributes");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();
	attributesManager.setAttributes(sess, group, attributes);
	// create group and set attribute with value
	attributesManager.removeAttributes(sess, group, attributes);
	// remove attributes from group (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
	assertNotNull("unable to return group attributes",retAttr);
	assertFalse("our group shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=GroupNotExistsException.class)
	public void removeGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupAttributesWhenGroupNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttributes(sess, new Group(), attributes);
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, group, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeGroupAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, group, attributes);
		// shouldn't find facility attribute on group

	}

@Test
public void removeAllGroupAttributes() throws Exception {
	System.out.println("attributesManager.removeAllGroupAttributes");

	vo = setUpVo();
	group = setUpGroup();
	attributes = setUpGroupAttribute();
	attributesManager.setAttributes(sess, group, attributes);
	// create group and set attribute with value
	attributesManager.removeAllAttributes(sess, group);
	// remove all attributes from group (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
	assertFalse("our group shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	// there are no core attributes

}

@Test (expected=GroupNotExistsException.class)
	public void removeAllGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeAllGroupAttributesWhenGroupNotExists");

		attributesManager.removeAllAttributes(sess, new Group());
		// shouldn't find group

	}

@Test
public void removeGroupResourceAttribute() throws Exception {
	System.out.println("attributesManager.removeGroupResourceAttribute");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	attributes = setUpGroupResourceAttribute();
	attributesManager.setAttribute(sess, resource, group, attributes.get(0));
	// create group-resource and set attribute with value
	attributesManager.removeAttribute(sess, resource, group, attributes.get(0));
	// remove attribute from group-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
	assertFalse("our group-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeGroupResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributeWhenResourceNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttribute(sess, new Resource(), group, attributes.get(0));
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void removeGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributeWhenGroupNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttribute(sess, resource, new Group(), attributes.get(0));
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, resource, group, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, resource, group, attributes.get(0));
		// shouldn't find facility attribute on group-resource

	}

@Test
public void removeGroupResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeGroupResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	attributes = setUpGroupResourceAttribute();
	attributesManager.setAttribute(sess, resource, group, attributes.get(0));
	// create group-resource and set attribute with value
	attributesManager.removeAttributes(sess, resource, group, attributes);
	// remove attributes from group-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
	assertFalse("our group-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=ResourceNotExistsException.class)
	public void removeGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributesWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttributes(sess, new Resource(), group, attributes);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void removeGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributesWhenGroupNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttributes(sess, resource, new Group(), attributes);
		// shouldn't find group

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeGroupResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, resource, group, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupResourceAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeGroupResourceAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, resource, group, attributes);
		// shouldn't find facility attribute on group-resource

	}

@Test
public void removeAllGroupResourceAttributes() throws Exception {
	System.out.println("attributesManager.removeAllGroupResourceAttributes");

	vo = setUpVo();
	facility = setUpFacility();
	resource = setUpResource();
	group = setUpGroup();
	attributes = setUpGroupResourceAttribute();
	attributesManager.setAttribute(sess, resource, group, attributes.get(0));
	// create group-resource and set attribute with value
	attributesManager.removeAllAttributes(sess, resource, group);
	// remove all attributes from member-resource (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
	assertFalse("our group-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	// group-resource don't have core attributes ??

}

@Test (expected=ResourceNotExistsException.class)
	public void removeAllGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println("attributesManager.removeAllGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.removeAllAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

@Test (expected=GroupNotExistsException.class)
	public void removeAllGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println("attributesManager.removeAllGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.removeAllAttributes(sess, resource, new Group());
		// shouldn't find group

	}

@Test
public void removeHostAttribute() throws Exception {
	System.out.println("attributesManager.removeHostAttribute");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttribute(sess, host, attributes.get(0));
	// create host and set attribute with value
	attributesManager.removeAttribute(sess, host, attributes.get(0));
	// remove attribute from vo (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
	assertFalse("our host shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=HostNotExistsException.class)
	public void removeHostAttributeWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.removeHostAttributeWhenHostNotExists");

		attributes = setUpHostAttribute();
		attributesManager.removeAttribute(sess, new Host(), attributes.get(0));
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, host, attributes.get(0));
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, host, attributes.get(0));
		// shouldn't find facility attribute on host

	}

@Test
public void removeHostAttributes() throws Exception {
	System.out.println("attributesManager.removeHostAttributes");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttribute(sess, host, attributes.get(0));
	// create host and set attribute with value
	attributesManager.removeAttributes(sess, host, attributes);
	// remove attributes from host (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
	assertFalse("our host shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

}

@Test (expected=HostNotExistsException.class)
	public void removeHostAttributesWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.removeHostAttributesWhenHostNotExists");

		attributes = setUpHostAttribute();
		attributesManager.removeAttributes(sess, new Host(), attributes);
		// shouldn't find host

	}

@Test (expected=AttributeNotExistsException.class)
	public void removeHostAttributesWhenAttributeNotExists() throws Exception {
		System.out.println("attributesManager.removeHostAttributesWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, host, attributes);
		// shouldn't find attribute

	}

@Test (expected=WrongAttributeAssignmentException.class)
	public void removeHostAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println("attributesManager.removeHostAttributesWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, host, attributes);
		// shouldn't find facility attribute on host

	}

@Test
public void removeAllHostAttributes() throws Exception {
	System.out.println("attributesManager.removeAllHostAttributes");

	host = setUpHost().get(0);
	attributes = setUpHostAttribute();
	attributesManager.setAttribute(sess, host, attributes.get(0));
	// create host and set attribute with value
	attributesManager.removeAllAttributes(sess, host);
	// remove all attributes from host (definition or attribute)
	List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
	assertFalse("our host shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
	assertTrue("our host should still have core attribute",retAttr.contains(attributesManager.getAttribute(sess, host, "urn:perun:host:attribute-def:core:id")));

}

@Test (expected=HostNotExistsException.class)
	public void removeAllHostAttributesWhenHostNotExists() throws Exception {
		System.out.println("attributesManager.removeAllHostAttributesWhenHostNotExists");

		attributesManager.removeAllAttributes(sess, new Host());
		// shouldn't find host

	}











// ==============  12. REST CHECK METHODS ================================




@Test
public void isCoreAttribute() throws Exception {
	System.out.println("attributesManager.isCoreAttribute");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setFriendlyName("attr_manager_test_attribute");
	attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
	attrDef.setType(String.class.getName());
	attrDef.setDescription("AttributesManagerTest");

	assertFalse("opt attribute is considered core!!",attributesManager.isCoreAttribute(sess, attrDef));
	attrDef.setNamespace("urn:perun:facility:attribute-def:core");
	assertTrue("core attribute is not considered core!!",attributesManager.isCoreAttribute(sess, attrDef));

}


@Test
public void isOptAttribute() throws Exception {
	System.out.println("attributesManager.isOptAttribute");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setFriendlyName("attr_manager_test_attribute");
	attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
	attrDef.setType(String.class.getName());
	attrDef.setDescription("AttributesManagerTest");

	assertTrue("opt attribute is not considered opt!!",attributesManager.isOptAttribute(sess, attrDef));
	attrDef.setNamespace("urn:perun:facility:attribute-def:core");
	assertFalse("core attribute is considered opt!!",attributesManager.isOptAttribute(sess, attrDef));

}

@Ignore
@Test
public void isCoreManagedAttribute() throws Exception {
	System.out.println("attributesManager.isCoreManagedAttribute");
	// TODO co je míněno core managed attributem ??
	/*
		 AttributeDefinition attrDef = new AttributeDefinition();
		 attrDef.setFriendlyName("attr_manager_test_attribute");
		 attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		 attrDef.setType(String.class.getName());
		 attrDef.setDescription("AttributesManagerTest");

		 assertTrue("opt attribute is not considered opt!!",attributesManager.isCoreAttribute(sess, attrDef));
		 attrDef.setNamespace("urn:perun:facility:attribute-def:core");
		 assertFalse("core attribute is considered opt!!",attributesManager.isCoreAttribute(sess, attrDef));
		 */
}


@Test
public void isFromNamespace() throws Exception {
	System.out.println("attributesManager.isFromNamespace");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setFriendlyName("attr_manager_test_attribute");
	attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
	attrDef.setType(String.class.getName());
	attrDef.setDescription("AttributesManagerTest");

	assertTrue("bad recognition of attribute namespace",attributesManager.isFromNamespace(sess, attrDef, "urn:perun:facility:attribute-def:opt"));
	attrDef.setNamespace("urn:perun:facility:attribute-def:core");
	assertFalse("bad recognition of attribute namespace",attributesManager.isFromNamespace(sess, attrDef, "urn:perun:facility:attribute-def:opt"));

}


@Test (expected=WrongAttributeAssignmentException.class)
	public void checkNamespace() throws Exception {
		System.out.println("attributesManager.checkNamespace");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr_manager_test_attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		attributesManager.checkNamespace(sess, attrDef, "urn:perun:facility:attribute-def:core");
		// should throw exception - wrong attr assignment

	}

@Test
public void checkNamespaceList() throws Exception {
	System.out.println("attributesManager.checkNamespaceList");

	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setFriendlyName("attr_manager_test_attribute");
	attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
	attrDef.setType(String.class.getName());
	attrDef.setDescription("AttributesManagerTest");

	Attribute attribute = new Attribute(attrDef);

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attribute);

	attributesManager.checkNamespace(sess, attributes, "urn:perun:facility:attribute-def:opt");

}

@Test
public void getNamespaceFromAttributeName() throws Exception {
	System.out.println("attributesManager.getNamespaceFromAttributeName");

	String nameSpace = attributesManager.getNamespaceFromAttributeName("urn:perun:facility:attribute-def:opt:attr_manager_test_attribute");
	assertTrue("get wrong namespace from name",nameSpace.equals("urn:perun:facility:attribute-def:opt"));

}

@Test
public void getFriendlyNameFromAttributeName() throws Exception {
	System.out.println("attributesManager.getFriendlyNameFromAttributeName");

	String nameSpace = attributesManager.getFriendlyNameFromAttributeName("urn:perun:facility:attribute-def:opt:attr_manager_test_attribute");
	assertTrue("get wrong namespace from name",nameSpace.equals("attr_manager_test_attribute"));

}

@Test
public void getLogins() throws Exception {
	System.out.println("attributesManager.getLogins");

	vo = setUpVo();
	member = setUpMember();
	User user = perun.getUsersManager().getUserByMember(sess, member);

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:user:attribute-def:opt");
	attr.setFriendlyName("login-namespace:META:login");
	// je to správná syntaxe pro loginy ??
	attr.setType(String.class.getName());
	attr.setValue("UserLoginNamespaceAttribute");

	assertNotNull("unable to create login namespace attribute",attributesManager.createAttribute(sess, attr));

	attributesManager.setAttribute(sess, user, attr);

	List<Attribute> attributes = attributesManager.getLogins(sess, user);

	assertTrue("user should have 1 login-namespace attribute",attributes.size()>=1);
	assertTrue("our attribute should be returned",attributes.contains(attr));

}

@Test (expected=UserNotExistsException.class)
	public void getLoginsWhenUserNotExists() throws Exception {
		System.out.println("attributesManager.getLoginsWhenUserNotExists");

		attributesManager.getLogins(sess, new User());

	}

@Test
public void getAttributeRights() throws Exception {
	System.out.println("attributesManager.getAttributeRights");

	// setting rights
	List<ActionType> listOfActions = new ArrayList<ActionType>();
	listOfActions.add(ActionType.WRITE);
	listOfActions.add(ActionType.READ);
	List<AttributeRights> rights = new ArrayList<AttributeRights>();
	rights.add(new AttributeRights(1, Role.VOADMIN, listOfActions));
	rights.add(new AttributeRights(1, Role.SELF, new ArrayList<ActionType>()));
	perun.getAttributesManager().setAttributeRights(sess, rights);

	// getting rights
	rights.clear();
	rights = perun.getAttributesManager().getAttributeRights(sess, 1);
	assertTrue("list of rights should have 4 items for each role", rights.size() == 4);
	for (AttributeRights attributeRights : rights) {
		if (attributeRights.getRole().equals(Role.VOADMIN)) {
			assertTrue("our attribute 1 should have right READ for VOADMIN", attributeRights.getRights().contains(ActionType.READ));
			assertTrue("our attribute 1 should have right WRITE for VOADMIN", attributeRights.getRights().contains(ActionType.WRITE));
		}
		if (attributeRights.getRole().equals(Role.SELF)) {
			assertTrue("our attribute 1 should not have rights for SELF", attributeRights.getRights().isEmpty());
		}
	}
}

@Test
public void setAttributeRights() throws Exception {
	System.out.println("attributesManager.setAttributeRights");
	List<ActionType> listOfActions = new ArrayList<ActionType>();
	listOfActions.add(ActionType.WRITE);
	listOfActions.add(ActionType.READ);
	List<AttributeRights> rights = new ArrayList<AttributeRights>();
	rights.add(new AttributeRights(1, Role.VOADMIN, listOfActions));
	listOfActions.clear();
	listOfActions.add(ActionType.READ);
	rights.add(new AttributeRights(1, Role.SELF, listOfActions));
	perun.getAttributesManager().setAttributeRights(sess, rights);

	listOfActions.clear();
	rights.clear();
	listOfActions.add(ActionType.WRITE);
	rights.add(new AttributeRights(1, Role.VOADMIN, new ArrayList<ActionType>()));
	rights.add(new AttributeRights(1, Role.SELF, listOfActions));
	perun.getAttributesManager().setAttributeRights(sess, rights);

	rights.clear();
	rights = perun.getAttributesManager().getAttributeRights(sess, 1);

	for (AttributeRights attributeRights : rights) {
		if (attributeRights.getRole().equals(Role.SELF)) {
			assertTrue("our attribute 1 should not have right READ for VOADMIN", !(attributeRights.getRights().contains(ActionType.READ)));
			assertTrue("our attribute 1 should have right WRITE for VOADMIN", attributeRights.getRights().contains(ActionType.WRITE));
		}
		if (attributeRights.getRole().equals(Role.VOADMIN)) {
			assertTrue("our attribute 1 should not have rights for VOADMIN", attributeRights.getRights().isEmpty());
		}
	}
}













// PRIVATE METHODS ----------------------------------------------

private Vo setUpVo() throws Exception {

	Vo vo = new Vo();
	vo.setName("AttributesMangerTestVo");
	vo.setShortName("AMTVO");
	assertNotNull("unable to create VO",perun.getVosManager().createVo(sess, vo));
	return vo;

}

private Member setUpMember() throws Exception {

	String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
	String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
	String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

	Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
	candidate.setFirstName(userFirstName);
	candidate.setId(0);
	candidate.setMiddleName("");
	candidate.setLastName(userLastName);
	candidate.setTitleBefore("");
	candidate.setTitleAfter("");
	UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
	candidate.setUserExtSource(userExtSource);
	candidate.setAttributes(new HashMap<String,String>());

	Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
	assertNotNull("No member created", member);
	usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
	// save user for deletion after test
	return member;
}

private Facility setUpFacility() throws Exception {

	facility = new Facility();
	facility.setName("AttributesManagerTestFacility");
	assertNotNull(perun.getFacilitiesManager().createFacility(sess, facility));
	return facility;

}

private Resource setUpResource() throws Exception {

	Resource resource = new Resource();
	resource.setName("AttributesManagerTestResource");
	resource.setDescription("testing resource");
	assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

	return resource;

}

private Service setUpService() throws Exception {

	Service service = new Service();
	service.setName("AttributesManagerTestService");

	Owner owner = new Owner();
	owner.setName("AttrManagerTestServiceOwner");
	owner.setContact("AttrManagTestContact");
	owner.setType(OwnerType.technical);

	perun.getOwnersManager().createOwner(sess, owner);
	perun.getServicesManager().createService(sess, service, owner);

	return service;

}

private Service setUpService2() throws Exception {
	Service service = new Service();
	service.setName("AttributesManagerTestService2");

	Owner owner = new Owner();
	owner.setName("AttrManagerTestServiceOwner2");
	owner.setContact("AttrManagTestContact2");
	owner.setType(OwnerType.technical);

	perun.getOwnersManager().createOwner(sess, owner);
	perun.getServicesManager().createService(sess, service, owner);

	return service;
}

private Group setUpGroup() throws Exception {

	Group group = perun.getGroupsManager().createGroup(sess, vo, new Group("AttrTestGroup","AttrTestGroupDescription"));
	assertNotNull("unable to create a group",group);
	return group;

}

private Group setUpGroup(Vo vo, Member member) throws Exception {

	Group group = new Group("ResourcesManagerTestGroup","");
	group = perun.getGroupsManager().createGroup(sess, vo, group);
	perun.getGroupsManager().addMember(sess, group, member);
	return group;

}

private void setUpMemberToResource() throws Exception {

	perun.getGroupsManager().addMember(sess, group, member);
	perun.getResourcesManager().assignGroupToResource(sess, group, resource);
}

private List<Host> setUpHost() throws Exception {

	Host host = new Host();
	host.setHostname("AttrTestHost");
	List<Host> hosts = new ArrayList<Host>();
	hosts.add(host);

	// create cluster type facility
	facility = new Facility();
	facility.setName("AttrTestFacility");
	facility = perun.getFacilitiesManager().createFacility(sess, facility);

	hosts = perun.getFacilitiesManager().addHosts(sess, hosts, facility);
	// save hosts for deletion after test
	hostsForDeletion.add(hosts.get(0));

	return hosts;

}

private List<Attribute> setUpRequiredAttributes() throws Exception {

	List<Attribute> attrList = new ArrayList<Attribute>();

	attrList.add(setUpFacilityAttribute().get(0));
	attrList.add(setUpVoAttribute().get(0));
	attrList.add(setUpFacilityUserAttribute().get(0));
	attrList.add(setUpResourceAttribute().get(0));
	attrList.add(setUpMemberAttribute().get(0));
	attrList.add(setUpMemberResourceAttribute().get(0));
	attrList.add(setUpUserAttribute().get(0));
	attrList.add(setUpHostAttribute().get(0));
	attrList.add(setUpGroupResourceAttribute().get(0));
	attrList.add(setUpGroupAttribute().get(0));

	perun.getServicesManager().addRequiredAttributes(sess, service, attrList);

	return attrList;

}

private Attribute setUpResourceRequiredAttributeForService(Service service) throws Exception {

	Attribute attribute = new Attribute();
	List<Attribute> listOfAttrs = new ArrayList<>();

	attribute.setNamespace("urn:perun:resource:attribute-def:opt");
	attribute.setFriendlyName("resource_test_attribute_2");
	attribute.setType(String.class.getName());
	attribute.setValue("ResourceAttribute");
	assertNotNull("unable to create resource attribute",attributesManager.createAttribute(sess, attribute));
	
	listOfAttrs.add(attribute);
	
	perun.getServicesManager().addRequiredAttributes(sess, service, listOfAttrs);

	return attribute;
}

private List<Attribute> setUpFacilityUserAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:user_facility:attribute-def:opt");
	attr.setFriendlyName("user_facility_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("UserFacilityAttribute");

	assertNotNull("unable to create user_facility attribute",attributesManager.createAttribute(sess, attr));
	// create new facility-user attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpFacilityAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:facility:attribute-def:opt");
	attr.setFriendlyName("facility_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("FacilityAttribute");
	assertNotNull("unable to create facility attribute",attributesManager.createAttribute(sess, attr));
	// create new facility attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpEntitylessAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
	attr.setFriendlyName("entityless_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("EntitylessAttribute");
	assertNotNull("unable to create facility attribute",attributesManager.createAttribute(sess, attr));
	//create new entityless attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it
	return attributes;
}

private List<Attribute> setUpVoAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:vo:attribute-def:opt");
	attr.setFriendlyName("vo_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("VoAttribute");
	assertNotNull("unable to create vo attribute",attributesManager.createAttribute(sess, attr));
	// create new vo attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpResourceAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:resource:attribute-def:opt");
	attr.setFriendlyName("resource_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("ResourceAttribute");
	assertNotNull("unable to create resource attribute",attributesManager.createAttribute(sess, attr));
	// create new resource attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpMemberResourceAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:member_resource:attribute-def:opt");
	attr.setFriendlyName("member_resource_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("MemberResourceAttribute");
	assertNotNull("unable to create member-resource attribute",attributesManager.createAttribute(sess, attr));
	// create new resource member attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpUserAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:user:attribute-def:opt");
	attr.setFriendlyName("user_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("UserAttribute");
	assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
	// create new resource member attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpUserLargeAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:user:attribute-def:opt");
	attr.setFriendlyName("user_large_test_attribute");
	attr.setType(LinkedHashMap.class.getName());
	Map<String, String> value = new LinkedHashMap<String, String>();
	value.put("UserLargeAttribute", "test value");
	attr.setValue(value);
	assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
	// create new resource member attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpResourceLargeAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:resource:attribute-def:opt");
	attr.setFriendlyName("resource_large_test_attribute");
	attr.setType(LinkedHashMap.class.getName());
	Map<String, String> value = new LinkedHashMap<String, String>();
	value.put("ResourceLargeAttribute", "test value");
	value.put("ResourceTestLargeAttr", "test value 2");
	attr.setValue(value);
	assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
	// create new resource member attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it
	return attributes;

}

private List<Attribute> setUpMemberAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:member:attribute-def:opt");
	attr.setFriendlyName("member_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("MemberAttribute");

	assertNotNull("unable to create member attribute",attributesManager.createAttribute(sess, attr));
	// create new resource member attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}


private List<Attribute> setUpGroupAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:group:attribute-def:opt");
	attr.setFriendlyName("group_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("GroupAttribute");

	assertNotNull("unable to create group attribute",attributesManager.createAttribute(sess, attr));
	// create new group attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpGroupAttributes() throws Exception {

	Attribute attr = new Attribute();
	String namespace = "group_test_uniqueattribute:specialNamespace";
	attr.setNamespace(AttributesManager.NS_GROUP_ATTR_OPT);
	attr.setFriendlyName(namespace + "1");
	attr.setType(String.class.getName());
	attr.setValue("GroupAttribute");

	List<Attribute> attributes = new ArrayList<Attribute>();
	assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr));
	attributes.add(attr);

	Attribute attr2 = new Attribute(attr);
	attr2.setFriendlyName(namespace + "2");
	attr2.setValue("next2");
	assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr2));
	attributes.add(attr2);

	Attribute attr3 = new Attribute(attr);
	attr3.setFriendlyName(namespace + "3");
	attr3.setValue("next3");
	assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr3));
	attributes.add(attr3);

	//And one attribute with other name
	Attribute attr4 = new Attribute(attr);
	attr4.setFriendlyName("group_test_uniqueEattribute:specialNamespace");
	attr4.setValue("next4");
	assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr4));

	//Attribute with null value
	Attribute attr5 = new Attribute(attr);
	attr5.setFriendlyName(namespace + "5");
	assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr5));
	attributes.add(attr5);


	return attributes;
}

private List<Attribute> setUpResourceAttributes() throws Exception {

	Attribute attr = new Attribute();
	String namespace = "resource_test_uniqueattribute:specialNamespace";
	attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_OPT);
	attr.setFriendlyName(namespace + "1");
	attr.setType(String.class.getName());
	attr.setValue("ResourceAttribute");

	List<Attribute> attributes = new ArrayList<Attribute>();
	assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attr));
	attributes.add(attr);

	Attribute attr2 = new Attribute(attr);
	attr2.setFriendlyName(namespace + "2");
	attr2.setValue("next2");
	assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attr2));
	attributes.add(attr2);

	Attribute attr3 = new Attribute(attr);
	attr3.setFriendlyName(namespace + "3");
	attr3.setValue("next3");
	assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attr3));
	attributes.add(attr3);

	//And one attribute with other name
	Attribute attr4 = new Attribute(attr);
	attr4.setFriendlyName("resource_test_uniqueEattribute:specialNamespace");
	attr4.setValue("next4");
	assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attr4));

	//Attribute with null value
	Attribute attr5 = new Attribute(attr);
	attr5.setFriendlyName(namespace + "5");
	assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attr5));
	attributes.add(attr5);

	return attributes;
}

private List<Attribute> setUpHostAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:host:attribute-def:opt");
	attr.setFriendlyName("host_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("HostAttribute");

	assertNotNull("unable to create host attribute",attributesManager.createAttribute(sess, attr));
	// create new host attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private List<Attribute> setUpEntitylessAttributeWithListValue() throws Exception {
	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:entityless:attribute-def:opt");
	attr.setFriendlyName("listEntitylessAttributeForTest");
	attr.setType(ArrayList.class.getName());
	List<String> listOfTestStrings = new ArrayList<>();
	listOfTestStrings.add("first");
	listOfTestStrings.add("second");
	attr.setValue(listOfTestStrings);
	assertNotNull("unable to create host attribute",attributesManager.createAttribute(sess, attr));
	
	List<Attribute> attributes = new ArrayList<>();
	attributes.add(attr);
	
	return attributes;
}

private List<Attribute> setUpEntitylessAttributeWithMapValue() throws Exception {
	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:entityless:attribute-def:opt");
	attr.setFriendlyName("mapEntitylessAttributeForTest");
	attr.setType(LinkedHashMap.class.getName());
	Map<String, String> mapOfTestStrings = new LinkedHashMap<>();
	mapOfTestStrings.put("G11", "20005");
	mapOfTestStrings.put("R27", "11113");
	mapOfTestStrings.put("N23658", "23658");
	attr.setValue(mapOfTestStrings);
	assertNotNull("unable to create host attribute",attributesManager.createAttribute(sess, attr));
	
	List<Attribute> attributes = new ArrayList<>();
	attributes.add(attr);
	
	return attributes;
}

private List<Attribute> setUpGroupResourceAttribute() throws Exception {

	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:group_resource:attribute-def:opt");
	attr.setFriendlyName("group_resource_test_attribute");
	attr.setType(String.class.getName());
	attr.setValue("GroupResourceAttribute");

	assertNotNull("unable to create Group_Resource attribute",attributesManager.createAttribute(sess, attr));
	// create new group resource attribute

	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(attr);
	// put attribute into list because setAttributes requires it

	return attributes;

}

private Attribute setUpSpecificMemberResourceAttribute(Member member, Resource resource) throws Exception {
	Attribute attr = new Attribute();
	attr.setNamespace("urn:perun:member_resource:attribute-def:opt");
	attr.setFriendlyName("specificMemberResourceAttributeForTest");
	attr.setType(String.class.getName());
	attr.setValue("test value");

	assertNotNull("unable to create specific memberResource attribute",attributesManager.createAttribute(sess, attr));

	return attr;
}

public Attribute setAttributeInNamespace(String namespace) throws Exception {
	AttributeDefinition attrDef = new AttributeDefinition();
	attrDef.setNamespace(namespace);
	attrDef.setDescription("Test attribute description");
	attrDef.setFriendlyName("testingAttribute");
	attrDef.setType(String.class.getName());
	attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
	Attribute attribute = new Attribute(attrDef);
	attribute.setValue("Testing value");
	return attribute;
}

private Map<AttributeDefinition, Set<AttributeDefinition>> getAllDependenciesMapForTesting() {
	Map<AttributeDefinition, Set<AttributeDefinition>> allDependenciesForTesting = new HashMap<AttributeDefinition, Set<AttributeDefinition>>();
	//Prepare every possible way to test Attribute with Attribute

	//TODO FILL THIS MAP FOR USING

	return allDependenciesForTesting;
}
}
