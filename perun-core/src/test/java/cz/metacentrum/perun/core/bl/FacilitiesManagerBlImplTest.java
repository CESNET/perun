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
		perun.getExtSourcesManagerBl().loadExtSourcesDefinitions(sess);


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

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		// second branch

		vo2 = new Vo(0, "FacilitiesManagerBlImplTestVo2", "FacMgrBlImplTestVo2");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);

		member2 = perun.getMembersManagerBl().createMemberSync(sess, vo2, candidate);

		group2 = new Group("testGroup", "testGroup");
		group2 = perun.getGroupsManagerBl().createGroup(sess, vo2, group2);

		perun.getGroupsManagerBl().addMember(sess, group2, member2);

		resource2 = new Resource(0, "testRes2", null, facility.getId(), vo2.getId());
		resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo2 ,facility);

		// third branch

		facility2 = new Facility(0, "testFac2");
		facility2 = perun.getFacilitiesManagerBl().createFacility(sess, facility2);

		resource3 = new Resource(0, "testRes3", null, facility2.getId(), vo2.getId());
		resource3 = perun.getResourcesManagerBl().createResource(sess, resource3, vo2 ,facility2);

		perun.getResourcesManagerBl().assignGroupToResources(sess, group2, Arrays.asList(resource2, resource3));

	}

	@Test
	public void getAllowedFacilitiesTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAllowedFacilitiesTest");

		user = perun.getUsersManagerBl().getUserByMember(sess, member);

		List<Facility> allowedFacilitiesOld = getFacilitiesOldStyle();
		List<Facility> allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertTrue(allowedFacilitiesOld.containsAll(allowedFacilitiesNew));
		Assert.assertTrue(allowedFacilitiesNew.size() == 2);

		// disable 1st member => should make no change, since 2nd member is on both facilities
		perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);

		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertTrue(allowedFacilitiesOld.containsAll(allowedFacilitiesNew));
		Assert.assertTrue(allowedFacilitiesNew.size() == 2);

		// disable 2nd member => user shouldn't be allowed anywhere
		perun.getMembersManagerBl().setStatus(sess, member2, Status.DISABLED);

		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		Assert.assertTrue(allowedFacilitiesNew.isEmpty() && allowedFacilitiesOld.isEmpty());

		// enable 1st member => should be only on first facility
		perun.getMembersManagerBl().setStatus(sess, member, Status.VALID);
		allowedFacilitiesOld = getFacilitiesOldStyle();
		allowedFacilitiesNew = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Assert.assertTrue(allowedFacilitiesOld.size() == 1);
		Assert.assertTrue(allowedFacilitiesNew.size() == 1);
		Assert.assertTrue(allowedFacilitiesNew.contains(facility));
		Assert.assertTrue(!allowedFacilitiesNew.contains(facility2));

	}

	@Test
	public void getAllowedFacilitiesForMemberTest() throws Exception {
		System.out.println("FacilitiesManagerBlImpl.getAllowedFacilitiesForMemberTest");

		List<Facility> allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member);
		Assert.assertTrue(allowedFacilities.contains(facility));
		Assert.assertTrue(allowedFacilities.size() == 1);
		Assert.assertTrue(!allowedFacilities.contains(facility2));

		// second member is on both facilities
		List<Facility> allowedFacilities2 = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member2);
		Assert.assertTrue(allowedFacilities2.contains(facility));
		Assert.assertTrue(allowedFacilities2.contains(facility2));
		Assert.assertTrue(allowedFacilities2.size() == 2);

		// If member is not valid, we shouldn have any allowed facilities from it
		perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);

		allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member);
		Assert.assertTrue(allowedFacilities.isEmpty());

		// If member2 is not valid, we shouldn have any allowed facilities from it
		perun.getMembersManagerBl().setStatus(sess, member2, Status.DISABLED);

		allowedFacilities = perun.getFacilitiesManagerBl().getAllowedFacilities(sess, member2);
		Assert.assertTrue(allowedFacilities.isEmpty());

	}

	/**
	 * Returns allowed facilities using old implementation by iteration
	 * @return
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
