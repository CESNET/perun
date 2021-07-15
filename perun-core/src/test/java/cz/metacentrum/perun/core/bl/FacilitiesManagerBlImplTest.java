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
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
		@ContextConfiguration(locations = { "classpath:perun-base.xml", "classpath:perun-core.xml" })
})
@Transactional(transactionManager = "springTransactionManager")
public class FacilitiesManagerBlImplTest {

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

	private static final String EXT_SOURCE_NAME = "FacilitiesManagerBlExtSource";
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


		vo = new Vo(0, "FacilitiesManagerBlImplTestVo", "FacMgrBlImplTestVo");
		vo = perun.getVosManagerBl().createVo(sess, vo);

		member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

		group = new Group("testGroup", "testGroup");
		group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

		perun.getGroupsManagerBl().addMember(sess, group, member);

		facility = new Facility(0, "testFac");
		facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);

		resource = new Resource(0, "testRes", null, facility.getId(), vo.getId());
		resource = perun.getResourcesManagerBl().createResource(sess, resource, vo ,facility);

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
		resource3 = perun.getResourcesManagerBl().createResource(sess, resource3, vo2 ,facility2);

		perun.getResourcesManagerBl().assignGroupToResources(sess, group2, Arrays.asList(resource2, resource3), false);

		user = perun.getUsersManagerBl().getUserByMember(sess, member);

	}

	@Test
	public void getAllowedFacilitiesTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAllowedFacilities");

		List<Facility> allowedFacilitiesOld = getFacilitiesOldStyle();
		List<Facility> allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertTrue(allowedFacilitiesOld.containsAll(allowedFacilitiesNew));
		Assert.assertEquals(2, allowedFacilitiesNew.size());

		// disable 1st member => should make no change, since 2nd member is on both facilities
		perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);

		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertTrue(allowedFacilitiesOld.containsAll(allowedFacilitiesNew));
		Assert.assertEquals(2, allowedFacilitiesNew.size());

		// disable 2nd member => user shouldn't be allowed anywhere
		perun.getMembersManagerBl().setStatus(sess, member2, Status.DISABLED);

		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		Assert.assertTrue(allowedFacilitiesNew.isEmpty() && allowedFacilitiesOld.isEmpty());

		// enable 1st member => should be only on first facility
		perun.getMembersManagerBl().setStatus(sess, member, Status.VALID);
		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertEquals(1, allowedFacilitiesOld.size());
		Assert.assertEquals(1, allowedFacilitiesNew.size());
		Assert.assertTrue(allowedFacilitiesNew.contains(facility));
		Assert.assertFalse(allowedFacilitiesNew.contains(facility2));

	}

	@Test
	public void getAllowedFacilitiesForMemberTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAllowedFacilitiesForMember");

		List<Facility> allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member);
		Assert.assertTrue(allowedFacilities.contains(facility));
		Assert.assertEquals(1, allowedFacilities.size());
		Assert.assertFalse(allowedFacilities.contains(facility2));

		// second member is on both facilities
		List<Facility> allowedFacilities2 = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member2);
		Assert.assertTrue(allowedFacilities2.contains(facility));
		Assert.assertTrue(allowedFacilities2.contains(facility2));
		Assert.assertEquals(2, allowedFacilities2.size());

		// If member is not valid, we shouldn have any allowed facilities from it
		perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);

		allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member);
		Assert.assertTrue(allowedFacilities.isEmpty());

		// If member2 is not valid, we shouldn have any allowed facilities from it
		perun.getMembersManagerBl().setStatus(sess, member2, Status.DISABLED);

		allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member2);
		Assert.assertTrue(allowedFacilities.isEmpty());

	}

	@Test
	public void getAssignedUsersTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAssignedUsers(facility)");

		List<User> users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility2);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		// removing member2 from group2 should affect only second facility
		perun.getGroupsManagerBl().removeMember(sess, group2, member2);

		users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility2);
		Assert.assertTrue(users.isEmpty());

	}

	@Test
	public void getAssignedUsersByServiceTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAssignedUsers(facility, service)");

		Service service = new Service(0, "dummy_service");
		service = perun.getServicesManagerBl().createService(sess, service);

		perun.getResourcesManagerBl().assignService(sess, resource, service);

		List<User> users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility, service);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

		users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility2, service);
		Assert.assertTrue(users.isEmpty());

		// adding service3 to another facility should work
		perun.getResourcesManagerBl().assignService(sess, resource3, service);

		users = perun.getFacilitiesManagerBl().getAssignedUsers(sess, facility2, service);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(user));

	}

	/**
	 * Returns allowed facilities using old implementation by iteration
	 * @return allowed facilities
	 */
	private List<Facility> getFacilitiesOldStyle() {

		Set<Facility> assignedFacilities = new HashSet<>();
		for(Member member : perun.getMembersManagerBl().getMembersByUser(sess, user)) {
			if(!perun.getMembersManagerBl().haveStatus(sess, member, Status.INVALID) &&
					!perun.getMembersManagerBl().haveStatus(sess, member, Status.DISABLED)) {
				assignedFacilities.addAll(perun.getFacilitiesManagerBl().getAssignedFacilities(sess, member));
			}
		}
		return new ArrayList<>(assignedFacilities);

	}

}
