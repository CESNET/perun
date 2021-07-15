package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
		@ContextConfiguration(locations = { "classpath:perun-base.xml", "classpath:perun-core.xml" })
})
@Transactional(transactionManager = "springTransactionManager")
public class ResourcesManagerBlImplTest {

	@Autowired
	private PerunBl perun;

	private PerunSession sess;
	private User user;
	private Vo vo;
	private Member member;
	private Group group;
	private Resource resource;
	private Facility facility;

	private Vo vo2;
	private Member member2;
	private Group group2;
	private Resource resource2;

	private Resource resource3;
	private Facility facility2;

	private static final String EXT_SOURCE_NAME = "ResourcesManagerBlExtSource";
	private ExtSource extSource = new ExtSource(0, EXT_SOURCE_NAME, ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Candidate candidate;
	private UserExtSource ues;


	@Before
	public void setUp() throws Exception {

		candidate = new Candidate();
		candidate.setFirstName("some");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("testingUser");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		ues = new UserExtSource(extSource, "extLogin");
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		sess = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());


		vo = new Vo(0, "ResourcesBlImplTestVo", "ResMgrBlImplTestVo");
		vo = perun.getVosManagerBl().createVo(sess, vo);

		member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

		group = new Group("testGroup", "testGroup");
		group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

		perun.getGroupsManagerBl().addMember(sess, group, member);

		facility = new Facility(0, "testFac");
		facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);

		resource = new Resource(0, "testRes", null, facility.getId(), vo.getId());
		resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false);

		// second branch

		vo2 = new Vo(0, "FacilitiesManagerBlImplTestVo2", "FacMgrBlImplTestVo2");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);

		member2 = perun.getMembersManagerBl().createMemberSync(sess, vo2, candidate);

		group2 = new Group("testGroup", "testGroup");
		group2 = perun.getGroupsManagerBl().createGroup(sess, vo2, group2);

		perun.getGroupsManagerBl().addMember(sess, group2, member2);

		resource2 = new Resource(0, "testRes2", null, facility.getId(), vo2.getId());
		resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo2, facility);

		// third branch

		facility2 = new Facility(0, "testFac2");
		facility2 = perun.getFacilitiesManagerBl().createFacility(sess, facility2);

		resource3 = new Resource(0, "testRes3", null, facility2.getId(), vo2.getId());
		resource3 = perun.getResourcesManagerBl().createResource(sess, resource3, vo2, facility2);

		perun.getResourcesManagerBl().assignGroupToResources(sess, group2, Arrays.asList(resource2, resource3), false);

		user = perun.getUsersManagerBl().getUserByMember(sess, member);

	}


	@Test
	public void getAssignedResourcesForUserAndVo() throws Exception {
		System.out.println("ResourcesManagerBlImpl.getAssignedResources(user,vo)");

		List<Resource> resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo);
		Assert.assertTrue(resourceList.contains(resource));
		Assert.assertEquals(1, resourceList.size());

		resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo2);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource2, resource3)));
		Assert.assertEquals(2, resourceList.size());

		// disabling member should have no effect
		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo);
		Assert.assertTrue(resourceList.contains(resource));
		Assert.assertEquals(1, resourceList.size());

		resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo2);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource2, resource3)));
		Assert.assertEquals(2, resourceList.size());

		// removing member2 from group2 should have effect
		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo);
		Assert.assertTrue(resourceList.contains(resource));
		Assert.assertEquals(1, resourceList.size());

		resourceList = perun.getResourcesManagerBl().getAssignedResources(sess, user, vo2);
		Assert.assertTrue(resourceList.isEmpty());

	}

	@Test
	public void getAssignedUsers() throws Exception {
		System.out.println("ResourcesManagerBlImpl.getAssignedUsers(resource)");

		List<User> usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource2);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource3);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		// disabling member should have no effect
		perun.getMembersManagerBl().disableMember(sess, member);

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource2);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource3);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		// removing member2 from group2 should have effect
		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource);
		Assert.assertTrue(usersList.contains(user));
		Assert.assertEquals(1, usersList.size());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource2);
		Assert.assertTrue(usersList.isEmpty());

		usersList = perun.getResourcesManagerBl().getAssignedUsers(sess, resource3);
		Assert.assertTrue(usersList.isEmpty());

	}

	@Test
	public void isUserAssigned() throws Exception {
		System.out.println("ResourcesManagerBlImpl.isUserAssigned(user, resource)");

		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource2));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource3));

		// disabling member should have no effect
		perun.getMembersManagerBl().disableMember(sess, member);

		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource2));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource3));

		// removing member2 from group2 should have effect
		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		Assert.assertTrue(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource));
		Assert.assertFalse(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource2));
		Assert.assertFalse(perun.getResourcesManagerBl().isUserAssigned(sess, user, resource3));

	}

	@Test
	public void isUserAllowed() throws Exception {
		System.out.println("ResourcesManagerBlImpl.isUserAllowed(user, resource)");

		Assert.assertTrue(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource2));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource3));

		// disabling member should have effect
		perun.getMembersManagerBl().disableMember(sess, member);

		Assert.assertFalse(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource2));
		Assert.assertTrue(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource3));

		// removing member2 from group2 should have effect too
		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		Assert.assertFalse(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource));
		Assert.assertFalse(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource2));
		Assert.assertFalse(perun.getResourcesManagerBl().isUserAllowed(sess, user, resource3));

	}

	@Test
	public void getAllowedUsersNotExpiredTest() throws Exception {
		System.out.println("ResourcesManagerBlImpl.getAllowedUsersNotExpiredInGrouús(resource)");

		List<User> users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource2);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource3);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		// expiring member2 in group2 should have effect
		perun.getGroupsManagerBl().expireMemberInGroup(sess, member2, group2);

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource2);
		Assert.assertTrue(users.isEmpty());

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource3);
		Assert.assertTrue(users.isEmpty());

		// disabling member should have effect too
		perun.getMembersManagerBl().disableMember(sess, member);

		users = perun.getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource);
		Assert.assertTrue(users.isEmpty());

	}

	@Test
	public void getAllowedMembersNotExpiredInGroup() throws Exception {
		System.out.println("ResourcesManagerBlImpl.getAllowedMembersNotExpiredInGroups(resource)");

		List<Member> members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
		Assert.assertEquals(1, members.size());
		Assert.assertTrue(members.contains(member));

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource2);
		Assert.assertEquals(1, members.size());
		Assert.assertTrue(members.contains(member2));

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource3);
		Assert.assertEquals(1, members.size());
		Assert.assertTrue(members.contains(member2));

		// expiring member2 in group2 should have effect
		perun.getGroupsManagerBl().expireMemberInGroup(sess, member2, group2);

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
		Assert.assertEquals(1, members.size());
		Assert.assertTrue(members.contains(member));

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource2);
		Assert.assertTrue(members.isEmpty());

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource3);
		Assert.assertTrue(members.isEmpty());

		// disabling member should have effect too
		perun.getMembersManagerBl().disableMember(sess, member);

		members = perun.getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
		Assert.assertTrue(members.isEmpty());

	}

}
