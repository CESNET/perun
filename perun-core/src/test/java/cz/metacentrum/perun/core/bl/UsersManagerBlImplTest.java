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
import cz.metacentrum.perun.core.api.RichResource;
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
public class UsersManagerBlImplTest {

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

	private static final String EXT_SOURCE_NAME = "UsersManagerBlExtSource";
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


		vo = new Vo(0, "UsersBlImplTestVo", "UsrMgrBlImplTestVo");
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
	public void getAllowedResourcesForFacilityAndUserTest() throws Exception {
		System.out.println("UsersManagerBlImpl.getAllowedResources(facility,user)");

		List<Resource> resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2)));
		Assert.assertEquals(2, resourceList.size());

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.contains(resource3));
		Assert.assertEquals(1, resourceList.size());

		// disable member 1, we should have only single allowed resource

		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility, user);
		Assert.assertTrue(resourceList.contains(resource2));
		Assert.assertEquals(1, resourceList.size());

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.contains(resource3));
		Assert.assertEquals(1, resourceList.size());

		// disable member 2, we should have only single allowed resource

		perun.getMembersManagerBl().disableMember(sess, member2);

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility, user);
		Assert.assertTrue(resourceList.isEmpty());

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.isEmpty());

	}

	@Test
	public void getAssignedResourcesForFacilityAndUserTest() throws Exception {
		System.out.println("UsersManagerBlImpl.getAssignedResources(facility,user)");

		List<Resource> resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2)));
		Assert.assertEquals(2, resourceList.size());

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.contains(resource3));
		Assert.assertEquals(1, resourceList.size());

		// disable member 1, it shouldn't have any effect

		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2)));
		Assert.assertEquals(2, resourceList.size());

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.contains(resource3));
		Assert.assertEquals(1, resourceList.size());

		// remove member2 from group2

		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility, user);
		Assert.assertTrue(resourceList.contains(resource));
		Assert.assertEquals(1, resourceList.size());

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, facility2, user);
		Assert.assertTrue(resourceList.isEmpty());

	}

	@Test
	public void getAllowedResourcesForUserTest() throws Exception {
		System.out.println("UsersManagerBlImpl.getAllowedResources(user)");

		List<Resource> resourceList = perun.getUsersManagerBl().getAllowedResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2, resource3)));
		Assert.assertEquals(3, resourceList.size());

		// disable member 1, we should have only two allowed resource from two facilities

		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource2, resource3)));
		Assert.assertEquals(2, resourceList.size());

		// disable member 2, we shouldn't have any allowed resource

		perun.getMembersManagerBl().disableMember(sess, member2);

		resourceList = perun.getUsersManagerBl().getAllowedResources(sess, user);
		Assert.assertTrue(resourceList.isEmpty());

	}

	@Test
	public void getAssignedResourcesForUserTest() throws Exception {
		System.out.println("UsersManagerBlImpl.getAssignedResources(user)");

		List<Resource> resourceList = perun.getUsersManagerBl().getAssignedResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2, resource3)));
		Assert.assertEquals(3, resourceList.size());

		// disable member 1, it shouldn't have any effect

		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(resource, resource2, resource3)));
		Assert.assertEquals(3, resourceList.size());

		// remove member2 from group2, we should have single resource left

		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		resourceList = perun.getUsersManagerBl().getAssignedResources(sess, user);
		Assert.assertTrue(resourceList.contains(resource));
		Assert.assertEquals(1, resourceList.size());

	}

	@Test
	public void getAssignedRichResourcesForUserTest() throws Exception {
		System.out.println("UsersManagerBlImpl.getAssignedRichResources(user)");

		RichResource rr = perun.getResourcesManagerBl().getRichResourceById(sess, resource.getId());
		RichResource rr2 = perun.getResourcesManagerBl().getRichResourceById(sess, resource2.getId());
		RichResource rr3 = perun.getResourcesManagerBl().getRichResourceById(sess, resource3.getId());

		List<RichResource> resourceList = perun.getUsersManagerBl().getAssignedRichResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(rr, rr2, rr3)));
		Assert.assertEquals(3, resourceList.size());

		// disable member 1, it shouldn't have any effect

		perun.getMembersManagerBl().disableMember(sess, member);

		resourceList = perun.getUsersManagerBl().getAssignedRichResources(sess, user);
		Assert.assertTrue(resourceList.containsAll(Arrays.asList(rr, rr2, rr3)));
		Assert.assertEquals(3, resourceList.size());

		// remove member2 from group2, we should have single resource left

		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		resourceList = perun.getUsersManagerBl().getAssignedRichResources(sess, user);
		Assert.assertTrue(resourceList.contains(rr));
		Assert.assertEquals(1, resourceList.size());

	}

}
