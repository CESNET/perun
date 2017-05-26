package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

/**
 * Integration tests of VosManager.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class VosManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private VosManager vosManagerEntry;
	private Vo myVo;
	private ExtSource es;

	private final int someNumber = 55;
	private final String voShortName = "TestShortN-" + someNumber;
	private final String voName = "Test Vo Name " + someNumber;
	private final static String extSourceName = "VosManagerEntryIntegrationTest";
	private final static String CLASS_NAME = "VosManager.";

	@Before
	public void setUp() throws Exception {
		vosManagerEntry = perun.getVosManager();
		myVo = new Vo(0, voName, voShortName);
		ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
		es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);

	}

	@Test
	public void createVo() throws Exception {
		System.out.println(CLASS_NAME + "createVo");

		final Vo newVo = vosManagerEntry.createVo(sess, myVo);

		assertTrue("id must be greater than zero", newVo.getId() > 0);
	}

	@Test
	public void createAndUpdateVoWithLongShortName() throws Exception {
		System.out.println(CLASS_NAME + "createAndUpdateVoWithLongShortName");
		String longName = "1234567890123456789";
		String longerName = "12345678901234567890123456789012";
		Vo voWithLongShortname = new Vo(0, longName, longName);

		Vo newVo = vosManagerEntry.createVo(sess, voWithLongShortname);
		assertTrue("id must be greater than zero", newVo.getId() > 0);

		newVo.setShortName(longerName);
		newVo = vosManagerEntry.updateVo(sess, newVo);
		assertTrue("newVo shortName has 32 characters length", newVo.getShortName().length() == 32);
	}

	@Test(expected = VoExistsException.class)
	public void createVoWhichAlreadyExists() throws Exception {
		System.out.println(CLASS_NAME + "createVoWhichAlreadyExists");

		vosManagerEntry.createVo(sess, myVo);
		// this should throw exception
		vosManagerEntry.createVo(sess, myVo);
	}

	@Test
	public void getVoById() throws Exception {
		System.out.println(CLASS_NAME + "getVoById");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
		final Vo returnedVo = vosManagerEntry.getVoById(sess, myVo.getId());

		final String createVoFailMsg = "The created vo is not ok, maybe try to check createVo()?";

		assertNotNull(createVoFailMsg, createdVo);
		assertNotNull("returned vo should not be null", returnedVo);

		assertEquals(createdVo.getId(), returnedVo.getId());
		assertEquals("name is not the same", createdVo.getName(),
				returnedVo.getName());
		assertEquals("shortName is not the same", createdVo.getShortName(),
				returnedVo.getShortName());
	}

	@Test
	public void getVoByShortName() throws Exception {
		System.out.println(CLASS_NAME + "getVoByShortName");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
		final Vo returnedVo = vosManagerEntry.getVoByShortName(sess, voShortName);

		assertEquals(createdVo, returnedVo);
	}

	@Test(expected = VoNotExistsException.class)
	public void getVoWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoWhichNotExists");

		final String nonExistingShortName = "_i_am_not_in_db_";
		vosManagerEntry.getVoByShortName(sess, nonExistingShortName);
	}

	@Test
	public void getVos() throws Exception {
		System.out.println(CLASS_NAME + "getVos");

		final Vo vo = vosManagerEntry.createVo(sess, myVo);
		final List<Vo> vos = vosManagerEntry.getVos(sess);

		assertTrue(vos.contains(vo));
	}

	@Test
	public void getVosNotNull() throws Exception {
		System.out.println(CLASS_NAME + "getVosNotNull");

		// should not never return null or throw exception, even if no result
		// found
		assertNotNull(vosManagerEntry.getVos(sess));
	}

	@Test
	public void updateVo() throws Exception {
		System.out.println(CLASS_NAME + "updateVo");

		Vo voToUpdate = vosManagerEntry.createVo(sess, myVo);
		voToUpdate.setName("Cosa");
		voToUpdate.setShortName("Nostra");
		final Vo updatedVo = vosManagerEntry.updateVo(sess, voToUpdate);

		assertEquals(voToUpdate, updatedVo);
	}

	@Test(expected = VoNotExistsException.class)
	public void updateVoWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "updateVoWhichNotExists");

		vosManagerEntry.updateVo(sess, new Vo());
	}

	@Test
	@Ignore
	public void findCandidates() throws Exception {
		System.out.println(CLASS_NAME + "findCandidates");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		addExtSourceDelegate(createdVo);

		final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
				createdVo, "kouril");

		// TODO tohle se mi moc nelibi, kde se nejaci candidates vzali? To by
		// bylo dobre plnit na zacatku testu db nebo je vytvorit rucne zde
		assertTrue("Candidates count must be greater than 0",
				candidates.size() > 0);
		removeExtSourceDelegate(createdVo);
	}

	@Test(expected = VoNotExistsException.class)
	@Ignore
	public void findCandidatesForNonExistingVo() throws Exception {
		System.out.println(CLASS_NAME + "findCandidatesForNonExistingVo");

		addExtSourceDelegate(new Vo());

		vosManagerEntry.findCandidates(sess, new Vo(), "kouril");
	}

	@Test
	@Ignore
	public void findCandidatesWithOneResult() throws Exception {
		System.out.println(CLASS_NAME + "findCandidatesWithOneResult");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		addExtSourceDelegate(createdVo);

		final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
				createdVo, "kouril", 1);

		assertEquals(1, candidates.size());
		removeExtSourceDelegate(createdVo);
	}


	@Test
	public void addAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addAdmin");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		final Member member = createMemberFromExtSource(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		vosManagerEntry.addAdmin(sess, createdVo, user);
		final List<User> admins = vosManagerEntry.getAdmins(sess, createdVo);

		assertNotNull(admins);
		assertTrue(admins.size() > 0);
	}

	@Test(expected=VoNotExistsException.class)
	public void addAdminIntoNonExistingVo() throws Exception {
		System.out.println(CLASS_NAME + "addAdminIntoNonExistingVo");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
		final Member member = createMemberFromExtSource(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		vosManagerEntry.addAdmin(sess, new Vo(), user);
	}

	@Test(expected=UserNotExistsException.class)
	public void addAdminAsNonExistingUser() throws Exception {
		System.out.println(CLASS_NAME + "addAdminAsNonExistingMember");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		vosManagerEntry.addAdmin(sess, createdVo, new User());
	}

	@Test
	public void addAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWithGroup");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		// set up authorized group
		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
		vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

		final List<Group> admins = vosManagerEntry.getAdminGroups(sess, createdVo);

		assertNotNull(admins);
		assertTrue(admins.size() > 0);
		assertTrue(admins.contains(authorizedGroup));
	}

	@Test
	public void getAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getAdmins");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		// set up first user
		final Member member = createMemberFromExtSource(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		vosManagerEntry.addAdmin(sess, createdVo, user);

		// set up authorized group
		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
		vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

		// set up second user
		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("Josef");
		candidate.setId(4);
		candidate.setMiddleName("");
		candidate.setLastName("Novak");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource",
				"cz.metacentrum.perun.core.impl.ExtSourceInternal"), Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());

		Member member2 = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
		User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
		perun.getGroupsManager().addMember(sess, returnedGroup, member2);

		// test
		List<User> admins = vosManagerEntry.getAdmins(sess, createdVo);
		assertTrue("should have 2 admins",admins.size() == 2);
		assertTrue("our member as direct user should be admin",admins.contains(user));
		assertTrue("our member as member of admin group should be admin",admins.contains(user2));
	}

	@Test
	public void getDirectAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getDirectAdmins");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		final Member member = createMemberFromExtSource(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		vosManagerEntry.addAdmin(sess, createdVo, user);
		assertTrue(vosManagerEntry.getDirectAdmins(sess, createdVo).contains(user));
	}

	@Test
	public void getAdminGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAdminGroups");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		final Group group = new Group("testGroup", "just for testing");
		perun.getGroupsManager().createGroup(sess, createdVo, group);

		vosManagerEntry.addAdmin(sess, createdVo, group);
		assertTrue(vosManagerEntry.getAdminGroups(sess, createdVo).contains(group));
	}

	@Test(expected=UserNotExistsException.class)
	public void removeAdminWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminWhichNotExists");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		vosManagerEntry.removeAdmin(sess, createdVo, new User());
	}

	@Test
	public void removeAdmin() throws Exception {
		System.out.println(CLASS_NAME + "removeAdmin");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
		final Member member = createMemberFromExtSource(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		vosManagerEntry.addAdmin(sess, createdVo, user);
		assertTrue(vosManagerEntry.getAdmins(sess, createdVo).contains(user));

		vosManagerEntry.removeAdmin(sess, createdVo, user);
		assertFalse(vosManagerEntry.getAdmins(sess, createdVo).contains(user));
	}

	@Test
	public void removeAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminWithGroup");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		// set up authorized group
		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
		vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

		vosManagerEntry.removeAdmin(sess, createdVo, returnedGroup);
		assertFalse(vosManagerEntry.getAdminGroups(sess, createdVo).contains(returnedGroup));
	}

	@Test(expected = VoNotExistsException.class)
	public void deleteVo() throws Exception {
		System.out.println(CLASS_NAME + "deleteVo");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
		vosManagerEntry.deleteVo(sess, createdVo);
		vosManagerEntry.getVoById(sess, createdVo.getId());
	}

	@Test(expected = VoNotExistsException.class)
	public void deleteVoWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteVoWhichNotExists");

		vosManagerEntry.deleteVo(sess, new Vo());
	}

	// private methods ------------------------------------------------------------------

	private Member createMemberFromExtSource(final Vo createdVo) throws Exception {

		//This is obsolete approach which is dependent on extSource, remove these lines in future...
		//addExtSourceDelegate(createdVo);
		//final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
		//		createdVo, "kouril", 1);

		final Candidate candidate = prepareCandidate();

		final MembersManager membersManagerEntry = perun.getMembersManager();
		final Member member = perun.getMembersManagerBl().createMemberSync(sess,createdVo, candidate);//candidates.get(0));
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;
	}

	private Candidate prepareCandidate() {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		final Candidate candidate = new Candidate();//Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private void addExtSourceDelegate(final Vo createdVo) throws Exception {
		ExtSourcesManager esme = perun.getExtSourcesManager();
		esme.addExtSource(sess, createdVo, es);
	}

	private void removeExtSourceDelegate(Vo createdVo) throws Exception {
		ExtSourcesManager esme = perun.getExtSourcesManager();
		esme.removeExtSource(sess, createdVo, es);
	}

	@Test
	public void getVosCount() throws Exception {
		System.out.println(CLASS_NAME + "getVosCount");

		final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

		int count = vosManagerEntry.getVosCount(sess);
		assertTrue(count>0);
	}

}
