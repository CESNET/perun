package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.impl.AuthzRoles;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.*;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.HashSet;
import java.util.Set;

/**
 * Integration tests of AuthzResolver
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class AuthzResolverIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "AuthzResolver.";
	final ExtSource extSource = new ExtSource(0, "AuthzResolverExtSource", ExtSourcesManager.EXTSOURCE_LDAP);

	@Test
	public void isAuthorizedInvalidPrincipal() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedInvalidPrincipal");

		assertTrue(!
				AuthzResolver.isAuthorized(new PerunSessionImpl(
					perun,
					new PerunPrincipal("pepa", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
					new PerunClient()
				), Role.PERUNADMIN));
	}

	@Test
	public void setRoleVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "setRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession sess1 = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(sess1);

		assertTrue(AuthzResolver.isAuthorized(sess1, Role.VOADMIN,createdVo));
	}

	@Test
	public void setRoleVoObserver() throws Exception {
		System.out.println(CLASS_NAME + "setRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOOBSERVER);

		PerunSession sess1 = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(sess1);

		assertTrue(AuthzResolver.isAuthorized(sess1, Role.VOOBSERVER,createdVo));
	}

	@Test
	public void unsetRoleVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "unsetRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession sess1 = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(sess1);

		assertTrue(AuthzResolver.isAuthorized(sess1, Role.VOADMIN,createdVo));

		AuthzResolver.unsetRole(sess, createdUser, createdVo, Role.VOADMIN);
		AuthzResolver.refreshAuthz(sess1);

		assertTrue(!AuthzResolver.isAuthorized(sess1, Role.VOADMIN,createdVo));
	}

	@Test (expected = UserNotAdminException.class)
	public void unsetRoleWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "unsetRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.unsetRole(sess, createdUser, createdVo, Role.VOADMIN);
	}

	@Test (expected = UserNotAdminException.class)
	public void setUnsuportedRole() throws Exception {
		System.out.println(CLASS_NAME + "setRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.unsetRole(sess, createdUser, createdVo, Role.VOADMIN);
	}

	@Test
	public void isVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "isVoAdmin");

		assertTrue(! AuthzResolver.isVoAdmin(sess));

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"sdf","sdfh"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession sess1 = getHisSession(createdMember);

		assertTrue(! AuthzResolver.isVoAdmin(sess1));

		perun.getVosManager().addAdmin(sess, createdVo, createdUser);
		AuthzResolver.refreshAuthz(sess1);

		assertTrue(AuthzResolver.isVoAdmin(sess1));
	}

	@Test
	public void isGroupAdmin() throws Exception {
		System.out.println(CLASS_NAME + "isGroupAdmin");

		sess = mock(PerunSession.class, RETURNS_DEEP_STUBS);
		when(sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN)).thenReturn(true);

		assertTrue(AuthzResolver.isGroupAdmin(sess));
	}

	@Test
	public void isFacilityAdmin() {
		System.out.println(CLASS_NAME + "isFacilityAdmin");

		sess = mock(PerunSession.class, RETURNS_DEEP_STUBS);
		when(sess.getPerunPrincipal().getRoles().hasRole(Role.FACILITYADMIN)).thenReturn(true);

		assertTrue(AuthzResolver.isFacilityAdmin(sess));
	}

//	@Test
//	public void isResourceAdmin() {
//		System.out.println(CLASS_NAME + "isResourceAdmin");
//
//		sess = mock(PerunSession.class, RETURNS_DEEP_STUBS);
//		when(sess.getPerunPrincipal().getRoles().hasRole(Role.RESOURCEADMIN)).thenReturn(true);
//
//		assertTrue(AuthzResolver.isResourceAdmin(sess));
//	}

	@Test
	public void isVoAdminUnit() {
		System.out.println(CLASS_NAME + "isVoAdminUnit");

		sess = mock(PerunSession.class, RETURNS_DEEP_STUBS);
		when(sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN)).thenReturn(true);

		assertTrue(AuthzResolver.isVoAdmin(sess));
	}

	@Test
	public void isPerunAdmin() {
		System.out.println(CLASS_NAME + "isPerunAdmin");

		sess = mock(PerunSession.class, RETURNS_DEEP_STUBS);
		when(sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)).thenReturn(true);

		assertTrue(AuthzResolver.isPerunAdmin(sess));
	}

	@Test
	public void isAuthorized() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorized");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"sdf","sdfh"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession sess1 = getHisSession(createdMember);
		perun.getVosManager().addAdmin(sess, createdVo, createdUser);

		AuthzResolver.refreshAuthz(sess1);

		assertTrue(AuthzResolver.isAuthorized(sess1, Role.VOADMIN, createdVo));
	}

	@Test
	public void addAllSubgroupsToAuthzRoles() throws Exception {
		System.out.println(CLASS_NAME + "addAllSubgroupsToAuthzRoles");

		Vo testVo = new Vo(1000, "AuthzResolver-testVo", "AuthzResolver-testVo");
		testVo = perun.getVosManagerBl().createVo(sess, testVo);

		Group testGroupA = new Group("AuthzResolver-testGroupA", "testGroupA");
		Group testGroupB = new Group("AuthzResolver-testGroupB", "testGroupB");
		Group testGroupC = new Group("AuthzResolver-testGroupC", "testGroupC");
		testGroupA = perun.getGroupsManagerBl().createGroup(sess, testVo, testGroupA);
		testGroupB = perun.getGroupsManagerBl().createGroup(sess, testGroupA, testGroupB);
		testGroupC = perun.getGroupsManagerBl().createGroup(sess, testGroupB, testGroupC);

		HashMap<String, Set<Integer>> mapWithRights = new HashMap<String, Set<Integer>>();
		Set<Integer> listWithIds = new HashSet<Integer>();
		listWithIds.add(testGroupA.getId());
		mapWithRights.put("Vo", listWithIds);
		mapWithRights.put("Group", listWithIds);

		AuthzRoles authzRoles = new AuthzRoles(Role.GROUPADMIN, mapWithRights);
		authzRoles = AuthzResolverBlImpl.addAllSubgroupsToAuthzRoles(sess, authzRoles);

		assertTrue(authzRoles.hasRole(Role.GROUPADMIN));
		assertTrue(!authzRoles.hasRole(Role.VOADMIN));
		assertTrue(authzRoles.get(Role.GROUPADMIN).containsKey("Group"));
		assertTrue(authzRoles.get(Role.GROUPADMIN).containsKey("Vo"));
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Group").contains(testGroupA.getId()));
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Group").contains(testGroupB.getId()));
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Group").contains(testGroupC.getId()));
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Group").size() == 3);
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Vo").contains(testGroupA.getId()));
		assertTrue(authzRoles.get(Role.GROUPADMIN).get("Vo").size() == 1);
	}

	@Test
	public void isAuthorizedInOtherVo() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedInOtherVo");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"som3Vo","VoSom3Nam3"));
		final Member createdMemberKouril = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMemberKouril);
		PerunSession sessKouril = getHisSession(createdMemberKouril);

		perun.getVosManager().addAdmin(sess, createdVo, createdUser);

		AuthzResolver.refreshAuthz(sessKouril);

		assertTrue("User is not authorized in own VO", AuthzResolver.isAuthorized(sessKouril, Role.VOADMIN, createdVo));
		final Vo otherVo = perun.getVosManager().createVo(sess, new Vo(0,"otherVo","bliblaVo"));
		assertTrue("User is authorized in foreign VO", !AuthzResolver.isAuthorized(sessKouril, Role.VOADMIN, otherVo));
	}

	@Test
	public void isAuthorizedWrongRole() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedWrongRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"sdf","sdfh"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession sess1 = getHisSession(createdMember);
		perun.getVosManager().addAdmin(sess, createdVo, createdUser);

		AuthzResolver.refreshAuthz(sess1);

		assertTrue( ! AuthzResolver.isAuthorized(sess1, Role.FACILITYADMIN, createdVo));
		assertTrue( ! AuthzResolver.isAuthorized(sess1, Role.GROUPADMIN, createdVo));
		assertTrue( ! AuthzResolver.isAuthorized(sess1, Role.SELF, createdVo));
		assertTrue( ! AuthzResolver.isAuthorized(sess1, Role.PERUNADMIN, createdVo));
	}

	@Test
	public void getPrincipalRoleNames() throws Exception {
		System.out.println(CLASS_NAME + "getPrincipalRoleNames");

		// Principal perunTests is PERUNADMIN
		PerunPrincipal pp =  new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		PerunSession ps = new PerunSessionImpl(perun, pp, new PerunClient());

		List<String> roleNames = cz.metacentrum.perun.core.api.AuthzResolver.getPrincipalRoleNames(ps);

		assertTrue(roleNames.contains(Role.PERUNADMIN.getRoleName()));
	}

	// private methods ==============================================================

	private Candidate setUpCandidate() {

		String userFirstName = "FirstTest";
		String userLastName = "LastTest";
		String extLogin = "ExtLoginTest";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Member createSomeMember(final Vo createdVo) throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException {
		final Candidate candidate = setUpCandidate();
		final Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
		return createdMember;
	}

	private PerunSession getHisSession(final Member createdMember) throws InternalErrorException {

		List<UserExtSource> ues = perun.getUsersManagerBl().getUserExtSources(sess, perun.getUsersManagerBl().getUserByMember(sess, createdMember));
		if (ues.size() == 0) {
			throw new InternalErrorException("Empty userExtSource list");
		}
		UserExtSource ue = new UserExtSource();
		for (UserExtSource u : ues) {
			if (u.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_LDAP)) {
				ue = u;
				break;
			}
		}

		PerunPrincipal pp1 = new PerunPrincipal(ue.getLogin(), ue.getExtSource().getName(), ue.getExtSource().getType());
		PerunSession sess1 = perun.getPerunSession(pp1, new PerunClient());

		return sess1;
	}

}
