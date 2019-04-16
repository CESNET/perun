package cz.metacentrum.perun.core.entry;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.test.annotation.IfProfileValue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.api.AttributesManager.NS_MEMBER_ATTR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests of AttributesManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AttributesManagerEntryIntegrationTestAbstract extends AbstractPerunIntegrationTest {

	private final static Logger log = LoggerFactory.getLogger(AttributesManagerEntryIntegrationTestAbstract.class);

	private static String CLASS_NAME = "AttributesManager.";

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
	 * 13. private methods for attribute dependencies logic
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
	private UserExtSource userExtSource1;
	private UserExtSource userExtSource2;
	private UserExtSource userExtSource3;
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

	//for testing attribute dependencies logic (no. 13) - VOs
	private AttributeDefinition vo_toEmail_def;
	private AttributeDefinition vo_fromEmail_def;
	private Attribute vo1_toEmail_attribute;
	private Attribute vo2_toEmail_attribute;
	private Attribute vo2_fromEmail_attribute;
	private AttributesManagerBlImpl attributesManagerBl;

	//for testing attribute dependencies logic (no. 13) - USERs
	private AttributeDefinition user_phone_atr_def;
	private AttributeDefinition user_email_atr_def;
	private Attribute user1_phone_attribute;
	private Attribute user2_phone_attribute;
	private Attribute user2_email_attribute;
	private Attribute user3_email_attribute;

	//for testing attribute dependencies logic (no. 13) - MEMBERs
	private AttributeDefinition member_phone_atr_def;
	private AttributeDefinition member_email_atr_def;
	private Attribute member1OfUser1_phone_attribute;
	private Attribute member1OfUser2_phone_attribute;
	private Attribute member1OfUser2_mail_attribute;
	private Attribute member1OfUser3_mail_attribute;
	private Attribute member2OfUser1_phone_attribute;
	private Attribute member2OfUser2_phone_attribute;
	private Attribute member2OfUser2_mail_attribute;
	private Attribute member2OfUser3_mail_attribute;

	//for testing attribute dependencies logic (no. 13) - GROUPS
	private AttributeDefinition group_fromEmail_atr_def;
	private Attribute group1InVo1_email_atr;
	private Attribute group2InVo1_email_atr;
	private Attribute group1InVo2_email_atr;
	private Attribute group2InVo2_email_atr;
	private Attribute membersGroupOfVo1_email_atr;
	private Attribute membersGroupOfVo2_email_atr;

	//for testing attribute dependencies logic (no. 13) - RESOURCEs
	private AttributeDefinition resource_test_atr_def;
	private Attribute resource1InVo1_test_atr;
	private Attribute resource2InVo1_test_atr;
	private Attribute resource1InVo2_test_atr;
	private Attribute resource2InVo2_test_atr;

	//for testing attribute dependencies logic (no. 13) - FACILITIES
	private AttributeDefinition facility_test_atr_def;
	private Attribute facility1_test_atr;
	private Attribute facility2_test_atr;
	private Attribute facility3_test_atr;

	//for testing attribute dependencies logic (no. 13) - HOSTS
	private AttributeDefinition host_test_atr_def;
	private Attribute host1F1_test_atr;
	private Attribute host2F1_test_atr;
	private Attribute host1F2_test_atr;
	private Attribute host1F3_test_atr;
	private Attribute host2F2_test_atr;
	private Attribute host2F3_test_atr;

	//for testing attribute dependencies logic (no. 13) - UESs
	private AttributeDefinition ues_test_atr_def;
	private Attribute ues1_test_atr;
	private Attribute ues2_test_atr;
	private Attribute ues3_test_atr;
	private Attribute internal_ues_atr;

	//for testing attribute dependencies logic (no. 13) - GROUP-RESOURCEs
	private AttributeDefinition groupResource_test_atr_def;
	private Attribute group1VO1Res1VO1_test_attribute;
	private Attribute group2VO1Res1VO1_test_attribute;
	private Attribute group2VO1Res2VO1_test_attribute;
	private Attribute group1VO2Res1VO2_test_attribute;
	private Attribute group2VO2Res2VO2_test_attribute;
	private Attribute group2VO2Res1VO2_test_attribute;

	//for testing attribute dependencies logic (no. 13) - MEMBER-GROUPs
	private AttributeDefinition memberGroup_test_atr_def;
	private Attribute member1U1Group1Vo1_test_attribute;
	private Attribute member1U1Group2Vo1_test_attribute;
	private Attribute member2U1Group1Vo2_test_attribute;
	private Attribute member2U1Group2Vo2_test_attribute;
	private Attribute member1U2Group1Vo2_test_attribute;
	private Attribute member1U2Group2Vo2_test_attribute;
	private Attribute member2U2Group1Vo1_test_attribute;
	private Attribute member2U2Group2Vo1_test_attribute;
	private Attribute member1U3Group1Vo1_test_attribute;
	private Attribute member2U3Group2Vo2_test_attribute;

	//for testing attribute dependencies logic (no. 13) - MEMBER-RESOURCEs
	private AttributeDefinition memberResource_test_atr_def;
	private Attribute member1U1Res1Vo1_test_attribute;
	private Attribute member1U1Res2Vo1_test_attribute;
	private Attribute member1U2Res1Vo2_test_attribute;
	private Attribute member1U2Res2Vo2_test_attribute;
	private Attribute member1U3Res1Vo1_test_attribute;
	private Attribute member2U3Res1Vo2_test_attribute;
	private Attribute member2U3Res2Vo2_test_attribute;

	//for testing attribute dependencies logic (no. 13) - USER-FACILITYs
	private AttributeDefinition userFacility_test_atr_def;
	private Attribute user1Facility1_test_attribute;
	private Attribute user1Facility2_test_attribute;
	private Attribute user2Facility2_test_attribute;
	private Attribute user2Facility3_test_attribute;
	private Attribute user3Facility3_test_attribute;
	private Attribute user3Facility1_test_attribute;
	private Attribute user3Facility2_test_attribute;

	//for testing attribute dependencies logic (no. 13) - ENTITYLESS
	private AttributeDefinition entityless_test_atr_def;
	private Attribute entityless_test_attribute1;
	private Attribute entityless_test_attribute2;
	private Attribute entityless_test_attribute3;

	@Before
	public void setUp() throws Exception {

		attributesManager = perun.getAttributesManager();
		resourcesManager = perun.getResourcesManager();
		this.setUpWorld();

	}

	public void setClassName(String className) {
		CLASS_NAME = className;
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
		userExtSource1 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user1TestLogin");
		userExtSource2 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user2TestLogin");
		userExtSource3 = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), "user3TestLogin");
		can1.setUserExtSource(userExtSource1);
		can1.setAttributes(new HashMap<>());
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
		System.out.println(CLASS_NAME + "setGroupNameWillProduceSettingMoreThanOneGIDAtOnce");

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
		perun.getAttributesManagerBl().createAttribute(sess, g_gn_BBB_def);

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
		perun.getAttributesManagerBl().createAttribute(sess, r_gn_AAA_def);

		//create attribute group_name in namespace aaa
		AttributeDefinition r_gn_BBB_def = new AttributeDefinition();
		r_gn_BBB_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gn_BBB_def.setDescription("groupName in namespace BBB");
		r_gn_BBB_def.setFriendlyName("unixGroupName-namespace:" + namespaceBBB);
		r_gn_BBB_def.setType(String.class.getName());
		perun.getAttributesManagerBl().createAttribute(sess, r_gn_BBB_def);

		//create attribute gid in namespace aaa
		AttributeDefinition r_gid_AAA_def = new AttributeDefinition();
		r_gid_AAA_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gid_AAA_def.setDescription("gid in namespace AAA");
		r_gid_AAA_def.setFriendlyName("unixGID-namespace:" + namespaceAAA);
		r_gid_AAA_def.setType(Integer.class.getName());
		perun.getAttributesManagerBl().createAttribute(sess, r_gid_AAA_def);

		//create attribute gid in namespace bbb
		AttributeDefinition r_gid_BBB_def = new AttributeDefinition();
		r_gid_BBB_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		r_gid_BBB_def.setDescription("gid in namespace BBB");
		r_gid_BBB_def.setFriendlyName("unixGID-namespace:" + namespaceBBB);
		r_gid_BBB_def.setType(Integer.class.getName());
		perun.getAttributesManagerBl().createAttribute(sess, r_gid_BBB_def);

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

		//Create attribute def entityless gidRanges
		AttributeDefinition gidRangesAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges");

		//Create gidRanges for new namespace
		Attribute gidRangesAAA = new Attribute(gidRangesAttrDef);
		Attribute gidRangesBBB = new Attribute(gidRangesAttrDef);
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "10000");
		gidRangesAAA.setValue(gidRangesValue);
		gidRangesBBB.setValue(gidRangesValue);

		perun.getAttributesManagerBl().setAttribute(sess, namespaceAAA, gidRangesAAA);
		perun.getAttributesManagerBl().setAttribute(sess, namespaceBBB, gidRangesBBB);

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
		Service s1 = new Service(0, "testService01", null);
		s1 = perun.getServicesManagerBl().createService(sess, s1);
		perun.getResourcesManagerBl().assignService(sess, r1, s1);
		perun.getResourcesManagerBl().assignService(sess, r2, s1);

		//Create attribute virt facility gidRanges
		AttributeDefinition gidRangesVirtualAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_VIRT + ":GIDRanges");

		//create other required attributes and add them to the service
		AttributeDefinition g_v_gn = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:group_resource:attribute-def:virt:unixGroupName");
		AttributeDefinition g_v_gid = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:group_resource:attribute-def:virt:unixGID");
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, groupNameNamespaceForFacilitiesAttrDef);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, GIDNamespaceForFacilitiesAttrDef);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, gidRangesVirtualAttrDef);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, g_v_gn);
		perun.getServicesManagerBl().addRequiredAttribute(sess, s1, g_v_gid);

		//set group_name to group g1
		perun.getAttributesManagerBl().setAttribute(sess, g1, g_gn_AAA);

		Attribute groupGIDInAAA = perun.getAttributesManagerBl().getAttribute(sess, g1, g_gid_AAA_def.getName());
		Attribute groupGIDInBBB = perun.getAttributesManagerBl().getAttribute(sess, g1, g_gid_BBB_def.getName());

		assertEquals(100, groupGIDInAAA.getValue());
		assertEquals(100, groupGIDInBBB.getValue());
	}

	@Test
	public void setRequiredAttributesIfMemberAddedToSubGroup() throws Exception {
		System.out.println(CLASS_NAME + "setRequiredAttributesIfMemberAddedToSubGroup");

		vo = setUpVo();
		member = setUpMember();
		Group topGroup = perun.getGroupsManagerBl().createGroup(sess, vo, new Group("topGroup", ""));
		Group subGroup = perun.getGroupsManagerBl().createGroup(sess, topGroup, new Group("subGroup", ""));
		Group subSubGroup = perun.getGroupsManagerBl().createGroup(sess, subGroup, new Group("subSubGroup", ""));

		facility = setUpFacility();
		resource = setUpResource();
		perun.getResourcesManagerBl().assignGroupToResource(sess, topGroup, resource);
		service = setUpService();
		perun.getResourcesManagerBl().assignService(sess, resource, service);

		String namespace = "testing";

		Attribute userUidNamespace = new Attribute();
		userUidNamespace.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		userUidNamespace.setFriendlyName("uid-namespace:" + namespace);
		userUidNamespace.setType(Integer.class.getName());
		userUidNamespace.setDescription("Uid namespace.");
		userUidNamespace = new Attribute(perun.getAttributesManagerBl().createAttribute(sess, userUidNamespace));
		perun.getServicesManagerBl().addRequiredAttribute(sess, service, userUidNamespace);

		Attribute namespaceMaxUID = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID"));
		namespaceMaxUID.setValue(100);
		perun.getAttributesManagerBl().setAttribute(sess, namespace, namespaceMaxUID);

		Attribute namespaceMinUID = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minUID"));
		namespaceMinUID.setValue(1);
		perun.getAttributesManagerBl().setAttribute(sess, namespace, namespaceMinUID);

		Attribute facilityUIDNamespace = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace"));
		facilityUIDNamespace.setValue(namespace);
		perun.getAttributesManagerBl().setAttribute(sess, facility, facilityUIDNamespace);

		perun.getGroupsManagerBl().addMember(sess, subSubGroup, member);

		List<Member> membersOfTopGroup = perun.getGroupsManagerBl().getGroupMembers(sess, topGroup);
		List<Member> membersOfSubGroup = perun.getGroupsManagerBl().getGroupMembers(sess, subGroup);
		List<Member> membersOfSubSubGroup = perun.getGroupsManagerBl().getGroupMembers(sess, subSubGroup);

		assertTrue(membersOfTopGroup.contains(member));
		assertEquals(membersOfTopGroup.size(), 1);
		assertEquals(membersOfTopGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfSubGroup.contains(member));
		assertEquals(membersOfSubGroup.size(), 1);
		assertEquals(membersOfSubGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfSubSubGroup.contains(member));
		assertEquals(membersOfSubSubGroup.size(), 1);
		assertEquals(membersOfSubSubGroup.get(0).getMembershipType(), MembershipType.DIRECT);

		User ourUser = perun.getUsersManagerBl().getUserByMember(sess, member);
		Attribute automaticlySettedAttribute = perun.getAttributesManagerBl().getAttribute(sess, ourUser, userUidNamespace.getName());
		Integer value = (Integer) automaticlySettedAttribute.getValue();
		assertTrue(value == 1);
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndMember() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<>(resource1InVo1, member1OfUser1, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);
		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("the Vo is vo1", vo1.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndGroup() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResourceAndGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<>(resource1InVo1, group1InVo1, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);
		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo1", vo1.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUserAndFacility() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUserAndFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute<>(user2, facility2, null);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("return only 1 vo", listOfRichAttributes.size() == 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		assertTrue("the Vo is vo2", vo2.equals(listOfRichAttributes.get(0).getPrimaryHolder()));
		assertTrue("attribute in richAttribute is equals to our attribute", (listOfRichAttributes.get(0).getAttribute()).equals(attribute));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromGroup() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
		perun.getAttributesManagerBl().setAttribute(sess, vo1, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder(member2OfUser2);

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		assertTrue("Return no vo.", listOfRichAttributes.size() == 0);
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResource() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromResource");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromUser");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromHost");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		List<Vo> returnedVos = new ArrayList<>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo1", returnedVos.contains(vo1));
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromFacility() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		List<Vo> returnedVos = new ArrayList<>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
		assertTrue("returned vos not contains vo1", !returnedVos.contains(vo1));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetVosFromVo() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromVo");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetVosFromKey");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_VO_ATTR_DEF);
		perun.getAttributesManagerBl().setAttribute(sess, vo2, attribute);

		//Prepare richAttribute with holders (attribute is not needed but holders are needed)
		RichAttribute richAttr = new RichAttribute();
		richAttr.setPrimaryHolder("String");

		List<RichAttribute> listOfRichAttributes = perun.getAttributesManagerBl().getRichAttributesWithHoldersForAttributeDefinition(sess, new AttributeDefinition(attribute), richAttr);

		//Return facilities Administrator too if exists
		assertTrue("return at least 2 vos", listOfRichAttributes.size() > 1);
		assertTrue("primary holder is type of vo", listOfRichAttributes.get(0).getPrimaryHolder() instanceof Vo);
		assertTrue("secondary holder is null", listOfRichAttributes.get(0).getSecondaryHolder() == null);
		List<Vo> returnedVos = new ArrayList<>();
		for(RichAttribute ra: listOfRichAttributes) {
			returnedVos.add((Vo) ra.getPrimaryHolder());
		}
		assertTrue("returned vos contains vo2", returnedVos.contains(vo2));
		assertTrue("returned vos contains vo1", returnedVos.contains(vo1));
	}

	@Test
	public void getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndMember() throws Exception {
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndMember");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromResourceAndGroup");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
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
		System.out.println(CLASS_NAME + "getRichAttributesWithHoldersForAttributeDefinitionGetGroupFromUserAndFacility");
		//Prepare attribute, create it and set it with testing value
		Attribute attribute = setAttributeInNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
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
		List<Group> groups = new ArrayList<>();
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
		System.out.println(CLASS_NAME + "getAllSimilarAttributeNames");

		String name = "urn:perun:user:attribute-def:def:login-namespace";
		List<String> similarAttrNames = perun.getAttributesManagerBl().getAllSimilarAttributeNames(sess, name);
		assertFalse("returned no names", similarAttrNames.isEmpty());
	}

	@Test
	public void getFacilityAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributes");

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
		System.out.println(CLASS_NAME + "getFacilityAttributesWhenFacilityNotExists");

		attributesManager.getAttributes(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getAllGroupAttributesStartWithNameWithoutNullValue() throws Exception {
		System.out.println(CLASS_NAME + "getAllAttributesStartWithNameWithoutNullValue");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttributes();

		for(Attribute a: attributes) {
			attributesManager.setAttribute(sess, group, a);
		}

		List<Attribute> retAttr = attributesManager.getAllAttributesStartWithNameWithoutNullValue(sess, group, AttributesManager.NS_GROUP_ATTR_OPT + ":group-test-uniqueattribute");

		assertNotNull("unable to get group attributes", retAttr);


		assertTrue("our atttributes not returned",attributes.containsAll(retAttr));
		assertTrue("returned 3 attributes",retAttr.size() == 3);
	}

	@Test
	public void getAllResourceAttributesStartWithNameWithoutNullValue() throws Exception {
		System.out.println(CLASS_NAME + "getAllAttributesStartWithNameWithoutNullValue");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttributes();

		for(Attribute a: attributes) {
			attributesManager.setAttribute(sess, resource, a);
		}

		List<Attribute> retAttr = attributesManager.getAllAttributesStartWithNameWithoutNullValue(sess, resource, AttributesManager.NS_RESOURCE_ATTR_OPT + ":resource-test-uniqueattribute:");
		assertNotNull("unable to get resource attributes", retAttr);

		assertTrue("our atttributes not returned",attributes.containsAll(retAttr));
		assertTrue("returned 3 attributes",retAttr.size() == 3);
	}

	@Test
	public void getVoAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributes");

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
		List<Attribute> attribute = new ArrayList<>();
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
		perun.getAttributesManagerBl().setAttribute(sess, member, resource, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, member, resource, attributes.get(0).getName()));
		attributes = setUpUserLargeAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, user, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, user, attributes.get(0).getName()));
		attributes = setUpResourceLargeAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, attributes.get(0));
		assertEquals(attributes.get(0), perun.getAttributesManagerBl().getAttribute(sess, resource, attributes.get(0).getName()));
	}

	@Test
	public void getEntitylessAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributes");

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
		System.out.println(CLASS_NAME + "getEntitylessAttributeForUpdate");

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
		System.out.println(CLASS_NAME + "getEntitylessAttributeForUpdate");

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
		System.out.println(CLASS_NAME + "getEntitylessKeys");

		attributes = setUpEntitylessAttribute();
		String key = "Test Attributu Michal2";
		attributesManager.setAttribute(sess, key, attributes.get(0));

		List<String> entStr = attributesManager.getEntitylessKeys(sess, attributesManager.getAttributeDefinition(sess, attributes.get(0).getName()));
		assertNotNull("unable to get entityless attributes", entStr);

		assertTrue("our String not returned",entStr.contains(key));
		assertEquals("We expected 1 and we get "+entStr.size(), 1, entStr.size());

	}

	@Test
	public void createAndDeleteEntitylessAttribute() throws Exception {

		setAttributesForEntitylessTest();
		perun.getAttributesManager().deleteAttribute(sess, entityless_test_attribute1);

	}

	/*@Test
		public void checkAttributeDependenciesForAllAttributesInMap() throws Exception {
		System.out.println(CLASS_NAME + "checkAttributeDependenciesForAllAttributesInMap");
		AttributesManagerBlImpl attributesManagerBlImpl = mock(AttributesManagerBlImpl.class, RETURNS_DEEP_STUBS); //RETURNS_DEEP_STUBS = budeme mockovat nekolik vnorenych volani metod
//spy(attributesManagerBlImpl).checkAttributeValue(sess, resource, null);
//when(attributesManagerBlImpl.checkAttributeValue(any(PerunSession.class), any(Resource.class), any(Attribute.class)))
}*/

	@Test (expected=VoNotExistsException.class)
	public void getVoAttributesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesWhenVoNotExists");

		attributesManager.getAttributes(sess, new Vo());
		// shouldn't find VO

	}

	@Test
	public void getResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributes");

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
		System.out.println(CLASS_NAME + "getResourceAttributesWhenResourceNotExists");

		attributesManager.getAttributes(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void getResourceMemberAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceMemberAttributes");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();
		attributesManager.setAttribute(sess, member, resource, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource);
		assertNotNull("unable to get member-resource attributes", retAttr);
		assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));

	}

// výjímky pro Member a Resource Not Exists nenastanou,
// protože si to bere data z předaných objektů a ne z databáze


	@Test
	public void getResourceMemberAttributesForUser() throws Exception {
		System.out.println(CLASS_NAME + "getResourceMemberAttributesForUser");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.setAttribute(sess, user, attributes.get(0));

		// return members and users attributes from resources members
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource, true);
		assertNotNull("unable to get member-resource(work with user) attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceMemberAttributesForUserWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceMemberAttributesForUserWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttributes(sess, member, new Resource(), true);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getResourceMemberAttributesForUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceMemberAttributesForUserWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributes(sess, new Member(), resource, true);
		// shouldn't find member

	}

	@Test
	public void getMemberGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributes");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertNotNull("unable to get member-group attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));
	}

	@Test
	public void getFacilityAttributesByListOfNames() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByListOfNames");

		vo = setUpVo();
		facility = setUpFacility();

		List<Attribute> facilityAttrs = setUpFacilityAttribute();
		perun.getAttributesManagerBl().setAttributes(sess, facility, facilityAttrs);

		List<String> attrNames = new ArrayList<>();
		for(Attribute attribute: facilityAttrs) {
			attrNames.add(attribute.getName());
		}

		List<Attribute> returnedAttributes = perun.getAttributesManagerBl().getAttributes(sess, facility, attrNames);

		for(Attribute attribute: facilityAttrs) {
			assertTrue(returnedAttributes.contains(attribute));
		}
	}

	@Test
	public void getGroupResourceAttributesByListOfNames() throws Exception {
		vo = setUpVo();
		facility = setUpFacility();
		group = setUpGroup();
		resource = setUpResource();
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<Attribute> groupResourceAttrs = setUpGroupResourceAttribute();
		perun.getAttributesManagerBl().setAttributes(sess, resource, group, groupResourceAttrs);
		List<Attribute> groupAttrs = setUpGroupAttribute();
		perun.getAttributesManagerBl().setAttributes(sess, group, groupAttrs);

		List<String> attrNames = new ArrayList<>();
		for(Attribute attribute: groupResourceAttrs) {
			attrNames.add(attribute.getName());
		}
		for(Attribute attribute: groupAttrs) {
			attrNames.add(attribute.getName());
		}

		List<Attribute> returnedAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, attrNames, true);
		List<Attribute> returnedAttributesWithoutGroupAttributes = perun.getAttributesManagerBl().getAttributes(sess, resource, group, attrNames, false);

		for(Attribute attribute: groupResourceAttrs) {
			assertTrue(returnedAttributes.contains(attribute));
			assertTrue(returnedAttributesWithoutGroupAttributes.contains(attribute));
		}
		for(Attribute attribute: groupAttrs) {
			assertTrue(returnedAttributes.contains(attribute));
			assertTrue(!returnedAttributesWithoutGroupAttributes.contains(attribute));
		}
	}

	@Test
	public void getMemberGroupAttributesForUser() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesForUser");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();
		attributes = setUpUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.setAttribute(sess, user, attributes.get(0));

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_group:attribute-def:opt");
		attr.setFriendlyName("member-group-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberGroupAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, group, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attributes.get(0).getName());
		attrNames.add(attr.getName());

		// return members and users attributes from groups members
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group, attrNames, true);
		assertNotNull("unable to get member-group(work with user) attributes", retAttr);
		assertTrue("our attribute was not returned",retAttr.contains(attributes.get(0)));
	}

	@Test (expected=GroupNotExistsException.class)
	public void getMemberGroupAttributesForUserWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesForUserWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttributes(sess, member, new Group(), new ArrayList<>(), true);
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberGroupAttributesForUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesForUserWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttributes(sess, new Member(), group, new ArrayList<>(), true);
		// shouldn't find member
	}

	@Test
	public void getMemberGroupAttributesByListOfNames1() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByListOfNames");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_group:attribute-def:opt");
		attr.setFriendlyName("member-group-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberGroupAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, group, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attributes.get(0).getName());
		attrNames.add(attr.getName());

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group, attrNames);
		assertNotNull("unable to get member-group attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));
		assertTrue("our attribute was not returned", retAttr.contains(attr));
	}

	@Test
	public void getMemberGroupAttributesByListOfNames2() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByListOfNames");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_group:attribute-def:opt");
		attr.setFriendlyName("member-group-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberGroupAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, group, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attr.getName());

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group, attrNames);
		assertNotNull("unable to get member attributes", retAttr);
		assertFalse("our attribute was not returned", retAttr.contains(attributes.get(0)));
		assertTrue("our attribute was not returned", retAttr.contains(attr));
	}

	@Test
	public void getMemberAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributes");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributesManager.setAttribute(sess, member, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member);
		assertNotNull("unable to get member attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesWhenMemberNotExists");

		attributesManager.getAttributes(sess, new Member());
		// shouldn't find member

	}

	@Test
	public void getMemberAttributesByListOfNames1() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByListOfNames");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributesManager.setAttribute(sess, member, attributes.get(0));

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member:attribute-def:opt");
		attr.setFriendlyName("member-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attributes.get(0).getName());
		attrNames.add(attr.getName());

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, attrNames);
		assertNotNull("unable to get member attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));
		assertTrue("our attribute was not returned",retAttr.contains(attr));
	}

	@Test
	public void getMemberAttributesByListOfNames2() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByListOfNames");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributesManager.setAttribute(sess, member, attributes.get(0));

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member:attribute-def:opt");
		attr.setFriendlyName("member-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attr.getName());

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, attrNames);
		assertNotNull("unable to get member attributes", retAttr);
		assertFalse("our attribute was not returned", retAttr.contains(attributes.get(0)));
		assertTrue("our attribute was not returned", retAttr.contains(attr));
	}

	@Test
	public void getFacilityUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributes");

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
		assertTrue("returned incorrect facility-user", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityUserAttributesWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributesWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributes(sess, new Facility(), user);
		// shouldn't find facility;

	}

	@Test (expected=UserNotExistsException.class)
	public void getFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributesWhenUserNotExists");

		facility = setUpFacility();

		attributesManager.getAttributes(sess, facility, new User());
		// shouldn't find user;

	}

	@Test
	public void getUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributes");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.setAttribute(sess, user, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
		assertNotNull("unable to get user attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=UserNotExistsException.class)
	public void getUserAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesWhenUserNotExists");

		attributesManager.getAttributes(sess, new User());
		// souldn't find user

	}

	@Test
	public void getUserLargeAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributes - large attributes");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserLargeAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.setAttribute(sess, user, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, user);
		assertNotNull("unable to get user attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));
	}

	@Test
	public void getGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributes");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributesManager.setAttribute(sess, group, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, group);
		assertNotNull("unable to get group attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesWhenGroupNotExists");

		attributesManager.getAttributes(sess, new Group());
		// shouldn't find group

	}

	@Test
	public void getGroupResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributes");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();
		attributesManager.setAttribute(sess, resource, group, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, resource, group);
		assertNotNull("unable to get group-resource attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getGroupResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributes(sess, resource, new Group());
		// shouldn't find member

	}

	@Test
	public void getHostAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributes");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributesManager.setAttribute(sess, host, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
		assertNotNull("unable to get host attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=HostNotExistsException.class)
	public void getHostAttributesWhenHostNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesWhenHostNotExists");

		attributesManager.getAttributes(sess, new Host());
		// shouldn't find host

	}

	@Test
	public void getAttributesByAttributeDefinition() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByAttributeDefinition");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributesManager.setAttribute(sess, host, attributes.get(0));

		List<Attribute> retAttr = attributesManager.getAttributesByAttributeDefinition(sess, attributes.get(0));
		assertNotNull("unable to get attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));

	}

	@Test
	public void getUserExtSourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributes");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttribute(sess, ues, attributes.get(0));
		List<Attribute> retAttr = attributesManager.getAttributes(sess, ues);
		assertNotNull("unable to get ues attributes", retAttr);
		assertTrue("our attribute was not returned", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceAttributesWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesWhenUserExtSourceNotExists");

		attributesManager.getAttributes(sess, setUpUserExtSource());
		// shouldn't find userExtSource

	}


// ==============  2.  SET ATTRIBUTES ================================




	@Test
	public void setFacilityAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributes");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();

		attributesManager.setAttributes(sess, facility, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, facility);

		assertTrue("unable to set/or return facility attribute we created", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void setFacilityAttributesWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributesWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();

		attributesManager.setAttributes(sess, new Facility(), attributes);
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setFacilityAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributesWhenAttrbuteNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object no existing in db by setting it's ID to 0
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setFacilityAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributesWhenWrongAttrAssigment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		// create Vo attribute instead Facility attribute to raise exception
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't set wrong attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setFacilityAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributesWhenTypeMismatch");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, facility, attributes);
		// shouldn't set wrong attribute

	}

	@Test
	public void setVoAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributes");

		vo = setUpVo();
		attributes = setUpVoAttribute();

		attributesManager.setAttributes(sess, vo, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, vo);
		assertTrue("unable to set/or return vo attribute we created", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=VoNotExistsException.class)
	public void setVoAttributesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributesWhenVoNotExists");

		attributes = setUpVoAttribute();

		attributesManager.setAttributes(sess, new Vo(), attributes);
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setVoAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributes");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		// make valid attribute into not existing by setting ID = 0
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setVoAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributesWhenWrongAttrAssigment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		// create Facility attribute instead Vo attribute to raise exception
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't set wrong attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setVoAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributesWhenTypeMismatch");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, vo, attributes);
		// shouldn't set wrong attribute

	}

	@Test
	public void setResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();

		attributesManager.setAttributes(sess, resource, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, resource);
		assertTrue("unable to set/or return resource attribute we created", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setResourceAttributesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributesWhenVoNotExists");

		attributes = setUpResourceAttribute();

		attributesManager.setAttributes(sess, new Resource(), attributes);
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributes");

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
		System.out.println(CLASS_NAME + "setResourceAttributesWhenWrongAttrAssigment");

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
		System.out.println(CLASS_NAME + "setResourceAttributesWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setMemberResourceAttributes");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, member, resource, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource);
		assertNotNull("unable to get member-resource attributes", retAttr);
		assertTrue("unable to set/or return our member-resource attribute", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setResourceMemberAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, member, new Resource(), attributes);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void setMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, new Member(), resource, attributes);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0
		attributesManager.setAttributes(sess, member, resource, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberResourceAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributes = setUpVoAttribute();
		// set up wrong attribute - vo instead of member-resource
		attributesManager.setAttributes(sess, member, resource, attributes);
		// shouldn't set attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setMemberResourceAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributesWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, member, resource, attributes);
		// shouldn't set wrong attribute

	}

	@Test
	public void setUserAttributesForMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberResource");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, member, resource, attributes, true);

		// return users attributes from resource member
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource, true);
		assertNotNull("unable to set or get member-resource(work with user) attributes", attributes);
		assertTrue("our attribute was not set/returned", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=MemberNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberResourceWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, new Member(), resource, attributes, true);
		// shouldn't find member

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberResourceWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, member, new Resource(), attributes, true);
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributesForMemberResourceWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberResourceWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object not existing in DB

		attributesManager.setAttributes(sess, member, resource, attributes, true);
		// shouldn't find attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setUserAttributesForMemberResourceWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberResourceWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpUserAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, member, resource, attributes, true);
		// shouldn't set wrong attribute

	}

	@Test
	public void setMemberGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributes");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, member, group, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertNotNull("unable to get member-group attributes", retAttr);
		assertTrue("unable to set/or return our member-group attribute", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=GroupNotExistsException.class)
	public void setMemberGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, member, new Group(), attributes);
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void setMemberGroupAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, new Member(), group, attributes);
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();

		attributes = setUpMemberGroupAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0
		attributesManager.setAttributes(sess, member, group, attributes);
		// shouldn't find attribute
	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberGroupAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();

		attributes = setUpVoAttribute();
		// set up wrong attribute - vo instead of member-group
		attributesManager.setAttributes(sess, member, group, attributes);
		// shouldn't set attribute
	}

	@Test (expected=InternalErrorException.class)
	public void setMemberGroupAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, member, group, attributes);
		// shouldn't set wrong attribute
	}

	@Test
	public void setUserAttributesForMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberGroup");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpUserAttribute();

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_group:attribute-def:opt");
		attr.setFriendlyName("member-group-test-for-list-of-names-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberGroupAttributeForList");
		attributesManager.createAttribute(sess, attr);
		attributesManager.setAttribute(sess, member, group, attr);

		List<String> attrNames = new ArrayList<>();
		attrNames.add(attributes.get(0).getName());
		attrNames.add(attr.getName());

		attributesManager.setAttributes(sess, member, group, attributes, true);

		// return users attributes from member group
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group, attrNames, true);
		assertNotNull("unable to set or get member-group(work with user) attributes", attributes);
		assertTrue("our attribute was not set/returned", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=MemberNotExistsException.class)
	public void setUserAttributesForMemberGroupWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberGroupWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, new Member(), group, attributes, true);
		// shouldn't find member
	}

	@Test (expected=GroupNotExistsException.class)
	public void setUserAttributesForMemberGroupWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberGroupWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, member, new Group(), attributes, true);
		// shouldn't find group
	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributesForMemberGroupWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberGroupWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpUserAttribute();
		attributes.get(0).setId(0);
		// make valid attribute object not existing in DB

		attributesManager.setAttributes(sess, member, group, attributes, true);
		// shouldn't find attribute
	}

	@Test (expected=InternalErrorException.class)
	public void setUserAttributesForMemberGroupWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesForMemberGroupWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpUserAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, member, group, attributes, true);
		// shouldn't set wrong attribute
	}

	@Test
	public void setMemberAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttributes");

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
		System.out.println(CLASS_NAME + "setMemberAttributesWhenMemberNotExists");

		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, new Member(), attributes);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setMemberAttributesWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "setMemberAttributesWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setUserAttributes");

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
		System.out.println(CLASS_NAME + "setUserAttributesWhenUserNotExists");

		attributes = setUpUserAttribute();

		attributesManager.setAttributes(sess, new User(), attributes);
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setUserAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, user, attributes);
		// shouldn't add attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setUserAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributesWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setGroupAttributes");

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
		System.out.println(CLASS_NAME + "setGroupAttributesWhenGroupNotExists");

		attributes = setUpGroupAttribute();

		attributesManager.setAttributes(sess, new Group(), attributes);
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setGroupAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, group, attributes);
		// shouldn't add attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setGroupAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setGroupAttributesWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setMemberWorkWithUserAttributes");
		vo = setUpVo();
		member = setUpMember();
		List<Attribute> attributes_member = setUpMemberAttribute();
		User user =sess.getPerun().getUsersManager().getUserByMember(sess, member);
		List<Attribute> attributes_user = setUpUserAttribute();
		attributes = new ArrayList<>();
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
		System.out.println(CLASS_NAME + "setMemberWorkWithoutUserAttributes");
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
		System.out.println(CLASS_NAME + "setGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "setGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, new Resource(), group, attributes);
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void setGroupResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, new Group(), attributes);
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setGroupResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupResourceAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setGroupResourceAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setGroupResourceAttributesWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, resource, group, attributes);
		// shouldn't set wrong attribute

	}

	@Test
	public void setHostAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributes");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, host, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, host);
		assertNotNull("unable to get host attributes", retAttr);
		assertTrue("our attribute is not set or returned from group", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=HostNotExistsException.class)
	public void setHostAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributesWhenHostNotExists");

		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, new Host(), attributes);
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setHostAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributesWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID=0

		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setHostAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributesWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpMemberAttribute();

		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't add attribute

	}

	@Test (expected=InternalErrorException.class)
	public void setHostAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributesWhenTypeMismatch");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, host, attributes);
		// shouldn't set wrong attribute

	}

	@Test
	public void setUserExtSourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributes");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttributes(sess, ues, attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, ues);
		assertTrue("unable to set/or return userExtSource attribute we created", retAttr.contains(attributes.get(0)));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void setUserExtSourceAttributesWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributesWhenUserExtSourceNotExists");

		attributes = setUpUserExtSourceAttribute();

		attributesManager.setAttributes(sess, setUpUserExtSource(), attributes);
		// shouldn't find userExtSource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserExtSourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributesWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute into not existing by setting ID = 0
		attributesManager.setAttributes(sess, ues, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setUserExtSourceAttributesWhenWrongAttrAssigment() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributesWhenWrongAttrAssigment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpVoAttribute();
		// create Vo attribute instead UserExtSource attribute to raise exception
		attributesManager.setAttributes(sess, ues, attributes);
		// shouldn't set wrong attribute
	}

	@Test (expected=InternalErrorException.class)
	public void setUserExtSourceAttributesWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributesWhenTypeMismatch");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setValue(1);
		// set wrong value - integer into string
		attributesManager.setAttributes(sess, ues, attributes);
		// shouldn't set wrong attribute
	}

	@Test
	public void setMemberGroupAttributesFacilityResourceUser() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributesFacilityResourceUser");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, facility, resource, group, user, member,  attributes);

		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertNotNull("unable to get member-group attributes", retAttr);

		for(Attribute a: attributes) {
			assertTrue("returned member-group attributes are not same as stored", retAttr.contains(a));
		}
	}



// ==============  3.  GET ATTRIBUTE (by name) ================================
//
// attribute name is namespace:friendlyName (urn:perun:facility:attribute-def:core:id)



	@Test
	public void getFacilityAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttribute");

		facility = setUpFacility();

		Attribute retAttr = attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:id");
		assertNotNull("unable to get core attribute facility id", retAttr);
		assertEquals("returned core attr value is not correct",retAttr.getValue(),facility.getId());

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityAttributeWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeWhenFacilityNotExists");

		attributesManager.getAttribute(sess, new Facility(), "urn:perun:facility:attribute-def:core:id");
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();

		attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();

		attributesManager.getAttribute(sess, facility, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on facility

	}

	@Test
	public void getVoAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttribute");

		vo = setUpVo();

		Attribute retAttr = attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:core:id");
		assertNotNull("unable to get core attribute vo id", retAttr);
		assertEquals("returned core attr value is not correct",retAttr.getValue(),vo.getId());

	}

	@Test (expected=VoNotExistsException.class)
	public void getVoAttributeWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeWhenVoNotExists");

		attributesManager.getAttribute(sess, new Vo(), "urn:perun:vo:attribute-def:core:id");
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeWhenAttributeNotExists");

		vo = setUpVo();

		attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();

		attributesManager.getAttribute(sess, vo, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on vo

	}

	@Test
	public void getResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttribute");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		Attribute retAttr = attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:core:id");
		assertNotNull("unable to get core attribute resource id", retAttr);
		assertEquals("returned core attr value is not correct", retAttr.getValue(), resource.getId());

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeWhenResourceNotExists");

		attributesManager.getAttribute(sess, new Resource(), "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, "urn:perun:vo:attribute-def:core:id");
		// shouldn't find vo attribute on resource

	}

	@Test
	public void getSelectedMemberResourceAssociatedAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getSelectedMemberResourceAssociatedAttributes");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		facility = setUpFacility();
		resource = setUpResource();

		Attribute userAttribute1 = setUpAttribute(String.class.getName(), "testUserAttribute1", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		Attribute userAttribute2 = setUpAttribute(String.class.getName(), "testUserAttribute2", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		attributesManager.setAttributes(sess, user, new ArrayList<>(Arrays.asList(userAttribute1, userAttribute2)));
		Attribute memberAttribute1 = setUpAttribute(Integer.class.getName(), "testMemberAttribute1", AttributesManager.NS_MEMBER_ATTR_DEF, 15);
		Attribute memberAttribute2 = setUpAttribute(Integer.class.getName(), "testMemberAttribute2", AttributesManager.NS_MEMBER_ATTR_DEF, 15);
		attributesManager.setAttributes(sess, member, new ArrayList<>(Arrays.asList(memberAttribute1, memberAttribute2)));
		Attribute userFacilityAttribute1 = setUpAttribute(ArrayList.class.getName(), "testUserFacilityAttribute1", AttributesManager.NS_USER_FACILITY_ATTR_DEF, new ArrayList<>(Arrays.asList("A", "B")));
		Attribute userFacilityAttribute2 = setUpAttribute(ArrayList.class.getName(), "testUserFacilityAttribute2", AttributesManager.NS_USER_FACILITY_ATTR_DEF, new ArrayList<>(Arrays.asList("A", "B")));
		attributesManager.setAttributes(sess, facility, user, new ArrayList<>(Arrays.asList(userFacilityAttribute1, userFacilityAttribute2)));
		Map<String, String> map = new LinkedHashMap<>();
		map.put("A", "B");
		map.put("C", "D");
		Attribute memberResourceAttribute1 = setUpAttribute(LinkedHashMap.class.getName(), "testMemberResourceAttribute1", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, map);
		Attribute memberResourceAttribute2 = setUpAttribute(LinkedHashMap.class.getName(), "testMemberResourceAttribute2", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, map);
		attributesManager.setAttributes(sess, member, resource, new ArrayList<>(Arrays.asList(memberResourceAttribute1, memberResourceAttribute2)));

		List<String> attrNames = new ArrayList<>(Arrays.asList(userAttribute1.getName(), memberAttribute1.getName(), userFacilityAttribute1.getName(), memberResourceAttribute1.getName()));
		List<Attribute> returnedAttributes = attributesManager.getAttributes(sess, member, resource, attrNames, true);

		assertTrue(returnedAttributes.size() == 4);
		assertTrue(returnedAttributes.contains(userAttribute1));
		assertTrue(returnedAttributes.contains(memberAttribute1));
		assertTrue(returnedAttributes.contains(userFacilityAttribute1));
		assertTrue(returnedAttributes.contains(memberResourceAttribute1));
	}

	@Test
	public void getMemberResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttribute");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, member, resource, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, member, resource, "urn:perun:member_resource:attribute-def:opt:member-resource-test-attribute");
		assertNotNull("unable to get opt member resource attribute ", retAttr);
		assertEquals("returned opt attr value is not correct", retAttr.getValue(), attributes.get(0).getValue());

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, member, resource, attributes);

		attributesManager.getAttribute(sess, member, new Resource(), "urn:perun:member_resource:attribute-def:opt:member-resource-test-attribute");
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeWhenMemberNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttributes(sess, member, resource, attributes);

		attributesManager.getAttribute(sess, new Member(), resource, "urn:perun:member_resource:attribute-def:opt:member-resource-test-attribute");
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, member, resource, "urn:perun:member_resource:attribute-def:opt:nesmysl");
		// shouldn't find member resource attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, member, resource, "urn:perun:resource:attribute-def:opt:member-resource-test-attribute");
		// shouldn't find resource attribute instead of member-resource

	}

	@Test
	public void getMemberGroupAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttribute");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, member, group, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, member, group, "urn:perun:member_group:attribute-def:opt:member-group-test-attribute");
		assertNotNull("unable to get opt member group attribute ", retAttr);
		assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

	}

	@Test (expected=GroupNotExistsException.class)
	public void getMemberGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeWhenGroupNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, member, group, attributes);

		attributesManager.getAttribute(sess, member, new Group(), "urn:perun:member_group:attribute-def:opt:member-group-test-attribute");
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberGroupAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttributes(sess, member, group, attributes);

		attributesManager.getAttribute(sess, new Member(), group, "urn:perun:member_group:attribute-def:opt:member-group-test-attribute");
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, group, "urn:perun:member_group:attribute-def:opt:nesmysl");
		// shouldn't find member group attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, group, "urn:perun:group:attribute-def:opt:member-groupe-test-attribute");
		// shouldn't find group attribute instead of member-group

	}

	@Test
	public void getMemberAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttribute");

		vo = setUpVo();
		member = setUpMember();

		Attribute retAttr = attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:core:id");
		assertNotNull("unable to get core attribute member id", retAttr);
		assertEquals("returned core attr value is not correct",retAttr.getValue(),member.getId());

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeWhenMemberNotExists");

		attributesManager.getAttribute(sess, new Member(), "urn:perun:member:attribute-def:core:id");
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttribute(sess, member, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on member

	}

	@Test
	public void getFacilityUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttribute");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, facility, user, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:opt:user-facility-test-attribute");
		assertNotNull("unable to get opt user_facility attribute ", retAttr);
		assertEquals("returned opt attr value is not correct", retAttr.getValue(), attributes.get(0).getValue());

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityUserAttributeWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, facility, user, attributes);

		attributesManager.getAttribute(sess, new Facility(), user, "urn:perun:user_facility:attribute-def:opt:user-facility-test-attribute");
		// shouldn't find facility

	}

	@Test (expected=UserNotExistsException.class)
	public void getFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeWhenUserNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttributes(sess, facility, user, attributes);

		attributesManager.getAttribute(sess, facility, new User(), "urn:perun:user_facility:attribute-def:opt:user-facility-test-attribute");
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, facility, user, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on user-facility

	}

	@Test
	public void getUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttribute");

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
		System.out.println(CLASS_NAME + "getUserAttributeWhenUserNotExists");

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
		System.out.println(CLASS_NAME + "getUserAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:core:nesmysl");
		// shouldn't find core attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttribute(sess, user, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on user

	}

	@Test
	public void getGroupAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttribute");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();

		attributesManager.setAttribute(sess, group, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:group-test-attribute");
		assertNotNull("unable to get opt group attribute", retAttr);
		assertEquals("returned opt attr value is not correct", retAttr.getValue(), attributes.get(0).getValue());

	}

	@Test
	public void getGroupAttributesFromList() throws Exception {
		System.out.println(CLASS_NAME + "getAttributes");

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
		System.out.println(CLASS_NAME + "getGroupAttributeWhenGroupNotExists");

		attributesManager.getAttribute(sess, new Group(), "urn:perun:group:attribute-def:opt:group-test-attribute");
		// shouldn't find groups

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:nesmysl");
		// shouldn't find opt attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getAttribute(sess, group, "urn:perun:resource:attribute-def:core:id");
		// shouldn't find resource attribute on group

	}

	@Test
	public void getGroupResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttribute");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, group, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, resource, group,"urn:perun:group_resource:attribute-def:opt:group-resource-test-attribute");
		assertNotNull("unable to get opt group resource attribute ", retAttr);
		assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getGroupResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, group, attributes);

		attributesManager.getAttribute(sess, new Resource(), group, "urn:perun:group_resource:attribute-def:opt:group-resource-test-attribute");
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributeWhenGroupNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttributes(sess, resource, group, attributes);

		attributesManager.getAttribute(sess, resource, new Group(), "urn:perun:group_resource:attribute-def:opt:group-resource-test-attribute");
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, group, "urn:perun:group_resource:attribute-def:opt:nesmysl");
		// shouldn't find member resource attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttribute(sess, resource, group, "urn:perun:resource:attribute-def:opt:group-resource-test-attribute");
		// shouldn't find resource attribute instead of member-resource

	}

	@Test
	public void getHostAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttribute");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, host, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, host,"urn:perun:host:attribute-def:opt:host-test-attribute");
		assertNotNull("unable to get opt host attribute ", retAttr);
		assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());

	}

	@Test (expected=HostNotExistsException.class)
	public void getHostAttributeWhenHostNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributeWhenHostNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttributes(sess, host, attributes);

		attributesManager.getAttribute(sess, new Host(), "urn:perun:host:attribute-def:opt:host-test-attribute");
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);

		attributesManager.getAttribute(sess, host, "urn:perun:host:attribute-def:opt:nesmysl");
		// shouldn't find host attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);

		attributesManager.getAttribute(sess, host, "urn:perun:resource:attribute-def:opt:host-test-attribute");
		// shouldn't find resource attribute instead of host

	}

	@Test
	public void getUserExtSourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttribute");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttributes(sess, ues, attributes);

		Attribute retAttr = attributesManager.getAttribute(sess, ues,"urn:perun:ues:attribute-def:opt:userExtSource-test-attribute");
		assertNotNull("unable to get opt user external source attribute ", retAttr);
		assertEquals("returned opt attr value is not correct",retAttr.getValue(),attributes.get(0).getValue());
	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceAttributeWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeWhenUserExtSourceNotExists");

		attributesManager.getAttribute(sess, setUpUserExtSource(), "urn:perun:ues:attribute-def:opt:userExtSource-test-attribute");
		// shouldn't find user external source

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getUserExtSourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributesManager.getAttribute(sess, ues, "urn:perun:ues:attribute-def:opt:nesmysl");
		// shouldn't find opt attribute "nesmysl"

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserExtSourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeWhenWrongAttrAssignment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributesManager.getAttribute(sess, ues, "urn:perun:vo:attribute-def:core:id");
		// shouldn't find vo attribute on user external source

	}


// ==============  4.  GET ATTRIBUTE DEFINITION ================================


	@Test
	public void getAttributeDefinition() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinition");

		AttributeDefinition attrDef = attributesManager.getAttributeDefinition(sess, "urn:perun:vo:attribute-def:core:id");
		assertNotNull("unable to get attribute definition by name",attrDef);
		assertTrue("returned wrong attr def by name", attrDef.getName().equals("urn:perun:vo:attribute-def:core:id"));

	}

	@Test
	public void getAttributeDefinitionWithRights() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionWithRights");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		member = setUpMember();

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);
		perun.getGroupsManagerBl().addMember(sess, group, member);

		Attribute attr = setUpSpecificMemberResourceAttribute(member, resource);

		List<PerunBean> perunBeans = new ArrayList<>();
		perunBeans.add(member);
		perunBeans.add(resource);

		List<AttributeDefinition> attrDefs = attributesManager.getAttributesDefinitionWithRights(sess, perunBeans);
		List<AttributeDefinition> allAttrDef = attributesManager.getAttributesDefinition(sess);

		assertFalse(attrDefs.isEmpty());
		assertFalse(attrDefs.containsAll(allAttrDef));
		assertTrue(allAttrDef.containsAll(attrDefs));
		assertTrue(attrDefs.contains(attr));

		for(AttributeDefinition ad: attrDefs) {
			assertTrue(attributesManager.isFromNamespace(sess, ad, NS_MEMBER_ATTR) ||
					attributesManager.isFromNamespace(sess, ad, AttributesManager.NS_RESOURCE_ATTR) ||
					attributesManager.isFromNamespace(sess, ad, AttributesManager.NS_MEMBER_RESOURCE_ATTR));
			assertTrue(ad.getWritable());
		}

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getAttributeDefinitionWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionWhenAttributeNotExists");

		attributesManager.getAttributeDefinition(sess, "urn:perun:vo:attribute-def:core:nesmysl");
		// shouldn't find vo attribute "nesmysl"

	}

	@Test
	public void getAttributesDefinition() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesDefinition");

		List<AttributeDefinition> attrDef = attributesManager.getAttributesDefinition(sess);
		assertNotNull("unable to get attributes definition",attrDef);
		assertTrue("there should be some attributes definition", attrDef.size() > 0);

	}

	@Test
	public void getAttributeDefinitionById() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesDefinitionById");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setDescription("attributesManagerTestAttrDef");
		attrDef.setFriendlyName("attrDef");
		attrDef.setNamespace("urn:perun:member:attribute-def:opt");
		attrDef.setType(String.class.getName());

		attributesManager.createAttribute(sess, attrDef);
		// store attr definition in DB

		AttributeDefinition retAttrDef = attributesManager.getAttributeDefinitionById(sess, attrDef.getId());
		assertNotNull("unable to get attribute definition",retAttrDef);
		assertTrue("returned wrong attr definition", retAttrDef.getName().equals(attrDef.getName()));

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getAttributeDefinitionByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesDefinitionByIdWhenAttributeNotExists");

		attributesManager.getAttributeDefinitionById(sess, 0);
		// shouldn't find attribute definition

	}

	@Test
	public void getAttributesDefinitionByNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesDefinitionByNamespace");

		List<AttributeDefinition> attrDef = attributesManager.getAttributesDefinitionByNamespace(sess, "urn:perun:member:attribute-def:core");
		assertNotNull("unable to get attributes definition",attrDef);
		assertTrue("there should be some attributes definition",attrDef.size() > 0);

	}










// ==============  5.  GET ATTRIBUTE BY ID ================================




	@Test
	public void getFacilityAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeById");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributesManager.setAttributes(sess, facility, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, facility, id);
		assertNotNull("unable to get facility attribute by id", retAttr);
		assertEquals("returned attribute is not same as we stored", retAttr, attributes.get(0));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityAttributeByIdWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeByIdWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Facility(), id);
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getFacilityAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeByIdWhenAttributeNotExists");

		facility = setUpFacility();

		attributesManager.getAttributeById(sess, facility, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributeByIdWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, facility, id);
		// shouldn't return facility attribute when ID belong to different type of attribute

	}

	@Test
	public void getVoAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeById");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributesManager.setAttributes(sess, vo, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, vo, id);
		assertNotNull("unable to get vo attribute by id",retAttr);
		assertEquals("returned attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=VoNotExistsException.class)
	public void getVoAttributeByIdWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeByIdWhenVoNotExists");

		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Vo(), id);
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getVoAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();

		attributesManager.getAttributeById(sess, vo, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getVoAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, vo, id);
		// shouldn't return vo attribute when ID belong to different type of attribute

	}

	@Test
	public void getResourceAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeById");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();
		attributesManager.setAttributes(sess, resource, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, resource, id);
		assertNotNull("unable to get resource attribute by id",retAttr);
		assertEquals("returned attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceAttributeByIdWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeByIdWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Resource(), id);
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, resource, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributeByIdWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "getMemberResourceAttributeById");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();
		attributesManager.setAttributes(sess, member, resource, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, member, resource, id);
		assertNotNull("unable to get resource member attribute by id",retAttr);
		assertEquals("returned attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeByIdWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, new Resource(), id);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeByIdWhenMemberNotExists");

		vo = setUpVo();
		facility= setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Member(), resource, id);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, member, resource, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, resource, id);
		// shouldn't return member resource attribute when ID belong to different type of attribute

	}

	@Test
	public void getMemberGroupAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeById");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttributes(sess, member, group, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, member, group, id);
		assertNotNull("unable to get group member attribute by id", retAttr);
		assertEquals("returned attribute is not same as stored", retAttr, attributes.get(0));
	}

	@Test (expected=GroupNotExistsException.class)
	public void getMemberGroupAttributeByIdWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeByIdWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, new Group(), id);
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberGroupAttributeByIdWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeByIdWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpMemberGroupAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Member(), group, id);
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberGroupAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();

		attributesManager.getAttributeById(sess, member, group, 0);
		// shouldn't find attribute
	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberGroupAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, group, id);
		// shouldn't return member group attribute when ID belong to different type of attribute
	}

	@Test
	public void getMemberAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeById");

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
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Member(), id);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getMemberAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getAttributeById(sess, member, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getMemberAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, member, id);
		// shouldn't return member attribute when ID belong to different type of attribute

	}

	@Test
	public void getFacilityUserAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeById");

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
		System.out.println(CLASS_NAME + "getFacilityUserAttributeByIdWhenFacilityNotExists");

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
		System.out.println(CLASS_NAME + "getFacilityUserAttributeByIdWhenUserNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, facility, new User(), id);
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getFacilityUserAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributeById(sess, facility, user, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getFacilityUserAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityUserAttributeByIdWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "getUserAttributeById");

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
		System.out.println(CLASS_NAME + "getUserAttributeByIdWhenMemberNotExists");

		attributes = setUpUserAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new User(), id);
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getUserAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getAttributeById(sess, user, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "getGroupResourceAttributeById");

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
		System.out.println(CLASS_NAME + "getGroupResourceAttributeByIdWhenGroupNotExists");

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
		System.out.println(CLASS_NAME + "getGroupResourceAttributeByIdWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Resource(), group, id);
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getGroupResourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributeByIdWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getAttributeById(sess, resource, group, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getGroupResourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributeByIdWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "getHostAttributeById");

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
		System.out.println(CLASS_NAME + "getHostAttributeByIdWhenHostNotExists");

		attributes = setUpHostAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, new Host(), id);
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getHostAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributeByIdWhenAttributeNotExists");

		host = setUpHost().get(0);

		attributesManager.getAttributeById(sess, host, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getHostAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributeByIdWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpVoAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, host, id);
		// shouldn't return host attribute when ID belong to different type of attribute

	}

	@Test
	public void getUserExtSourceAttributeById() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeById");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttributes(sess, ues, attributes);

		int id = attributes.get(0).getId();

		Attribute retAttr = attributesManager.getAttributeById(sess, ues, id);
		assertNotNull("unable to get userExtSource attribute by id",retAttr);
		assertEquals("returned attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceAttributeByIdWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeByIdWhenUserExtSourceNotExists");

		attributes = setUpUserExtSourceAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, setUpUserExtSource(), id);
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void getUserExtSourceAttributeByIdWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeByIdWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributesManager.getAttributeById(sess, ues, 0);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void getUserExtSourceAttributeByIdWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributeByIdWhenWrongAttrAssignment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpMemberAttribute();
		int id = attributes.get(0).getId();

		attributesManager.getAttributeById(sess, ues, id);
		// shouldn't return userExtSource attribute when ID belong to different type of attribute

	}


// ==============  6. SET ATTRIBUTE ================================


	@Test
	public void setFacilityAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttribute");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();

		attributesManager.setAttribute(sess, facility, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:opt:facility-test-attribute");
		assertNotNull("unable to get facility attribute by name",retAttr);
		assertEquals("returned facility attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void setFacilityAttributeWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributeWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();

		attributesManager.setAttribute(sess, new Facility(), attributes.get(0));
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't add vo attribute into facility

	}

	@Test (expected=InternalErrorException.class)
	public void setFacilityAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityAttributeWhenTypeMismatch");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setFacilityUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityUserAttribute");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.setAttribute(sess, facility, user, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, facility, user, "urn:perun:user_facility:attribute-def:opt:user-facility-test-attribute");
		assertNotNull("unable to get facility-user attribute by name",retAttr);
		assertEquals("returned facility-user attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void setFacilityUserAttributeWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityUserAttributeWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityUserAttribute();

		attributesManager.setAttribute(sess, new Facility(), user, attributes.get(0));
		// shouldn't find facility

	}

	@Test (expected=UserNotExistsException.class)
	public void setFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityUserAttributeWhenUserNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityUserAttribute();

		attributesManager.setAttribute(sess, facility, new User(), attributes.get(0));
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setFacilityUserAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setFacilityUserAttributeWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "setFacilityUserAttributeWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setVoAttribute");

		vo = setUpVo();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, vo, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, vo, "urn:perun:vo:attribute-def:opt:vo-test-attribute");
		assertNotNull("unable to get vo attribute by name",retAttr);
		assertEquals("returned vo attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=VoNotExistsException.class)
	public void setVoAttributeWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributeWhenVoNotExists");

		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, new Vo(), attributes.get(0));
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributeWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't add facility attribute into vo

	}

	@Test (expected=InternalErrorException.class)
	public void setVoAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setVoAttributeWhenTypeMismatch");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, vo, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttribute");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpResourceAttribute();

		attributesManager.setAttribute(sess, resource, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, resource, "urn:perun:resource:attribute-def:opt:resource-test-attribute");
		assertNotNull("unable to get resource attribute by name", retAttr);
		assertEquals("returned resource attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributeWhenResourceNotExists");

		attributes = setUpResourceAttribute();

		attributesManager.setAttribute(sess, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, resource, attributes.get(0));
		// shouldn't add vo attribute into resource

	}

	@Test (expected=InternalErrorException.class)
	public void setResourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributeWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setMemberResourceAttribute");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttribute(sess, member, resource, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, member, resource, "urn:perun:member_resource:attribute-def:opt:member-resource-test-attribute");
		assertNotNull("unable to get member-resource attribute by name", retAttr);
		assertEquals("returned member-resource attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttribute(sess, member, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void setMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributeWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpMemberResourceAttribute();

		attributesManager.setAttribute(sess, new Member(), resource, attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// shouldn't add vo attribute into member-resource

	}

	@Test (expected=InternalErrorException.class)
	public void setMemberResourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setMemberResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setMemberGroupAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttribute");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttribute(sess, member, group, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, member, group, "urn:perun:member_group:attribute-def:opt:member-group-test-attribute");
		assertNotNull("unable to get member-group attribute by name", retAttr);
		assertEquals("returned member-group attribute is not same as stored", retAttr, attributes.get(0));
	}

	@Test (expected=GroupNotExistsException.class)
	public void setMemberGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributeWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttribute(sess, member, new Group(), attributes.get(0));
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void setMemberGroupAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributeWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpMemberGroupAttribute();

		attributesManager.setAttribute(sess, new Member(), group, attributes.get(0));
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// shouldn't find attribute
	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setMemberGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// shouldn't add vo attribute into member-group
	}

	@Test (expected=InternalErrorException.class)
	public void setMemberGroupAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setMemberGroupAttributeWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// shouldn't add attribute with String type and Integer value
	}

	@Test
	public void setMemberAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttribute");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();

		attributesManager.setAttribute(sess, member, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, member, "urn:perun:member:attribute-def:opt:member-test-attribute");
		assertNotNull("unable to get member attribute by name",retAttr);
		assertEquals("returned member attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=MemberNotExistsException.class)
	public void setMemberAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttributeWhenMemberNotExists");

		attributes = setUpMemberAttribute();

		attributesManager.setAttribute(sess, new Member(), attributes.get(0));
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, member, attributes.get(0));
		// shouldn't add vo attribute into member

	}

	@Test (expected=InternalErrorException.class)
	public void setMemberAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setMemberAttributeWhenTypeMismatch");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, member, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttribute");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpUserAttribute();

		attributesManager.setAttribute(sess, user, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, user, "urn:perun:user:attribute-def:opt:user-test-attribute");
		assertNotNull("unable to get user attribute by name",retAttr);
		assertEquals("returned user attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=UserNotExistsException.class)
	public void setUserAttributeWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributeWhenUserNotExists");

		attributes = setUpUserAttribute();

		attributesManager.setAttribute(sess, new User(), attributes.get(0));
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, user, attributes.get(0));
		// shouldn't add vo attribute into user

	}

	@Test (expected=InternalErrorException.class)
	public void setUserAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserAttributeWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setGroupAttribute");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();

		attributesManager.setAttribute(sess, group, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, group, "urn:perun:group:attribute-def:opt:group-test-attribute");
		assertNotNull("unable to get group attribute by name",retAttr);
		assertEquals("returned group attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=GroupNotExistsException.class)
	public void setGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupAttributeWhenGroupNotExists");

		attributes = setUpGroupAttribute();

		attributesManager.setAttribute(sess, new Group(), attributes.get(0));
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, group, attributes.get(0));
		// shouldn't add vo attribute into group

	}

	@Test (expected=InternalErrorException.class)
	public void setGroupAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setResourceAttributeWhenTypeMismatch");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, group, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setGroupResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setGroupResourceAttribute");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttribute(sess, resource, group, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, resource, group, "urn:perun:group_resource:attribute-def:opt:group-resource-test-attribute");
		assertNotNull("unable to get group-resource attribute by name",retAttr);
		assertEquals("returned group-resource attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void setGroupResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupResourceAttributeWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttribute(sess, new Resource(), group, attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void setGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupResourceAttributeWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpGroupResourceAttribute();

		attributesManager.setAttribute(sess, resource, new Group(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setGroupResourceAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "setGroupResourceAttributeWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "setGroupResourceAttributeWhenTypeMismatch");

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
		System.out.println(CLASS_NAME + "setHostAttribute");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttribute(sess, host, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, host, "urn:perun:host:attribute-def:opt:host-test-attribute");
		assertNotNull("unable to get host attribute by name",retAttr);
		assertEquals("returned host attribute is not same as stored",retAttr,attributes.get(0));

	}

	@Test (expected=HostNotExistsException.class)
	public void setHostAttributeWhenHostNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributeWhenHostNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();

		attributesManager.setAttribute(sess, new Host(), attributes.get(0));
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't add vo attribute into host

	}

	@Test (expected=InternalErrorException.class)
	public void setHostAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setHostAttributeWhenTypeMismatch");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, host, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}

	@Test
	public void setUserExtSourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttribute");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttribute(sess, ues, attributes.get(0));

		Attribute retAttr = attributesManager.getAttribute(sess, ues, "urn:perun:ues:attribute-def:opt:userExtSource-test-attribute");
		assertNotNull("unable to get userExtSource attribute by name", retAttr);
		assertEquals("returned userExtSource attribute is not same as stored", retAttr, attributes.get(0));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void setUserExtSourceAttributeWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributeWhenUserExtSourceNotExists");

		attributes = setUpUserExtSourceAttribute();

		attributesManager.setAttribute(sess, setUpUserExtSource(), attributes.get(0));
		// shouldn't find userExtSource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void setUserExtSourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributeWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setId(0);
		// make valid attribute not existing in DB by setting ID = 0

		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void setUserExtSourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributeWhenWrongAttrAssignment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpVoAttribute();

		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// shouldn't add vo attribute into userExtSource

	}

	@Test (expected=InternalErrorException.class)
	public void setUserExtSourceAttributeWhenTypeMismatch() throws Exception {
		System.out.println(CLASS_NAME + "setUserExtSourceAttributeWhenTypeMismatch");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setValue(1);

		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// shouldn't add attribute with String type and Integer value

	}


// ==============  7. CREATE ATTRIBUTE / DELETE ATTRIBUTE ================================



	@Test
	public void createAttribute() throws Exception {
		System.out.println(CLASS_NAME + "createAttribute");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-def-facility-tests-attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		// create attr definition
		attributesManager.createAttribute(sess, attrDef);
		// store attr def in DB acording namespace

		AttributeDefinition retAttrDef = attributesManager.getAttributeDefinition(sess, "urn:perun:facility:attribute-def:opt:attr-def-facility-tests-attr");

		assertNotNull("unable to get attr definition by name",retAttrDef);
		assertEquals("returned attr definition is not same as stored",attrDef,retAttrDef);

	}

	@Test (expected=AttributeDefinitionExistsException.class)
	public void createAttributeWhenAttributeExists() throws Exception {
		System.out.println(CLASS_NAME + "createAttributeWhenAttributeExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-def-facility-tests-attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		// create attr definition
		attributesManager.createAttribute(sess, attrDef);
		// store attr def in DB acording namespace
		attributesManager.createAttribute(sess, attrDef);
		// shouldn't add attr def twice

	}

	@Test
	public void deleteAttribute() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttribute");
		for(String entity: AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.keySet()) {
			String namespace = AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.get(entity) + ":def";
			String friendlyName = "test-attr";
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setFriendlyName(friendlyName);
			attrDef.setNamespace(namespace);
			attrDef.setDescription("poznamka");
			attrDef.setType(String.class.getName());

			if (entity.equals("entityless")) {
				// entityless attributes can't be unique
				attrDef.setUnique(false);
			} else {
				attrDef.setUnique(true);
			}

			assertNotNull("unable to create attribute before deletion",attributesManager.createAttribute(sess, attrDef));
			attributesManager.deleteAttribute(sess, attrDef);
			try {
				attributesManager.getAttributeDefinition(sess, namespace + ":" + friendlyName);
			} catch (AttributeNotExistsException ignored) {
				//expected
			}
		}
	}

	@Test
	public void testConvertingToUniqAttribute() throws Exception {
		System.out.println(CLASS_NAME + "testConvertingToUniqAttribute");
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());
		int counter = 1;
		for (String bean : AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.keySet()) {

			// converting entityless is not supported
			if (bean.equals("entityless")) continue;

			for (String type : AttributesManagerImpl.ATTRIBUTE_TYPES) {
				log.debug("conversion to unique bean {} type {}", bean, type);
				String namespace = AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.get(bean) + ":def";
				String friendlyName = "test-conv-attr" + (counter++);
				String description = "poznamka";
				AttributeDefinition attrDef = new AttributeDefinition();
				attrDef.setUnique(false);
				attrDef.setFriendlyName(friendlyName);
				attrDef.setNamespace(namespace);
				attrDef.setDescription(description);
				attrDef.setType(type);
				attributesManager.createAttribute(sess, attrDef);
				AttributeDefinition attributeDefinition = attributesManager.getAttributeDefinition(sess, namespace + ":" + friendlyName);
				assertFalse("attribute marked unique", attributeDefinition.isUnique());
				assertTrue("friendly name not loaded correctly", friendlyName.equals(attributeDefinition.getFriendlyName()));
				assertTrue("namespace not loaded correctly", namespace.equals(attributeDefinition.getNamespace()));
				assertTrue("description not loaded correctly", description.equals(attributeDefinition.getDescription()));

				//create values
				Attribute a = new Attribute(attributeDefinition);
				Attribute b = new Attribute(attributeDefinition);
				switch (type) {
					case "java.lang.String":
					case BeansUtils.largeStringClassName:
						a.setValue("string1");
						b.setValue("string2");
						break;
					case "java.lang.Integer":
						a.setValue(Integer.MIN_VALUE);
						b.setValue(Integer.MAX_VALUE);
						break;
					case "java.lang.Boolean":
						a.setValue(Boolean.FALSE);
						b.setValue(Boolean.TRUE);
						break;
					case "java.util.ArrayList":
					case BeansUtils.largeArrayListClassName:
						a.setValue(new ArrayList<>(Arrays.asList("value1","value2")));
						b.setValue(new ArrayList<>(Arrays.asList("value3","value4")));
						break;
					case "java.util.LinkedHashMap":
						LinkedHashMap<String,String> m1 = new LinkedHashMap<>();
						m1.put("k1","v1");
						m1.put("k2","v2");
						LinkedHashMap<String,String> m2 = new LinkedHashMap<>();
						m2.put("k4","v4");
						m2.put("k3","v3");
						a.setValue(m1);
						b.setValue(m2);
						break;
					default:
						throw new Exception("unknown type "+type);
				}
				switch (bean) {
					case "user": //
						attributesManager.setAttribute(sess, user1, a);
						attributesManager.setAttribute(sess, user2, b);
						break;
					case "member": //
						attributesManager.setAttribute(sess, member1OfUser1, a);
						attributesManager.setAttribute(sess, member2OfUser1, b);
						break;
					case "facility": //
						attributesManager.setAttribute(sess, facility1, a);
						attributesManager.setAttribute(sess, facility2, b);
						break;
					case "vo": //
						attributesManager.setAttribute(sess, vo1, a);
						attributesManager.setAttribute(sess, vo2, b);
						break;
					case "host": //
						attributesManager.setAttribute(sess, host1OnFacility1, a);
						attributesManager.setAttribute(sess, host2OnFacility2, b);
						break;
					case "group": //
						attributesManager.setAttribute(sess, group1InVo1, a);
						attributesManager.setAttribute(sess, group2InVo2, b);
						break;
					case "resource": //
						attributesManager.setAttribute(sess, resource1InVo1, a);
						attributesManager.setAttribute(sess, resource2InVo2, b);
						break;
					case "member_resource": //
						attributesManager.setAttribute(sess, member1OfUser1, resource1InVo1, a);
						attributesManager.setAttribute(sess, member2OfUser2, resource2InVo1, b);
						break;
					case "member_group":
						attributesManager.setAttribute(sess, member1OfUser1, group1InVo1, a);
						attributesManager.setAttribute(sess, member2OfUser1, group1InVo1, b);
						break;
					case "user_facility":
						attributesManager.setAttribute(sess, facility1, user1, a);
						attributesManager.setAttribute(sess, facility2, user2, b);
						break;
					case "group_resource":
						attributesManager.setAttribute(sess, resource1InVo1, group1InVo1, a);
						attributesManager.setAttribute(sess, resource2InVo2, group2InVo2, b);
						break;
					case "user_ext_source":
						attributesManager.setAttribute(sess, userExtSource1, a);
						attributesManager.setAttribute(sess, userExtSource2, b);
						break;
					default:
						throw new Exception("unknown entity "+bean);
				}
				attributesManager.convertAttributeToUnique(sess,attributeDefinition.getId());
			}
		}
	}

	@Test
	public void testGetPerunBeanIdsForUniqueAttributeValue() throws Exception {
		System.out.println(CLASS_NAME + "testGetPerunBeanIdsForUniqueAttributeValue");
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());
		String bean = "group_resource";
		String type = ArrayList.class.getName();
		String namespace = AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.get(bean) + ":def";
		String friendlyName = "test-getPerunBeanIds-attr";
		String description = "tests getPerunBeanIdsForUniqueAttributeValue() method for ArrayList";
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setUnique(true);
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(namespace);
		attrDef.setDescription(description);
		attrDef.setType(type);
		attributesManager.createAttribute(sess, attrDef);
		AttributeDefinition attributeDefinition = attributesManager.getAttributeDefinition(sess, namespace + ":" + friendlyName);
		//create non-overlaping values for two group-resource pairs
		Attribute a = new Attribute(attributeDefinition);
		Attribute b = new Attribute(attributeDefinition);
		a.setValue(Lists.newArrayList("value1","value2"));
		b.setValue(Lists.newArrayList("value3","value4"));
		attributesManager.setAttribute(sess, resource1InVo1, group1InVo1, a);
		attributesManager.setAttribute(sess, resource2InVo2, group2InVo2, b);
		//try find id for value made by adding overlapping value
		a.setValue(Lists.newArrayList("value1","value2","value3"));
		Set<Pair<Integer, Integer>> pairs = attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, a);
		Pair<Integer, Integer> first = new Pair<>(group1InVo1.getId(),resource1InVo1.getId());
		Pair<Integer, Integer> second = new Pair<>(group2InVo2.getId(),resource2InVo2.getId());
		assertThat("expected two pairs",pairs,equalTo(new HashSet<>(Arrays.asList(first,second))));
	}

	@Test
	public void testUniqAttributes() throws Exception {
		System.out.println(CLASS_NAME + "testUniqAttributes");
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());
		int counter=1;
		for(String bean: AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.keySet()) {

			if (bean.equals("entityless")) continue;

			for(String type: AttributesManagerImpl.ATTRIBUTE_TYPES) {
				log.debug(" uniqueness check bean {} type {}", bean, type);
				String namespace = AttributesManagerImpl.BEANS_TO_NAMESPACES_MAP.get(bean) + ":def";
				String friendlyName = "test-attr"+(counter++);
				String description = "poznamka";
				AttributeDefinition attrDef = new AttributeDefinition();
				attrDef.setUnique(true);
				attrDef.setFriendlyName(friendlyName);
				attrDef.setNamespace(namespace);
				attrDef.setDescription(description);
				attrDef.setType(type);
				attributesManager.createAttribute(sess, attrDef);
				AttributeDefinition attributeDefinition = attributesManager.getAttributeDefinition(sess, namespace + ":" + friendlyName);
				assertTrue("attribute not marked unique", attributeDefinition.isUnique());
				assertTrue("friendly name not loaded correctly", friendlyName.equals(attributeDefinition.getFriendlyName()));
				assertTrue("namespace not loaded correctly", namespace.equals(attributeDefinition.getNamespace()));
				assertTrue("description not loaded correctly", description.equals(attributeDefinition.getDescription()));
				//test uniqueness check
				Attribute a = new Attribute(attributeDefinition);
				Attribute b = new Attribute(attributeDefinition);
				switch (type) {
					case "java.lang.String":
					case BeansUtils.largeStringClassName:
						a.setValue("samestring");
						b.setValue("samestring");
						break;
					case "java.lang.Integer":
						a.setValue(Integer.MAX_VALUE);
						b.setValue(Integer.MAX_VALUE);
						break;
					case "java.lang.Boolean":
						a.setValue(Boolean.TRUE);
						b.setValue(Boolean.TRUE);
						break;
					case "java.util.ArrayList":
					case BeansUtils.largeArrayListClassName:
						a.setValue(Lists.newArrayList("value1","value2"));
						b.setValue(Lists.newArrayList("value3","value2"));
						break;
					case "java.util.LinkedHashMap":
						LinkedHashMap<String,String> m1 = new LinkedHashMap<>();
						m1.put("k1","v1");
						m1.put("k2","v2");
						LinkedHashMap<String,String> m2 = new LinkedHashMap<>();
						m2.put("k2","v2");
						m2.put("k3","v3");
						a.setValue(m1);
						b.setValue(m2);
						break;
					default:
						throw new Exception("unknown type "+type);
				}
				try {
					switch (bean) {
						case "user": //
							attributesManager.setAttribute(sess, user1, a);
							attributesManager.setAttribute(sess, user2, b);
							break;
						case "member": //
							attributesManager.setAttribute(sess, member1OfUser1, a);
							attributesManager.setAttribute(sess, member2OfUser1, b);
							break;
						case "facility": //
							attributesManager.setAttribute(sess, facility1, a);
							attributesManager.setAttribute(sess, facility2, b);
							break;
						case "vo": //
							attributesManager.setAttribute(sess, vo1, a);
							attributesManager.setAttribute(sess, vo2, b);
							break;
						case "host": //
							attributesManager.setAttribute(sess, host1OnFacility1, a);
							attributesManager.setAttribute(sess, host2OnFacility2, b);
							break;
						case "group": //
							attributesManager.setAttribute(sess, group1InVo1, a);
							attributesManager.setAttribute(sess, group2InVo2, b);
							break;
						case "resource": //
							attributesManager.setAttribute(sess, resource1InVo1, a);
							attributesManager.setAttribute(sess, resource2InVo2, b);
							break;
						case "member_resource": //
							attributesManager.setAttribute(sess, member1OfUser1, resource1InVo1, a);
							attributesManager.setAttribute(sess, member2OfUser2, resource2InVo1, b);
							break;
						case "member_group":
							attributesManager.setAttribute(sess, member1OfUser1, group1InVo1, a);
							attributesManager.setAttribute(sess, member2OfUser1, group1InVo1, b);
							break;
						case "user_facility":
							attributesManager.setAttribute(sess, facility1, user1, a);
							attributesManager.setAttribute(sess, facility2, user2, b);
							break;
						case "group_resource":
							attributesManager.setAttribute(sess, resource1InVo1, group1InVo1, a);
							attributesManager.setAttribute(sess, resource2InVo2, group2InVo2, b);
							break;
						case "user_ext_source":
							attributesManager.setAttribute(sess, userExtSource1, a);
							attributesManager.setAttribute(sess, userExtSource2, b);
							break;
						default:
							throw new Exception("unknown entity "+bean);
					}
					fail("allowed same values for unique attribute of type " + type + " for bean " + bean);
				} catch (WrongAttributeValueException ignored) {
					log.debug("caught expected exception for duplicate values - bean " + bean + " type " + type);
					log.debug("exceptions message: {}",ignored.getMessage());
				}
				switch (bean) {
					case "user": //
						Integer userId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("user with duplicate value is not the one",userId, is(user1.getId()));
						attributesManager.removeAttribute(sess, user1, a);
						attributesManager.setAttribute(sess, user2, b);
						break;
					case "member": //
						Integer memberId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("member with duplicate value is not the one",memberId, is(member1OfUser1.getId()));
						attributesManager.removeAttribute(sess, member1OfUser1, a);
						attributesManager.setAttribute(sess, member2OfUser1, b);
						break;
					case "facility": //
						Integer facilityId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("facility with duplicate value is not the one",facilityId, is(facility1.getId()));
						attributesManager.removeAttribute(sess, facility1, a);
						attributesManager.setAttribute(sess, facility2, b);
						break;
					case "vo": //
						Integer voId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("vo with duplicate value is not the one",voId, is(vo1.getId()));
						attributesManager.removeAttribute(sess, vo1, a);
						attributesManager.setAttribute(sess, vo2, b);
						break;
					case "host": //
						Integer hostId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("host with duplicate value is not the one",hostId, is(host1OnFacility1.getId()));
						attributesManager.removeAttribute(sess, host1OnFacility1, a);
						attributesManager.setAttribute(sess, host2OnFacility2, b);
						break;
					case "group": //
						Integer groupId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("group with duplicate value is not the one",groupId, is(group1InVo1.getId()));
						attributesManager.removeAttribute(sess, group1InVo1, a);
						attributesManager.setAttribute(sess, group2InVo2, b);
						break;
					case "resource": //
						Integer resourceId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("resource with duplicate value is not the one",resourceId, is(resource1InVo1.getId()));
						attributesManager.removeAttribute(sess, resource1InVo1, a);
						attributesManager.setAttribute(sess, resource2InVo2, b);
						break;
					case "member_resource":
						Pair<Integer, Integer> mId_rId = BeansUtils.getSinglePair(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertNotNull("member_resource should not be null", mId_rId);
						assertThat("member with duplicate value is not the one",mId_rId.getLeft(), is(member1OfUser1.getId()));
						assertThat("resource with duplicate value is not the one",mId_rId.getRight(), is(resource1InVo1.getId()));
						attributesManager.removeAttribute(sess, member1OfUser1, resource1InVo1, a);
						attributesManager.setAttribute(sess, member2OfUser2, resource2InVo1, b);
						break;
					case "member_group":
						Pair<Integer,Integer> mId_gId = BeansUtils.getSinglePair(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertNotNull("member_group should not be null", mId_gId);
						assertThat("member with duplicate value is not the one",mId_gId.getLeft(), is(member1OfUser1.getId()));
						assertThat("group with duplicate value is not the one",mId_gId.getRight(), is(group1InVo1.getId()));
						attributesManager.removeAttribute(sess, member1OfUser1, group1InVo1, a);
						attributesManager.setAttribute(sess, member2OfUser1, group1InVo1, b);
						break;
					case "user_facility":
						Pair<Integer,Integer> uId_fId = BeansUtils.getSinglePair(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertNotNull("user_facility should not be null", uId_fId);
						assertThat("user with duplicate value is not the one",uId_fId.getLeft(), is(user1.getId()));
						assertThat("facility with duplicate value is not the one",uId_fId.getRight(), is(facility1.getId()));
						attributesManager.removeAttribute(sess, facility1, user1, a);
						attributesManager.setAttribute(sess, facility2, user2, b);
						break;
					case "group_resource":
						Pair<Integer,Integer> gId_rId = BeansUtils.getSinglePair(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertNotNull("group_resource should not be null", gId_rId);
						assertThat("group with duplicate value is not the one",gId_rId.getLeft(), is(group1InVo1.getId()));
						assertThat("resource with duplicate value is not the one",gId_rId.getRight(), is(resource1InVo1.getId()));
						attributesManager.removeAttribute(sess, resource1InVo1, group1InVo1, a);
						attributesManager.setAttribute(sess, resource2InVo2, group2InVo2, b);
						break;
					case "user_ext_source":
						Integer uesId = BeansUtils.getSingleId(attributesManagerBl.getPerunBeanIdsForUniqueAttributeValue(sess, b));
						assertThat("ues with duplicate value is not the one",uesId, is(userExtSource1.getId()));
						attributesManager.removeAttribute(sess, userExtSource1, a);
						attributesManager.setAttribute(sess, userExtSource2, b);
						break;
					default:
						throw new Exception("unknown entity "+bean);
				}
			}
		}
	}

	@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttributeWhenAttributeNotExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-def-facility-tests-attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());

		attributesManager.deleteAttribute(sess, attrDef);
		// shouldn't find attribute

	}

	@Ignore
	@Test (expected=RelationExistsException.class)
	public void deleteAttributeWhenRelationExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttributeWhenRelationExists");

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
		System.out.println(CLASS_NAME + "deleteAttributeForce");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-def-facility-tests-attr");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setDescription("poznamka");
		attrDef.setType(String.class.getName());
		//
		attributesManager.createAttribute(sess, attrDef);
		assertNotNull("unable to create attribute before deletion",attributesManager.createAttribute(sess, attrDef));

		attributesManager.deleteAttribute(sess, attrDef, true);

		attributesManager.getAttributeDefinition(sess, "urn:perun:facility:attribute-def:opt:attr-def-facility-tests-attr");
		// shouldn't find attribute definition in db

	}

	@Test (expected=AttributeNotExistsException.class)
	public void deleteAttributeForceWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttributeForceWhenAttributeNotExists");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-def-facility-tests-attr");
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
		System.out.println(CLASS_NAME + "deleteAttributeForceWhenRelationExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributesManager.setAttribute(sess, facility, attributes.get(0));
		// setting particular attribute to facility to make relation
		attributesManager.deleteAttribute(sess, attributes.get(0), true);
		// delete assigned attribute
		attributesManager.getAttribute(sess, facility, "urn:perun:facility:attribute-def:opt:facility-test-attribute");
		// shouldn't find attribute because force deleted

	}


























// ==============  8. GET REQUIRED ATTRIBUTES ================================

	@Test
	public void getRequiredFacilityAttributesForItsServices() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesForItsServices");

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
		System.out.println(CLASS_NAME + "setRequiredAttributesForMemberResourceFacilityUser");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		List<Attribute> attributes = new ArrayList<>();
		attributes.addAll(setUpMemberAttribute());
		attributes.addAll(setUpUserAttribute());
		attributes.addAll(setUpMemberResourceAttribute());
		attributes.addAll(setUpFacilityUserAttribute());
		perun.getResourcesManager().assignService(sess, resource, service);

		perun.getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member);
	}

	@Test
	public void getResourceRequiredGroupResourceAndGroupAttributesForItsServices() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesForItsServices");

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
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesForItsServicesWhenFacilityNotExists");

		attributesManager.getRequiredAttributes(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getRequiredResourceAttributesForItsServices() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredResourceAttributesForItsServices");

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
		System.out.println(CLASS_NAME + "getRequiredResourceAttributesForItsServicesWhenResourceNotExists");

		attributesManager.getRequiredAttributes(sess, new Resource());
		// shouldn't find Resource

	}

	@Test
	public void getResourceRequiredMemberResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributes");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, resource);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("should have only 1 req member resource attribute",reqAttr.size() == 1);

	}

	@Test
	public void getServiceRequiredResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getServiceRequiredResourceAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member, resource);
		// shouldn't find resource

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWhenSecondResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWhenSecondResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, member, new Resource());
		// shouldn't find resource

	}

	@Test
	public void getResourceRequiredMemberResourceAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWhenFakeResource");

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

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, resource);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

		reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, fakeResource);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource and no value set",reqAttr.size() == 0);

		reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, fakeResource);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Should return 1 attribute (but with no value)",reqAttr.size() == 1);

	}


	@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member(), resource);
		// shouldn't find member

	}

	@Test
	public void getResourceRequiredMemberResourceAttributesWorkWithUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesWorkWithUserAttributes");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, resource, true);
		assertNotNull("unable to get required member resource (work with user) attributes for its services",reqAttr);
		assertTrue("should have more than 1 req attribute",reqAttr.size() >= 1);

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWorkWithUserWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member, resource, true);
		// shouldn't find resource

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenSecondResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWorkWithUserWhenSecondResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, member, new Resource(), true);
		// shouldn't find resource

	}

	@Test
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberResourceAttributesWorkWithUserWhenFakeResource");

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

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, resource, true);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

		reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, fakeResource, true);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource and no value set",reqAttr.size() == 0);

		reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, fakeResource, true);
		assertNotNull("unable to get required member resource attributes for its services",reqAttr);
		assertTrue("Should return 4 attributes (but with no value)",reqAttr.size() == 4);
		// member_resource, user_facility, user, member

	}

	@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberResourceAttributesWorkWithUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesWorkWithUserWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member(), resource, true);
		// shouldn't find member
	}

	@Test
	public void getResourceRequiredMemberGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributes");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, group);
		assertNotNull("unable to get required member group attributes for its services", reqAttr);
		assertTrue("should have only 1 req member group attribute", reqAttr.size() == 1);
	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member, group);
		// shouldn't find resource
	}

	@Test (expected=GroupNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, member, new Group());
		// shouldn't find group
	}

	@Test
	public void getResourceRequiredMemberGroupAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWhenFakeResource");

		vo = setUpVo();
		group = setUpGroup();
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

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, group);
		assertNotNull("unable to get required member group attributes for its services", reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource", reqAttr.size() == 0);
	}


	@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member(), group);
		// shouldn't find member
	}

	@Test
	public void getResourceRequiredMemberGroupAttributesWorkWithUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberGroupAttributesWorkWithUserAttributes");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, member, group, true);
		assertNotNull("unable to get required member group (work with user) attributes for its services", reqAttr);
		assertTrue("should have more than 1 req attribute", reqAttr.size() >= 1);
	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWorkWithUserWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWorkWithUserWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member, group, true);
		// shouldn't find resource
	}

	@Test (expected=GroupNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWorkWithUserWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWorkWithUserWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, member, new Group(), true);
		// shouldn't find group
	}

	@Test
	public void getResourceRequiredMemberGroupAttributesWorkWithUserWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberGroupAttributesWorkWithUserWhenFakeResource");

		vo = setUpVo();
		group = setUpGroup();
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

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, member, group, true);
		assertNotNull("unable to get required member group attributes for its services", reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource", reqAttr.size() == 0);
	}

	@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberGroupAttributesWorkWithUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberGroupAttributesWorkWithUserWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member(), group, true);
		// shouldn't find member
	}

	@Test
	public void getResourceRequiredFacilityUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredFacilityUserAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredFacilityUserAttributesWhenFacilityNotExists");

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
		System.out.println(CLASS_NAME + "getResourceRequiredFacilityUserAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), facility, user);
		// shouldn't find resource

	}

	@Test (expected=UserNotExistsException.class)
	public void getResourceRequiredFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredFacilityUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, facility, new User());
		// shouldn't find user

	}

	@Test
	public void getResourceRequiredFacilityUserAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredFacilityUserAttributesWhenFakeResource");

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
		System.out.println(CLASS_NAME + "getResourceRequiredMemberAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredMemberAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), member);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getResourceRequiredMemberAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Member());
		// shouldn't find member

	}

	@Test
	public void getResourceRequiredMemberAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredMemberAttributesWhenFakeResource");

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
		System.out.println(CLASS_NAME + "getResourceRequiredUserAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredUserAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), user);
		// shouldn't find resource

	}

	@Test (expected=UserNotExistsException.class)
	public void getResourceRequiredUserAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new User());
		// shouldn't find member

	}

	@Test
	public void getResourceRequiredUserAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredUserAttributesWhenFakeResource");

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
		System.out.println(CLASS_NAME + "getResourceRequiredGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), resource,  group);
		// shouldn't find resource

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getResourceRequiredGroupResourceAttributesWhenSecondResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredGroupResourceAttributesWhenSecondResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Resource(),  group);
		// shouldn't find resource

	}

	@Test
	public void getResourceRequiredGroupResourceAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredGroupResourceAttributesWhenFakeResource");

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
		System.out.println(CLASS_NAME + "getResourceRequiredGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, resource, new Group());
		// shouldn't find group

	}

	@Test
	public void getResourceRequiredGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredGroupAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredGroupAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void getResourceRequiredGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredGroupAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Group());
		// shouldn't find group

	}

	@Test
	public void getResourceRequiredGroupAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredGroupAttributesWhenFakeResource");

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
		System.out.println(CLASS_NAME + "getResourceRequiredHostAttributes");

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
		System.out.println(CLASS_NAME + "getResourceRequiredHostAttributesWhenResourceNotExists");

		host = setUpHost().get(0);

		attributesManager.getResourceRequiredAttributes(sess, new Resource(), host);
		// shouldn't find resource

	}

	@Test (expected=HostNotExistsException.class)
	public void getResourceRequiredHostAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredHostAttributesWhenHostNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getResourceRequiredAttributes(sess, resource, new Host());
		// shouldn't find host

	}

	@Test
	public void getResourceRequiredHostAttributesWhenFakeResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceRequiredHostAttributesWhenFakeResource");

		host = setUpHost().get(0); // also creates cluster type facility
		vo = setUpVo();
		Resource fakeResource = setUpResource(); // without service

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, fakeResource, host);
		assertNotNull("unable to get required host attributes for resource",reqAttr);
		assertTrue("Shouldn't return attribute, when there is no service on resource",reqAttr.size() == 0);

	}

	@Test
	public void getResourceRequiredMemberGroupResourceAttributesWorkWithUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceGroupAttributesWorkWithUserAttributes");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getResourceRequiredAttributes(sess, resource, resource, group, member, true);
		assertNotNull("unable to get required (work with user) member resource / member group attributes for its services", reqAttr);
		assertTrue("should have more than 2 req attributes",reqAttr.size() >= 2);

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
		System.out.println(CLASS_NAME + "getRequiredAttributesDefinition");

		service = setUpService();
		attributes = setUpRequiredAttributes();

		List<AttributeDefinition> reqAttr = attributesManager.getRequiredAttributesDefinition(sess, service);
		assertNotNull("unable to get required services attribute definition",reqAttr);
		assertTrue("should have at least 7 req attribute definitions",reqAttr.size() >= 7);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getRequiredAttributesDefinitionWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredAttributesDefinitionWhenServiceNotExists");

		attributesManager.getRequiredAttributesDefinition(sess, new Service());
		// shouldn't find service

	}

	@Test
	public void getRequiredFacilityAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesFromOneServiceWhenServiceNotExists");

		attributesManager.getRequiredAttributes(sess, new Service(), facility);
		// shouldn't find service

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getRequiredFacilityAttributesFromOneServiceWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredFacilityAttributesFromOneServiceWhenFacilityNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Facility());
		// shouldn't find service

	}

	@Test
	public void getRequiredResourceAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredResourceAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, new Service(), resource);
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getRequiredResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Resource());
		// shouldn't find resource

	}

	@Test
	public void getRequiredMemberResourceAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneService");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, member, resource);
		assertNotNull("unable to get required resource-member attributes for one service",reqAttr);
		assertTrue("should have only 1 req attribute",reqAttr.size() == 1);

	}

	@Test
	public void getRequiredMembersResourceAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMembersResourceAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), member, resource);
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, service, member, new Resource());
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWhenMemberNotExists");


		vo = setUpVo();
		member = setUpMember();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, new Member(), resource);
		// shouldn't find member

	}

	@Test
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUser() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWorkWithUser");

		vo = setUpVo();
		member = setUpMember();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, member, resource, true);
		assertNotNull("unable to get required resource-member attributes for one service",reqAttr);
		assertTrue("should have at least 4 req attributes",reqAttr.size() >= 4);

	}

	@Test
	public void getRequiredMemberResourceGroupAttributesFromOneServiceWorkWithUser() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceGroupAttributesFromOneServiceWorkWithUser");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, resource, group, member, true);
		assertNotNull("unable to get required attributes for one service",reqAttr);
		assertTrue("should have at least 5 req attributes",reqAttr.size() >= 5);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), member, resource, true);
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, service, member, new Resource(), true);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberResourceAttributesFromOneServiceWorkWithUserWhenMemberNotExists");


		vo = setUpVo();
		member = setUpMember();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, new Member(), resource, true);
		// shouldn't find member

	}

	@Test
	public void getRequiredMembersAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredMemberAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredMemberAndUserAttributesFromOneService");

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
		assertEquals("getRequiredAttributes(sess, member, true) returns wrong count of attributes", 2, reqAttr.size());


	}

	@Test
	public void getRequiredMemberGroupAndUserAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberGroupAndUserAttributesFromOneService");

		vo = setUpVo();
		member = setUpMember();
		group = setUpGroup();
		facility = setUpFacility();
		resource = setUpResource();

		this.setUpMemberToResource();

		service = setUpService();
		attributes = setUpRequiredAttributes();
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, member, group, true);
		assertNotNull("unable to get required user / member / member-group attributes for one service", reqAttr);
		assertEquals("getRequiredAttributes(sess, member, group, true) returns wrong count of attributes", 3, reqAttr.size());

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getRequiredMemberAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.getRequiredAttributes(sess, new Service(), member);
		// shouldn't find service

	}

	@Test (expected=MemberNotExistsException.class)
	public void getRequiredMemberAttributesFromOneServiceWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredMemberAttributesFromOneServiceWhenMemberNotExists");

		service = setUpService();

		attributesManager.getRequiredAttributes(sess, service, new Member());
		// shouldn't find member

	}

	@Test
	public void getRequiredGroupResourceAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredGroupResourceAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredGroupResourceAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, new Service(), resource, group);
		// shouldn't find service

	}

	@Test (expected=ResourceNotExistsException.class)
	public void getRequiredGroupResourceAttributesFromOneServiceWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredGroupResourceAttributesFromOneServiceWhenResourceNotExists");

		service = setUpService();
		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, service, member, new Resource());
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void getRequiredGroupResourceAttributesFromOneServiceWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredGroupResourceAttributesFromOneServiceWhenGroupNotExists");


		vo = setUpVo();
		service = setUpService();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.getRequiredAttributes(sess, service, resource, new Group());
		// shouldn't find group

	}

	@Test
	public void getRequiredGroupAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredGroupAttributesFromOneService");

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
		System.out.println(CLASS_NAME + "getRequiredGroupAttributesFromOneServiceWhenGroupNotExists");

		service = setUpService();
		attributes = setUpRequiredAttributes();

		attributesManager.getRequiredAttributes(sess, service, new Group());
		// shouldn't find group

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getRequiredGroupAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredGroupAttributesFromOneServiceWhenServiceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.getRequiredAttributes(sess, new Service(), group);
		// shouldn't find service

	}

// TODO - není metoda na získání pouze req. user atributů z 1 service

	@Test
	public void getRequiredHostAttributesFromOneService() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredHostAttributesFromOneService");

		host = setUpHost().get(0);  // also creates cluster type facility
		service = setUpService();
		attributes = setUpRequiredAttributes();

		List<Attribute> reqAttr = attributesManager.getRequiredAttributes(sess, service, host);
		assertNotNull("Unable to get required host attributes for one service",reqAttr);
		assertTrue("There should be 1 required host attribute",reqAttr.size() == 1);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getRequiredHostAttributesFromOneServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredHostAttributesFromOneServiceWhenServiceNotExists");

		host = setUpHost().get(0);  // also creates cluster type facility

		attributesManager.getRequiredAttributes(sess, new Service(), host);
		// shouldn't find service

	}

	@Test (expected=HostNotExistsException.class)
	public void getRequiredHostAttributesFromOneServiceWhenHostNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getRequiredHostAttributesFromOneServiceWhenHostNotExists");

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
		System.out.println(CLASS_NAME + "removeFacilityAttribute");

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
		System.out.println(CLASS_NAME + "removeFacilityAttributeWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, new Facility(), attributes.get(0));
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributeWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, facility, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributeWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, facility, attributes.get(0));
		// shouldn't find vo attribute on facility

	}

	@Test
	public void removeFacilityAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributes");

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
		System.out.println(CLASS_NAME + "removeUserMemberResourceToMemberAndUserToFacilityAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		member = setUpMember();
		User user = sess.getPerun().getUsersManager().getUserByMember(sess, member);
		resource = setUpResource();

		List<Attribute> attributes_user = setUpUserAttribute();
		List<Attribute> attributes_member = setUpMemberAttribute();
		List<Attribute> attributes_user_facility = setUpFacilityUserAttribute();
		List<Attribute> attributes_member_resource = setUpMemberResourceAttribute();

		List<Attribute> attributes = new ArrayList<>();
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
	public void removeUserMemberResourceMemberGroupAndUserFacilityAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeUserMemberResourceMemberGroupAndUserFacilityAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributes = setUpUserAttribute();
		attributes.addAll(setUpMemberGroupAttribute());
		attributes.addAll(setUpMemberResourceAttribute());
		attributes.addAll(setUpFacilityUserAttribute());

		attributesManager.setAttributes(sess, facility, resource, group, user, member, attributes);
		List<Attribute> retAttr = attributesManager.getAttributes(sess, facility, resource, user, member);
		retAttr.addAll(attributesManager.getAttributes(sess, member, group));

		for(Attribute a : attributes) {
			assertTrue("our member or user does have this attribute set: " + a, retAttr.contains(a));
		}

		//remove all new attributes
		attributesManager.removeAttributes(sess, facility, resource, group, user, member, attributes);

		//check if all are removed
		retAttr = attributesManager.getAttributes(sess, facility, resource, user, member);
		retAttr.addAll(attributesManager.getAttributes(sess, member, group));

		for(Attribute a : attributes) {
			assertFalse("our member or user does not have this attribute set: " + a, retAttr.contains(a));
		}

	}

	@Test
	public void removeEntitylessAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeEntitylessAttribute");
		attributes = setUpEntitylessAttribute();
		String key = "Test123456";
		attributesManager.setAttribute(sess, key, attributes.get(0));
		attributesManager.removeAttribute(sess, key, attributes.get(0));
		List<Attribute> retAttr = attributesManager.getAttributes(sess, key);
		assertFalse("There should not been set this entityless attribute, because it was removed.",retAttr.contains(attributes.get(0)));
	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeFacilityAttributesWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributesWhenFacilityNotExists");

		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, new Facility(), attributes);
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributesWhenAttributeNotExists");

		facility = setUpFacility();
		attributes = setUpFacilityAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, facility, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeFacilityAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityAttributesWhenWrongAttrAssignment");

		facility = setUpFacility();
		attributes = setUpVoAttribute();
		attributesManager.removeAttributes(sess, facility, attributes);
		// shouldn't find vo attribute on facility

	}

	@Test
	public void removeAllGroupResourceAndGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeAllGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeAllFacilityAttributes");

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
		System.out.println(CLASS_NAME + "removeAllFacilityAttributesExceptUserFacilityAttributes");

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
		System.out.println(CLASS_NAME + "removeAllFacilityAttributesExceptUserFacilityAttributes");

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
		System.out.println(CLASS_NAME + "removeAllFacilityAttributesWhenFacilityNotExists");

		attributesManager.removeAllAttributes(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void removeVoAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttribute");

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
		System.out.println(CLASS_NAME + "removeVoAttributeWhenVoNotExists");

		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, new Vo(), attributes.get(0));
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeVoAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttributeWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, vo, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeVoAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, vo, attributes.get(0));
		// shouldn't find facility attribute on vo

	}

	@Test
	public void removeVoAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttributes");

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
		System.out.println(CLASS_NAME + "removeVoAttributesWhenVoNotExists");

		attributes = setUpVoAttribute();
		attributesManager.removeAttributes(sess, new Vo(), attributes);
		// shouldn't find vo

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeVoAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttributesWhenAttributeNotExists");

		vo = setUpVo();
		attributes = setUpVoAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, vo, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeVoAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeVoAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, vo, attributes);
		// shouldn't find facility attribute on vo

	}

	@Test
	public void removeAllVoAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllVoAttributes");

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
		System.out.println(CLASS_NAME + "removeAllVoAttributesWhenVoNotExists");

		attributesManager.removeAllAttributes(sess, new Vo());
		// shouldn't find vo

	}

	@Test
	public void removeResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeResourceAttribute");

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
		System.out.println(CLASS_NAME + "removeResourceAttributeWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		attributesManager.removeAttribute(sess, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeResourceAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, resource, attributes.get(0));
		// shouldn't find facility attribute on resource

	}

	@Test
	public void removeResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeResourceAttributesWhenResourceNotExists");

		attributes = setUpResourceAttribute();
		attributesManager.removeAttributes(sess, new Resource(), attributes);
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeResourceAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeResourceAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, resource, attributes);
		// shouldn't find facility attribute on resource

	}

	@Test
	public void removeAllResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeAllResourceAttributesWhenResourceNotExists");

		attributesManager.removeAllAttributes(sess, new Resource());
		// shouldn't find resource

	}

	@Test
	public void removeMemberResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttribute");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// create member-resource and set attribute with value
		attributesManager.removeAttribute(sess, member, resource, attributes.get(0));
		// remove attribute from member-resource (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource);
		assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeMemberResourceAttributeWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributeWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttribute(sess, member, new Resource(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void removeMemberResourceAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributeWhenMemberNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttribute(sess, new Member(), resource, attributes.get(0));
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributeWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, member, resource, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberResourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, member, resource, attributes.get(0));
		// shouldn't find facility attribute on member-resource

	}

	@Test
	public void removeMemberResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// create member-resource and set attribute with value
		attributesManager.removeAttributes(sess, member, resource, attributes);
		// remove attributes from member-resource (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource);
		assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributesWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttributes(sess, member, new Resource(), attributes);
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void removeMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributesWhenMemberNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttributes(sess, new Member(), resource, attributes);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributesWhenAttributeNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, member, resource, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberResourceAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberResourceAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, member, resource, attributes);
		// shouldn't find facility attribute on member-resource

	}

	@Test
	public void removeAllMemberResourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberResourceAttributes");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		member = setUpMember();
		attributes = setUpMemberResourceAttribute();
		attributesManager.setAttribute(sess, member, resource, attributes.get(0));
		// create member-resource and set attribute with value
		attributesManager.removeAllAttributes(sess, member, resource);
		// remove all attributes from member-resource (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, resource);
		assertFalse("our member-resource shouldn't have set our attribute",retAttr.contains(attributes.get(0)));
		// member-resource don't have core attributes ??

	}

	@Test (expected=ResourceNotExistsException.class)
	public void removeAllMemberResourceAttributesWhenResourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.removeAllAttributes(sess, member, new Resource());
		// shouldn't find resource

	}

	@Test (expected=MemberNotExistsException.class)
	public void removeAllMemberResourceAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberResourceAttributesWhenMemberNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.removeAllAttributes(sess, new Member(), resource);
		// shouldn't find member

	}

	@Test
	public void removeMemberGroupAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttribute");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// create member-group and set attribute with value
		attributesManager.removeAttribute(sess, member, group, attributes.get(0));
		// remove attribute from member-group (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertFalse("our member-group shouldn't have set our attribute", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=GroupNotExistsException.class)
	public void removeMemberGroupAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributeWhenGroupNotExists");

		attributes = setUpMemberGroupAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttribute(sess, member, new Group(), attributes.get(0));
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void removeMemberGroupAttributeWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributeWhenMemberNotExists");

		attributes = setUpMemberGroupAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttribute(sess, new Member(), group, attributes.get(0));
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, member, group, attributes.get(0));
		// shouldn't find attribute
	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, member, group, attributes.get(0));
		// shouldn't find facility attribute on member-group
	}

	@Test
	public void removeMemberGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributes");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// create member-group and set attribute with value
		attributesManager.removeAttributes(sess, member, group, attributes);
		// remove attributes from member-group (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertFalse("our member-group shouldn't have set our attribute", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=GroupNotExistsException.class)
	public void removeMemberGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributesWhenGroupNotExists");

		attributes = setUpMemberGroupAttribute();
		vo = setUpVo();
		member = setUpMember();
		attributesManager.removeAttributes(sess, member, new Group(), attributes);
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void removeMemberGroupAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributesWhenMemberNotExists");

		attributes = setUpMemberGroupAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttributes(sess, new Member(), group, attributes);
		// shouldn't find member
	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpResourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, member, group, attributes);
		// shouldn't find attribute
	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberGroupAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberGroupAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, member, group, attributes);
		// shouldn't find facility attribute on member-group
	}

	@Test
	public void removeAllMemberGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberGroupAttributes");

		vo = setUpVo();
		group = setUpGroup();
		member = setUpMember();
		attributes = setUpMemberGroupAttribute();
		attributesManager.setAttribute(sess, member, group, attributes.get(0));
		// create member-group and set attribute with value
		attributesManager.removeAllAttributes(sess, member, group);
		// remove all attributes from member-group (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, member, group);
		assertFalse("our member-group shouldn't have set our attribute", retAttr.contains(attributes.get(0)));
	}

	@Test (expected=GroupNotExistsException.class)
	public void removeAllMemberGroupAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberGroupAttributesWhenGroupNotExists");

		vo = setUpVo();
		member = setUpMember();

		attributesManager.removeAllAttributes(sess, member, new Group());
		// shouldn't find group
	}

	@Test (expected=MemberNotExistsException.class)
	public void removeAllMemberGroupAttributesWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberGroupAttributesWhenMemberNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.removeAllAttributes(sess, new Member(), group);
		// shouldn't find member
	}

	@Test
	public void removeMemberAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttribute");

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
		System.out.println(CLASS_NAME + "removeMemberAttributeWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		attributesManager.removeAttribute(sess, new Member(), attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttributeWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, member, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, member, attributes.get(0));
		// shouldn't find facility attribute on member

	}

	@Test
	public void removeMemberAttributesWorkWithUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttributesWorkWithUserAttributes");
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
		System.out.println(CLASS_NAME + "removeMemberAttributesWorkWithoutUserAtributes");
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
		System.out.println(CLASS_NAME + "removeMemberAttributes");

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
		System.out.println(CLASS_NAME + "removeMemberAttributesWhenMemberNotExists");

		attributes = setUpMemberAttribute();
		attributesManager.removeAttributes(sess, new Member(), attributes);
		// shouldn't find member

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeMemberAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttributesWhenAttributeNotExists");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpMemberAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, member, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeMemberAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, member, attributes);
		// shouldn't find facility attribute on member

	}

	@Test
	public void removeAllMemberAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllMemberAttributes");

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
		System.out.println(CLASS_NAME + "removeAllMemberAttributesWhenMemberNotExists");

		attributesManager.removeAllAttributes(sess, new Member());
		// shouldn't find member

	}

	@Test
	public void removeFacilityUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityUserAttribute");

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
		System.out.println(CLASS_NAME + "removeFacilityUserResourceAttributeWhenFacilityNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.removeAttribute(sess, new Facility(), user, attributes.get(0));
		// shouldn't find facility

	}

	@Test (expected=UserNotExistsException.class)
	public void removeFacilityUserAttributeWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityUserAttributeWhenUserNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		attributesManager.removeAttribute(sess, facility, new User(), attributes.get(0));
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityUserAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeFacilityUserAttributeWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "removeFacilityUserAttributes");

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
		System.out.println(CLASS_NAME + "removeFacilityUserAttributesWhenFacilityNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributesManager.removeAttributes(sess, new Facility(), user, attributes);
		// shouldn't find facility

	}

	@Test (expected=UserNotExistsException.class)
	public void removeFacilityUserResourceAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityUserAttributesWhenUserNotExists");

		attributes = setUpFacilityUserAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		attributesManager.removeAttributes(sess, facility, new User(), attributes);
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeFacilityUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityUserAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeFacilityUserAttributesWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "removeAllFacilityUserAttributes");

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
		System.out.println(CLASS_NAME + "removeAllFacilityUserAttributesWhenFacilityNotExists");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		attributesManager.removeAllAttributes(sess, new Facility(), user);
		// shouldn't find facility

	}

	@Test (expected=UserNotExistsException.class)
	public void removeAllFacilityUserAttributesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllFacilityUserAttributesWhenUserNotExists");

		vo = setUpVo();
		facility = setUpFacility();

		attributesManager.removeAllAttributes(sess, facility, new User());
		// shouldn't find user

	}

	@Test
	public void removeUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeUserAttribute");

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
		System.out.println(CLASS_NAME + "removeUserAttributeWhenUserNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttribute(sess, new User(), attributes.get(0));
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeUserAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeUserAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, user, attributes.get(0));
		// shouldn't find facility attribute on user

	}

	@Test
	public void removeUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeUserAttributes");

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
		System.out.println(CLASS_NAME + "removeUserAttributesWhenUserNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttributes(sess, new User(), attributes);
		// shouldn't find user

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeUserAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeUserAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, user, attributes);
		// shouldn't find facility attribute on user

	}

	@Test
	public void removeAllUserAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllUserAttributes");

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
		System.out.println(CLASS_NAME + "removeAllUserAttributesWhenUserNotExists");

		attributesManager.removeAllAttributes(sess, new User());
		// shouldn't find user

	}

	@Test
	public void removeGroupAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttribute");

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
		System.out.println(CLASS_NAME + "removeGroupAttributeWhenGroupNotExists");

		attributes = setUpGroupAttribute();
		attributesManager.removeAttribute(sess, new Group(), attributes.get(0));
		// shouldn't find facility

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeGroupAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttributeWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, group, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttributeWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpVoAttribute();
		attributesManager.removeAttribute(sess, group, attributes.get(0));
		// shouldn't find vo attribute on group

	}

	@Test
	public void removeGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttributes");

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
		System.out.println(CLASS_NAME + "removeGroupAttributesWhenGroupNotExists");

		attributes = setUpUserAttribute();
		attributesManager.removeAttributes(sess, new Group(), attributes);
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeGroupAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttributesWhenAttributeNotExists");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpGroupAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, group, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeGroupAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupAttributesWhenWrongAttrAssignment");

		vo = setUpVo();
		group = setUpGroup();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, group, attributes);
		// shouldn't find facility attribute on group

	}

	@Test
	public void removeAllGroupAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllGroupAttributes");

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
		System.out.println(CLASS_NAME + "removeAllGroupAttributesWhenGroupNotExists");

		attributesManager.removeAllAttributes(sess, new Group());
		// shouldn't find group

	}

	@Test
	public void removeGroupResourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupResourceAttribute");

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
		System.out.println(CLASS_NAME + "removeGroupResourceAttributeWhenResourceNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttribute(sess, new Resource(), group, attributes.get(0));
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeGroupResourceAttributeWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupResourceAttributeWhenGroupNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttribute(sess, resource, new Group(), attributes.get(0));
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeGroupResourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupResourceAttributeWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeGroupResourceAttributeWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "removeGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeGroupResourceAttributesWhenResourceNotExists");

		attributes = setUpMemberResourceAttribute();
		vo = setUpVo();
		group = setUpGroup();
		attributesManager.removeAttributes(sess, new Resource(), group, attributes);
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupResourceAttributesWhenGroupNotExists");

		attributes = setUpGroupResourceAttribute();
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		attributesManager.removeAttributes(sess, resource, new Group(), attributes);
		// shouldn't find group

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeGroupResourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupResourceAttributesWhenAttributeNotExists");

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
		System.out.println(CLASS_NAME + "removeGroupResourceAttributesWhenWrongAttrAssignment");

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
		System.out.println(CLASS_NAME + "removeAllGroupResourceAttributes");

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
		System.out.println(CLASS_NAME + "removeAllGroupResourceAttributesWhenResourceNotExists");

		vo = setUpVo();
		group = setUpGroup();

		attributesManager.removeAllAttributes(sess, new Resource(), group);
		// shouldn't find resource

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeAllGroupResourceAttributesWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllGroupResourceAttributesWhenGroupNotExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		attributesManager.removeAllAttributes(sess, resource, new Group());
		// shouldn't find group

	}

	@Test
	public void removeHostAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttribute");

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
		System.out.println(CLASS_NAME + "removeHostAttributeWhenHostNotExists");

		attributes = setUpHostAttribute();
		attributesManager.removeAttribute(sess, new Host(), attributes.get(0));
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeHostAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttributeWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, host, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeHostAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttributeWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, host, attributes.get(0));
		// shouldn't find facility attribute on host

	}

	@Test
	public void removeHostAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttributes");

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
		System.out.println(CLASS_NAME + "removeHostAttributesWhenHostNotExists");

		attributes = setUpHostAttribute();
		attributesManager.removeAttributes(sess, new Host(), attributes);
		// shouldn't find host

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeHostAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttributesWhenAttributeNotExists");

		host = setUpHost().get(0);
		attributes = setUpHostAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, host, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeHostAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeHostAttributesWhenWrongAttrAssignment");

		host = setUpHost().get(0);
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, host, attributes);
		// shouldn't find facility attribute on host

	}

	@Test
	public void removeAllHostAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllHostAttributes");

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
		System.out.println(CLASS_NAME + "removeAllHostAttributesWhenHostNotExists");

		attributesManager.removeAllAttributes(sess, new Host());
		// shouldn't find host

	}

	@Test
	public void removeUserExtSourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttribute");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// create user external source and set attribute with value
		attributesManager.removeAttribute(sess, ues, attributes.get(0));
		// remove attribute from user external source (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, ues);
		assertFalse("our user external source shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeUserExtSourceAttributeWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributeWhenUserExtSourceNotExists");

		attributes = setUpUserExtSourceAttribute();
		attributesManager.removeAttribute(sess, setUpUserExtSource(), attributes.get(0));
		// shouldn't find user external source

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeUserExtSourceAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributeWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttribute(sess, ues, attributes.get(0));
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeUserExtSourceAttributeWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributeWhenWrongAttrAssignment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttribute(sess, ues, attributes.get(0));
		// shouldn't find facility attribute on user external source

	}

	@Test
	public void removeUserExtSourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributes");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// create userExtSource and set attribute with value
		attributesManager.removeAttributes(sess, ues, attributes);
		// remove attributes from resource (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, ues);
		assertFalse("our user external source shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeUserExtSourceAttributesWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributesWhenUserExtSourceNotExists");

		attributes = setUpUserExtSourceAttribute();
		UserExtSource ues = setUpUserExtSource();
		attributesManager.removeAttributes(sess, ues, attributes);
		// shouldn't find user external source

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeUserExtSourceAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributesWhenAttributeNotExists");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributes.get(0).setId(0);
		attributesManager.removeAttributes(sess, ues, attributes);
		// shouldn't find attribute

	}

	@Test (expected=WrongAttributeAssignmentException.class)
	public void removeUserExtSourceAttributesWhenWrongAttrAssignment() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceAttributesWhenWrongAttrAssignment");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpFacilityAttribute();
		attributesManager.removeAttributes(sess, ues, attributes);
		// shouldn't find facility attribute on user external source

	}

	@Test
	public void removeAllUserExtSourceAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllUserExtSourceAttributes");

		UserExtSource ues = setUpUserExtSourceTest();
		attributes = setUpUserExtSourceAttribute();
		attributesManager.setAttribute(sess, ues, attributes.get(0));
		// create user external source and set attribute with value
		attributesManager.removeAllAttributes(sess, ues);
		// remove all attributes from user external source (definition or attribute)
		List<Attribute> retAttr = attributesManager.getAttributes(sess, ues);
		assertFalse("our user external source shouldn't have set our attribute",retAttr.contains(attributes.get(0)));

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeAllUserExtSourceAttributesWhenUserExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllUserExtSourceAttributesWhenUserExtSourceNotExists");

		attributesManager.removeAllAttributes(sess, setUpUserExtSource());
		// shouldn't find user external source

	}









// ==============  12. REST CHECK METHODS ================================




	@Test
	public void isCoreAttribute() {
		System.out.println(CLASS_NAME + "isCoreAttribute");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-manager-test-attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		assertFalse("opt attribute is considered core!!",attributesManager.isCoreAttribute(sess, attrDef));
		attrDef.setNamespace("urn:perun:facility:attribute-def:core");
		assertTrue("core attribute is not considered core!!",attributesManager.isCoreAttribute(sess, attrDef));

	}


	@Test
	public void isOptAttribute() {
		System.out.println(CLASS_NAME + "isOptAttribute");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-manager-test-attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		assertTrue("opt attribute is not considered opt!!",attributesManager.isOptAttribute(sess, attrDef));
		attrDef.setNamespace("urn:perun:facility:attribute-def:core");
		assertFalse("core attribute is considered opt!!",attributesManager.isOptAttribute(sess, attrDef));

	}

	@Ignore
	@Test
	public void isCoreManagedAttribute() {
		System.out.println(CLASS_NAME + "isCoreManagedAttribute");
		// TODO co je míněno core managed attributem ??
	/*
		 AttributeDefinition attrDef = new AttributeDefinition();
		 attrDef.setFriendlyName("attr-manager-test-attribute");
		 attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		 attrDef.setType(String.class.getName());
		 attrDef.setDescription("AttributesManagerTest");

		 assertTrue("opt attribute is not considered opt!!",attributesManager.isCoreAttribute(sess, attrDef));
		 attrDef.setNamespace("urn:perun:facility:attribute-def:core");
		 assertFalse("core attribute is considered opt!!",attributesManager.isCoreAttribute(sess, attrDef));
		 */
	}


	@Test
	public void isFromNamespace() {
		System.out.println(CLASS_NAME + "isFromNamespace");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-manager-test-attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		assertTrue("bad recognition of attribute namespace",attributesManager.isFromNamespace(sess, attrDef, "urn:perun:facility:attribute-def:opt"));
		attrDef.setNamespace("urn:perun:facility:attribute-def:core");
		assertFalse("bad recognition of attribute namespace",attributesManager.isFromNamespace(sess, attrDef, "urn:perun:facility:attribute-def:opt"));

	}


	@Test (expected=WrongAttributeAssignmentException.class)
	public void checkNamespace() throws Exception {
		System.out.println(CLASS_NAME + "checkNamespace");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-manager-test-attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		attributesManager.checkNamespace(sess, attrDef, "urn:perun:facility:attribute-def:core");
		// should throw exception - wrong attr assignment

	}

	@Test
	public void checkNamespaceList() throws Exception {
		System.out.println(CLASS_NAME + "checkNamespaceList");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setFriendlyName("attr-manager-test-attribute");
		attrDef.setNamespace("urn:perun:facility:attribute-def:opt");
		attrDef.setType(String.class.getName());
		attrDef.setDescription("AttributesManagerTest");

		Attribute attribute = new Attribute(attrDef);

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attribute);

		attributesManager.checkNamespace(sess, attributes, "urn:perun:facility:attribute-def:opt");

	}

	@Test
	public void getNamespaceFromAttributeName() throws Exception {
		System.out.println(CLASS_NAME + "getNamespaceFromAttributeName");

		String nameSpace = attributesManager.getNamespaceFromAttributeName("urn:perun:facility:attribute-def:opt:attr-manager-test-attribute");
		assertTrue("get wrong namespace from name",nameSpace.equals("urn:perun:facility:attribute-def:opt"));

	}

	@Test
	public void getFriendlyNameFromAttributeName() throws Exception {
		System.out.println(CLASS_NAME + "getFriendlyNameFromAttributeName");

		String nameSpace = attributesManager.getFriendlyNameFromAttributeName("urn:perun:facility:attribute-def:opt:attr-manager-test-attribute");
		assertTrue("get wrong namespace from name",nameSpace.equals("attr-manager-test-attribute"));

	}

	@Test
	public void getLogins() throws Exception {
		System.out.println(CLASS_NAME + "getLogins");

		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("login-namespace:META-login");
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
		System.out.println(CLASS_NAME + "getLoginsWhenUserNotExists");

		attributesManager.getLogins(sess, new User());

	}

	@Test
	public void getAttributeRights() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeRights");

		// setting rights
		List<ActionType> listOfActions = new ArrayList<>();
		listOfActions.add(ActionType.WRITE);
		listOfActions.add(ActionType.READ);
		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(1, Role.VOADMIN, listOfActions));
		rights.add(new AttributeRights(1, Role.SELF, new ArrayList<>()));
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
		System.out.println(CLASS_NAME + "setAttributeRights");
		List<ActionType> listOfActions = new ArrayList<>();
		listOfActions.add(ActionType.WRITE);
		listOfActions.add(ActionType.READ);
		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(1, Role.VOADMIN, listOfActions));
		listOfActions.clear();
		listOfActions.add(ActionType.READ);
		rights.add(new AttributeRights(1, Role.SELF, listOfActions));
		perun.getAttributesManager().setAttributeRights(sess, rights);

		listOfActions.clear();
		rights.clear();
		listOfActions.add(ActionType.WRITE);
		rights.add(new AttributeRights(1, Role.VOADMIN, new ArrayList<>()));
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










	// ============= 13. private methods for attribute dependencies logic ====================================
	// In these methods are suppressed "unchecked" warnings because of casting the returned type of
	// testedMethod.invoke() to a List<RichAttribute>


//----------------------- VO attributes -------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByUser");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, user1, vo_toEmail_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_user1.size());
		assertTrue(attrs_user1.contains(vo1_toEmail_attribute));

		//get attributes for user2
		List<RichAttribute> ra_user2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, user2, vo_fromEmail_def);
		List<Attribute> attrs_user2 = new ArrayList<>();
		ra_user2.forEach(ra -> attrs_user2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_user2.size());
		assertTrue(attrs_user2.contains(vo2_fromEmail_attribute));

		//get attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, user3, vo_toEmail_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 2, attrs_user3.size());
		assertTrue(attrs_user3.contains(vo2_toEmail_attribute));
		assertTrue(attrs_user3.contains(vo1_toEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByMember");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for member1OfUser1
		List<RichAttribute> ra_member1_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, member1OfUser1, vo_toEmail_def);
		List<Attribute> attrs_member1_user1 = new ArrayList<>();
		ra_member1_user1.forEach(ra -> attrs_member1_user1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_member1_user1.size());
		assertTrue(attrs_member1_user1.contains(vo1_toEmail_attribute));

		//get vo_toEmail_def attributes for member member1OfUser2
		List<RichAttribute> ra_member1_user2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, member1OfUser2, vo_toEmail_def);
		List<Attribute> attrs_member1_user2 = new ArrayList<>();
		ra_member1_user2.forEach(ra -> attrs_member1_user2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_member1_user2.size());
		assertTrue(attrs_member1_user2.contains(vo2_toEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByGroup");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for group1InVo1
		List<RichAttribute> ra_group1_invo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, group1InVo1, vo_toEmail_def);
		List<Attribute> attrs_group1_invo1 = new ArrayList<>();
		ra_group1_invo1.forEach(ra -> attrs_group1_invo1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_group1_invo1.size());
		assertTrue(attrs_group1_invo1.contains(vo1_toEmail_attribute));

		//get vo_fromEmail_def attributes for group1InVo2
		List<RichAttribute> ra_group1_invo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, group1InVo2, vo_fromEmail_def);
		List<Attribute> attrs_group1_invo2 = new ArrayList<>();
		ra_group1_invo2.forEach(ra -> attrs_group1_invo2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_group1_invo2.size());
		assertTrue(attrs_group1_invo2.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByResource");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for resource1InVo1
		List<RichAttribute> ra_res1_invo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, resource1InVo1, vo_toEmail_def);
		List<Attribute> attrs_res1_invo1 = new ArrayList<>();
		ra_res1_invo1.forEach(ra -> attrs_res1_invo1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_res1_invo1.size());
		assertTrue(attrs_res1_invo1.contains(vo1_toEmail_attribute));

		//get vo_fromEmail_def attributes for resource1InVo2
		List<RichAttribute> ra_res1_invo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, resource1InVo2, vo_fromEmail_def);
		List<Attribute> attrs_res1_invo2 = new ArrayList<>();
		ra_res1_invo2.forEach(ra -> attrs_res1_invo2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_res1_invo2.size());
		assertTrue(attrs_res1_invo2.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByResource");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for Vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, vo1, vo_toEmail_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(vo1_toEmail_attribute));

		//get vo_fromEmail_def attributes for Vo1
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, vo2, vo_fromEmail_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByFacility");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, facility2, vo_toEmail_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 2, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(vo1_toEmail_attribute));
		assertTrue(attrs_facility2.contains(vo2_toEmail_attribute));

		//get vo_fromEmail_def attributes for facility3
		List<RichAttribute> ra_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, facility3, vo_fromEmail_def);
		List<Attribute> attrs_facility3 = new ArrayList<>();
		ra_facility3.forEach(ra -> attrs_facility3.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_facility3.size());
		assertTrue(attrs_facility3.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByHost");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for host2OnFacility2
		List<RichAttribute> ra_host2_onfac2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, host2OnFacility2, vo_toEmail_def);
		List<Attribute> attrs_host2_onfac2 = new ArrayList<>();
		ra_host2_onfac2.forEach(ra -> attrs_host2_onfac2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 2, attrs_host2_onfac2.size());
		assertTrue(attrs_host2_onfac2.contains(vo1_toEmail_attribute));
		assertTrue(attrs_host2_onfac2.contains(vo2_toEmail_attribute));

		//get vo_fromEmail_def attributes for host1OnFacility3
		List<RichAttribute> ra_host1_onfac3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, host1OnFacility3, vo_fromEmail_def);
		List<Attribute> attrs_host1_onfac3 = new ArrayList<>();
		ra_host1_onfac3.forEach(ra -> attrs_host1_onfac3.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_host1_onfac3.size());
		assertTrue(attrs_host1_onfac3.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByUserExtSource");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for userExtSource1
		List<RichAttribute> ra_userExtSrc1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, userExtSource1, vo_toEmail_def);
		List<Attribute> attrs_userExtSrc1 = new ArrayList<>();
		ra_userExtSrc1.forEach(ra -> attrs_userExtSrc1.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_userExtSrc1.size());
		assertTrue(attrs_userExtSrc1.contains(vo1_toEmail_attribute));

		//get vo_fromEmail_def attributes for userExtSource2
		List<RichAttribute> ra_userExtSrc2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, userExtSource2, vo_fromEmail_def);
		List<Attribute> attrs_userExtSrc2 = new ArrayList<>();
		ra_userExtSrc2.forEach(ra -> attrs_userExtSrc2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes",1, attrs_userExtSrc2.size());
		assertTrue(attrs_userExtSrc2.contains(vo2_fromEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByUserFacility");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//get vo_toEmail_def attributes for user3 and facility2
		List<RichAttribute> ra_user3_fac2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility2, vo_toEmail_def);
		List<Attribute> attrs_user3_fac2 = new ArrayList<>();
		ra_user3_fac2.forEach(ra -> attrs_user3_fac2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_user3_fac2.size());
		assertTrue(attrs_user3_fac2.contains(vo2_toEmail_attribute));

		List<RichAttribute> ra_user2_fac2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility2, vo_toEmail_def);
		List<Attribute> attrs_user2_fac2 = new ArrayList<>();
		ra_user2_fac2.forEach(ra -> attrs_user2_fac2.add(ra.getAttribute()));

		assertEquals("Found invalid number of attributes", 1, attrs_user2_fac2.size());
		assertTrue(attrs_user2_fac2.contains(vo2_toEmail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getVoAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getVoAttributesByKey");

		setAttributesForVoAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getVoAttributes",
				PerunSession.class, AttributeDefinition.class);

		//get all vo_toEmail_def attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl, sess, vo_toEmail_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Found invalid number of attributes", 2 <= attrs_all.size());
		assertTrue(attrs_all.contains(vo1_toEmail_attribute));
		assertTrue(attrs_all.contains(vo2_toEmail_attribute));
	}

	//----------------------- USER attributes -------------------------//


	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByUser");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find phone attributes for user1
		List<RichAttribute> ra_phone_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, user_phone_atr_def);
		List<Attribute> attrs_phone_user1 = new ArrayList<>();
		ra_phone_user1.forEach(ra -> attrs_phone_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_user1.size());
		assertTrue(attrs_phone_user1.contains(user1_phone_attribute));

		//find email attribute for user2
		List<RichAttribute> ra_email_user2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, user_email_atr_def);
		List<Attribute> attrs_email_user2 = new ArrayList<>();
		ra_email_user2.forEach(ra -> attrs_email_user2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_email_user2.size());
		assertTrue(attrs_email_user2.contains(user2_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByMember");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find phone attributes for member2OfUser1 - disallowed member
		List<RichAttribute> ra_phone_member2U1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, user_phone_atr_def);
		List<Attribute> attrs_phone_member2U1 = new ArrayList<>();
		ra_phone_member2U1.forEach(ra -> attrs_phone_member2U1.add(ra.getAttribute()));

		assertTrue(attrs_phone_member2U1.isEmpty());

		//find email attributes for member1OfUser3
		List<RichAttribute> ra_phone_member1U3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, user_email_atr_def);
		List<Attribute> attrs_phone_member1U3 = new ArrayList<>();
		ra_phone_member1U3.forEach(ra -> attrs_phone_member1U3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_member1U3.size());
		assertTrue(attrs_phone_member1U3.contains(user3_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByGroup");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find user preferred email attributes in group1InVo1
		List<RichAttribute> ra_email_group1VO1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, user_email_atr_def);
		List<Attribute> attrs_email_group1VO1 = new ArrayList<>();
		ra_email_group1VO1.forEach(ra -> attrs_email_group1VO1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_group1VO1.size());
		assertTrue(attrs_email_group1VO1.contains(user3_email_attribute));
		//contains empty value from user1 who does not have email set
		assertTrue(attrs_email_group1VO1.contains(new Attribute(user_email_atr_def)));

		//find user preferred email attributes in group2InVo2
		List<RichAttribute> ra_email_group2VO2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, user_email_atr_def);
		List<Attribute> attrs_email_group2VO2 = new ArrayList<>();
		ra_email_group2VO2.forEach(ra -> attrs_email_group2VO2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_group2VO2.size());
		assertTrue(attrs_email_group2VO2.contains(user3_email_attribute));
		assertTrue(attrs_email_group2VO2.contains(user2_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByResource");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find user phone attributes by resource2InVo1
		List<RichAttribute> ra_phone_res2VO1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo1, user_phone_atr_def);
		List<Attribute> attrs_phone_res2VO1 = new ArrayList<>();
		ra_phone_res2VO1.forEach(ra -> attrs_phone_res2VO1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_res2VO1.size());
		assertTrue(attrs_phone_res2VO1.contains(user1_phone_attribute));

		//find user preffered email attributes by resource1InVo2
		List<RichAttribute> ra_phone_res1VO2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, user_email_atr_def);
		List<Attribute> attrs_phone_res1VO2 = new ArrayList<>();
		ra_phone_res1VO2.forEach(ra -> attrs_phone_res1VO2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_res1VO2.size());
		assertTrue(attrs_phone_res1VO2.contains(user2_email_attribute));
		assertTrue(attrs_phone_res1VO2.contains(user3_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByVo");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find user phone attributes by vo1
		List<RichAttribute> ra_phone_VO1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, user_phone_atr_def);
		List<Attribute> attrs_phone_VO1 = new ArrayList<>();
		ra_phone_VO1.forEach(ra -> attrs_phone_VO1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_VO1.size());
		assertTrue(attrs_phone_VO1.contains(user1_phone_attribute));
		//contains empty attribute value from user3 who does not have phone set
		assertTrue(attrs_phone_VO1.contains(new Attribute(user_phone_atr_def)));

		//find user preferred email attributes by vo2
		List<RichAttribute> ra_email_VO2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, user_email_atr_def);
		List<Attribute> attrs_email_VO2 = new ArrayList<>();
		ra_email_VO2.forEach(ra -> attrs_email_VO2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_VO2.size());
		assertTrue(attrs_email_VO2.contains(user2_email_attribute));
		assertTrue(attrs_email_VO2.contains(user3_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByFacility");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find user phone attributes by facility2
		List<RichAttribute> ra_phone_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, user_phone_atr_def);
		List<Attribute> attrs_phone_facility2 = new ArrayList<>();
		ra_phone_facility2.forEach(ra -> attrs_phone_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_phone_facility2.size());
		assertTrue(attrs_phone_facility2.contains(user1_phone_attribute));
		assertTrue(attrs_phone_facility2.contains(user2_phone_attribute));
		//contains empty attribute value from user3 who does not have phone set
		assertTrue(attrs_phone_facility2.contains(new Attribute(user_phone_atr_def)));

		//find user preferred email attributes by facility3
		List<RichAttribute> ra_email_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility3, user_email_atr_def);
		List<Attribute> attrs_email_facility3 = new ArrayList<>();
		ra_email_facility3.forEach(ra -> attrs_email_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_facility3.size());
		assertTrue(attrs_email_facility3.contains(user2_email_attribute));
		assertTrue(attrs_email_facility3.contains(user3_email_attribute	));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByHost");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find user phone attributes by host1OnFacility2
		List<RichAttribute> ra_phone_host1_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, user_phone_atr_def);
		List<Attribute> attrs_phone_host1_facility2 = new ArrayList<>();
		ra_phone_host1_facility2.forEach(ra -> attrs_phone_host1_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_phone_host1_facility2.size());
		assertTrue(attrs_phone_host1_facility2.contains(user1_phone_attribute));
		assertTrue(attrs_phone_host1_facility2.contains(user2_phone_attribute));
		//contains empty attribute value from user3 who does not have phone set
		assertTrue(attrs_phone_host1_facility2.contains(new Attribute(user_phone_atr_def)));

		//find user preferred email attributes by host1OnFacility3
		List<RichAttribute> ra_email_host1_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility3, user_email_atr_def);
		List<Attribute> attrs_email_facility3 = new ArrayList<>();
		ra_email_host1_facility3.forEach(ra -> attrs_email_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_facility3.size());
		assertTrue(attrs_email_facility3.contains(user2_email_attribute));
		assertTrue(attrs_email_facility3.contains(user3_email_attribute	));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByUserExtSource");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find phone attributes for userExtSource1
		List<RichAttribute> ra_phone_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, user_phone_atr_def);
		List<Attribute> attrs_phone_userExtSource1 = new ArrayList<>();
		ra_phone_userExtSource1.forEach(ra -> attrs_phone_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_userExtSource1.size());
		assertTrue(attrs_phone_userExtSource1.contains(user1_phone_attribute));

		//find email attribute for userExtSource2
		List<RichAttribute> ra_email_userExtSource2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource2, user_email_atr_def);
		List<Attribute> attrs_email_userExtSource2 = new ArrayList<>();
		ra_email_userExtSource2.forEach(ra -> attrs_email_userExtSource2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_email_userExtSource2.size());
		assertTrue(attrs_email_userExtSource2.contains(user2_email_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByKey");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all phone user attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user_phone_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		//actually contains 4 user attributes because of test user John Doe
		assertTrue("Invalid number of attributes found", 4 <= attrs_all.size());
		assertTrue(attrs_all.contains(user1_phone_attribute));
		assertTrue(attrs_all.contains(user2_phone_attribute));
		//contains empty user3 attribute
		assertTrue(attrs_all.contains(new Attribute(user_phone_atr_def)));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserAttributesByUserFacility");

		setAttributesForUserAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find phone user attributes for user1 and facility1
		List<RichAttribute> ra_user1_fac1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility1, user_phone_atr_def);
		List<Attribute> attrs_user1_fac1 = new ArrayList<>();
		ra_user1_fac1.forEach(ra -> attrs_user1_fac1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_fac1.size());
		assertTrue(attrs_user1_fac1.contains(user1_phone_attribute));

		//find phone user attributes for user2 and facility1
		List<RichAttribute> ra_user2_fac1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility1, user_phone_atr_def);
		List<Attribute> attrs_user2_fac1 = new ArrayList<>();
		ra_user2_fac1.forEach(ra -> attrs_user2_fac1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_user2_fac1.size());
	}

	//----------------------- MEMBER attributes -------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByUser");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find phone member attributes for user1
		List<RichAttribute> ra_phone_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, member_phone_atr_def);
		List<Attribute> attrs_phone_user1 = new ArrayList<>();
		ra_phone_user1.forEach(ra -> attrs_phone_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_user1.size());
		assertTrue(attrs_phone_user1.contains(member1OfUser1_phone_attribute));

		//find email member attributes for user3
		List<RichAttribute> ra_email_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, member_email_atr_def);
		List<Attribute> attrs_email_user3 = new ArrayList<>();
		ra_email_user3.forEach(ra -> attrs_email_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_user3.size());
		assertTrue(attrs_email_user3.contains(member1OfUser3_mail_attribute));
		assertTrue(attrs_email_user3.contains(member2OfUser3_mail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByMember");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find phone member attributes for member1OfUser1
		List<RichAttribute> ra_phone_member1U1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, member_phone_atr_def);
		List<Attribute> attrs_phone_member1U1 = new ArrayList<>();
		ra_phone_member1U1.forEach(ra -> attrs_phone_member1U1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_member1U1.size());
		assertTrue(attrs_phone_member1U1.contains(member1OfUser1_phone_attribute));

		//find email member attributes for member2OfUser1
		List<RichAttribute> ra_email_member2U1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, member_email_atr_def);
		List<Attribute> attrs_email_user3 = new ArrayList<>();
		ra_email_member2U1.forEach(ra -> attrs_email_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_email_user3.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByGroup");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find phone member attributes for group1InVo1
		List<RichAttribute> ra_phone_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, member_phone_atr_def);
		List<Attribute> attrs_phone_group1Vo1 = new ArrayList<>();
		ra_phone_group1Vo1.forEach(ra -> attrs_phone_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_group1Vo1.size());
		assertTrue(attrs_phone_group1Vo1.contains(member1OfUser1_phone_attribute));
		//contains an empty attribute from member1OfUser3
		assertTrue(attrs_phone_group1Vo1.contains(new Attribute(member_phone_atr_def)));

		//find email member attributes for group2InVo2
		List<RichAttribute> ra_phone_group2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, member_email_atr_def);
		List<Attribute> attrs_phone_group2Vo2 = new ArrayList<>();
		ra_phone_group2Vo2.forEach(ra -> attrs_phone_group2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_group2Vo2.size());
		assertTrue(attrs_phone_group2Vo2.contains(member1OfUser2_mail_attribute));
		assertTrue(attrs_phone_group2Vo2.contains(member2OfUser3_mail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByResource");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find phone member attributes for resource1InVo1
		List<RichAttribute> ra_phone_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, member_phone_atr_def);
		List<Attribute> attrs_phone_res1Vo1 = new ArrayList<>();
		ra_phone_res1Vo1.forEach(ra -> attrs_phone_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_res1Vo1.size());
		assertTrue(attrs_phone_res1Vo1.contains(member1OfUser1_phone_attribute));
		//contains an empty attribute from member1OfUser3
		assertTrue(attrs_phone_res1Vo1.contains(new Attribute(member_phone_atr_def)));

		//find email member attributes for resource1InVo2
		List<RichAttribute> ra_phone_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, member_email_atr_def);
		List<Attribute> attrs_phone_res1Vo2 = new ArrayList<>();
		ra_phone_res1Vo2.forEach(ra -> attrs_phone_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_res1Vo2.size());
		assertTrue(attrs_phone_res1Vo2.contains(member1OfUser2_mail_attribute));
		assertTrue(attrs_phone_res1Vo2.contains(member2OfUser3_mail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByVo");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find phone member attributes for vo1
		List<RichAttribute> ra_phone_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, member_phone_atr_def);
		List<Attribute> attrs_phone_vo1 = new ArrayList<>();
		ra_phone_vo1.forEach(ra -> attrs_phone_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_vo1.size());
		assertTrue(attrs_phone_vo1.contains(member1OfUser1_phone_attribute));
		//contains an empty attribute from member1OfUser3
		assertTrue(attrs_phone_vo1.contains(new Attribute(member_phone_atr_def)));

		//find email member attributes for vo2
		List<RichAttribute> ra_phone_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, member_email_atr_def);
		List<Attribute> attrs_phone_vo2 = new ArrayList<>();
		ra_phone_vo2.forEach(ra -> attrs_phone_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_vo2.size());
		assertTrue(attrs_phone_vo2.contains(member1OfUser2_mail_attribute));
		assertTrue(attrs_phone_vo2.contains(member2OfUser3_mail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByFacility");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find phone member attributes for facility1
		List<RichAttribute> ra_phone_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, member_phone_atr_def);
		List<Attribute> attrs_phone_facility1 = new ArrayList<>();
		ra_phone_facility1.forEach(ra -> attrs_phone_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_facility1.size());
		assertTrue(attrs_phone_facility1.contains(member1OfUser1_phone_attribute));
		//contains an empty attribute from member1OfUser3
		assertTrue(attrs_phone_facility1.contains(new Attribute(member_phone_atr_def)));

		//find email member attributes for facility2
		List<RichAttribute> ra_phone_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, member_email_atr_def);
		List<Attribute> attrs_phone_facility2 = new ArrayList<>();
		ra_phone_facility2.forEach(ra -> attrs_phone_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_phone_facility2.size());
		assertTrue(attrs_phone_facility2.contains(member1OfUser2_mail_attribute));
		assertTrue(attrs_phone_facility2.contains(member2OfUser3_mail_attribute));
		//contains an empty attribute from member1OfUser1
		assertTrue(attrs_phone_facility2.contains(new Attribute(member_email_atr_def)));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByHost");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find phone member attributes for host1OnFacility1
		List<RichAttribute> ra_phone_host1F1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, member_phone_atr_def);
		List<Attribute> attrs_phone_host1F1 = new ArrayList<>();
		ra_phone_host1F1.forEach(ra -> attrs_phone_host1F1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_phone_host1F1.size());
		assertTrue(attrs_phone_host1F1.contains(member1OfUser1_phone_attribute));
		//contains an empty attribute from member1OfUser3
		assertTrue(attrs_phone_host1F1.contains(new Attribute(member_phone_atr_def)));

		//find email member attributes for host1OnFacility2
		List<RichAttribute> ra_phone_host1F2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, member_email_atr_def);
		List<Attribute> attrs_phone_host1F2 = new ArrayList<>();
		ra_phone_host1F2.forEach(ra -> attrs_phone_host1F2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_phone_host1F2.size());
		assertTrue(attrs_phone_host1F2.contains(member1OfUser2_mail_attribute));
		assertTrue(attrs_phone_host1F2.contains(member2OfUser3_mail_attribute));
		//contains an empty attribute from member1OfUser1
		assertTrue(attrs_phone_host1F2.contains(new Attribute(member_email_atr_def)));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByUserExtSource");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find phone member attributes for userExtSource1
		List<RichAttribute> ra_phone_userExtSrc1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, member_phone_atr_def);
		List<Attribute> attrs_phone_userExtSrc1 = new ArrayList<>();
		ra_phone_userExtSrc1.forEach(ra -> attrs_phone_userExtSrc1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_phone_userExtSrc1.size());
		assertTrue(attrs_phone_userExtSrc1.contains(member1OfUser1_phone_attribute));

		//find email member attributes for userExtSource3
		List<RichAttribute> ra_email_userExtSrc3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, member_email_atr_def);
		List<Attribute> attrs_email_userExtSrc3 = new ArrayList<>();
		ra_email_userExtSrc3.forEach(ra -> attrs_email_userExtSrc3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_email_userExtSrc3.size());
		assertTrue(attrs_email_userExtSrc3.contains(member1OfUser3_mail_attribute));
		assertTrue(attrs_email_userExtSrc3.contains(member2OfUser3_mail_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByKey");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all phone member attributes
		List<RichAttribute> ra_phone_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member_phone_atr_def);
		List<Attribute> attrs_phone_all = new ArrayList<>();
		ra_phone_all.forEach(ra -> attrs_phone_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 4 <= attrs_phone_all.size());
		assertTrue(attrs_phone_all.contains(member1OfUser1_phone_attribute));
		assertTrue(attrs_phone_all.contains(member1OfUser2_phone_attribute));
		//contains 2 empty attributes from member1OfUser3 and member2OfUser3
		assertTrue(attrs_phone_all.contains(new Attribute(member_phone_atr_def)));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberAttributesByUserFacility");

		setAttributesForMemberAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find email member attributes for user3 and facility1
		List<RichAttribute> ra_user3_fac1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility1, member_email_atr_def);
		List<Attribute> attrs_user3_fac1 = new ArrayList<>();
		ra_user3_fac1.forEach(ra -> attrs_user3_fac1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_fac1.size());
		assertTrue(attrs_user3_fac1.contains(member1OfUser3_mail_attribute));

		//find phone member attributes for user1 and facility2
		List<RichAttribute> ra_user1_fac2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility2, member_phone_atr_def);
		List<Attribute> attrs_user1_fac2 = new ArrayList<>();
		ra_user1_fac2.forEach(ra -> attrs_user1_fac2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_fac2.size());
		assertTrue(attrs_user1_fac2.contains(member1OfUser1_phone_attribute));
	}

	//----------------------- GROUP attributes -------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByUser");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find email group attributes by user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, group_fromEmail_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user1.size());
		assertTrue(attrs_user1.contains(group1InVo1_email_atr));
		assertTrue(attrs_user1.contains(group2InVo1_email_atr));
		assertTrue(attrs_user1.contains(membersGroupOfVo1_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByUser");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find email group attributes by member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group_fromEmail_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(group1InVo1_email_atr));
		assertTrue(attrs_member1OfUser1.contains(group2InVo1_email_atr));
		assertTrue(attrs_member1OfUser1.contains(membersGroupOfVo1_email_atr));

		//find email group attributes by member2OfUser1
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group_fromEmail_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();
		ra_member2OfUser1.forEach(ra -> attrs_member2OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByGroup");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find email group attributes by group1InVo1
		List<RichAttribute> ra_group1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_group1InVo1 = new ArrayList<>();
		ra_group1InVo1.forEach(ra -> attrs_group1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1InVo1.size());
		assertTrue(attrs_group1InVo1.contains(group1InVo1_email_atr));

		//find email group attributes by group1InVo2
		List<RichAttribute> ra_group1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo2, group_fromEmail_atr_def);
		List<Attribute> attrs_group1InVo2 = new ArrayList<>();
		ra_group1InVo2.forEach(ra -> attrs_group1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1InVo2.size());
		assertTrue(attrs_group1InVo2.contains(group1InVo2_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByResource");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find email group attributes by resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(group1InVo1_email_atr));
		assertTrue(attrs_resource1InVo1.contains(group2InVo1_email_atr));

		//find email group attributes by resource2InVo2
		List<RichAttribute> ra_resource2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo2, group_fromEmail_atr_def);
		List<Attribute> attrs_resource2InVo2 = new ArrayList<>();
		ra_resource2InVo2.forEach(ra -> attrs_resource2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource2InVo2.size());
		assertTrue(attrs_resource2InVo2.contains(group2InVo2_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByVo");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find email group attributes by vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, group_fromEmail_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(group1InVo1_email_atr));
		assertTrue(attrs_vo1.contains(group2InVo1_email_atr));
		assertTrue(attrs_vo1.contains(membersGroupOfVo1_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByFacility");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find email group attributes by facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, group_fromEmail_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(group1InVo1_email_atr));
		assertTrue(attrs_facility1.contains(group2InVo1_email_atr));

		//find email group attributes by facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, group_fromEmail_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(group2InVo1_email_atr));
		assertTrue(attrs_facility2.contains(group1InVo2_email_atr));
		assertTrue(attrs_facility2.contains(group2InVo2_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByHost");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find email group attributes by host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, group_fromEmail_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(group1InVo1_email_atr));
		assertTrue(attrs_host1OnFacility1.contains(group2InVo1_email_atr));

		//find email group attributes by host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, group_fromEmail_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(group2InVo1_email_atr));
		assertTrue(attrs_host1OnFacility2.contains(group1InVo2_email_atr));
		assertTrue(attrs_host1OnFacility2.contains(group2InVo2_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByUserExtSource");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find email group attributes by userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, group_fromEmail_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(group1InVo1_email_atr));
		assertTrue(attrs_userExtSource1.contains(group2InVo1_email_atr));
		assertTrue(attrs_userExtSource1.contains(membersGroupOfVo1_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByKey");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all email group attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group_fromEmail_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 6 <= attrs_all.size());
		assertTrue(attrs_all.contains(group1InVo1_email_atr));
		assertTrue(attrs_all.contains(group2InVo1_email_atr));
		assertTrue(attrs_all.contains(group1InVo2_email_atr));
		assertTrue(attrs_all.contains(group2InVo2_email_atr));
		assertTrue(attrs_all.contains(membersGroupOfVo1_email_atr));
		assertTrue(attrs_all.contains(membersGroupOfVo2_email_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByMemberGroup");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find email group attributes by group1InVo1 and member1OfUser1
		List<RichAttribute> ra_group1Vo1_mem1U1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_group1Vo1_mem1U1 = new ArrayList<>();
		ra_group1Vo1_mem1U1.forEach(ra -> attrs_group1Vo1_mem1U1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1Vo1_mem1U1.size());
		assertTrue(attrs_group1Vo1_mem1U1.contains(group1InVo1_email_atr));

		//find email group attributes by group1InVo2 and member2OfUser1
		List<RichAttribute> ra_group1Vo2_mem2U1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo2, group_fromEmail_atr_def);
		List<Attribute> attrs_group1Vo2_mem2U1 = new ArrayList<>();
		ra_group1Vo2_mem2U1.forEach(ra -> attrs_group1Vo2_mem2U1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_group1Vo2_mem2U1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByMemberGroup");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find email group attributes by member1OfUser1 and resource1InVo1
		List<RichAttribute> ra_member1U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_member1U1_res1Vo1 = new ArrayList<>();
		ra_member1U1_res1Vo1.forEach(ra -> attrs_member1U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_res1Vo1.size());
		assertTrue(attrs_member1U1_res1Vo1.contains(group1InVo1_email_atr));
		assertTrue(attrs_member1U1_res1Vo1.contains(group2InVo1_email_atr));

		//find email group attributes by member1OfUser3 and resource1InVo1
		List<RichAttribute> ra_member1U3_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, resource1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_member1U3_res1Vo1 = new ArrayList<>();
		ra_member1U3_res1Vo1.forEach(ra -> attrs_member1U3_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U3_res1Vo1.size());
		assertTrue(attrs_member1U3_res1Vo1.contains(group1InVo1_email_atr));

		//find email group attributes by member2OfUser2 and resource1InVo1
		List<RichAttribute> ra_member2U2_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser2, resource1InVo1, group_fromEmail_atr_def);
		List<Attribute> attrs_member2U2_res1Vo1 = new ArrayList<>();
		ra_member2U2_res1Vo1.forEach(ra -> attrs_member2U2_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U2_res1Vo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getGroupAttributesByUserFacility");

		setAttributesForGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find email group attributes by user1 and facility2
		List<RichAttribute> ra_user1_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility2, group_fromEmail_atr_def);
		List<Attribute> attrs_user1_facility2 = new ArrayList<>();
		ra_user1_facility2.forEach(ra -> attrs_user1_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_facility2.size());
		assertTrue(attrs_user1_facility2.contains(group2InVo1_email_atr));

		//find email group attributes by user2 and facility3
		List<RichAttribute> ra_user2_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility3, group_fromEmail_atr_def);
		List<Attribute> attrs_user2_facility3 = new ArrayList<>();
		ra_user2_facility3.forEach(ra -> attrs_user2_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user2_facility3.size());
		assertTrue(attrs_user2_facility3.contains(group2InVo2_email_atr));

		//find email group attributes by user1 and facility1
		List<RichAttribute> ra_user1_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility1, group_fromEmail_atr_def);
		List<Attribute> attrs_user1_facility1 = new ArrayList<>();
		ra_user1_facility1.forEach(ra -> attrs_user1_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1_facility1.size());
		assertTrue(attrs_user1_facility1.contains(group1InVo1_email_atr));
		assertTrue(attrs_user1_facility1.contains(group2InVo1_email_atr));
	}

	//----------------------- RESOURCEs attributes -------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByUser");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find test resource attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, resource_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(resource1InVo1_test_atr));
		assertTrue(attrs_user1.contains(resource2InVo1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByMember");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find test resource attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(resource1InVo1_test_atr));
		assertTrue(attrs_member1OfUser1.contains(resource2InVo1_test_atr));

		//find test resource attributes for member1OfUser3
		List<RichAttribute> ra_member1OfUser3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, resource_test_atr_def);
		List<Attribute> attrs_member1OfUser3 = new ArrayList<>();
		ra_member1OfUser3.forEach(ra -> attrs_member1OfUser3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1OfUser3.size());
		assertTrue(attrs_member1OfUser3.contains(resource1InVo1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByGroup");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find test resource attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, resource_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(resource1InVo1_test_atr));
		assertTrue(attrs_group2InVo1.contains(resource2InVo1_test_atr));

		//find test resource attributes for membersGroupOfVo1
		List<RichAttribute> ra_membersGroupOfVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, membersGroupOfVo1, resource_test_atr_def);
		List<Attribute> attrs_membersGroupOfVo1 = new ArrayList<>();
		ra_membersGroupOfVo1.forEach(ra -> attrs_membersGroupOfVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_membersGroupOfVo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByResource");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find test resource attributes for resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, resource_test_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(resource1InVo1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByVo");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find test resource attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, resource_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(resource1InVo1_test_atr));
		assertTrue(attrs_vo1.contains(resource2InVo1_test_atr));

		//find test resource attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, resource_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(resource1InVo2_test_atr));
		assertTrue(attrs_vo2.contains(resource2InVo2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByFacility");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find test resource attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, resource_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(resource1InVo1_test_atr));

		//find test resource attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, resource_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(resource2InVo1_test_atr));
		assertTrue(attrs_facility2.contains(resource1InVo2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByHost");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find test resource attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, resource_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(resource1InVo1_test_atr));

		//find test resource attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, resource_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(resource2InVo1_test_atr));
		assertTrue(attrs_host1OnFacility2.contains(resource1InVo2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByUserExtSource");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find test resource attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, resource_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(resource1InVo1_test_atr));
		assertTrue(attrs_userExtSource1.contains(resource2InVo1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByKey");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test resource attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 4 <= attrs_all.size());
		assertTrue(attrs_all.contains(resource1InVo1_test_atr));
		assertTrue(attrs_all.contains(resource2InVo1_test_atr));
		assertTrue(attrs_all.contains(resource1InVo2_test_atr));
		assertTrue(attrs_all.contains(resource2InVo2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByMemberGroup");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find test resource attributes for member2OfUser3 and group2InVo2
		List<RichAttribute> ra_mem2U3_group2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, group2InVo2, resource_test_atr_def);
		List<Attribute> attrs_mem2U3_group2Vo2 = new ArrayList<>();
		ra_mem2U3_group2Vo2.forEach(ra -> attrs_mem2U3_group2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_mem2U3_group2Vo2.size());
		assertTrue(attrs_mem2U3_group2Vo2.contains(resource1InVo2_test_atr));
		assertTrue(attrs_mem2U3_group2Vo2.contains(resource2InVo2_test_atr));

		//find test resource attributes for member2OfUser1 and group1InVo1
		List<RichAttribute> ra_mem2U1_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo1, resource_test_atr_def);
		List<Attribute> attrs_mem2U1_group1Vo1 = new ArrayList<>();
		ra_mem2U1_group1Vo1.forEach(ra -> attrs_mem2U1_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_mem2U1_group1Vo1.size());

		//find test resource attributes for member1OfUser1 and group1InVo1
		List<RichAttribute> ra_mem1U1_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group1InVo1, resource_test_atr_def);
		List<Attribute> attrs_mem1U1_group1Vo1 = new ArrayList<>();
		ra_mem1U1_group1Vo1.forEach(ra -> attrs_mem1U1_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_mem1U1_group1Vo1.size());
		assertTrue(attrs_mem1U1_group1Vo1.contains(resource1InVo1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByMemberResource");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find test resource attributes for member2OfUser3 and resource2InVo2
		List<RichAttribute> ra_mem2U3_res2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, resource2InVo2, resource_test_atr_def);
		List<Attribute> attrs_mem2U3_res2Vo2 = new ArrayList<>();
		ra_mem2U3_res2Vo2.forEach(ra -> attrs_mem2U3_res2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_mem2U3_res2Vo2.size());
		assertTrue(attrs_mem2U3_res2Vo2.contains(resource2InVo2_test_atr));

		//find test resource attributes for member2OfUser1 and resource1InVo1
		List<RichAttribute> ra_mem2U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, resource1InVo1, resource_test_atr_def);
		List<Attribute> attrs_mem2U1_res1Vo1 = new ArrayList<>();
		ra_mem2U1_res1Vo1.forEach(ra -> attrs_mem2U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_mem2U1_res1Vo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getResourceAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getResourceAttributesByUserFacility");

		setAttributesForResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getResourceAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find test resource attributes for user3 and facility3
		List<RichAttribute> ra_user3_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility3, resource_test_atr_def);
		List<Attribute> attrs_user3_facility3 = new ArrayList<>();
		ra_user3_facility3.forEach(ra -> attrs_user3_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility3.size());
		assertTrue(attrs_user3_facility3.contains(resource2InVo2_test_atr));

		//find test resource attributes for user2 and facility2
		List<RichAttribute> ra_user2_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility2, resource_test_atr_def);
		List<Attribute> attrs_user2_facility2 = new ArrayList<>();
		ra_user2_facility2.forEach(ra -> attrs_user2_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user2_facility2.size());
		assertTrue(attrs_user2_facility2.contains(resource1InVo2_test_atr));
	}

	//----------------------- FACILITIES attributes -------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByUser");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find test resource attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(facility1_test_atr));
		assertTrue(attrs_user1.contains(facility2_test_atr));

		//find test resource attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user3.size());
		assertTrue(attrs_user3.contains(facility1_test_atr));
		assertTrue(attrs_user3.contains(facility2_test_atr));
		assertTrue(attrs_user3.contains(facility3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByMember");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find test resource attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, facility_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(facility1_test_atr));
		assertTrue(attrs_member1OfUser1.contains(facility2_test_atr));

		//find test resource attributes for member2OfUser2
		List<RichAttribute> ra_member2OfUser2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser2, facility_test_atr_def);
		List<Attribute> attrs_member2OfUser2 = new ArrayList<>();
		ra_member2OfUser2.forEach(ra -> attrs_member2OfUser2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByGroup");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find test resource attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, facility_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(facility1_test_atr));
		assertTrue(attrs_group2InVo1.contains(facility2_test_atr));

		//find test resource attributes for group1InVo2
		List<RichAttribute> ra_group1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo2, facility_test_atr_def);
		List<Attribute> attrs_group1InVo2 = new ArrayList<>();
		ra_group1InVo2.forEach(ra -> attrs_group1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1InVo2.size());
		assertTrue(attrs_group1InVo2.contains(facility2_test_atr));

		//find test resource attributes for membersGroupOfVo1
		List<RichAttribute> ra_membersGroupOfVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, membersGroupOfVo1, facility_test_atr_def);
		List<Attribute> attrs_membersGroupOfVo1 = new ArrayList<>();
		ra_membersGroupOfVo1.forEach(ra -> attrs_membersGroupOfVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_membersGroupOfVo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByResource");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find test resource attributes for resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, facility_test_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(facility1_test_atr));

		//find test resource attributes for resource2InVo2
		List<RichAttribute> ra_resource2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo2, facility_test_atr_def);
		List<Attribute> attrs_resource2InVo2 = new ArrayList<>();
		ra_resource2InVo2.forEach(ra -> attrs_resource2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource2InVo2.size());
		assertTrue(attrs_resource2InVo2.contains(facility3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByVo");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find test resource attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, facility_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(facility1_test_atr));
		assertTrue(attrs_vo1.contains(facility2_test_atr));

		//find test resource attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, facility_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(facility2_test_atr));
		assertTrue(attrs_vo2.contains(facility3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByFacility");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find test resource attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, facility_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(facility1_test_atr));

		//find test resource attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, facility_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(facility2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByHost");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find test resource attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, facility_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(facility1_test_atr));

		//find test resource attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, facility_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(facility2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByUserExtSource");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find test resource attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, facility_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(facility1_test_atr));
		assertTrue(attrs_userExtSource1.contains(facility2_test_atr));

		//find test resource attributes for userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, facility_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();
		ra_userExtSource3.forEach(ra -> attrs_userExtSource3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(facility1_test_atr));
		assertTrue(attrs_userExtSource3.contains(facility2_test_atr));
		assertTrue(attrs_userExtSource3.contains(facility3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByKey");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test resource attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 3 <= attrs_all.size());
		assertTrue(attrs_all.contains(facility1_test_atr));
		assertTrue(attrs_all.contains(facility2_test_atr));
		assertTrue(attrs_all.contains(facility3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByMemberGroup");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find test resource attributes for member1OfUser1 group2InVo1
		List<RichAttribute> ra_member1U1_group2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group2InVo1, facility_test_atr_def);
		List<Attribute> attrs_member1U1_group2Vo1 = new ArrayList<>();
		ra_member1U1_group2Vo1.forEach(ra -> attrs_member1U1_group2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_group2Vo1.size());
		assertTrue(attrs_member1U1_group2Vo1.contains(facility1_test_atr));
		assertTrue(attrs_member1U1_group2Vo1.contains(facility2_test_atr));

		//find test resource attributes for member1OfUser2 group1InVo2
		List<RichAttribute> ra_member1U2_group1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, group1InVo2, facility_test_atr_def);
		List<Attribute> attrs_member1U2_group1Vo2 = new ArrayList<>();
		ra_member1U2_group1Vo2.forEach(ra -> attrs_member1U2_group1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U2_group1Vo2.size());
		assertTrue(attrs_member1U2_group1Vo2.contains(facility2_test_atr));

		//find test resource attributes for member2OfUser1 group1InVo2
		List<RichAttribute> ra_member2U1_group1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo2, facility_test_atr_def);
		List<Attribute> attrs_member2U1_group1Vo2 = new ArrayList<>();
		ra_member2U1_group1Vo2.forEach(ra -> attrs_member2U1_group1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_group1Vo2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByMemberResource");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find test resource attributes for member1OfUser1 resource2InVo1
		List<RichAttribute> ra_member1U1_res2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource2InVo1, facility_test_atr_def);
		List<Attribute> attrs_member1U1_res2Vo1 = new ArrayList<>();
		ra_member1U1_res2Vo1.forEach(ra -> attrs_member1U1_res2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U1_res2Vo1.size());
		assertTrue(attrs_member1U1_res2Vo1.contains(facility2_test_atr));

		//find test resource attributes for member1OfUser2 resource2InVo2
		List<RichAttribute> ra_member1U2_res2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, resource2InVo2, facility_test_atr_def);
		List<Attribute> attrs_member1U2_res2Vo2 = new ArrayList<>();
		ra_member1U2_res2Vo2.forEach(ra -> attrs_member1U2_res2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U2_res2Vo2.size());
		assertTrue(attrs_member1U2_res2Vo2.contains(facility3_test_atr));

		//find test resource attributes for member2OfUser1 resource1InVo2
		List<RichAttribute> ra_member2U1_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, resource1InVo2, facility_test_atr_def);
		List<Attribute> attrs_member2U1_res1Vo2 = new ArrayList<>();
		ra_member2U1_res1Vo2.forEach(ra -> attrs_member2U1_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_res1Vo2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getFacilityAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityAttributesByUserFacility");

		setAttributesForFacilityAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getFacilityAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find test resource attributes for user2 facility2
		List<RichAttribute> ra_user2_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility2, facility_test_atr_def);
		List<Attribute> attrs_user2_facility2 = new ArrayList<>();
		ra_user2_facility2.forEach(ra -> attrs_user2_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user2_facility2.size());
		assertTrue(attrs_user2_facility2.contains(facility2_test_atr));

		//find test resource attributes for user2 facility1
		List<RichAttribute> ra_user2_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user2, facility1, facility_test_atr_def);
		List<Attribute> attrs_user2_facility1 = new ArrayList<>();
		ra_user2_facility1.forEach(ra -> attrs_user2_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_user2_facility1.size());
	}

	//---------------------HOSTS------------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByUser");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find test host attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, host_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_user1.size());
		assertTrue(attrs_user1.contains(host1F1_test_atr));
		assertTrue(attrs_user1.contains(host2F1_test_atr));
		assertTrue(attrs_user1.contains(host1F2_test_atr));
		assertTrue(attrs_user1.contains(host2F2_test_atr));

		//find test host attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, host_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 6, attrs_user3.size());
		assertTrue(attrs_user3.contains(host1F1_test_atr));
		assertTrue(attrs_user3.contains(host2F1_test_atr));
		assertTrue(attrs_user3.contains(host1F2_test_atr));
		assertTrue(attrs_user3.contains(host2F2_test_atr));
		assertTrue(attrs_user3.contains(host1F3_test_atr));
		assertTrue(attrs_user3.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByMember");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find test host attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, host_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(host1F1_test_atr));
		assertTrue(attrs_member1OfUser1.contains(host2F1_test_atr));
		assertTrue(attrs_member1OfUser1.contains(host1F2_test_atr));
		assertTrue(attrs_member1OfUser1.contains(host2F2_test_atr));

		//find test host attributes for member2OfUser2
		List<RichAttribute> ra_member2OfUser2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser2, host_test_atr_def);
		List<Attribute> attrs_member2OfUser2 = new ArrayList<>();
		ra_member2OfUser2.forEach(ra -> attrs_member2OfUser2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser2.size());

		//find test host attributes for member2OfUser3
		List<RichAttribute> ra_member2OfUser3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, host_test_atr_def);
		List<Attribute> attrs_member2OfUser3 = new ArrayList<>();
		ra_member2OfUser3.forEach(ra -> attrs_member2OfUser3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_member2OfUser3.size());
		assertTrue(attrs_member2OfUser3.contains(host1F2_test_atr));
		assertTrue(attrs_member2OfUser3.contains(host2F2_test_atr));
		assertTrue(attrs_member2OfUser3.contains(host1F3_test_atr));
		assertTrue(attrs_member2OfUser3.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByGroup");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find test host attributes for group1InVo2
		List<RichAttribute> ra_group1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo2, host_test_atr_def);
		List<Attribute> attrs_group1InVo2 = new ArrayList<>();
		ra_group1InVo2.forEach(ra -> attrs_group1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group1InVo2.size());
		assertTrue(attrs_group1InVo2.contains(host1F2_test_atr));
		assertTrue(attrs_group1InVo2.contains(host2F2_test_atr));

		//find test host attributes for group2InVo2
		List<RichAttribute> ra_group2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, host_test_atr_def);
		List<Attribute> attrs_group2InVo2 = new ArrayList<>();
		ra_group2InVo2.forEach(ra -> attrs_group2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_group2InVo2.size());
		assertTrue(attrs_group2InVo2.contains(host1F2_test_atr));
		assertTrue(attrs_group2InVo2.contains(host2F2_test_atr));
		assertTrue(attrs_group2InVo2.contains(host1F3_test_atr));
		assertTrue(attrs_group2InVo2.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByResource");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find test host attributes for resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, host_test_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(host1F1_test_atr));
		assertTrue(attrs_resource1InVo1.contains(host2F1_test_atr));

		//find test host attributes for resource2InVo1
		List<RichAttribute> ra_resource2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo1, host_test_atr_def);
		List<Attribute> attrs_resource2InVo1 = new ArrayList<>();
		ra_resource2InVo1.forEach(ra -> attrs_resource2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource2InVo1.size());
		assertTrue(attrs_resource2InVo1.contains(host1F2_test_atr));
		assertTrue(attrs_resource2InVo1.contains(host2F2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByVo");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find test host attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, host_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(host1F1_test_atr));
		assertTrue(attrs_vo1.contains(host2F1_test_atr));
		assertTrue(attrs_vo1.contains(host1F2_test_atr));
		assertTrue(attrs_vo1.contains(host2F2_test_atr));

		//find test host attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, host_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(host1F2_test_atr));
		assertTrue(attrs_vo2.contains(host2F2_test_atr));
		assertTrue(attrs_vo2.contains(host1F3_test_atr));
		assertTrue(attrs_vo2.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByFacility");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find test host attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, host_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(host1F1_test_atr));
		assertTrue(attrs_facility1.contains(host2F1_test_atr));

		//find test host attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, host_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(host1F2_test_atr));
		assertTrue(attrs_facility2.contains(host2F2_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByHost");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find test host attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, host_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(host1F1_test_atr));

		//find test host attributes for host2OnFacility3
		List<RichAttribute> ra_host2OnFacility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host2OnFacility3, host_test_atr_def);
		List<Attribute> attrs_host2OnFacility3 = new ArrayList<>();
		ra_host2OnFacility3.forEach(ra -> attrs_host2OnFacility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_host2OnFacility3.size());
		assertTrue(attrs_host2OnFacility3.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByUserExtSource");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find test host attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, host_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(host1F1_test_atr));
		assertTrue(attrs_userExtSource1.contains(host2F1_test_atr));
		assertTrue(attrs_userExtSource1.contains(host1F2_test_atr));
		assertTrue(attrs_userExtSource1.contains(host2F2_test_atr));

		//find test host attributes for userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, host_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();
		ra_userExtSource3.forEach(ra -> attrs_userExtSource3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 6, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(host1F1_test_atr));
		assertTrue(attrs_userExtSource3.contains(host2F1_test_atr));
		assertTrue(attrs_userExtSource3.contains(host1F2_test_atr));
		assertTrue(attrs_userExtSource3.contains(host2F2_test_atr));
		assertTrue(attrs_userExtSource3.contains(host1F3_test_atr));
		assertTrue(attrs_userExtSource3.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByKey");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test host attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 6 <= attrs_all.size());
		assertTrue(attrs_all.contains(host1F1_test_atr));
		assertTrue(attrs_all.contains(host2F1_test_atr));
		assertTrue(attrs_all.contains(host1F2_test_atr));
		assertTrue(attrs_all.contains(host2F2_test_atr));
		assertTrue(attrs_all.contains(host1F3_test_atr));
		assertTrue(attrs_all.contains(host2F3_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByMemberGroup");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find test host attributes for member1OfUser2 and group1InVo2
		List<RichAttribute> ra_member1U2_group1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, group1InVo2, host_test_atr_def);
		List<Attribute> attrs_member1U2_group1Vo2 = new ArrayList<>();
		ra_member1U2_group1Vo2.forEach(ra -> attrs_member1U2_group1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U2_group1Vo2.size());
		assertTrue(attrs_member1U2_group1Vo2.contains(host1F2_test_atr));
		assertTrue(attrs_member1U2_group1Vo2.contains(host2F2_test_atr));

		//find test host attributes for member2OfUser3 and group2InVo2
		List<RichAttribute> ra_member2U3_group2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess,  member2OfUser3, group2InVo2, host_test_atr_def);
		List<Attribute> attrs_member2U3_group2Vo2 = new ArrayList<>();
		ra_member2U3_group2Vo2.forEach(ra -> attrs_member2U3_group2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_member2U3_group2Vo2.size());
		assertTrue(attrs_member2U3_group2Vo2.contains(host1F2_test_atr));
		assertTrue(attrs_member2U3_group2Vo2.contains(host2F2_test_atr));
		assertTrue(attrs_member2U3_group2Vo2.contains(host1F3_test_atr));
		assertTrue(attrs_member2U3_group2Vo2.contains(host2F3_test_atr));

		//find test host attributes for member2OfUser1 and group2InVo2
		List<RichAttribute> ra_member2U1_group2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess,  member2OfUser1, group2InVo2, host_test_atr_def);
		List<Attribute> attrs_member2U1_group2Vo2 = new ArrayList<>();
		ra_member2U1_group2Vo2.forEach(ra -> attrs_member2U1_group2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_group2Vo2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getHostAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getHostAttributesByMemberResource");

		setAttributesForHostAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getHostAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find test host attributes for member1OfUser1 and resource1InVo1
		List<RichAttribute> ra_mem1U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource1InVo1, host_test_atr_def);
		List<Attribute> attrs_mem1U1_res1Vo1 = new ArrayList<>();
		ra_mem1U1_res1Vo1.forEach(ra -> attrs_mem1U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_mem1U1_res1Vo1.size());
		assertTrue(attrs_mem1U1_res1Vo1.contains(host1F1_test_atr));
		assertTrue(attrs_mem1U1_res1Vo1.contains(host2F1_test_atr));

		//find test host attributes for member2OfUser2 and resource2InVo1
		List<RichAttribute> ra_mem2U2_res2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser2, resource2InVo1, host_test_atr_def);
		List<Attribute> attrs_mem2U2_res2Vo1 = new ArrayList<>();
		ra_mem2U2_res2Vo1.forEach(ra -> attrs_mem2U2_res2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_mem2U2_res2Vo1.size());
	}

	//----------------------UESs--------------------------//

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByUser");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find test UES attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, ues_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(ues1_test_atr));
		assertTrue(attrs_user1.contains(internal_ues_atr));

		//find test UES attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, ues_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user3.size());
		assertTrue(attrs_user3.contains(ues3_test_atr));
		assertTrue(attrs_user3.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByMember");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find test UES attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, ues_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(ues1_test_atr));
		assertTrue(attrs_member1OfUser1.contains(internal_ues_atr));

		//find test UES attributes for member2OfUser1
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, ues_test_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();
		ra_member2OfUser1.forEach(ra -> attrs_member2OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByGroup");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find test UES attributes for group1InVo1
		List<RichAttribute> ra_group1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, ues_test_atr_def);
		List<Attribute> attrs_group1InVo1 = new ArrayList<>();
		ra_group1InVo1.forEach(ra -> attrs_group1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_group1InVo1.size());
		//contains two internal_ues_atr
		assertTrue(attrs_group1InVo1.contains(ues1_test_atr));
		assertTrue(attrs_group1InVo1.contains(ues3_test_atr));
		assertTrue(attrs_group1InVo1.contains(internal_ues_atr));

		//find test UES attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, ues_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(ues1_test_atr));
		assertTrue(attrs_group2InVo1.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByResource");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find test UES attributes for resource1InVo2
		List<RichAttribute> ra_resource1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, ues_test_atr_def);
		List<Attribute> attrs_resource1InVo2 = new ArrayList<>();
		ra_resource1InVo2.forEach(ra -> attrs_resource1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_resource1InVo2.size());
		//contains two internal_ues_atr
		assertTrue(attrs_resource1InVo2.contains(ues2_test_atr));
		assertTrue(attrs_resource1InVo2.contains(ues3_test_atr));
		assertTrue(attrs_resource1InVo2.contains(internal_ues_atr));

		//find test UES attributes for resource2InVo1
		List<RichAttribute> ra_resource2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo1, ues_test_atr_def);
		List<Attribute> attrs_resource2InVo1 = new ArrayList<>();
		ra_resource2InVo1.forEach(ra -> attrs_resource2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource2InVo1.size());
		assertTrue(attrs_resource2InVo1.contains(ues1_test_atr));
		assertTrue(attrs_resource2InVo1.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByVo");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find test UES attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, ues_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo1.size());
		//contains two internal_ues_atr
		assertTrue(attrs_vo1.contains(ues1_test_atr));
		assertTrue(attrs_vo1.contains(ues3_test_atr));
		assertTrue(attrs_vo1.contains(internal_ues_atr));

		//find test UES attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, ues_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(ues2_test_atr));
		assertTrue(attrs_vo2.contains(ues3_test_atr));
		assertTrue(attrs_vo2.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByFacility");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find test UES attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, ues_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_facility1.size());
		//contains two internal_ues_atr
		assertTrue(attrs_facility1.contains(ues1_test_atr));
		assertTrue(attrs_facility1.contains(ues3_test_atr));
		assertTrue(attrs_facility1.contains(internal_ues_atr));

		//find test UES attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, ues_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 6, attrs_facility2.size());
		//contains three internal_ues_atr
		assertTrue(attrs_facility2.contains(ues1_test_atr));
		assertTrue(attrs_facility2.contains(ues2_test_atr));
		assertTrue(attrs_facility2.contains(ues3_test_atr));
		assertTrue(attrs_facility2.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByHost");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find test UES attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, ues_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_host1OnFacility1.size());
		//contains two internal_ues_atr
		assertTrue(attrs_host1OnFacility1.contains(ues1_test_atr));
		assertTrue(attrs_host1OnFacility1.contains(ues3_test_atr));
		assertTrue(attrs_host1OnFacility1.contains(internal_ues_atr));

		//find test UES attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, ues_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 6, attrs_host1OnFacility2.size());
		//contains three internal_ues_atr
		assertTrue(attrs_host1OnFacility2.contains(ues1_test_atr));
		assertTrue(attrs_host1OnFacility2.contains(ues2_test_atr));
		assertTrue(attrs_host1OnFacility2.contains(ues3_test_atr));
		assertTrue(attrs_host1OnFacility2.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByUserExtSource");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find test UES attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, ues_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(ues1_test_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByKey");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test UES attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, ues_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 6, attrs_all.size());
		assertTrue(attrs_all.contains(ues1_test_atr));
		assertTrue(attrs_all.contains(ues2_test_atr));
		assertTrue(attrs_all.contains(ues3_test_atr));
		//contains three internal ues attributes
		assertTrue(attrs_all.contains(internal_ues_atr));
	}

	@SuppressWarnings({"unchecked"})
	@IfProfileValue(name = "default")
	@Test
	public void getUserExtSourceAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceAttributesByUserFacility");

		setAttributesForUESAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserExtSourceAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find test UES attributes for user1 facility1
		List<RichAttribute> ra_user1_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility1, ues_test_atr_def);
		List<Attribute> attrs_user1_facility1 = new ArrayList<>();
		ra_user1_facility1.forEach(ra -> attrs_user1_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1_facility1.size());
		assertTrue(attrs_user1_facility1.contains(ues1_test_atr));
		assertTrue(attrs_user1_facility1.contains(internal_ues_atr));

		//find test UES attributes for user1 and facility3
		List<RichAttribute> ra_user1_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility3, ues_test_atr_def);
		List<Attribute> attrs_user1_facility3 = new ArrayList<>();
		ra_user1_facility3.forEach(ra -> attrs_user1_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_user1_facility3.size());
	}

	//-----------------------GROUP-RESOURCE--------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByUser");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find test group-resource attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, groupResource_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user1.size());
		assertTrue(attrs_user1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_user1.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_user1.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, groupResource_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user3.size());
		assertTrue(attrs_user3.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_user3.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_user3.contains(group2VO2Res2VO2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByMember");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find test group-resource attributes for member2OfUser3
		List<RichAttribute> ra_member2OfUser3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, groupResource_test_atr_def);
		List<Attribute> attrs_member2OfUser3 = new ArrayList<>();
		ra_member2OfUser3.forEach(ra -> attrs_member2OfUser3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member2OfUser3.size());
		assertTrue(attrs_member2OfUser3.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_member2OfUser3.contains(group2VO2Res2VO2_test_attribute));

		//find test group-resource attributes for member1OfUser2
		List<RichAttribute> ra_member1OfUser2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, groupResource_test_atr_def);
		List<Attribute> attrs_member1OfUser2 = new ArrayList<>();
		ra_member1OfUser2.forEach(ra -> attrs_member1OfUser2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_member1OfUser2.size());
		assertTrue(attrs_member1OfUser2.contains(group1VO2Res1VO2_test_attribute));
		assertTrue(attrs_member1OfUser2.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_member1OfUser2.contains(group2VO2Res2VO2_test_attribute));

		//find test group-resource attributes for member2OfUser1
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, groupResource_test_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();
		ra_member2OfUser1.forEach(ra -> attrs_member2OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByGroup");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find test group-resource attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, groupResource_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_group2InVo1.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for group1InVo2
		List<RichAttribute> ra_group1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo2, groupResource_test_atr_def);
		List<Attribute> attrs_group1InVo2 = new ArrayList<>();
		ra_group1InVo2.forEach(ra -> attrs_group1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1InVo2.size());
		assertTrue(attrs_group1InVo2.contains(group1VO2Res1VO2_test_attribute));

		//find test group-resource attributes for membersGroupOfVo1
		List<RichAttribute> ra_membersGroupOfVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, membersGroupOfVo1, groupResource_test_atr_def);
		List<Attribute> attrs_membersGroupOfVo1 = new ArrayList<>();
		ra_membersGroupOfVo1.forEach(ra -> attrs_membersGroupOfVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_membersGroupOfVo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByResource");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find test group-resource attributes for resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, groupResource_test_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_resource1InVo1.contains(group2VO1Res1VO1_test_attribute));

		//find test group-resource attributes for resource2InVo2
		List<RichAttribute> ra_resource2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo2, groupResource_test_atr_def);
		List<Attribute> attrs_resource2InVo2 = new ArrayList<>();
		ra_resource2InVo2.forEach(ra -> attrs_resource2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource2InVo2.size());
		assertTrue(attrs_resource2InVo2.contains(group2VO2Res2VO2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByVo");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find test group-resource attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, groupResource_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_vo1.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_vo1.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, groupResource_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(group1VO2Res1VO2_test_attribute));
		assertTrue(attrs_vo2.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_vo2.contains(group2VO2Res2VO2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByFacility");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find test group-resource attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, groupResource_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(group1VO2Res1VO2_test_attribute));
		assertTrue(attrs_facility2.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_facility2.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, groupResource_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_facility1.contains(group2VO1Res1VO1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByHost");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find test group-resource attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, groupResource_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(group1VO2Res1VO2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, groupResource_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_host1OnFacility1.contains(group2VO1Res1VO1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByUserExtSource");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find test group-resource attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, groupResource_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_userExtSource1.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_userExtSource1.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, groupResource_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();
		ra_userExtSource3.forEach(ra -> attrs_userExtSource3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_userExtSource3.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_userExtSource3.contains(group2VO2Res2VO2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByGroupResource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByGroupResource");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Group.class, Resource.class, AttributeDefinition.class);

		//find test group-resource attributes for group1InVo1 and resource1InVo1
		List<RichAttribute> ra_group1Vo1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, resource1InVo1, groupResource_test_atr_def);
		List<Attribute> attrs_group1Vo1_res1Vo1 = new ArrayList<>();
		ra_group1Vo1_res1Vo1.forEach(ra -> attrs_group1Vo1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group1Vo1_res1Vo1.size());
		assertTrue(attrs_group1Vo1_res1Vo1.contains(group1VO1Res1VO1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByMemberGroup");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find test group-resource attributes for member1OfUser1 and group2InVo1
		List<RichAttribute> ra_member1U1_group2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group2InVo1, groupResource_test_atr_def);
		List<Attribute> attrs_member1U1_group2Vo1 = new ArrayList<>();
		ra_member1U1_group2Vo1.forEach(ra -> attrs_member1U1_group2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_group2Vo1.size());
		assertTrue(attrs_member1U1_group2Vo1.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_member1U1_group2Vo1.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for member2OfUser1 group1InVo2
		List<RichAttribute> ra_member2U1_group2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo2, groupResource_test_atr_def);
		List<Attribute> attrs_member2U1_group2Vo1 = new ArrayList<>();
		ra_member2U1_group2Vo1.forEach(ra -> attrs_member2U1_group2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_group2Vo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByMemberResource");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find test group-resource attributes for member1OfUser1 and resource1InVo1
		List<RichAttribute> ra_member1U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource1InVo1, groupResource_test_atr_def);
		List<Attribute> attrs_member1U1_res1Vo1 = new ArrayList<>();
		ra_member1U1_res1Vo1.forEach(ra -> attrs_member1U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_res1Vo1.size());
		assertTrue(attrs_member1U1_res1Vo1.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_member1U1_res1Vo1.contains(group2VO1Res1VO1_test_attribute));

		//find test group-resource attributes for member2OfUser1 and resource1InVo2
		List<RichAttribute> ra_member2U1_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, resource1InVo2, groupResource_test_atr_def);
		List<Attribute> attrs_member2U1_res1Vo2 = new ArrayList<>();
		ra_member2U1_res1Vo2.forEach(ra -> attrs_member2U1_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_res1Vo2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByUserFacility");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find test group-resource attributes for user1 and facility2
		List<RichAttribute> ra_user1_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility2, groupResource_test_atr_def);
		List<Attribute> attrs_user1_facility2 = new ArrayList<>();
		ra_user1_facility2.forEach(ra -> attrs_user1_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_facility2.size());
		assertTrue(attrs_user1_facility2.contains(group2VO1Res2VO1_test_attribute));

		//find test group-resource attributes for user3 and facility1
		List<RichAttribute> ra_user3_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility1, groupResource_test_atr_def);
		List<Attribute> attrs_user3_facility1 = new ArrayList<>();
		ra_user3_facility1.forEach(ra -> attrs_user3_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility1.size());
		assertTrue(attrs_user3_facility1.contains(group1VO1Res1VO1_test_attribute));

		//find test group-resource attributes for user3 and facility2
		List<RichAttribute> ra_user3_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility2, groupResource_test_atr_def);
		List<Attribute> attrs_user3_facility2 = new ArrayList<>();
		ra_user3_facility2.forEach(ra -> attrs_user3_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility2.size());
		assertTrue(attrs_user3_facility2.contains(group2VO2Res1VO2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getGroupResourceAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getGroupResourceAttributesByKey");

		setAttributesForGroupResourceAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getGroupResourceAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test group-resource attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, groupResource_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 6 <= attrs_all.size());
		assertTrue(attrs_all.contains(group1VO1Res1VO1_test_attribute));
		assertTrue(attrs_all.contains(group2VO1Res1VO1_test_attribute));
		assertTrue(attrs_all.contains(group2VO1Res2VO1_test_attribute));
		assertTrue(attrs_all.contains(group1VO2Res1VO2_test_attribute));
		assertTrue(attrs_all.contains(group2VO2Res1VO2_test_attribute));
		assertTrue(attrs_all.contains(group2VO2Res2VO2_test_attribute));
	}

	//-------------------------MEMBER-GROUP------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByUser");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find all test member-group attributes user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, memberGroup_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_user1 = attrs_user1;
		ra_user1.forEach(ra -> finalAttrs_user1.add(ra.getAttribute()));
		attrs_user1 = attrs_user1.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_user1.contains(member1U1Group2Vo1_test_attribute));

		//find all test member-group attributes user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, memberGroup_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_user3 = attrs_user3;
		ra_user3.forEach(ra -> finalAttrs_user3.add(ra.getAttribute()));
		attrs_user3 = attrs_user3.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 2, attrs_user3.size());
		assertTrue(attrs_user3.contains(member1U3Group1Vo1_test_attribute));
		assertTrue(attrs_user3.contains(member2U3Group2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByMember");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find all test member-group attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, memberGroup_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_member1U1 = attrs_member1OfUser1;
		ra_member1OfUser1.forEach(ra -> finalAttrs_member1U1.add(ra.getAttribute()));
		attrs_member1OfUser1 = attrs_member1OfUser1.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 2, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_member1OfUser1.contains(member1U1Group2Vo1_test_attribute));

		//find all test member-group attributes for member2OfUser1
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, memberGroup_test_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_member2U1 = attrs_member2OfUser1;
		ra_member2OfUser1.forEach(ra -> finalAttrs_member2U1.add(ra.getAttribute()));
		attrs_member2OfUser1 = attrs_member2OfUser1.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByGroup");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find all test member-group attributes for group2InVo2
		List<RichAttribute> ra_group2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, memberGroup_test_atr_def);
		List<Attribute> attrs_group2InVo2 = new ArrayList<>();
		ra_group2InVo2.forEach(ra -> attrs_group2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo2.size());
		assertTrue(attrs_group2InVo2.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(member2U3Group2Vo2_test_attribute));

		//find all test member-group attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(member1U1Group2Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByResource");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find all test member-group attributes for resource1InVo2
		List<RichAttribute> ra_resource1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, memberGroup_test_atr_def);
		List<Attribute> attrs_resource1InVo2 = new ArrayList<>();
		ra_resource1InVo2.forEach(ra -> attrs_resource1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_resource1InVo2.size());
		assertTrue(attrs_resource1InVo2.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_resource1InVo2.contains(member1U2Group1Vo2_test_attribute));
		assertTrue(attrs_resource1InVo2.contains(member2U3Group2Vo2_test_attribute));

		//find all test member-group attributes for resource1InVo1
		List<RichAttribute> ra_resource1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_resource1InVo1 = new ArrayList<>();
		ra_resource1InVo1.forEach(ra -> attrs_resource1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_resource1InVo1.size());
		assertTrue(attrs_resource1InVo1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_resource1InVo1.contains(member1U1Group2Vo1_test_attribute));
		assertTrue(attrs_resource1InVo1.contains(member1U3Group1Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByVo");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find all test member-group attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, memberGroup_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();

		//remove empty attributes which are not used in test
		List<Attribute> finalAttrs_vo1 = attrs_vo1;
		ra_vo1.forEach(ra -> finalAttrs_vo1.add(ra.getAttribute()));
		attrs_vo1 = attrs_vo1.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 3, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_vo1.contains(member1U1Group2Vo1_test_attribute));
		assertTrue(attrs_vo1.contains(member1U3Group1Vo1_test_attribute));

		//find all test member-group attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, memberGroup_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();

		//remove empty attributes which are not used in test
		List<Attribute> finalAttrs_vo2 = attrs_vo2;
		ra_vo2.forEach(ra -> finalAttrs_vo2.add(ra.getAttribute()));
		attrs_vo2 = attrs_vo2.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 3, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(member1U2Group1Vo2_test_attribute));
		assertTrue(attrs_vo2.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_vo2.contains(member2U3Group2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByFacility");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find all test member-group attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, memberGroup_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(member1U1Group2Vo1_test_attribute));
		assertTrue(attrs_facility2.contains(member1U2Group1Vo2_test_attribute));
		assertTrue(attrs_facility2.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_facility2.contains(member2U3Group2Vo2_test_attribute));

		//find all test member-group attributes for facility3
		List<RichAttribute> ra_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility3, memberGroup_test_atr_def);
		List<Attribute> attrs_facility3 = new ArrayList<>();
		ra_facility3.forEach(ra -> attrs_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility3.size());
		assertTrue(attrs_facility3.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_facility3.contains(member2U3Group2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByHost");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find all test member-group attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, memberGroup_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(member1U1Group2Vo1_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(member1U2Group1Vo2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(member2U3Group2Vo2_test_attribute));

		//find all test member-group attributes for host2OnFacility3
		List<RichAttribute> ra_host2OnFacility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host2OnFacility3, memberGroup_test_atr_def);
		List<Attribute> attrs_host2OnFacility3 = new ArrayList<>();
		ra_host2OnFacility3.forEach(ra -> attrs_host2OnFacility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host2OnFacility3.size());
		assertTrue(attrs_host2OnFacility3.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_host2OnFacility3.contains(member2U3Group2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByUserExtSource");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find all test member-group attributes userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, memberGroup_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_userExtSource1 = attrs_userExtSource1;
		ra_userExtSource1.forEach(ra -> finalAttrs_userExtSource1.add(ra.getAttribute()));
		attrs_userExtSource1 = attrs_userExtSource1.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_userExtSource1.contains(member1U1Group2Vo1_test_attribute));

		//find all test member-group attributes userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, memberGroup_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();

		//remove empty attributes which are not tested
		List<Attribute> finalAttrs_userExtSource3 = attrs_userExtSource3;
		ra_userExtSource3.forEach(ra -> finalAttrs_userExtSource3.add(ra.getAttribute()));
		attrs_userExtSource3 = attrs_userExtSource3.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(member1U3Group1Vo1_test_attribute));
		assertTrue(attrs_userExtSource3.contains(member2U3Group2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByMemberGroup");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find all test member-group attributesmember1OfUser1 and group1InVo1
		List<RichAttribute> ra_member1U1_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group1InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_member1U1_group1Vo1 = new ArrayList<>();
		ra_member1U1_group1Vo1.forEach(ra -> attrs_member1U1_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U1_group1Vo1.size());
		assertTrue(attrs_member1U1_group1Vo1.contains(member1U1Group1Vo1_test_attribute));

		//find all test member-group attributes member2OfUser1 and group1InVo2
		List<RichAttribute> ra_member2U1_group1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo2, memberGroup_test_atr_def);
		List<Attribute> attrs_member2U1_group1Vo2 = new ArrayList<>();
		ra_member2U1_group1Vo2.forEach(ra -> attrs_member2U1_group1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_group1Vo2.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByMemberResource");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find all test member-group attributes member1OfUser1 and resource2InVo1
		List<RichAttribute> ra_member1U1_res2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource2InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_member1U1_res2Vo1 = new ArrayList<>();
		ra_member1U1_res2Vo1.forEach(ra -> attrs_member1U1_res2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U1_res2Vo1.size());
		assertTrue(attrs_member1U1_res2Vo1.contains(member1U1Group2Vo1_test_attribute));

		//find all test member-group attributes member1OfUser1 and resource1InVo1
		List<RichAttribute> ra_member1U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource1InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_member1U1_res1Vo1 = new ArrayList<>();
		ra_member1U1_res1Vo1.forEach(ra -> attrs_member1U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_res1Vo1.size());
		assertTrue(attrs_member1U1_res1Vo1.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_member1U1_res1Vo1.contains(member1U1Group2Vo1_test_attribute));

		//find all test member-group attributes member2OfUser2 and resource2InVo1
		List<RichAttribute> ra_member2U2_res2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser2, resource2InVo1, memberGroup_test_atr_def);
		List<Attribute> attrs_member2U2_res2Vo1 = new ArrayList<>();
		ra_member2U2_res2Vo1.forEach(ra -> attrs_member2U2_res2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U2_res2Vo1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByUserFacility");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find all test member-group attributes for user3 and facility2
		List<RichAttribute> ra_user3_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility2, memberGroup_test_atr_def);
		List<Attribute> attrs_user3_facility2 = new ArrayList<>();

		//remove attributes which are not used in test
		List<Attribute> finalAttrs_user3_facility2 = attrs_user3_facility2;
		ra_user3_facility2.forEach(ra -> finalAttrs_user3_facility2.add(ra.getAttribute()));
		attrs_user3_facility2 = attrs_user3_facility2.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		System.out.println(attrs_user3_facility2);
		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility2.size());
		assertTrue(attrs_user3_facility2.contains(member2U3Group2Vo2_test_attribute));

		//find all test member-group attributes for user1 and facility2
		List<RichAttribute> ra_user1_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility2, memberGroup_test_atr_def);
		List<Attribute> attrs_user1_facility2 = new ArrayList<>();

		//remove attributes which are not used in test
		List<Attribute> finalAttrs_user1_facility2 = attrs_user1_facility2;
		ra_user1_facility2.forEach(ra -> finalAttrs_user1_facility2.add(ra.getAttribute()));
		attrs_user1_facility2 = attrs_user1_facility2.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		assertEquals("Invalid number of attributes found", 1, attrs_user1_facility2.size());
		assertTrue(attrs_user1_facility2.contains(member1U1Group2Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberGroupAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupAttributesByKey");

		setAttributesForMemberGroupAttributesTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberGroupAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test member-group attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, memberGroup_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();

		//remove attributes which are not used in test
		List<Attribute> finalAttrs_all = attrs_all;
		ra_all.forEach(ra -> finalAttrs_all.add(ra.getAttribute()));
		attrs_all = attrs_all.stream().filter(a -> a.getValue() != null).collect(Collectors.toList());

		System.out.println(attrs_all);
		assertTrue("Invalid number of attributes found", 6 <= attrs_all.size());
		assertTrue(attrs_all.contains(member1U1Group1Vo1_test_attribute));
		assertTrue(attrs_all.contains(member1U1Group2Vo1_test_attribute));
		assertTrue(attrs_all.contains(member1U2Group1Vo2_test_attribute));
		assertTrue(attrs_all.contains(member1U2Group2Vo2_test_attribute));
		assertTrue(attrs_all.contains(member1U3Group1Vo1_test_attribute));
		assertTrue(attrs_all.contains(member2U3Group2Vo2_test_attribute));
	}

	//----------------------------MEMBER-RESOURCE-----------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByUser");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find all test member-group attributes user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, memberResource_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_user1.contains(member1U1Res2Vo1_test_attribute));

		//find all test member-group attributes user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, memberResource_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user3.size());
		assertTrue(attrs_user3.contains(member1U3Res1Vo1_test_attribute));
		assertTrue(attrs_user3.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_user3.contains(member2U3Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByMember");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find all test member-group attributes for member2OfUser3
		List<RichAttribute> ra_member2OfUser3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, memberResource_test_atr_def);
		List<Attribute> attrs_member2OfUser3 = new ArrayList<>();
		ra_member2OfUser3.forEach(ra -> attrs_member2OfUser3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member2OfUser3.size());
		assertTrue(attrs_member2OfUser3.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_member2OfUser3.contains(member2U3Res2Vo2_test_attribute));

		//find all test member-group attributes for member2OfUser3
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, memberResource_test_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();
		ra_member2OfUser1.forEach(ra -> attrs_member2OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByGroup");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find all test member-group attributes for group2InVo2
		List<RichAttribute> ra_group2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, memberResource_test_atr_def);
		List<Attribute> attrs_group2InVo2 = new ArrayList<>();
		ra_group2InVo2.forEach(ra -> attrs_group2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_group2InVo2.size());
		assertTrue(attrs_group2InVo2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(member1U2Res2Vo2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(member2U3Res2Vo2_test_attribute));

		//find all test member-group attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_group2InVo1.contains(member1U1Res2Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByResource");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find all test member-group attributes for resource2InVo1
		List<RichAttribute> ra_resource2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_resource2InVo1 = new ArrayList<>();
		ra_resource2InVo1.forEach(ra -> attrs_resource2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource2InVo1.size());
		assertTrue(attrs_resource2InVo1.contains(member1U1Res2Vo1_test_attribute));

		//find all test member-group attributes for resource1InVo2
		List<RichAttribute> ra_resource1InVo21 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, memberResource_test_atr_def);
		List<Attribute> attrs_resource1InVo2 = new ArrayList<>();
		ra_resource1InVo21.forEach(ra -> attrs_resource1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource1InVo2.size());
		assertTrue(attrs_resource1InVo2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_resource1InVo2.contains(member2U3Res1Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByVo");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find all test member-group attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, memberResource_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(member1U1Res2Vo1_test_attribute));
		assertTrue(attrs_vo1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_vo1.contains(member1U3Res1Vo1_test_attribute));

		//find all test member-group attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, memberResource_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_vo2.contains(member1U2Res2Vo2_test_attribute));
		assertTrue(attrs_vo2.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_vo2.contains(member2U3Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByFacility");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find all test member-group attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, memberResource_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_facility1.contains(member1U3Res1Vo1_test_attribute));

		//find all test member-group attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, memberResource_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(member1U1Res2Vo1_test_attribute));
		assertTrue(attrs_facility2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_facility2.contains(member2U3Res1Vo2_test_attribute));

		//find all test member-group attributes for facility3
		List<RichAttribute> ra_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility3, memberResource_test_atr_def);
		List<Attribute> attrs_facility3 = new ArrayList<>();
		ra_facility3.forEach(ra -> attrs_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility3.size());
		assertTrue(attrs_facility3.contains(member1U2Res2Vo2_test_attribute));
		assertTrue(attrs_facility3.contains(member2U3Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByHost");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find all test member-group attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, memberResource_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_host1OnFacility1.contains(member1U3Res1Vo1_test_attribute));

		//find all test member-group attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, memberResource_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(member1U1Res2Vo1_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(member2U3Res1Vo2_test_attribute));

		//find all test member-group attributes for host2OnFacility3
		List<RichAttribute> ra_host2OnFacility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host2OnFacility3, memberResource_test_atr_def);
		List<Attribute> attrs_host2OnFacility3 = new ArrayList<>();
		ra_host2OnFacility3.forEach(ra -> attrs_host2OnFacility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host2OnFacility3.size());
		assertTrue(attrs_host2OnFacility3.contains(member1U2Res2Vo2_test_attribute));
		assertTrue(attrs_host2OnFacility3.contains(member2U3Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByUserExtSource");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find all test member-group attributes userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, memberResource_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_userExtSource1.contains(member1U1Res2Vo1_test_attribute));

		//find all test member-group attributes userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, memberResource_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();
		ra_userExtSource3.forEach(ra -> attrs_userExtSource3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(member1U3Res1Vo1_test_attribute));
		assertTrue(attrs_userExtSource3.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_userExtSource3.contains(member2U3Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByGroupResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByUserExtSource");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Group.class, Resource.class, AttributeDefinition.class);

		//find all test member-group attributes group2InVo1 and resource1InVo1
		List<RichAttribute> ra_group2Vo1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, resource1InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_group2Vo1_res1Vo1 = new ArrayList<>();
		ra_group2Vo1_res1Vo1.forEach(ra -> attrs_group2Vo1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group2Vo1_res1Vo1.size());
		assertTrue(attrs_group2Vo1_res1Vo1.contains(member1U1Res1Vo1_test_attribute));

		//find all test member-group attributes group2InVo2 and resource1InVo2
		List<RichAttribute> ra_group2Vo2_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, resource1InVo2, memberResource_test_atr_def);
		List<Attribute> attrs_group2Vo2_res1Vo2 = new ArrayList<>();
		ra_group2Vo2_res1Vo2.forEach(ra -> attrs_group2Vo2_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2Vo2_res1Vo2.size());
		assertTrue(attrs_group2Vo2_res1Vo2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_group2Vo2_res1Vo2.contains(member2U3Res1Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByMemberGroup");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find all test member-group attributes member1OfUser1 and group1InVo1
		List<RichAttribute> ra_member1U1_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group1InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_member1U1_group1Vo1 = new ArrayList<>();
		ra_member1U1_group1Vo1.forEach(ra -> attrs_member1U1_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U1_group1Vo1.size());
		assertTrue(attrs_member1U1_group1Vo1.contains(member1U1Res1Vo1_test_attribute));

		//find all test member-group for attributes member1OfUser2 and group2InVo2
		List<RichAttribute> ra_member1U2_group2Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, group2InVo2, memberResource_test_atr_def);
		List<Attribute> attrs_member1U2_group2Vo2 = new ArrayList<>();
		ra_member1U2_group2Vo2.forEach(ra -> attrs_member1U2_group2Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U2_group2Vo2.size());
		assertTrue(attrs_member1U2_group2Vo2.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_member1U2_group2Vo2.contains(member1U2Res2Vo2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByMemberResource");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find all test member-group attributes member1OfUser1 and resource1InVo1
		List<RichAttribute> ra_member1U1_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, resource1InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_member1U1_res1Vo1 = new ArrayList<>();
		ra_member1U1_res1Vo1.forEach(ra -> attrs_member1U1_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U1_res1Vo1.size());
		assertTrue(attrs_member1U1_res1Vo1.contains(member1U1Res1Vo1_test_attribute));

		//find all test member-group attributes member1OfUser3 and resource1InVo1
		List<RichAttribute> ra_member1U3_res1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, resource1InVo1, memberResource_test_atr_def);
		List<Attribute> attrs_member1U3_res1Vo1 = new ArrayList<>();
		ra_member1U3_res1Vo1.forEach(ra -> attrs_member1U3_res1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U3_res1Vo1.size());
		assertTrue(attrs_member1U3_res1Vo1.contains(member1U3Res1Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByUserFacility");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find all test member-group attributes user3 and facility2
		List<RichAttribute> ra_user3_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility2, memberResource_test_atr_def);
		List<Attribute> attrs_user3_facility2 = new ArrayList<>();
		ra_user3_facility2.forEach(ra -> attrs_user3_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility2.size());
		assertTrue(attrs_user3_facility2.contains(member2U3Res1Vo2_test_attribute));

		//find all test member-group attributes user1 and facility3
		List<RichAttribute> ra_user1_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility3, memberResource_test_atr_def);
		List<Attribute> attrs_user1_facility3 = new ArrayList<>();
		ra_user1_facility3.forEach(ra -> attrs_user1_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_user1_facility3.size());

		//find all test member-group attributes user1 and facility1
		List<RichAttribute> ra_user1_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility1, memberResource_test_atr_def);
		List<Attribute> attrs_user1_facility1 = new ArrayList<>();
		ra_user1_facility1.forEach(ra -> attrs_user1_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_facility1.size());
		assertTrue(attrs_user1_facility1.contains(member1U1Res1Vo1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getMemberResourceAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getMemberResourceAttributesByKey");

		setAttributesForMemberResourceTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getMemberResourceAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test member-group attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, memberResource_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 7 <= attrs_all.size());
		assertTrue(attrs_all.contains(member1U1Res1Vo1_test_attribute));
		assertTrue(attrs_all.contains(member1U1Res2Vo1_test_attribute));
		assertTrue(attrs_all.contains(member1U2Res1Vo2_test_attribute));
		assertTrue(attrs_all.contains(member1U2Res2Vo2_test_attribute));
		assertTrue(attrs_all.contains(member1U3Res1Vo1_test_attribute));
		assertTrue(attrs_all.contains(member2U3Res1Vo2_test_attribute));
		assertTrue(attrs_all.contains(member2U3Res2Vo2_test_attribute));
	}

	//--------------------------USER-FACILITY-------------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByUser() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByUser");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, User.class, AttributeDefinition.class);

		//find all test user-facility attributes for user1
		List<RichAttribute> ra_user1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, userFacility_test_atr_def);
		List<Attribute> attrs_user1 = new ArrayList<>();
		ra_user1.forEach(ra -> attrs_user1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_user1.size());
		assertTrue(attrs_user1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_user1.contains(user1Facility2_test_attribute));

		//find all test user-facility attributes for user3
		List<RichAttribute> ra_user3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, userFacility_test_atr_def);
		List<Attribute> attrs_user3 = new ArrayList<>();
		ra_user3.forEach(ra -> attrs_user3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_user3.size());
		assertTrue(attrs_user3.contains(user3Facility3_test_attribute));
		assertTrue(attrs_user3.contains(user3Facility2_test_attribute));
		assertTrue(attrs_user3.contains(user3Facility1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByMember() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByMember");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Member.class, AttributeDefinition.class);

		//find all test user-facility attributes for member2OfUser3
		List<RichAttribute> ra_member2OfUser3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, userFacility_test_atr_def);
		List<Attribute> attrs_member2OfUser3 = new ArrayList<>();
		ra_member2OfUser3.forEach(ra -> attrs_member2OfUser3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member2OfUser3.size());
		assertTrue(attrs_member2OfUser3.contains(user3Facility2_test_attribute));
		assertTrue(attrs_member2OfUser3.contains(user3Facility3_test_attribute));

		//find all test user-facility attributes for member2OfUser1
		List<RichAttribute> ra_member2OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, userFacility_test_atr_def);
		List<Attribute> attrs_member2OfUser1 = new ArrayList<>();
		ra_member2OfUser1.forEach(ra -> attrs_member2OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2OfUser1.size());

		//find all test user-facility attributes for member1OfUser1
		List<RichAttribute> ra_member1OfUser1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, userFacility_test_atr_def);
		List<Attribute> attrs_member1OfUser1 = new ArrayList<>();
		ra_member1OfUser1.forEach(ra -> attrs_member1OfUser1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1OfUser1.size());
		assertTrue(attrs_member1OfUser1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_member1OfUser1.contains(user1Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByGroup() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByGroup");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Group.class, AttributeDefinition.class);

		//find all test user-facility attributes for group2InVo2
		List<RichAttribute> ra_group2InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_group2InVo2 = new ArrayList<>();
		ra_group2InVo2.forEach(ra -> attrs_group2InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_group2InVo2.size());
		assertTrue(attrs_group2InVo2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(user2Facility3_test_attribute));
		assertTrue(attrs_group2InVo2.contains(user3Facility2_test_attribute));
		assertTrue(attrs_group2InVo2.contains(user3Facility3_test_attribute));

		//find all test user-facility attributes for group1InVo1
		List<RichAttribute> ra_group1InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_group1InVo1 = new ArrayList<>();
		ra_group1InVo1.forEach(ra -> attrs_group1InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group1InVo1.size());
		assertTrue(attrs_group1InVo1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_group1InVo1.contains(user3Facility1_test_attribute));

		//find all test user-facility attributes for group2InVo1
		List<RichAttribute> ra_group2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_group2InVo1 = new ArrayList<>();
		ra_group2InVo1.forEach(ra -> attrs_group2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2InVo1.size());
		assertTrue(attrs_group2InVo1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_group2InVo1.contains(user1Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByResource() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByResource");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Resource.class, AttributeDefinition.class);

		//find all test user-facility attributes for resource2InVo1
		List<RichAttribute> ra_resource2InVo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource2InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_resource2InVo1 = new ArrayList<>();
		ra_resource2InVo1.forEach(ra -> attrs_resource2InVo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_resource2InVo1.size());
		assertTrue(attrs_resource2InVo1.contains(user1Facility2_test_attribute));

		//find all test user-facility attributes for resource1InVo2
		List<RichAttribute> ra_resource1InVo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, resource1InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_resource1InVo2 = new ArrayList<>();
		ra_resource1InVo2.forEach(ra -> attrs_resource1InVo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_resource1InVo2.size());
		assertTrue(attrs_resource1InVo2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_resource1InVo2.contains(user3Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByVo() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByVo");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Vo.class, AttributeDefinition.class);

		//find all test user-facility attributes for vo1
		List<RichAttribute> ra_vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo1, userFacility_test_atr_def);
		List<Attribute> attrs_vo1 = new ArrayList<>();
		ra_vo1.forEach(ra -> attrs_vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_vo1.size());
		assertTrue(attrs_vo1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_vo1.contains(user1Facility2_test_attribute));
		assertTrue(attrs_vo1.contains(user3Facility1_test_attribute));

		//find all test user-facility attributes for vo2
		List<RichAttribute> ra_vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, vo2, userFacility_test_atr_def);
		List<Attribute> attrs_vo2 = new ArrayList<>();
		ra_vo2.forEach(ra -> attrs_vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 4, attrs_vo2.size());
		assertTrue(attrs_vo2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_vo2.contains(user2Facility3_test_attribute));
		assertTrue(attrs_vo2.contains(user3Facility2_test_attribute));
		assertTrue(attrs_vo2.contains(user3Facility3_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByFacility");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Facility.class, AttributeDefinition.class);

		//find all test user-facility attributes for facility1
		List<RichAttribute> ra_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility1, userFacility_test_atr_def);
		List<Attribute> attrs_facility1 = new ArrayList<>();
		ra_facility1.forEach(ra -> attrs_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility1.size());
		assertTrue(attrs_facility1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_facility1.contains(user3Facility1_test_attribute));

		//find all test user-facility attributes for facility2
		List<RichAttribute> ra_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility2, userFacility_test_atr_def);
		List<Attribute> attrs_facility2 = new ArrayList<>();
		ra_facility2.forEach(ra -> attrs_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_facility2.size());
		assertTrue(attrs_facility2.contains(user1Facility2_test_attribute));
		assertTrue(attrs_facility2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_facility2.contains(user3Facility2_test_attribute));

		//find all test user-facility attributes for facility3
		List<RichAttribute> ra_facility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, facility3, userFacility_test_atr_def);
		List<Attribute> attrs_facility3 = new ArrayList<>();
		ra_facility3.forEach(ra -> attrs_facility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_facility3.size());
		assertTrue(attrs_facility3.contains(user2Facility3_test_attribute));
		assertTrue(attrs_facility3.contains(user3Facility3_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByHost() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByHost");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Host.class, AttributeDefinition.class);

		//find all test user-facility attributes for host1OnFacility1
		List<RichAttribute> ra_host1OnFacility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility1, userFacility_test_atr_def);
		List<Attribute> attrs_host1OnFacility1 = new ArrayList<>();
		ra_host1OnFacility1.forEach(ra -> attrs_host1OnFacility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host1OnFacility1.size());
		assertTrue(attrs_host1OnFacility1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_host1OnFacility1.contains(user3Facility1_test_attribute));

		//find all test user-facility attributes for host1OnFacility2
		List<RichAttribute> ra_host1OnFacility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host1OnFacility2, userFacility_test_atr_def);
		List<Attribute> attrs_host1OnFacility2 = new ArrayList<>();
		ra_host1OnFacility2.forEach(ra -> attrs_host1OnFacility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_host1OnFacility2.size());
		assertTrue(attrs_host1OnFacility2.contains(user1Facility2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_host1OnFacility2.contains(user3Facility2_test_attribute));

		//find all test user-facility attributes for host2OnFacility3
		List<RichAttribute> ra_host2OnFacility3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, host2OnFacility3, userFacility_test_atr_def);
		List<Attribute> attrs_host2OnFacility3 = new ArrayList<>();
		ra_host2OnFacility3.forEach(ra -> attrs_host2OnFacility3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_host2OnFacility3.size());
		assertTrue(attrs_host2OnFacility3.contains(user2Facility3_test_attribute));
		assertTrue(attrs_host2OnFacility3.contains(user3Facility3_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByUserExtSource");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, UserExtSource.class, AttributeDefinition.class);

		//find all test user-facility attributes for userExtSource1
		List<RichAttribute> ra_userExtSource1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource1, userFacility_test_atr_def);
		List<Attribute> attrs_userExtSource1 = new ArrayList<>();
		ra_userExtSource1.forEach(ra -> attrs_userExtSource1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_userExtSource1.size());
		assertTrue(attrs_userExtSource1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_userExtSource1.contains(user1Facility2_test_attribute));

		//find all test user-facility attributes for userExtSource3
		List<RichAttribute> ra_userExtSource3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userExtSource3, userFacility_test_atr_def);
		List<Attribute> attrs_userExtSource3 = new ArrayList<>();
		ra_userExtSource3.forEach(ra -> attrs_userExtSource3.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_userExtSource3.size());
		assertTrue(attrs_userExtSource3.contains(user3Facility3_test_attribute));
		assertTrue(attrs_userExtSource3.contains(user3Facility2_test_attribute));
		assertTrue(attrs_userExtSource3.contains(user3Facility1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByGroupResource() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByGroupResource");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Group.class, Resource.class, AttributeDefinition.class);

		//find all test user-facility attributes for group2InVo1 and resource2InVo1
		List<RichAttribute> ra_group2Vo1_resource2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo1, resource2InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_group2Vo1_resource2Vo1 = new ArrayList<>();
		ra_group2Vo1_resource2Vo1.forEach(ra -> attrs_group2Vo1_resource2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_group2Vo1_resource2Vo1.size());
		assertTrue(attrs_group2Vo1_resource2Vo1.contains(user1Facility2_test_attribute));

		//find all test user-facility attributes for group1InVo1 and resource1InVo1
		List<RichAttribute> ra_group1Vo1_resource1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group1InVo1, resource1InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_group1Vo1_resource1Vo1 = new ArrayList<>();
		ra_group1Vo1_resource1Vo1.forEach(ra -> attrs_group1Vo1_resource1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group1Vo1_resource1Vo1.size());
		assertTrue(attrs_group1Vo1_resource1Vo1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_group1Vo1_resource1Vo1.contains(user3Facility1_test_attribute));

		//find all test user-facility attributes for group2InVo2 and resource1InVo2
		List<RichAttribute> ra_group2Vo2_resource1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, group2InVo2, resource1InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_group2Vo2_resource1Vo2 = new ArrayList<>();
		ra_group2Vo2_resource1Vo2.forEach(ra -> attrs_group2Vo2_resource1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_group2Vo2_resource1Vo2.size());
		assertTrue(attrs_group2Vo2_resource1Vo2.contains(user2Facility2_test_attribute));
		assertTrue(attrs_group2Vo2_resource1Vo2.contains(user3Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByMemberGroup() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByMemberGroup");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Member.class, Group.class, AttributeDefinition.class);

		//find all test user-facility attributes for member1OfUser1 and group2InVo1
		List<RichAttribute> ra_member1U1_group2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser1, group2InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_member1U1_group2Vo1 = new ArrayList<>();
		ra_member1U1_group2Vo1.forEach(ra -> attrs_member1U1_group2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 2, attrs_member1U1_group2Vo1.size());
		assertTrue(attrs_member1U1_group2Vo1.contains(user1Facility1_test_attribute));
		assertTrue(attrs_member1U1_group2Vo1.contains(user1Facility2_test_attribute));

		//find all test user-facility attributes for member2OfUser1 and group1InVo2
		List<RichAttribute> ra_member2U1_group1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser1, group1InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_member2U1_group1Vo2 = new ArrayList<>();
		ra_member2U1_group1Vo2.forEach(ra -> attrs_member2U1_group1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member2U1_group1Vo2.size());

		//find all test user-facility attributes for member1OfUser3 and group1InVo1
		List<RichAttribute> ra_member1U3_group1Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, group1InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_member1U3_group1Vo1 = new ArrayList<>();
		ra_member1U3_group1Vo1.forEach(ra -> attrs_member1U3_group1Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U3_group1Vo1.size());
		assertTrue(attrs_member1U3_group1Vo1.contains(user3Facility1_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByMemberResource() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByMemberResource");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, Member.class, Resource.class, AttributeDefinition.class);

		//find all test user-facility attributes for member1OfUser3 and resource2InVo1
		List<RichAttribute> ra_member1U1_res2Vo1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser3, resource2InVo1, userFacility_test_atr_def);
		List<Attribute> attrs_member1U1_res2Vo1 = new ArrayList<>();
		ra_member1U1_res2Vo1.forEach(ra -> attrs_member1U1_res2Vo1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 0, attrs_member1U1_res2Vo1.size());

		//find all test user-facility attributes for member2OfUser3 and resource1InVo2
		List<RichAttribute> ra_member2U3_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member2OfUser3, resource1InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_member2U3_res1Vo2 = new ArrayList<>();
		ra_member2U3_res1Vo2.forEach(ra -> attrs_member2U3_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member2U3_res1Vo2.size());
		assertTrue(attrs_member2U3_res1Vo2.contains(user3Facility2_test_attribute));

		//find all test user-facility attributes for member1OfUser2 and resource1InVo2
		List<RichAttribute> ra_member1U2_res1Vo2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, member1OfUser2, resource1InVo2, userFacility_test_atr_def);
		List<Attribute> attrs_member1U2_res1Vo2 = new ArrayList<>();
		ra_member1U2_res1Vo2.forEach(ra -> attrs_member1U2_res1Vo2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_member1U2_res1Vo2.size());
		assertTrue(attrs_member1U2_res1Vo2.contains(user2Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByUserFacility() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByUserFacility");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, User.class, Facility.class, AttributeDefinition.class);

		//find all test user-facility attributes for user1 and facility1
		List<RichAttribute> ra_user1_facility1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user1, facility1, userFacility_test_atr_def);
		List<Attribute> attrs_user1_facility1 = new ArrayList<>();
		ra_user1_facility1.forEach(ra -> attrs_user1_facility1.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user1_facility1.size());
		assertTrue(attrs_user1_facility1.contains(user1Facility1_test_attribute));

		//find all test user-facility attributes for user3 and facilty2
		List<RichAttribute> ra_user3_facility2 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, user3, facility2, userFacility_test_atr_def);
		List<Attribute> attrs_user3_facility2 = new ArrayList<>();
		ra_user3_facility2.forEach(ra -> attrs_user3_facility2.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 1, attrs_user3_facility2.size());
		assertTrue(attrs_user3_facility2.contains(user3Facility2_test_attribute));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getUserFacilityAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesByKey");

		setAttributesForUserFacilityTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getUserFacilityAttributes",
				PerunSession.class, AttributeDefinition.class);

		//find all test user-facility attributes
		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, userFacility_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 7 <= attrs_all.size());
		assertTrue(attrs_all.contains(user1Facility1_test_attribute));
		assertTrue(attrs_all.contains(user1Facility2_test_attribute));
		assertTrue(attrs_all.contains(user2Facility2_test_attribute));
		assertTrue(attrs_all.contains(user2Facility3_test_attribute));
		assertTrue(attrs_all.contains(user3Facility3_test_attribute));
		assertTrue(attrs_all.contains(user3Facility2_test_attribute));
		assertTrue(attrs_all.contains(user3Facility1_test_attribute));
	}

	//-------------------------ENTITYLESS----------------------------//

	@SuppressWarnings({"unchecked"})
	@Test
	public void getEntitylessAttributesAll() throws Exception {
		setAttributesForEntitylessTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getEntitylessAttributes",
				PerunSession.class, AttributeDefinition.class);

		List<RichAttribute> ra_all = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, entityless_test_atr_def);
		List<Attribute> attrs_all = new ArrayList<>();
		ra_all.forEach(ra -> attrs_all.add(ra.getAttribute()));

		assertEquals("Invalid number of attributes found", 3, attrs_all.size());
		assertTrue(attrs_all.contains(entityless_test_attribute1));
		assertTrue(attrs_all.contains(entityless_test_attribute2));
		assertTrue(attrs_all.contains(entityless_test_attribute3));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void getEntitylessAttributesByKey() throws Exception {
		setAttributesForEntitylessTest();

		Method testedMethod = getPrivateMethodFromAtrManager("getEntitylessAttributes",
				PerunSession.class, String.class, AttributeDefinition.class);

		List<RichAttribute> ra_key1 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, "1", entityless_test_atr_def);
		List<Attribute> attrs_key1 = new ArrayList<>();
		ra_key1.forEach(ra -> attrs_key1.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 1 <= attrs_key1.size());
		assertTrue(attrs_key1.contains(entityless_test_attribute1));

		List<RichAttribute> ra_key3 = (List<RichAttribute>) testedMethod.invoke(attributesManagerBl,
				sess, "3", entityless_test_atr_def);
		List<Attribute> attrs_key3 = new ArrayList<>();
		ra_key3.forEach(ra -> attrs_key3.add(ra.getAttribute()));

		assertTrue("Invalid number of attributes found", 1 <= attrs_key3.size());
		assertTrue(attrs_key3.contains(entityless_test_attribute3));
	}


// PRIVATE METHODS ----------------------------------------------

	/**
	 * entityless_test_attribute1 -> key:1
	 * entityless_test_attribute2 -> key:2
	 * entityless_test_attribute3 -> key:3
	 */
	private void setAttributesForEntitylessTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		entityless_test_atr_def = new AttributeDefinition();
		entityless_test_atr_def.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		entityless_test_atr_def.setDescription("entityless_test_atr_def");
		entityless_test_atr_def.setFriendlyName("test-entityless-test-atr-def");
		entityless_test_atr_def.setType(String.class.getName());
		entityless_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, entityless_test_atr_def);

		entityless_test_attribute1 = new Attribute(entityless_test_atr_def);
		entityless_test_attribute1.setValue("154");
		perun.getAttributesManagerBl().setAttribute(sess, "1", entityless_test_attribute1);

		entityless_test_attribute2 = new Attribute(entityless_test_atr_def);
		entityless_test_attribute2.setValue("202");
		perun.getAttributesManagerBl().setAttribute(sess, "2", entityless_test_attribute2);

		entityless_test_attribute3 = new Attribute(entityless_test_atr_def);
		entityless_test_attribute3.setValue("362");
		perun.getAttributesManagerBl().setAttribute(sess, "3", entityless_test_attribute3);
	}

	/**
	 * user1Facility1_test_attribute -> facility1, user1
	 * user1Facility2_test_attribute -> facility2, user1
	 * user2Facility2_test_attribute -> facility2, user2
	 * user2Facility3_test_attribute -> facility3, user2
	 * user3Facility3_test_attribute -> facility3, user3
	 * user3Facility2_test_attribute -> facility2, user3
	 * user3Facility1_test_attribute -> facility1, user3
	 */
	private void setAttributesForUserFacilityTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		userFacility_test_atr_def = new AttributeDefinition();
		userFacility_test_atr_def.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		userFacility_test_atr_def.setDescription("userFacility_test_atr_def");
		userFacility_test_atr_def.setFriendlyName("test-userFacility-test-atr-def");
		userFacility_test_atr_def.setType(String.class.getName());
		userFacility_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, userFacility_test_atr_def);

		user1Facility1_test_attribute = new Attribute(userFacility_test_atr_def);
		user1Facility1_test_attribute.setValue("user1Facility1");
		perun.getAttributesManagerBl().setAttribute(sess, facility1, user1, user1Facility1_test_attribute);

		user1Facility2_test_attribute = new Attribute(userFacility_test_atr_def);
		user1Facility2_test_attribute.setValue("user1Facility2");
		perun.getAttributesManagerBl().setAttribute(sess, facility2, user1, user1Facility2_test_attribute);

		user2Facility2_test_attribute = new Attribute(userFacility_test_atr_def);
		user2Facility2_test_attribute.setValue("user2Facility2");
		perun.getAttributesManagerBl().setAttribute(sess, facility2, user2, user2Facility2_test_attribute);

		user2Facility3_test_attribute = new Attribute(userFacility_test_atr_def);
		user2Facility3_test_attribute.setValue("user2Facility3");
		perun.getAttributesManagerBl().setAttribute(sess, facility3, user2, user2Facility3_test_attribute);

		user3Facility3_test_attribute = new Attribute(userFacility_test_atr_def);
		user3Facility3_test_attribute.setValue("user3Facility3");
		perun.getAttributesManagerBl().setAttribute(sess, facility3, user3, user3Facility3_test_attribute);

		user3Facility2_test_attribute = new Attribute(userFacility_test_atr_def);
		user3Facility2_test_attribute.setValue("user3Facility2");
		perun.getAttributesManagerBl().setAttribute(sess, facility2, user3, user3Facility2_test_attribute);

		user3Facility1_test_attribute = new Attribute(userFacility_test_atr_def);
		user3Facility1_test_attribute.setValue("user3Facility1");
		perun.getAttributesManagerBl().setAttribute(sess, facility1, user3, user3Facility1_test_attribute);
	}

	/**
	 * member1U1Res1Vo1_test_attribute -> member1OfUser1, resource1InVo1
	 * member1U1Res2Vo1_test_attribute -> member1OfUser1, resource2InVo1
	 * member1U2Res1Vo2_test_attribute -> member1OfUser2, resource1InVo2
	 * member1U2Res2Vo2_test_attribute -> member1OfUser2, resource2InVo2
	 * member1U3Res1Vo1_test_attribute -> member1OfUser3, resource1InVo1
	 * member2U3Res1Vo2_test_attribute -> member2OfUser3, resource1InVo2
	 * member2U3Res2Vo2_test_attribute -> member2OfUser3, resource2InVo2
	 */
	private void setAttributesForMemberResourceTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		memberResource_test_atr_def = new AttributeDefinition();
		memberResource_test_atr_def.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		memberResource_test_atr_def.setDescription("memberResource_test_atr_def");
		memberResource_test_atr_def.setFriendlyName("test-memberResource-test-atr-def");
		memberResource_test_atr_def.setType(String.class.getName());
		memberResource_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, memberResource_test_atr_def);

		member1U1Res1Vo1_test_attribute = new Attribute(memberResource_test_atr_def);
		member1U1Res1Vo1_test_attribute.setValue("member1U1Res1Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser1, resource1InVo1, member1U1Res1Vo1_test_attribute);

		member1U1Res2Vo1_test_attribute = new Attribute(memberResource_test_atr_def);
		member1U1Res2Vo1_test_attribute.setValue("member1U1Res2Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser1, resource2InVo1, member1U1Res2Vo1_test_attribute);

		member1U2Res1Vo2_test_attribute = new Attribute(memberResource_test_atr_def);
		member1U2Res1Vo2_test_attribute.setValue("member1U2Res1Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, resource1InVo2, member1U2Res1Vo2_test_attribute);

		member1U2Res2Vo2_test_attribute = new Attribute(memberResource_test_atr_def);
		member1U2Res2Vo2_test_attribute.setValue("member1U2Res2Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, resource2InVo2, member1U2Res2Vo2_test_attribute);

		member1U3Res1Vo1_test_attribute = new Attribute(memberResource_test_atr_def);
		member1U3Res1Vo1_test_attribute.setValue("member1U3Res1Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser3, resource1InVo1, member1U3Res1Vo1_test_attribute);

		member2U3Res1Vo2_test_attribute = new Attribute(memberResource_test_atr_def);
		member2U3Res1Vo2_test_attribute.setValue("member2U3Res1Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser3, resource1InVo2, member2U3Res1Vo2_test_attribute);

		member2U3Res2Vo2_test_attribute = new Attribute(memberResource_test_atr_def);
		member2U3Res2Vo2_test_attribute.setValue("member2U3Res2Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser3, resource2InVo2, member2U3Res2Vo2_test_attribute);
	}

	/**
	 * member1U1Group1Vo1_test_attribute -> member1OfUser1, group1InVo1
	 * member1U1Group2Vo1_test_attribute -> member1OfUser1, group2InVo1
	 * member2U1Group1Vo2_test_attribute -> member2OfUser1, group1InVo2
	 * member2U1Group2Vo2_test_attribute -> member2OfUser1, group2InVo2
	 * member1U2Group1Vo2_test_attribute -> member1OfUser2, group1InVo2
	 * member1U2Group2Vo2_test_attribute -> member1OfUser2, group2InVo2
	 * member2U2Group1Vo1_test_attribute -> member2OfUser2, group1InVo1
	 * member2U2Group2Vo1_test_attribute -> member2OfUser2, group2InVo1
	 * member1U3Group1Vo1_test_attribute -> member1OfUser3, group1InVo1
	 * member2U3Group2Vo2_test_attribute -> member2OfUser3, group2InVo2
	 *
	 */
	private void setAttributesForMemberGroupAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		memberGroup_test_atr_def = new AttributeDefinition();
		memberGroup_test_atr_def.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
		memberGroup_test_atr_def.setDescription("memberGroup_test_atr_def");
		memberGroup_test_atr_def.setFriendlyName("test-memberGroup-test-atr-def");
		memberGroup_test_atr_def.setType(String.class.getName());
		memberGroup_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, memberGroup_test_atr_def);

		member1U1Group1Vo1_test_attribute = new Attribute(memberGroup_test_atr_def);
		member1U1Group1Vo1_test_attribute.setValue("member1U1Group1Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser1, group1InVo1, member1U1Group1Vo1_test_attribute);

		member1U1Group2Vo1_test_attribute = new Attribute(memberGroup_test_atr_def);
		member1U1Group2Vo1_test_attribute.setValue("member1U1Group2Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser1, group2InVo1, member1U1Group2Vo1_test_attribute);

		member2U1Group1Vo2_test_attribute = new Attribute(memberGroup_test_atr_def);
		member2U1Group1Vo2_test_attribute.setValue("member2U1Group1Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser1, group1InVo2, member2U1Group1Vo2_test_attribute);

		member2U1Group2Vo2_test_attribute = new Attribute(memberGroup_test_atr_def);
		member2U1Group2Vo2_test_attribute.setValue("member2U1Group2Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser1, group2InVo2, member2U1Group2Vo2_test_attribute);

		member1U2Group1Vo2_test_attribute = new Attribute(memberGroup_test_atr_def);
		member1U2Group1Vo2_test_attribute.setValue("member1U2Group1Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, group1InVo2, member1U2Group1Vo2_test_attribute);

		member1U2Group2Vo2_test_attribute = new Attribute(memberGroup_test_atr_def);
		member1U2Group2Vo2_test_attribute.setValue("member1U2Group2Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, group2InVo2, member1U2Group2Vo2_test_attribute);

		member2U2Group1Vo1_test_attribute = new Attribute(memberGroup_test_atr_def);
		member2U2Group1Vo1_test_attribute.setValue("member2U2Group1Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser2, group1InVo1, member2U2Group1Vo1_test_attribute);

		member2U2Group2Vo1_test_attribute = new Attribute(memberGroup_test_atr_def);
		member2U2Group2Vo1_test_attribute.setValue("member2U2Group2Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser2, group2InVo1, member2U2Group2Vo1_test_attribute);

		member1U3Group1Vo1_test_attribute = new Attribute(memberGroup_test_atr_def);
		member1U3Group1Vo1_test_attribute.setValue("member1U3Group1Vo1");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser3, group1InVo1, member1U3Group1Vo1_test_attribute);

		member2U3Group2Vo2_test_attribute = new Attribute(memberGroup_test_atr_def);
		member2U3Group2Vo2_test_attribute.setValue("member2U3Group2Vo2");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser3, group2InVo2, member2U3Group2Vo2_test_attribute);
	}

	/**
	 * group1VO1Res1VO1_test_attribute -> resource1InVo1, group1InVo1
	 * group2VO1Res1VO1_test_attribute -> resource1InVo1, group2InVo1
	 * group2VO1Res2VO1_test_attribute -> resource2InVo1, group2InVo1
	 * group1VO2Res1VO2_test_attribute -> resource1InVo2, group1InVo2
	 * group2VO2Res1VO2_test_attribute -> resource1InVo2, group2InVo2
	 * group2VO2Res2VO2_test_attribute -> resource2InVo2, group2InVo2
	 */
	private void setAttributesForGroupResourceAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		groupResource_test_atr_def = new AttributeDefinition();
		groupResource_test_atr_def.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		groupResource_test_atr_def.setDescription("groupResource_test_atr_def");
		groupResource_test_atr_def.setFriendlyName("test-groupResource-test-atr-def");
		groupResource_test_atr_def.setType(String.class.getName());
		groupResource_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, groupResource_test_atr_def);

		group1VO1Res1VO1_test_attribute = new Attribute(groupResource_test_atr_def);
		group1VO1Res1VO1_test_attribute.setValue("G1VO1_RES1VO1");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo1, group1InVo1, group1VO1Res1VO1_test_attribute);

		group2VO1Res1VO1_test_attribute = new Attribute(groupResource_test_atr_def);
		group2VO1Res1VO1_test_attribute.setValue("G2VO1_RES1VO1");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo1, group2InVo1, group2VO1Res1VO1_test_attribute);

		group2VO1Res2VO1_test_attribute = new Attribute(groupResource_test_atr_def);
		group2VO1Res2VO1_test_attribute.setValue("G2VO1_RES2VO1");
		perun.getAttributesManagerBl().setAttribute(sess, resource2InVo1, group2InVo1, group2VO1Res2VO1_test_attribute);

		group1VO2Res1VO2_test_attribute = new Attribute(groupResource_test_atr_def);
		group1VO2Res1VO2_test_attribute.setValue("G1VO2_RES1VO2");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo2, group1InVo2, group1VO2Res1VO2_test_attribute);

		group2VO2Res1VO2_test_attribute = new Attribute(groupResource_test_atr_def);
		group2VO2Res1VO2_test_attribute.setValue("G2VO2_RES1VO2");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo2, group2InVo2, group2VO2Res1VO2_test_attribute);

		group2VO2Res2VO2_test_attribute = new Attribute(groupResource_test_atr_def);
		group2VO2Res2VO2_test_attribute.setValue("G2VO2_RES2VO2");
		perun.getAttributesManagerBl().setAttribute(sess, resource2InVo2, group2InVo2, group2VO2Res2VO2_test_attribute);
	}

	/**
	 * Attribute def types: vo_toEmail_def, vo_fromEmail_def
	 * vo1 -> vo1_toEmail_attribute
	 * vo2 -> vo2_toEmail_attribute, vo2_fromEmail_attribute
	 *
	 */
	private void setAttributesForVoAttributesTest() throws Exception {
		//get Impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		vo_toEmail_def = new AttributeDefinition();
		vo_toEmail_def.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		vo_toEmail_def.setDescription("vo_toEmail_def");
		vo_toEmail_def.setFriendlyName("test-vo-toEmail-def");
		vo_toEmail_def.setType(String.class.getName());
		vo_toEmail_def = perun.getAttributesManagerBl().createAttribute(sess, vo_toEmail_def);

		vo_fromEmail_def = new AttributeDefinition();
		vo_fromEmail_def.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		vo_fromEmail_def.setDescription("vo_fromEmail_def");
		vo_fromEmail_def.setFriendlyName("test-vo-fromEmail-def");
		vo_fromEmail_def.setType(String.class.getName());
		vo_fromEmail_def = perun.getAttributesManagerBl().createAttribute(sess, vo_fromEmail_def);

		//set vo_toEmail_def attribute to vo1
		vo1_toEmail_attribute = new Attribute(vo_toEmail_def);
		vo1_toEmail_attribute.setValue("vo1To@email.com");
		perun.getAttributesManagerBl().setAttribute(sess, vo1, vo1_toEmail_attribute);

		//set vo_toEmail_def attribute to vo2
		vo2_toEmail_attribute = new Attribute(vo_toEmail_def);
		vo2_toEmail_attribute.setValue("vo2To@email.com");
		perun.getAttributesManagerBl().setAttribute(sess, vo2, vo2_toEmail_attribute);

		//set vo_fromEmail_def attribute to vo2
		vo2_fromEmail_attribute = new Attribute(vo_fromEmail_def);
		vo2_fromEmail_attribute.setValue("vo2From@email.com");
		perun.getAttributesManagerBl().setAttribute(sess, vo2, vo2_fromEmail_attribute);
	}

	/**
	 * Sets user attributes like this:
	 * user1 - user1_phone_attribute
	 * user2 - user2_phone_attribute, user2_email_attribute
	 * user3 - user3_email_attribute
	 */
	private void setAttributesForUserAttributesTest() throws Exception {
		//get Impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		user_phone_atr_def = new AttributeDefinition();
		user_phone_atr_def.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		user_phone_atr_def.setDescription("user_phone_atr_def");
		user_phone_atr_def.setFriendlyName("test-user-phone-atr-def");
		user_phone_atr_def.setType(String.class.getName());
		user_phone_atr_def = perun.getAttributesManagerBl().createAttribute(sess, user_phone_atr_def);

		user_email_atr_def = new AttributeDefinition();
		user_email_atr_def.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		user_email_atr_def.setDescription("user_email_atr_def");
		user_email_atr_def.setFriendlyName("test-user-email-atr-def");
		user_email_atr_def.setType(String.class.getName());
		user_email_atr_def = perun.getAttributesManagerBl().createAttribute(sess, user_email_atr_def);

		//set Phone attribute for user1
		user1_phone_attribute = new Attribute(user_phone_atr_def);
		user1_phone_attribute.setValue("+420555444222");
		perun.getAttributesManagerBl().setAttribute(sess, user1, user1_phone_attribute);

		//set Phone attribute for user2
		user2_phone_attribute = new Attribute(user_phone_atr_def);
		user2_phone_attribute.setValue("+420888555444");
		perun.getAttributesManagerBl().setAttribute(sess, user2, user2_phone_attribute);

		//set Prefferred mail attribute for user2
		user2_email_attribute = new Attribute(user_email_atr_def);
		user2_email_attribute.setValue("user2@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, user2, user2_email_attribute);

		//set Preferred mail attribute for user3
		user3_email_attribute = new Attribute(user_email_atr_def);
		user3_email_attribute.setValue("user3@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, user3, user3_email_attribute);
	}

	/**
	 * Sets member attributes like this:
	 * member1OfUser1: member1OfUser1_phone_attribute
	 * member2OfUser1: member2OfUser1_phone_attribute - disallowed
	 * member1OfUser2: member1OfUser2_phone_attribute, member1OfUser2_mail_attribute
	 * member2OfUser2: member2OfUser2_phone_attribute, member2OfUser2_mail_attribute - disallowed
	 * member1OfUser3: member1OfUser3_mail_attribute
	 * member2OfUser3: member2OfUser3_mail_attribute
	 */
	private void setAttributesForMemberAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		member_phone_atr_def = new AttributeDefinition();
		member_phone_atr_def.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		member_phone_atr_def.setDescription("member_phone_atr_def");
		member_phone_atr_def.setFriendlyName("test-member-phone-atr-def");
		member_phone_atr_def.setType(String.class.getName());
		member_phone_atr_def = perun.getAttributesManagerBl().createAttribute(sess, member_phone_atr_def);

		member_email_atr_def = new AttributeDefinition();
		member_email_atr_def.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		member_email_atr_def.setDescription("member_email_atr_def");
		member_email_atr_def.setFriendlyName("test-member-email-atr-def");
		member_email_atr_def.setType(String.class.getName());
		member_email_atr_def = perun.getAttributesManagerBl().createAttribute(sess, member_email_atr_def);


		//set Phone attribute for member1OfUser1 and member2OfUser1
		member1OfUser1_phone_attribute = new Attribute(member_phone_atr_def);
		member1OfUser1_phone_attribute.setValue("+420555444222");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser1, member1OfUser1_phone_attribute);

		member2OfUser1_phone_attribute = new Attribute(member_phone_atr_def);
		member2OfUser1_phone_attribute.setValue("+420555444111");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser1, member2OfUser1_phone_attribute);

		//set Phone attribute for member1OfUser2 and member2OfUser2
		member1OfUser2_phone_attribute = new Attribute(member_phone_atr_def);
		member1OfUser2_phone_attribute.setValue("+420888555444");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, member1OfUser2_phone_attribute);

		member2OfUser2_phone_attribute = new Attribute(member_phone_atr_def);
		member2OfUser2_phone_attribute.setValue("+420888555898");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser2, member2OfUser2_phone_attribute);

		//set email attribute for member1OfUser2 and member2OfUser2
		member1OfUser2_mail_attribute = new Attribute(member_email_atr_def);
		member1OfUser2_mail_attribute.setValue("user2@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser2, member1OfUser2_mail_attribute);

		member2OfUser2_mail_attribute = new Attribute(member_email_atr_def);
		member2OfUser2_mail_attribute.setValue("user22@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser2, member2OfUser2_mail_attribute);

		//set email attribute for member1OfUser3 and member2OfUser3
		member1OfUser3_mail_attribute = new Attribute(member_email_atr_def);
		member1OfUser3_mail_attribute.setValue("user3@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, member1OfUser3, member1OfUser3_mail_attribute);

		member2OfUser3_mail_attribute = new Attribute(member_email_atr_def);
		member2OfUser3_mail_attribute.setValue("user32@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, member2OfUser3, member2OfUser3_mail_attribute);
	}

	/**
	 * group1InVo1:           group1InVo1_email_atr
	 * group2InVo1:           group2InVo1_email_atr
	 * group1InVo2:           group1InVo2_email_atr
	 * group2InVo2:           group2InVo2_email_atr
	 * membersGroupOfVo1:     membersGroupOfVo1_email_atr
	 * membersGroupOfVo2:     membersGroupOfVo2_email_atr
	 */
	private void setAttributesForGroupAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		group_fromEmail_atr_def = new AttributeDefinition();
		group_fromEmail_atr_def.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		group_fromEmail_atr_def.setDescription("group_fromEmail_atr_def");
		group_fromEmail_atr_def.setFriendlyName("test-group-fromEmail-atr-def");
		group_fromEmail_atr_def.setType(String.class.getName());
		group_fromEmail_atr_def = perun.getAttributesManagerBl().createAttribute(sess, group_fromEmail_atr_def);

		group1InVo1_email_atr = new Attribute(group_fromEmail_atr_def);
		group1InVo1_email_atr.setValue("1@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, group1InVo1, group1InVo1_email_atr);

		group2InVo1_email_atr = new Attribute(group_fromEmail_atr_def);
		group2InVo1_email_atr.setValue("2@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, group2InVo1, group2InVo1_email_atr);

		group1InVo2_email_atr = new Attribute(group_fromEmail_atr_def);
		group1InVo2_email_atr.setValue("3@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, group1InVo2, group1InVo2_email_atr);

		group2InVo2_email_atr = new Attribute(group_fromEmail_atr_def);
		group2InVo2_email_atr.setValue("4@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, group2InVo2, group2InVo2_email_atr);

		membersGroupOfVo1_email_atr = new Attribute(group_fromEmail_atr_def);
		membersGroupOfVo1_email_atr.setValue("5@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo1, membersGroupOfVo1_email_atr);

		membersGroupOfVo2_email_atr = new Attribute(group_fromEmail_atr_def);
		membersGroupOfVo2_email_atr.setValue("6@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo2, membersGroupOfVo2_email_atr);
	}

	/**
	 * resource1InVo1: resource1InVo1_test_atr
	 * resource2InVo1: resource2InVo1_test_atr
	 * resource1InVo2: resource1InVo2_test_atr
	 * resource2InVo2: resource2InVo2_test_atr
	 */
	private void setAttributesForResourceAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		resource_test_atr_def = new AttributeDefinition();
		resource_test_atr_def.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		resource_test_atr_def.setDescription("resource_test_atr_def");
		resource_test_atr_def.setFriendlyName("test-resource-test-atr-def");
		resource_test_atr_def.setType(String.class.getName());
		resource_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, resource_test_atr_def);

		resource1InVo1_test_atr = new Attribute(resource_test_atr_def);
		resource1InVo1_test_atr.setValue("1K");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo1, resource1InVo1_test_atr);

		resource2InVo1_test_atr = new Attribute(resource_test_atr_def);
		resource2InVo1_test_atr.setValue("2K");
		perun.getAttributesManagerBl().setAttribute(sess, resource2InVo1, resource2InVo1_test_atr);

		resource1InVo2_test_atr = new Attribute(resource_test_atr_def);
		resource1InVo2_test_atr.setValue("3K");
		perun.getAttributesManagerBl().setAttribute(sess, resource1InVo2, resource1InVo2_test_atr);

		resource2InVo2_test_atr = new Attribute(resource_test_atr_def);
		resource2InVo2_test_atr.setValue("4K");
		perun.getAttributesManagerBl().setAttribute(sess, resource2InVo2, resource2InVo2_test_atr);
	}

	/**
	 * facility1: facility1_test_atr
	 * facility2: facility2_test_atr
	 * facility3: facility3_test_atr
	 */
	private void setAttributesForFacilityAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		facility_test_atr_def = new AttributeDefinition();
		facility_test_atr_def.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		facility_test_atr_def.setDescription("facility_test_atr_def");
		facility_test_atr_def.setFriendlyName("test-facility-test-atr-def");
		facility_test_atr_def.setType(String.class.getName());
		facility_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, facility_test_atr_def);

		facility1_test_atr = new Attribute(facility_test_atr_def);
		facility1_test_atr.setValue("756");
		perun.getAttributesManagerBl().setAttribute(sess, facility1, facility1_test_atr);

		facility2_test_atr = new Attribute(facility_test_atr_def);
		facility2_test_atr.setValue("0475");
		perun.getAttributesManagerBl().setAttribute(sess, facility2, facility2_test_atr);

		facility3_test_atr = new Attribute(facility_test_atr_def);
		facility3_test_atr.setValue("0000");
		perun.getAttributesManagerBl().setAttribute(sess, facility3, facility3_test_atr);
	}

	/**
	 * host1OnFacility1: host1F1_test_atr
	 * host2OnFacility1: host2F1_test_atr
	 * host1OnFacility2: host1F2_test_atr
	 * host2OnFacility2: host2F2_test_atr
	 * host1OnFacility3: host1F3_test_atr
	 * host2OnFacility3: host2F3_test_atr
	 */
	private void setAttributesForHostAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());

		host_test_atr_def = new AttributeDefinition();
		host_test_atr_def.setNamespace(AttributesManager.NS_HOST_ATTR_DEF);
		host_test_atr_def.setDescription("host_test_atr_def");
		host_test_atr_def.setFriendlyName("test-host-test-atr-def");
		host_test_atr_def.setType(String.class.getName());
		host_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, host_test_atr_def);

		host1F1_test_atr = new Attribute(host_test_atr_def);
		host1F1_test_atr.setValue("host1F1");
		perun.getAttributesManagerBl().setAttribute(sess, host1OnFacility1, host1F1_test_atr);

		host2F1_test_atr = new Attribute(host_test_atr_def);
		host2F1_test_atr.setValue("host2F1");
		perun.getAttributesManagerBl().setAttribute(sess, host2OnFacility1, host2F1_test_atr);

		host1F2_test_atr = new Attribute(host_test_atr_def);
		host1F2_test_atr.setValue("host1F2");
		perun.getAttributesManagerBl().setAttribute(sess, host1OnFacility2, host1F2_test_atr);

		host2F2_test_atr = new Attribute(host_test_atr_def);
		host2F2_test_atr.setValue("host2F2");
		perun.getAttributesManagerBl().setAttribute(sess, host2OnFacility2, host2F2_test_atr);

		host1F3_test_atr = new Attribute(host_test_atr_def);
		host1F3_test_atr.setValue("host1F3");
		perun.getAttributesManagerBl().setAttribute(sess, host1OnFacility3, host1F3_test_atr);

		host2F3_test_atr = new Attribute(host_test_atr_def);
		host2F3_test_atr.setValue("host2F3");
		perun.getAttributesManagerBl().setAttribute(sess, host2OnFacility3, host2F3_test_atr);
	}

	/**
	 * userExtSource1: ues1_test_atr
	 * userExtSource2: ues2_test_atr
	 * userExtSource3: ues3_test_atr
	 */
	private void setAttributesForUESAttributesTest() throws Exception {
		//get impl object
		attributesManagerBl = getTargetObject(perun.getAttributesManagerBl());


		ues_test_atr_def = new AttributeDefinition();
		ues_test_atr_def.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		ues_test_atr_def.setDescription("ues_test_atr_def");
		ues_test_atr_def.setFriendlyName("test-ues-test-atr-def");
		ues_test_atr_def.setType(String.class.getName());
		ues_test_atr_def = perun.getAttributesManagerBl().createAttribute(sess, ues_test_atr_def);

		//create sample of internal attribute
		internal_ues_atr = new Attribute(ues_test_atr_def);

		ues1_test_atr = new Attribute(ues_test_atr_def);
		ues1_test_atr.setValue("ues1");
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource1, ues1_test_atr);

		ues2_test_atr = new Attribute(ues_test_atr_def);
		ues2_test_atr.setValue("ues2");
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource2, ues2_test_atr);

		ues3_test_atr = new Attribute(ues_test_atr_def);
		ues3_test_atr.setValue("ues3");
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource3, ues3_test_atr);
	}

	/**
	 * cast spring proxy type to regular impl type
	 */
	@SuppressWarnings({"unchecked"})
	private <T> T getTargetObject(Object proxy) throws Exception {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return (T) getTargetObject(((Advised)proxy).getTargetSource().getTarget());
		}
		return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
	}

	private Method getPrivateMethodFromAtrManager(String methodName, Class<?>... argClasses) throws Exception {
		Method method = AttributesManagerBlImpl.class.getDeclaredMethod(methodName, argClasses);
		method.setAccessible(true);
		return method;
	}

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
		candidate.setAttributes(new HashMap<>());

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
		perun.getServicesManager().createService(sess, service);

		return service;

	}

	private Service setUpService2() throws Exception {

		Service service = new Service();
		service.setName("AttributesManagerTestService2");
		perun.getServicesManager().createService(sess, service);

		return service;
	}

	private Group setUpGroup() throws Exception {

		Group group = perun.getGroupsManager().createGroup(sess, vo, new Group("AttrTestGroup","AttrTestGroupDescription"));
		assertNotNull("unable to create a group", group);
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
		List<Host> hosts = new ArrayList<>();
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

	private UserExtSource setUpUserExtSourceTest() throws Exception {
		vo = setUpVo();
		member = setUpMember();
		User user = perun.getUsersManager().getUserByMember(sess, member);
		List<UserExtSource> userExtSources = perun.getUsersManager().getUserExtSources(sess, user);

		UserExtSource ues = null;
		for (UserExtSource u: userExtSources) {
			if (!"PERUN".equals(u.getExtSource().getName())) {
				ues = u;
				break;
			}
		}
		assertTrue("User has more more UserExtSources than expected. Expected 2 (PERUN, testExtSource), contains " + userExtSources.size(), userExtSources.size() == 2);
		return ues;
	}

	private UserExtSource setUpUserExtSource() throws Exception {

		String extSourceName = "AttributesManagerEntryIntegrationTest";

		ExtSource extSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
		extSource = perun.getExtSourcesManager().createExtSource(sess, extSource, new HashMap<>());

		UserExtSource userExtSource = new UserExtSource(0, extSource, "let's fake it");
		return userExtSource;

	}

	private List<Attribute> setUpRequiredAttributes() throws Exception {

		List<Attribute> attrList = new ArrayList<>();

		attrList.add(setUpFacilityAttribute().get(0));
		attrList.add(setUpVoAttribute().get(0));
		attrList.add(setUpFacilityUserAttribute().get(0));
		attrList.add(setUpResourceAttribute().get(0));
		attrList.add(setUpMemberAttribute().get(0));
		attrList.add(setUpMemberResourceAttribute().get(0));
		attrList.add(setUpMemberGroupAttribute().get(0));
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
		attribute.setFriendlyName("resource-test-attribute-2");
		attribute.setType(String.class.getName());
		attribute.setValue("ResourceAttribute");
		assertNotNull("unable to create resource attribute", attributesManager.createAttribute(sess, attribute));

		listOfAttrs.add(attribute);

		perun.getServicesManager().addRequiredAttributes(sess, service, listOfAttrs);

		return attribute;
	}

	private List<Attribute> setUpFacilityUserAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user_facility:attribute-def:opt");
		attr.setFriendlyName("user-facility-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserFacilityAttribute");

		assertNotNull("unable to create user_facility attribute",attributesManager.createAttribute(sess, attr));
		// create new facility-user attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpFacilityAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:facility:attribute-def:opt");
		attr.setFriendlyName("facility-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("FacilityAttribute");
		assertNotNull("unable to create facility attribute",attributesManager.createAttribute(sess, attr));
		// create new facility attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpEntitylessAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("entityless-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("EntitylessAttribute");
		assertNotNull("unable to create facility attribute",attributesManager.createAttribute(sess, attr));
		//create new entityless attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it
		return attributes;
	}

	private List<Attribute> setUpVoAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:vo:attribute-def:opt");
		attr.setFriendlyName("vo-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("VoAttribute");
		assertNotNull("unable to create vo attribute",attributesManager.createAttribute(sess, attr));
		// create new vo attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:resource:attribute-def:opt");
		attr.setFriendlyName("resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("ResourceAttribute");
		assertNotNull("unable to create resource attribute",attributesManager.createAttribute(sess, attr));
		// create new resource attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private Attribute setUpAttribute(String type, String friendlyName, String namespace, Object value) throws Exception {
		Attribute attr = new Attribute();
		attr.setNamespace(namespace);
		attr.setFriendlyName(friendlyName);
		attr.setType(type);
		attr.setValue(value);
		attr.setDescription("TEST DESCRIPTION");
		assertNotNull("unable to create " + attr.getName() + " attribute",attributesManager.createAttribute(sess, attr));
		return attr;
	}

	private List<Attribute> setUpMemberResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_resource:attribute-def:opt");
		attr.setFriendlyName("member-resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberResourceAttribute");
		assertNotNull("unable to create member-resource attribute",attributesManager.createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpMemberGroupAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_group:attribute-def:opt");
		attr.setFriendlyName("member-group-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberGroupAttribute");
		assertNotNull("unable to create member-group attribute",attributesManager.createAttribute(sess, attr));
		// create new member-group attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;
	}

	private List<Attribute> setUpUserAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserAttribute");
		assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpUserLargeAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-large-test-attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<>();
		value.put("UserLargeAttribute", "test value");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpResourceLargeAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:resource:attribute-def:opt");
		attr.setFriendlyName("resource-large-test-attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<>();
		value.put("ResourceLargeAttribute", "test value");
		value.put("ResourceTestLargeAttr", "test value 2");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",attributesManager.createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it
		return attributes;

	}

	private List<Attribute> setUpMemberAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member:attribute-def:opt");
		attr.setFriendlyName("member-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberAttribute");

		assertNotNull("unable to create member attribute",attributesManager.createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}


	private List<Attribute> setUpGroupAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:group:attribute-def:opt");
		attr.setFriendlyName("group-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");

		assertNotNull("unable to create group attribute",attributesManager.createAttribute(sess, attr));
		// create new group attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpGroupAttributes() throws Exception {

		Attribute attr = new Attribute();
		String namespace = "group-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");

		List<Attribute> attributes = new ArrayList<>();
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
		attr4.setFriendlyName("group-test-uniqueEattribute:specialNamespace");
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
		String namespace = "resource-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("ResourceAttribute");

		List<Attribute> attributes = new ArrayList<>();
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
		attr4.setFriendlyName("resource-test-uniqueEattribute:specialNamespace");
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
		attr.setFriendlyName("host-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("HostAttribute");

		assertNotNull("unable to create host attribute",attributesManager.createAttribute(sess, attr));
		// create new host attribute

		List<Attribute> attributes = new ArrayList<>();
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
		attr.setFriendlyName("group-resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("GroupResourceAttribute");

		assertNotNull("unable to create Group_Resource attribute",attributesManager.createAttribute(sess, attr));
		// create new group resource attribute

		List<Attribute> attributes = new ArrayList<>();
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

	private List<Attribute> setUpUserExtSourceAttribute() throws Exception {
		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:ues:attribute-def:opt");
		attr.setFriendlyName("userExtSource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserExtSourceAttribute");

		assertNotNull("unable to create userExtSource attribute", attributesManager.createAttribute(sess, attr));

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		return attributes;
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
		Map<AttributeDefinition, Set<AttributeDefinition>> allDependenciesForTesting = new HashMap<>();
		//Prepare every possible way to test Attribute with Attribute

		//TODO FILL THIS MAP FOR USING

		return allDependenciesForTesting;
	}

}
