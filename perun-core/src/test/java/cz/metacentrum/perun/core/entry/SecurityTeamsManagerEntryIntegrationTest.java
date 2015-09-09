package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.SecurityTeamsManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */

public class SecurityTeamsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private SecurityTeamsManager securityTeamsManagerEntry;

	private SecurityTeam st0;
	private SecurityTeam st1;
	private SecurityTeam st2;
	private Facility f0;
	private Facility f1;
	private Facility f2;
	private User u0;
	private User u1;
	private User u2;
	private User u3;
	private User u4;

	@Before
	public void setUp() throws Exception {
		securityTeamsManagerEntry = perun.getSecurityTeamsManager();
	}


	@Test
	public void testGetSecurityTeamsPerunAdmin() throws Exception {
		AuthzRoles roles = sess.getPerunPrincipal().getRoles();
		try {
			List<SecurityTeam> expected = setUpSecurityTeams();
			sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.PERUNADMIN));

			List<SecurityTeam> actual = securityTeamsManagerEntry.getSecurityTeams(sess);
			assertEquals(expected, actual);
		} finally {
			sess.getPerunPrincipal().setRoles(roles);
		}
	}
	@Test
	public void testGetSecurityTeamsSecurityAdmin() throws Exception {
		AuthzRoles roles = sess.getPerunPrincipal().getRoles();
		try {
			setUpSecurityTeams();
			setUpUsers();

			List<SecurityTeam> expected = new ArrayList<>();
			expected.add(st0);
			sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.SECURITYADMIN, st0));

			List<SecurityTeam> actual = securityTeamsManagerEntry.getSecurityTeams(sess);

			assertEquals(expected, actual);
		} finally {
			sess.getPerunPrincipal().setRoles(roles);
		}
	}
	@Test
	public void testGetSecurityTeamsSecurityAdmin1() throws Exception {
		AuthzRoles roles = sess.getPerunPrincipal().getRoles();
		try {
			setUpSecurityTeams();
			setUpUsers();

			List<SecurityTeam> expected = new ArrayList<>();
			expected.add(st0);
			expected.add(st1);
			sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.SECURITYADMIN, expected));

			List<SecurityTeam> actual = securityTeamsManagerEntry.getSecurityTeams(sess);

			Collections.sort(expected);
			Collections.sort(actual);
			assertEquals(expected, actual);
		} finally {
			sess.getPerunPrincipal().setRoles(roles);
		}
	}
	@Test
	public void testGetSecurityTeamsEmpty() throws Exception {
		List<SecurityTeam> expected = new ArrayList<>();
		List<SecurityTeam> actual = securityTeamsManagerEntry.getSecurityTeams(sess);
		assertEquals(expected, actual);
	}


	@Test
	public void testGetAllSecurityTeams() throws Exception {
		List<SecurityTeam> expected = setUpSecurityTeams();
		List<SecurityTeam> actual = securityTeamsManagerEntry.getAllSecurityTeams(sess);
		assertEquals(expected, actual);
	}
	@Test
	public void testGetAllSecurityTeamsEmpty() throws Exception {
		List<SecurityTeam> expected = new ArrayList<>();
		List<SecurityTeam> actual = securityTeamsManagerEntry.getAllSecurityTeams(sess);
		assertEquals(expected, actual);
	}


	@Test
	public void testCreateSecurityTeam() throws Exception {
		SecurityTeam expected = new SecurityTeam("Name", "Desc");
		SecurityTeam actual = securityTeamsManagerEntry.createSecurityTeam(sess, expected);
		assertNotNull(actual);
		assertEquals(expected, actual);

		actual = securityTeamsManagerEntry.getSecurityTeamById(sess, actual.getId());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	@Test
	public void testCreateSecurityTeamWithoutDsc() throws Exception {
		SecurityTeam expected = new SecurityTeam("Name", null);
		SecurityTeam actual = securityTeamsManagerEntry.createSecurityTeam(sess, expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	@Test(expected = InternalErrorException.class)
	public void testCreateSecurityTeamWithoutName() throws Exception {
		SecurityTeam expected = new SecurityTeam(null, "Desc");
		securityTeamsManagerEntry.createSecurityTeam(sess, expected);
	}
	@Test(expected = SecurityTeamExistsException.class)
	public void testCreateSecurityTeamAlreadyExists() throws Exception {
		SecurityTeam expected = new SecurityTeam("Name", "Desc");
		securityTeamsManagerEntry.createSecurityTeam(sess, expected);
		SecurityTeam actual = securityTeamsManagerEntry.createSecurityTeam(sess, expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
		securityTeamsManagerEntry.createSecurityTeam(sess, actual);
	}
	@Test(expected = SecurityTeamExistsException.class)
	public void testCreateSecurityTeamUniqueName() throws Exception {
		securityTeamsManagerEntry.createSecurityTeam(sess, new SecurityTeam("UniqueName", "Desc 1"));
		securityTeamsManagerEntry.createSecurityTeam(sess, new SecurityTeam("UniqueName", "Desc 2"));
	}
	@Test(expected = InternalErrorException.class)
	public void testCreateSecurityTeamNameLength() throws Exception {
		securityTeamsManagerEntry.createSecurityTeam(sess, new SecurityTeam(
				"1---------2---------3---------4---------5---------6---------7---------8---------9---------10--------11--------12--------13--------",
				"Desc 1"));
	}


	@Test
	public void testUpdateSecurityTeam() throws Exception {
		List<SecurityTeam> teams = setUpSecurityTeams();
		SecurityTeam expected = teams.get(0);
		expected.setName("Updated");
		SecurityTeam middle = securityTeamsManagerEntry.updateSecurityTeam(sess, expected);

		assertNotNull(middle);
		assertEquals(expected, middle);

		SecurityTeam actual = securityTeamsManagerEntry.getSecurityTeamById(sess, expected.getId());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testUpdateSecurityTeamWithNoName() throws Exception {
		SecurityTeam expected = new SecurityTeam("ToUpdate", "Desc");
		securityTeamsManagerEntry.updateSecurityTeam(sess, expected);
	}
	@Test(expected = InternalErrorException.class)
	public void testUpdateSecurityTeamWithoutName() throws Exception {
		List<SecurityTeam> teams = setUpSecurityTeams();
		SecurityTeam expected = teams.get(0);
		expected.setName(null);
		securityTeamsManagerEntry.updateSecurityTeam(sess, expected);
	}


	@Test
	public void testDeleteSecurityTeam() throws Exception {
		List<SecurityTeam> expected = setUpSecurityTeams();
		expected.remove(st0);
		securityTeamsManagerEntry.deleteSecurityTeam(sess, st0);

		List<SecurityTeam> actual = securityTeamsManagerEntry.getAllSecurityTeams(sess);
		assertEquals(expected, actual);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testDeleteSecurityTeamShouldNotExists() throws Exception {
		setUpSecurityTeams();
		SecurityTeam actual = securityTeamsManagerEntry.getSecurityTeamById(sess, st0.getId());
		assertNotNull(actual);
		assertEquals(st0, actual);
		securityTeamsManagerEntry.deleteSecurityTeam(sess, st0);
		securityTeamsManagerEntry.getSecurityTeamById(sess, st0.getId());
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testDeleteSecurityTeamBeanNotExists() throws Exception {
		st0 = new SecurityTeam("Name", "Desc");
		st0.setId(10);
		securityTeamsManagerEntry.deleteSecurityTeam(sess, st0);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testDeleteSecurityTeamAlreadyDeleted() throws Exception {
		setUpSecurityTeams();
		securityTeamsManagerEntry.deleteSecurityTeam(sess, st0);
		securityTeamsManagerEntry.deleteSecurityTeam(sess, st0);
	}
	@Test(expected = InternalErrorException.class)
	public void testDeleteSecurityTeamWithNullSecurityTeam() throws Exception {
		securityTeamsManagerEntry.deleteSecurityTeam(sess, null);
	}


	@Test
	public void testGetSecurityTeamById() throws Exception {
		SecurityTeam expected = setUpSecurityTeams().get(0);
		SecurityTeam actual = securityTeamsManagerEntry.getSecurityTeamById(sess, expected.getId());
		assertEquals(expected, actual);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testGetSecurityTeamByIdNotExists() throws Exception {
		securityTeamsManagerEntry.getSecurityTeamById(sess, 10);
	}


	@Test
	public void testGetAdmins() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		List<User> expected = setUpAdmins(u0, u1, setUpGroup(u1, u2));
		List<User> actual = securityTeamsManagerEntry.getAdmins(sess, st0);
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}
	@Test
	public void testGetAdminsEmpty() throws Exception {
		setUpSecurityTeams();
		setUpUsers();

		List<User> expected = new ArrayList<>();
		List<User> actual = securityTeamsManagerEntry.getAdmins(sess, st0);

		assertEquals(expected, actual);
	}
	@Test(expected = InternalErrorException.class)
	public void testGetAdminsWithNullSecurityTeam() throws Exception {
		securityTeamsManagerEntry.getAdmins(sess, null);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testGetAdminsWithoutSecurityTeam() throws Exception {
		SecurityTeam st = new SecurityTeam(11, "Name", "Desc");
		securityTeamsManagerEntry.getAdmins(sess, st);
	}


	@Test
	public void testAddAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();

		List<User> admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (admins.size() > 0) fail();
		if (admins.contains(u0)) fail();

		securityTeamsManagerEntry.addAdmin(sess, st0, u0);

		admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (admins.size() != 1) fail();
		if (!admins.contains(u0)) fail();
	}
	@Test(expected = AlreadyAdminException.class)
	public void testAddAdminAlreadyAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpAdmins(u0, u1, setUpGroup(u1, u2));

		securityTeamsManagerEntry.addAdmin(sess, st0, u0);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testAddAdminSecurityTeamNotExists() throws Exception {
		setUpUsers();
		SecurityTeam st = new SecurityTeam(11, "Name", "Desc");
		securityTeamsManagerEntry.addAdmin(sess, st, u0);
	}
	@Test(expected = UserNotExistsException.class)
	public void testAddAdminUserNotExists() throws Exception {
		setUpSecurityTeams();
		User user = new User(11, "firstName", "lastName", "middleName", "titleBefore", "titleAfter");
		securityTeamsManagerEntry.addAdmin(sess, st0, user);
	}
	@Test(expected = InternalErrorException.class)
	public void testAddAdminNullSecurityTeam() throws Exception {
		setUpUsers();
		securityTeamsManagerEntry.addAdmin(sess, null, u0);
	}
	@Test(expected = InternalErrorException.class)
	public void testAddAdminNullUser() throws Exception {
		setUpSecurityTeams();
		securityTeamsManagerEntry.addAdmin(sess, st0, (User) null);
	}


	@Test
	public void testAddGroupAsAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		Group group = setUpGroup(u0, u1);

		List<User> admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (admins.size() > 0) fail();

		securityTeamsManagerEntry.addAdmin(sess, st0, group);

		admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (admins.size() != 2) fail();
		if (!admins.contains(u0)) fail();
		if (!admins.contains(u1)) fail();
		if (admins.contains(u2)) fail();
		if (admins.contains(u3)) fail();
		if (admins.contains(u4)) fail();
	}
	@Test(expected = AlreadyAdminException.class)
	public void testAddGroupAsAdminAlreadyAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		Group group = setUpGroup(u1, u2);
		setUpAdmins(u0, u1, group);

		securityTeamsManagerEntry.addAdmin(sess, st0, group);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testAddGroupAsAdminSecurityTeamNotExists() throws Exception {
		setUpUsers();
		Group group = setUpGroup(u0, u1);
		SecurityTeam st = new SecurityTeam(11, "Name", "Desc");
		securityTeamsManagerEntry.addAdmin(sess, st, group);
	}
	@Test(expected = GroupNotExistsException.class)
	public void testAddGroupAsAdminGroupNotExists() throws Exception {
		setUpSecurityTeams();
		Group group = new Group(11, "firstName", "lastName");
		securityTeamsManagerEntry.addAdmin(sess, st0, group);
	}
	@Test(expected = InternalErrorException.class)
	public void testAddGroupAsAdminNullSecurityTeam() throws Exception {
		setUpUsers();
		securityTeamsManagerEntry.addAdmin(sess, null, u0);
	}
	@Test(expected = InternalErrorException.class)
	public void testAddGroupAsAdminNullGroup() throws Exception {
		setUpSecurityTeams();
		securityTeamsManagerEntry.addAdmin(sess, st0, (Group) null);
	}


	@Test
	public void testRemoveAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpAdmins(u0, u1, setUpGroup(u1, u2));

		List<User> admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (!admins.contains(u0)) fail();

		securityTeamsManagerEntry.removeAdmin(sess, st0, u0);

		admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (admins.contains(u0)) fail();
	}
	@Test
	public void testRemoveAdminAlsoInGroup() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpAdmins(u0, u1, setUpGroup(u1, u2));

		List<User> admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (!admins.contains(u1)) fail();

		//Shouldnt remove from admins because he is also in group as admin
		securityTeamsManagerEntry.removeAdmin(sess, st0, u1);

		admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (!admins.contains(u1)) fail();
	}
	@Test(expected = UserNotAdminException.class)
	public void testRemoveAdminAlreadyRemoved() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpAdmins(u0, u1, setUpGroup(u1, u2));

		securityTeamsManagerEntry.removeAdmin(sess, st0, u3);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testRemoveAdminSecurityTeamNotExists() throws Exception {
		setUpUsers();
		SecurityTeam st = new SecurityTeam(11, "Name", "Desc");
		securityTeamsManagerEntry.removeAdmin(sess, st, u1);
	}
	@Test(expected = UserNotExistsException.class)
	public void testRemoveAdminUserNotExists() throws Exception {
		setUpSecurityTeams();
		User user = new User(11, "firstName", "lastName", "middleName", "titleBefore", "titleAfter");
		securityTeamsManagerEntry.removeAdmin(sess, st0, user);
	}


	@Test
	public void testRemoveGroupAsAdmin() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		Group group = setUpGroup(u1, u2);
		setUpAdmins(u0, u1, group);

		List<User> admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (!admins.contains(u1)) fail();
		if (!admins.contains(u2)) fail();

		securityTeamsManagerEntry.removeAdmin(sess, st0, group);

		admins = securityTeamsManagerEntry.getAdmins(sess, st0);
		if (!admins.contains(u1)) fail(); // Shouldnt be removed because is also admin as user
		if (admins.contains(u2)) fail();
	}
	@Test(expected = GroupNotAdminException.class)
	public void testRemoveGroupAsAdminAlreadyRemoved() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		Group group = setUpGroup(u1, u2);
		securityTeamsManagerEntry.removeAdmin(sess, st0, group);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testRemoveGroupAsAdminSecurityTeamNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		Group group = setUpGroup(u1, u2);
		setUpAdmins(u0, u1, group);
		SecurityTeam st = new SecurityTeam(11, "Name", "Desc");
		securityTeamsManagerEntry.removeAdmin(sess, st, group);
	}
	@Test(expected = GroupNotExistsException.class)
	public void testRemoveGroupAsAdminGroupNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpAdmins(u0, u1, setUpGroup(u1, u2));
		Group group = new Group(11, "Name", "name");
		securityTeamsManagerEntry.removeAdmin(sess, st0, group);
	}


	@Test
	public void testAddUserToBlacklist() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();

		List<User> expected = new ArrayList<>();
		expected.add(u0);
		securityTeamsManagerEntry.addUserToBlacklist(sess, st0, u0, "reason");

		List<User> actual = securityTeamsManagerEntry.getBlacklist(sess, st0);
		assertEquals(expected, actual);

		if (!perun.getSecurityTeamsManagerBl().isUserBlacklisted(sess, st0, u0)) {
			fail();
		}
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testAddUserToBlacklistSecurityTeamNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		SecurityTeam st = new SecurityTeam(11, "Security0", "Description test 0");
		securityTeamsManagerEntry.addUserToBlacklist(sess, st, u0,"reason");
	}
	@Test(expected = UserNotExistsException.class)
	public void testAddUserToBlacklistUserNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		User user = new User(11, "firstName", "lastName", "middleName", "titleBefore", "titleAfter");
		securityTeamsManagerEntry.addUserToBlacklist(sess, st0, user, null);
	}
	@Test(expected = UserAlreadyBlacklistedException.class)
	public void testAddUserToBlacklistAlreadyBlacklisted() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();
		securityTeamsManagerEntry.addUserToBlacklist(sess, st0, u1, null);
	}


	@Test
	public void testRemoveUserFromBlacklist() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();

		securityTeamsManagerEntry.removeUserFromBlacklist(sess, st0, u1);

		if (perun.getSecurityTeamsManagerBl().isUserBlacklisted(sess, st0, u1)) {
			fail();
		}
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testRemoveUserFromBlacklistSecurityTeamNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();
		SecurityTeam st = new SecurityTeam(11, "Security0", "Description test 0");
		securityTeamsManagerEntry.removeUserFromBlacklist(sess, st, u1);
	}
	@Test(expected = UserNotExistsException.class)
	public void testRemoveUserFromBlacklistUserNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();
		User user = new User(11, "firstName", "lastName", "middleName", "titleBefore", "titleAfter");
		securityTeamsManagerEntry.removeUserFromBlacklist(sess, st0, user);
	}
	@Test(expected = UserAlreadyRemovedException.class)
	public void testRemoveUserFromBlacklistUserNotInBlacklist() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();
		securityTeamsManagerEntry.removeUserFromBlacklist(sess, st0, u0);
	}


	@Test
	public void testGetBlacklistBySecurityTeam() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();

		List<User> expected = new ArrayList<>();
		expected.add(u1);
		expected.add(u2);

		List<User> actual = securityTeamsManagerEntry.getBlacklist(sess, st0);
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}
	@Test(expected = SecurityTeamNotExistsException.class)
	public void testGetBlacklistBySecurityTeamSecurityTeamNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();
		SecurityTeam st = new SecurityTeam(11, "Security0", "Description test 0");

		securityTeamsManagerEntry.getBlacklist(sess, st);
	}


	@Test
	public void testGetBlacklistByFacility() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();

		List<User> expected = new ArrayList<>();
		expected.add(u1);
		expected.add(u2);
		expected.add(u3);
		expected.add(u4);

		List<User> actual = securityTeamsManagerEntry.getBlacklist(sess, f0);

		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}
	@Test(expected = FacilityNotExistsException.class)
	public void testGetBlacklistByFacilityNotExists() throws Exception {
		setUpSecurityTeams();
		setUpUsers();
		setUpFacilities();
		setUpBlacklists();

		Facility facility = new Facility(11, "facility");

		securityTeamsManagerEntry.getBlacklist(sess, facility);
	}






	private List<SecurityTeam> setUpSecurityTeams() throws PrivilegeException, InternalErrorException, SecurityTeamExistsException {
		st0 = new SecurityTeam("Security0", "Description test 0");
		st1 = new SecurityTeam("Security1", "");
		st2 = new SecurityTeam("Security2", null);

		securityTeamsManagerEntry.createSecurityTeam(sess, st0);
		securityTeamsManagerEntry.createSecurityTeam(sess, st1);
		securityTeamsManagerEntry.createSecurityTeam(sess, st2);

		List<SecurityTeam> result = new ArrayList<>();
		result.add(st0);
		result.add(st1);
		result.add(st2);
		return result;
	}

	private List<Facility> setUpFacilities() throws PrivilegeException, FacilityExistsException, InternalErrorException, SecurityTeamAlreadyAssignedException, FacilityNotExistsException, SecurityTeamNotExistsException {
		f0 = new Facility();
		f1 = new Facility();
		f2 = new Facility();

		f0.setName("Facility 0");
		f1.setName("Facility 1");
		f2.setName("Facility 2");

		perun.getFacilitiesManager().createFacility(sess, f0);
		perun.getFacilitiesManager().createFacility(sess, f1);
		perun.getFacilitiesManager().createFacility(sess, f2);

		perun.getFacilitiesManager().assignSecurityTeam(sess, f0, st0);
		perun.getFacilitiesManager().assignSecurityTeam(sess, f0, st1);
		perun.getFacilitiesManager().assignSecurityTeam(sess, f1, st0);

		List<Facility> result = new ArrayList<>();
		result.add(f0);
		result.add(f1);
		result.add(f2);
		return result;
	}

	private void setUpUsers() throws PrivilegeException, InternalErrorException, UserAlreadyBlacklistedException, UserNotExistsException, SecurityTeamNotExistsException, AlreadyMemberException {
		u0 = new User();
		u1 = new User();
		u2 = new User();
		u3 = new User();
		u4 = new User();

		u0.setFirstName("User 0");
		u1.setFirstName("User 1");
		u2.setFirstName("User 2");
		u3.setFirstName("User 3");
		u4.setFirstName("User 4");

		perun.getUsersManager().createUser(sess, u0);
		perun.getUsersManager().createUser(sess, u1);
		perun.getUsersManager().createUser(sess, u2);
		perun.getUsersManager().createUser(sess, u3);
		perun.getUsersManager().createUser(sess, u4);
	}

	private void setUpBlacklists() throws PrivilegeException, InternalErrorException, UserAlreadyBlacklistedException, UserNotExistsException, SecurityTeamNotExistsException, AlreadyMemberException {
		securityTeamsManagerEntry.addUserToBlacklist(sess, st0, u1, "reason");
		securityTeamsManagerEntry.addUserToBlacklist(sess, st0, u2, null);
		securityTeamsManagerEntry.addUserToBlacklist(sess, st1, u2, "reason");
		securityTeamsManagerEntry.addUserToBlacklist(sess, st1, u3, null);
		securityTeamsManagerEntry.addUserToBlacklist(sess, st1, u4, "reason");
	}

	private Group setUpGroup(User u0, User u1) throws PrivilegeException, InternalErrorException, UserNotExistsException, AlreadyAdminException, SecurityTeamNotExistsException, VoExistsException, GroupExistsException, VoNotExistsException, GroupNotExistsException, AlreadyMemberException, MemberNotExistsException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, WrongAttributeValueException, ExtendMembershipException, NotMemberOfParentGroupException {
		Vo vo = new Vo();
		vo.setShortName("testVo");
		vo.setName("Test VO");
		perun.getVosManager().createVo(sess, vo);

		Group authGroup = new Group();
		authGroup.setShortName("testGroup");
		authGroup.setName("Test Group");
		Member m0 = perun.getMembersManager().createMember(sess, vo, u0);
		Member m1 = perun.getMembersManager().createMember(sess, vo, u1);
		perun.getGroupsManager().createGroup(sess, vo, authGroup);
		perun.getGroupsManager().addMember(sess, authGroup, m0);
		perun.getGroupsManager().addMember(sess, authGroup, m1);

		return authGroup;
	}

	private List<User> setUpAdmins(User u0, User u1, Group group) throws PrivilegeException, InternalErrorException, UserNotExistsException, AlreadyAdminException, SecurityTeamNotExistsException, VoExistsException, GroupExistsException, VoNotExistsException, GroupNotExistsException, AlreadyMemberException, MemberNotExistsException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, WrongAttributeValueException, ExtendMembershipException {
		securityTeamsManagerEntry.addAdmin(sess, st0, u0);
		securityTeamsManagerEntry.addAdmin(sess, st0, u1);

		securityTeamsManagerEntry.addAdmin(sess, st0, group);

		Set<User> set = new HashSet<>();
		set.add(u0);
		set.add(u1);
		for (Member member : perun.getGroupsManager().getGroupMembers(sess, group)) {
			set.add(perun.getUsersManager().getUserByMember(sess, member));
		}

		List<User> expected = new ArrayList<>(set);
		Collections.sort(expected);
		return expected;
	}
}
