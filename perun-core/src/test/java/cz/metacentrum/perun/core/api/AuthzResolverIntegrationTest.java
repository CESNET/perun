package cz.metacentrum.perun.core.api;

import com.google.common.collect.Sets;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests of AuthzResolver
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class AuthzResolverIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "AuthzResolver.";
	final ExtSource extSource = new ExtSource(0, "AuthzResolverExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
	private int userLoginSequence = 0;

	@Test
	public void unauthorizedPerunAdmin() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedPerunAdmin");
		assertFalse(AuthzResolver.authorizedInternal(new PerunSessionImpl(
			perun,
			new PerunPrincipal("pepa", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
			new PerunClient()
		), "default_policy",
			Collections.emptyList()));
	}

	@Test
	public void authorizedPerunAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedPerunAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, null, Role.PERUNADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		assertTrue(AuthzResolver.authorizedInternal(session, "default_policy", Collections.emptyList()));
	}

	@Test
	public void authorizedVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedVoAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_authorized_vo_admin",  Arrays.asList(createdVo)));
	}

	@Test
	public void unauthorizedVoAdminCycleAdmin() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedVoAdminCycleAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_cycle_voadmin",  Arrays.asList(createdVo)));
	}

	@Test
	public void authorizedGroupOrVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedGroupOrVoAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_authorized_group_admin",  Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void authorizedGroupOrVoAdmin2() throws Exception {
		System.out.println(CLASS_NAME + "authorizedGroupOrVoAdmin2");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_authorized_group_admin",  Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void unauthorizedGroupOrVoAdmin3() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedGroupOrVoAdmin3");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Vo createdVo2 = perun.getVosManager().createVo(sess, new Vo(1,"test123444444","test123444444"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo2, Role.VOADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_authorized_group_admin", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void authorizedGroupAndVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedGroupAndVoAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);
		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_groupadmin_voadmin", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void unauthorizedGroupAndVoAdmin2() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedGroupAndVoAdmin2");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_groupadmin_voadmin", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void unauthorizedGroupAndVoAdmin3() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedGroupAndVoAdmin3");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_groupadmin_voadmin", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void authorizedResourceAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedResourceAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdResource, Role.RESOURCEADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_resource_admin", Arrays.asList(createdResource)));

	}

	@Test
	public void authorizedTransitive() throws Exception {
		System.out.println(CLASS_NAME + "authorizedTransitive");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdResource, Role.RESOURCEADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_transitive_one", Arrays.asList(createdResource)));

	}

	@Test
	public void authorizedResourceAdminAndFacilityAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedResourceAdminAndFacilityAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdResource, Role.RESOURCEADMIN);
		AuthzResolver.setRole(sess, createdUser, createdFacility, Role.FACILITYADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_resource_and_facility_admin", Arrays.asList(createdResource, createdFacility)));

	}

	@Test
	public void unauthorizedResourceAdminAndFacilityAdmin2() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedResourceAdminAndFacilityAdmin2");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdResource, Role.RESOURCEADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_resource_and_facility_admin", Arrays.asList(createdResource, createdFacility)));

	}

	@Test
	public void unauthorizedResourceAdminAndFacilityAdmin3() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedResourceAdminAndFacilityAdmin3");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdFacility, Role.FACILITYADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_resource_and_facility_admin", Arrays.asList(createdResource, createdFacility)));

	}

	@Test
	public void unauthorizedResourceAdminAndFacilityAdmin4() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedResourceAdminAndFacilityAdmin4");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		Facility createdFacility = setUpFacility();
		Resource createdResource = setUpResource(createdVo, createdFacility);
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_resource_and_facility_admin", Arrays.asList(createdResource, createdFacility)));

	}

	@Test
	public void authorizedGroupAdminOrVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedGroupAdminOrVoAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_group_or_vo", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void authorizedGroupAdminOrVoAdmin2() throws Exception {
		System.out.println(CLASS_NAME + "authorizedGroupAdminOrVoAdmin2");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_group_or_vo", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void unauthorizedGroupAdminOrVoAdmin3() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedGroupAdminOrVoAdmin3");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_group_or_vo", Arrays.asList(createdVo, createdGroup)));
	}

	@Test
	public void authorizedSecurityTeamAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedSecurityTeamAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		SecurityTeam team = new SecurityTeam();
		team.setName("a");
		SecurityTeam createdTeam = perun.getSecurityTeamsManager().createSecurityTeam(sess, team);
		perun.getSecurityTeamsManager().addAdmin(sess, createdTeam, createdUser);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_security_admin", Arrays.asList(createdTeam)));

	}

	@Test
	public void unauthorizedEmptyList() throws Exception {
		System.out.println(CLASS_NAME + "unauthorizedEmptyList");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		SecurityTeam team = new SecurityTeam();
		team.setName("a");
		SecurityTeam createdTeam = perun.getSecurityTeamsManager().createSecurityTeam(sess, team);
		perun.getSecurityTeamsManager().addAdmin(sess, createdTeam, createdUser);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertFalse(AuthzResolver.authorizedInternal(session, "test_security_admin", Arrays.asList()));

	}

	@Test
	public void authorizedVoobserverAndTopgroupcreator() throws Exception {
		System.out.println(CLASS_NAME + "authorizedVoobserverAndTopgroupcreator");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.setRole(sess, createdUser, createdVo, Role.TOPGROUPCREATOR);
		AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOOBSERVER);


		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_voobserver_and_topgroupcreator", Arrays.asList(createdVo)));
	}

	@Test
	public void authorizedCabinetAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedCabinetAdmin");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.setRole(sess, createdUser, null, Role.CABINETADMIN);


		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_cabinet", Arrays.asList()));
	}

	@Test
	public void authorizedSelf() throws Exception {
		System.out.println(CLASS_NAME + "authorizedSelf");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);

		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_self", Arrays.asList(createdUser)));
	}


	@Test
	public void authorizedSponsor() throws Exception {
		System.out.println(CLASS_NAME + "authorizedSponsor");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);

		AuthzResolver.setRole(sess, createdUser, createdVo, Role.SPONSOR);

		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_sponsor", Arrays.asList(createdVo,createdMember)));
	}

	@Test
	public void authorizedResourceselfservice() throws Exception {
		System.out.println(CLASS_NAME + "authorizedResourceselfservice");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPADMIN);

		Facility facility = setUpFacility();
		Resource resource = setUpResource(createdVo, facility);

		perun.getResourcesManager().addResourceSelfServiceGroup(sess, resource, createdGroup);

		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_resourceselfservice", Arrays.asList(resource, createdGroup)));
	}

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
	public void setRoleResourceSelfServiceForUser() throws Exception {
		System.out.println(CLASS_NAME + "setRoleResourceSelfServiceForUser");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		final Resource resource = setUpResource(createdVo, setUpFacility());

		AuthzResolver.setRole(sess, createdUser, resource, Role.RESOURCESELFSERVICE);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		assertTrue(AuthzResolver.isAuthorized(session, Role.RESOURCESELFSERVICE, resource));
	}

	@Test
	public void unsetRoleResourceSelfServiceForUser() throws Exception {
		System.out.println(CLASS_NAME + "unsetRoleResourceSelfServiceForUser");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		final Resource resource = setUpResource(createdVo, setUpFacility());

		AuthzResolver.setRole(sess, createdUser, resource, Role.RESOURCESELFSERVICE);

		PerunSession userSession = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(userSession);

		AuthzResolver.unsetRole(sess, createdUser, resource, Role.RESOURCESELFSERVICE);
		AuthzResolver.refreshAuthz(userSession);

		assertFalse(AuthzResolver.isAuthorized(userSession, Role.RESOURCESELFSERVICE, resource));
	}

	@Test
	public void setRoleResourceSelfServiceForGroup() throws Exception {
		System.out.println(CLASS_NAME + "setRoleResourceSelfServiceForUser");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final Resource resource = setUpResource(createdVo, setUpFacility());
		final Group group = setUpGroup(createdVo, createdMember);

		AuthzResolver.setRole(sess, group, resource, Role.RESOURCESELFSERVICE);

		PerunSession userSession = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(userSession);

		assertTrue(AuthzResolver.isAuthorized(userSession, Role.RESOURCESELFSERVICE, resource));
	}

	@Test
	public void unsetRoleResourceSelfServiceForGroup() throws Exception {
		System.out.println(CLASS_NAME + "unsetRoleResourceSelfServiceForGroup");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final Resource resource = setUpResource(createdVo, setUpFacility());
		final Group group = setUpGroup(createdVo, createdMember);

		AuthzResolver.setRole(sess, group, resource, Role.RESOURCESELFSERVICE);

		PerunSession userSession = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(userSession);

		AuthzResolver.unsetRole(sess, group, resource, Role.RESOURCESELFSERVICE);
		AuthzResolver.refreshAuthz(userSession);

		assertFalse(AuthzResolver.isAuthorized(userSession, Role.RESOURCESELFSERVICE, resource));
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
	public void isGroupAdmin() {
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

		HashMap<String, Set<Integer>> mapWithRights = new HashMap<>();
		Set<Integer> listWithIds = new HashSet<>();
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

		assertTrue(roleNames.contains(Role.PERUNADMIN));
	}

	@Test
	public void isAuthorizedForAttributePublicReadVoFromUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedForAttributePublicReadVoFromUserAttribute");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Vo otherVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo2","testvo2"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Member attributeMember = createSomeMember(otherVo);
		final User attributeUser = perun.getUsersManagerBl().getUserByMember(sess, attributeMember);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testUserAttr");
		attrDef.setDisplayName("test user attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(attrDef.getId(), Role.SELF, Arrays.asList(ActionType.READ, ActionType.READ_PUBLIC)));
		perun.getAttributesManagerBl().setAttributeRights(sess, rights);

		Attribute userAttribute = new Attribute(attrDef, 2);
		perun.getAttributesManagerBl().setAttribute(sess, attributeUser, userAttribute);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.SELF, sessionUser));
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());


		assertTrue(AuthzResolver.isAuthorizedForAttribute(testSession, ActionType.READ, attrDef, attributeUser));
	}

	@Test
	public void isAuthorizedForAttributeValidSelfReadVoFromUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedForAttributeValidSelfReadVoFromUserAttribute");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Member attributeMember = createSomeMember(createdVo);
		final User attributeUser = perun.getUsersManagerBl().getUserByMember(sess, attributeMember);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testUserAttr");
		attrDef.setDisplayName("test user attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(attrDef.getId(), Role.SELF, Arrays.asList(ActionType.READ, ActionType.READ_VO)));
		perun.getAttributesManagerBl().setAttributeRights(sess, rights);

		Attribute userAttribute = new Attribute(attrDef, 2);
		perun.getAttributesManagerBl().setAttribute(sess, attributeUser, userAttribute);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.SELF, sessionUser));
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());


		assertTrue(AuthzResolver.isAuthorizedForAttribute(testSession, ActionType.READ, attrDef, attributeUser));
	}

	@Test
	public void isAuthorizedForAttributeInvalidSelfReadVoFromUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedForAttributeInvalidSelfReadVoFromUserAttribute");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Vo otherVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo2","testvo2"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Member attributeMember = createSomeMember(otherVo);
		final User attributeUser = perun.getUsersManagerBl().getUserByMember(sess, attributeMember);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testUserAttr");
		attrDef.setDisplayName("test user attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(attrDef.getId(), Role.SELF, Arrays.asList(ActionType.READ, ActionType.READ_VO)));
		perun.getAttributesManagerBl().setAttributeRights(sess, rights);

		Attribute userAttribute = new Attribute(attrDef, 2);
		perun.getAttributesManagerBl().setAttribute(sess, attributeUser, userAttribute);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.SELF, sessionUser));
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());


		assertFalse(AuthzResolver.isAuthorizedForAttribute(testSession, ActionType.READ, attrDef, attributeUser));
	}

	@Test
	public void hasOneOfTheRolesForObjectSucceeds() throws Exception {
		System.out.println(CLASS_NAME + "hasOneOfTheRolesForObjectSucceeds");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, testVo));

		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		assertTrue(AuthzResolver.hasOneOfTheRolesForObject(
			testSession, testGroup, Sets.newHashSet(Role.PERUNADMIN, Role.VOADMIN)));
	}

	@Test
	public void hasOneOfTheRolesForObjectFails() throws Exception {
		System.out.println(CLASS_NAME + "hasOneOfTheRolesForObjectFails");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles());

		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		assertFalse(AuthzResolver.hasOneOfTheRolesForObject(
			testSession, testGroup, Sets.newHashSet(Role.PERUNADMIN, Role.VOADMIN)));
	}

	@Test
	public void setRoleGroupAdminSucceedsForVoAdmin() throws Exception {
		System.out.println(CLASS_NAME + "setRoleGroupAdminSucceedsForVoAdmin");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, testVo));

		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(testSession, testUser, testGroup, Role.GROUPADMIN);
	}

	@Test
	public void setRoleGroupAdminFailsWithoutSufficientRole() throws Exception {
		System.out.println(CLASS_NAME + "setRoleGroupAdminFailsWithoutSufficientRole");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Vo otherVo = perun.getVosManager().createVo(sess, new Vo(1,"testvo2","testvo2"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, otherVo));

		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		assertThatExceptionOfType(PrivilegeException.class).isThrownBy(
			() -> AuthzResolver.setRole(testSession, testUser, testGroup, Role.GROUPADMIN));
	}

	@Test
	public void roleExistsForExistingRole() {
		assertTrue(AuthzResolver.roleExists("PERUNADMIN"));
	}

	@Test
	public void roleExistsForNotExistingRole() {
		assertFalse(AuthzResolver.roleExists("RANDOMROLE"));
	}

	// private methods ==============================================================

	private Facility setUpFacility() throws Exception {

		Facility facility = new Facility();
		facility.setName("ResourcesManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
		/*
			 Owner owner = new Owner();
			 owner.setName("ResourcesManagerTestOwner");
			 owner.setContact("testingOwner");
			 perun.getOwnersManager().createOwner(sess, owner);
			 perun.getFacilitiesManager().addOwner(sess, facility, owner);
			 */
		return facility;

	}

	private Resource setUpResource(Vo vo, Facility facility) throws Exception {

		Resource resource = new Resource();
		resource.setName("ResourcesManagerTestResource");
		resource.setDescription("Testovaci");
		resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
		return resource;

	}

	private Group setUpGroup(Vo vo, Member member) throws Exception {

		Group group = new Group("Test group", "test group");
		group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

		perun.getGroupsManagerBl().addMember(sess, group, member);

		return group;
	}

	private Candidate setUpCandidate(String login) {

		String userFirstName = "FirstTest";
		String userLastName = "LastTest";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, login);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private Member createSomeMember(final Vo createdVo) throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException {
		final Candidate candidate = setUpCandidate("Login" + userLoginSequence++);
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
