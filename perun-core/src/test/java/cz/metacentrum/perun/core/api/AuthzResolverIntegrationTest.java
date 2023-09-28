package cz.metacentrum.perun.core.api;

import com.google.common.collect.Sets;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MfaPrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.MfaRolePrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.AuthzResolverImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.metacentrum.perun.core.api.AuthzResolver.MFA_CRITICAL_ATTR;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.Assert.assertEquals;
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
		), "default_policy"));
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

		assertTrue(AuthzResolver.authorizedInternal(session, "default_policy"));
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
		assertFalse(AuthzResolver.authorizedInternal(session, "test_security_admin"));

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
	public void authorizedAuditAdmin() throws Exception {
		System.out.println(CLASS_NAME + "authorizedAuditAdmin");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.setRole(sess, createdUser, null, Role.AUDITCONSUMERADMIN);


		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_audit", Arrays.asList()));
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
	public void isAuthorizedAuditConsumerAdmin() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedAuditConsumerAdmin");

		PerunPrincipal pp1 = new PerunPrincipal("test", "test", "test");
		pp1.setRoles(new AuthzRoles(Role.AUDITCONSUMERADMIN));
		pp1.setAuthzInitialized(true);

		PerunSession sess1 = perun.getPerunSession(pp1, new PerunClient());

		assertTrue(AuthzResolver.authorizedInternal(sess1, "createAuditerConsumer_String_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_int_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_int_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "getMessagesPage_MessagesPageQuery_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "setLastProcessedId_String_int_policy"));
	}

	@Test
	public void isAuthorizedPerunObserver() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedPerunObserver");

		PerunPrincipal pp1 = new PerunPrincipal("test", "test", "test");
		pp1.setRoles(new AuthzRoles(Role.PERUNOBSERVER));
		pp1.setAuthzInitialized(true);

		PerunSession sess1 = perun.getPerunSession(pp1, new PerunClient());

		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_int_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_int_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_policy"));
		assertTrue(AuthzResolver.authorizedInternal(sess1, "getMessagesPage_MessagesPageQuery_policy"));
	}

	@Test
	public void isUnauthorizedPerunObserver() throws Exception {
		System.out.println(CLASS_NAME + "isUnauthorizedPerunObserver");

		PerunPrincipal pp1 = new PerunPrincipal("test", "test", "test");
		pp1.setRoles(new AuthzRoles(Role.PERUNOBSERVER));
		pp1.setAuthzInitialized(true);

		PerunSession sess1 = perun.getPerunSession(pp1, new PerunClient());

		assertFalse(AuthzResolver.authorizedInternal(sess1, "setLastProcessedId_String_int_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "createAuditerConsumer_String_policy"));
	}

	@Test
	public void isUnauthorizedOther() throws Exception {
		System.out.println(CLASS_NAME + "isUnauthorizedOther");

		PerunPrincipal pp1 = new PerunPrincipal("test", "test", "test");
		pp1.setRoles(new AuthzRoles(Role.GROUPADMIN));
		pp1.setAuthzInitialized(true);

		PerunSession sess1 = perun.getPerunSession(pp1, new PerunClient());

		assertFalse(AuthzResolver.authorizedInternal(sess1, "createAuditerConsumer_String_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_int_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "pollConsumerEvents_String_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_int_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "pollConsumerMessages_String_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "getMessagesPage_MessagesPageQuery_policy"));
		assertFalse(AuthzResolver.authorizedInternal(sess1, "setLastProcessedId_String_int_policy"));
	}

	@Test
	public void groupMembershipIsAuthorized() throws Exception {
		System.out.println(CLASS_NAME + "groupMembershipIsAuthorized");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final Group createdGroup = setUpGroup(createdVo, createdMember);

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, createdGroup, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.MEMBERSHIP, RoleObject.Group, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.READ, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		assertTrue(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.READ, attrDef, createdGroup, false));
		assertFalse(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.WRITE, attrDef, createdGroup, false));
	}

	@Test
	public void facilityMembershipIsAuthorized() throws Exception {
		System.out.println(CLASS_NAME + "facilityMembershipIsAuthorized");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final Group createdGroup = setUpGroup(createdVo, createdMember);
		final Facility createdFacility = setUpFacility();
		final Resource createdResource = setUpResource(createdVo, createdFacility);

		AttributeDefinition attrDef = setUpFacilityAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, createdFacility, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.MEMBERSHIP, RoleObject.Facility, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.READ, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		// group is not assigned to resource yet
		assertFalse(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.READ, attrDef, createdFacility, false));

		perun.getResourcesManagerBl().assignGroupToResource(sess, createdGroup, createdResource, false, false, false);
		AuthzResolver.refreshAuthz(session);

		assertTrue(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.READ, attrDef, createdFacility, false));
		assertFalse(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.WRITE, attrDef, createdFacility, false));
	}

	@Test
	public void voMembershipIsAuthorized() throws Exception {
		System.out.println(CLASS_NAME + "voMembershipIsAuthorized");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final Group createdGroup = perun.getGroupsManagerBl().createGroup(sess, createdVo, new Group("Test group", "test group"));

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, createdGroup, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.MEMBERSHIP, RoleObject.Vo, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.READ, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		assertTrue(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.READ, attrDef, createdGroup, false));
		assertFalse(AuthzResolver.isAuthorizedForAttribute(session, AttributeAction.WRITE, attrDef, createdGroup, false));
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
		authzRoles.put(Role.GROUPOBSERVER, mapWithRights);
		authzRoles = AuthzResolverBlImpl.addAllSubgroupsToAuthzRoles(sess, authzRoles, Role.GROUPADMIN);
		authzRoles = AuthzResolverBlImpl.addAllSubgroupsToAuthzRoles(sess, authzRoles, Role.GROUPOBSERVER);

		assertTrue(authzRoles.hasRole(Role.GROUPADMIN));
		assertTrue(!authzRoles.hasRole(Role.VOADMIN));
		assertTrue(authzRoles.hasRole(Role.GROUPOBSERVER));
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
	@Deprecated
	@Ignore
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
	@Deprecated
	@Ignore
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
	@Deprecated
	@Ignore
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
	public void isAuthorizedForAttributeAssociatedReadRole() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedForAttributeAssociatedReadRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Group group = setUpGroup(createdVo, sessionMember);

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, group, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.GROUPADMIN, RoleObject.Group, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.READ, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.GROUPOBSERVER, group));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		assertTrue(AuthzResolver.isAuthorizedForAttribute(testSession, AttributeAction.READ, attrDef, group, false));
	}

	@Test
	public void isNotAuthorizedForAttributeAssociatedReadRoleWriteAction() throws Exception {
		System.out.println(CLASS_NAME + "isNotAuthorizedForAttributeAssociatedReadRoleWriteAction");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Group group = setUpGroup(createdVo, sessionMember);

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, group, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.GROUPADMIN, RoleObject.Group, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.WRITE, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.GROUPADMIN, group));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		assertTrue(AuthzResolver.isAuthorizedForAttribute(testSession, AttributeAction.WRITE, attrDef, group, false));

		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.GROUPOBSERVER, group));
		assertFalse(AuthzResolver.isAuthorizedForAttribute(testSession, AttributeAction.WRITE, attrDef, group, false));
	}

	@Test
	public void isAuthorizedByDefault() throws Exception {
		System.out.println(CLASS_NAME + "isAuthorizedByDefault");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member sessionMember = createSomeMember(createdVo);
		final User sessionUser = perun.getUsersManagerBl().getUserByMember(sess, sessionMember);
		final Group group = setUpGroup(createdVo, sessionMember);

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		perun.getAttributesManagerBl().setAttribute(sess, group, new Attribute(attrDef));

		List<AttributePolicy> policies = List.of(new AttributePolicy(1, Role.GROUPADMIN, RoleObject.Group, 1));
		List<AttributePolicyCollection> policyCollections = List.of(new AttributePolicyCollection(1, attrDef.getId(), AttributeAction.READ, new ArrayList<>(policies)));

		perun.getAttributesManager().setAttributePolicyCollections(sess, policyCollections);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.PERUNOBSERVER));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getUser()).thenReturn(sessionUser);
		when(mockedPerunPrincipal.getUserId()).thenReturn(sessionUser.getId());

		PerunSessionImpl testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		assertFalse(AuthzResolver.isAuthorizedForAttribute(testSession, AttributeAction.WRITE, attrDef, group, false));
		assertTrue(AuthzResolver.isAuthorizedForAttribute(testSession, AttributeAction.READ, attrDef, group, false));
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
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getActor()).thenReturn("test");

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
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());

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


	@Test
	public void getVoAdminGroupsWithProperRights() throws Exception {
		System.out.println(CLASS_NAME + "getVoAdminGroupsWithProperRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, testVo));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getActor()).thenReturn("test");
		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(testSession, testGroup, testVo, Role.VOADMIN);
		List<Group> adminGroups = AuthzResolver.getAdminGroups(testSession, testVo, Role.VOADMIN);

		assertEquals(1, adminGroups.size());
		assertEquals(testGroup, adminGroups.get(0));
	}

	@Test (expected = PrivilegeException.class)
	public void getVoAdminGroupsWithInsufficientRights() throws Exception {
		System.out.println(CLASS_NAME + "getVoAdminGroupsWithInsufficientRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles());
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(sess, testGroup, testVo, Role.VOADMIN);
		AuthzResolver.getAdminGroups(testSession, testVo, Role.VOADMIN);
	}

	@Test
	public void getVoDirectRichAdminsWithProperRights() throws Exception {
		System.out.println(CLASS_NAME + "getVoDirectRichAdminsWithProperRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, testVo));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getActor()).thenReturn("test");
		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(testSession, testUser, testVo, Role.VOADMIN);
		List<RichUser> richAdmins = AuthzResolver.getRichAdmins(testSession, testVo, new ArrayList<>(), Role.VOADMIN, true, true);

		assertEquals(1, richAdmins.size());
		assertEquals(testUser, richAdmins.get(0));
	}

	@Test (expected = PrivilegeException.class)
	public void getVoDirectRichAdminsWithInsufficientRights() throws Exception {
		System.out.println(CLASS_NAME + "getVoDirectRichAdminsWithInsufficientRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles());
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(sess, testUser, testVo, Role.VOADMIN);
		AuthzResolver.getRichAdmins(testSession, testVo, new ArrayList<>(), Role.VOADMIN, true, true);
	}

	@Test
	public void getVoRichAdminsWithProperRights() throws Exception {
		System.out.println(CLASS_NAME + "getVoAdminGroupsWithProperRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		final Member testMember2 = createSomeMember(testVo);
		final User testUser2 = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		perun.getGroupsManager().addMember(sess, testGroup, testMember2);

		PerunPrincipal mockedPerunPrincipal = mock(PerunPrincipal.class, RETURNS_DEEP_STUBS);
		when(mockedPerunPrincipal.isAuthzInitialized()).thenReturn(true);
		when(mockedPerunPrincipal.getRoles()).thenReturn(new AuthzRoles(Role.VOADMIN, testVo));
		when(mockedPerunPrincipal.getRolesUpdatedAt()).thenReturn(System.currentTimeMillis());
		when(mockedPerunPrincipal.getActor()).thenReturn("test");
		PerunSession testSession = new PerunSessionImpl(sess.getPerun(), mockedPerunPrincipal, sess.getPerunClient());

		AuthzResolver.setRole(testSession, testUser, testVo, Role.VOADMIN);
		AuthzResolver.setRole(testSession, testGroup, testVo, Role.VOADMIN);
		List<RichUser> richAdmins = AuthzResolver.getRichAdmins(testSession, testVo, new ArrayList<>(), Role.VOADMIN, false, true);

		assertEquals(2, richAdmins.size());
		assertTrue(richAdmins.containsAll(Arrays.asList(testUser, testUser2)));
	}

	@Test (expected = RoleCannotBeManagedException.class)
	public void getVoRichAdminsWithWrongObjects() throws Exception {
		System.out.println(CLASS_NAME + "getVoAdminGroupsWithProperRights");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);

		AuthzResolver.setRole(sess, testUser, testVo, Role.VOADMIN);
		AuthzResolver.getRichAdmins(sess, testUser, new ArrayList<>(), Role.VOADMIN, false, true);
	}

	@Test
	public void getVosWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Vo testVo2 = perun.getVosManager().createVo(sess, new Vo(1,"testvo2","testvo2"));
		final Vo testVo3 = perun.getVosManager().createVo(sess, new Vo(2,"testvo3","testvo3"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testVo, Role.VOADMIN);
		AuthzResolver.setRole(sess, testUser, testVo2, Role.SPONSOR);
		AuthzResolver.setRole(sess, testGroup, testVo3, Role.VOOBSERVER);
		List<Vo> result = AuthzResolver.getVosWhereUserIsInRoles(sess, testUser, Arrays.asList(Role.VOADMIN, Role.VOOBSERVER));

		assertEquals(2, result.size());
		assertTrue(result.containsAll(Arrays.asList(testVo, testVo3)));
	}

	@Test
	public void getVosWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getVosWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Vo testVo2 = perun.getVosManager().createVo(sess, new Vo(1,"testvo2","testvo2"));
		final Vo testVo3 = perun.getVosManager().createVo(sess, new Vo(2,"testvo3","testvo3"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testVo, Role.VOADMIN);
		AuthzResolver.setRole(sess, testUser, testVo2, Role.SPONSOR);
		AuthzResolver.setRole(sess, testGroup, testVo3, Role.VOOBSERVER);
		List<Vo> result = AuthzResolver.getVosWhereUserIsInRoles(sess, null, Arrays.asList(Role.VOADMIN, Role.VOOBSERVER));

		assertEquals(2, result.size());
		assertTrue(result.containsAll(Arrays.asList(testVo, testVo3)));
	}

	@Test
	public void getFacilitiesWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Facility testFacility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0,"testfacility1","testfacility1"));
		final Facility testFacility2 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(1,"testfacility2","testfacility2"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testFacility, Role.FACILITYADMIN);
		AuthzResolver.setRole(sess, testGroup, testFacility2, Role.FACILITYOBSERVER);
		List<Facility> result = AuthzResolver.getFacilitiesWhereUserIsInRoles(sess, testUser, Collections.singletonList(Role.FACILITYADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testFacility));
	}

	@Test
	public void getFacilitiesWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0, "testvo1", "testvo1"));
		final Facility testFacility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "testfacility1", "testfacility1"));
		final Facility testFacility2 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(1, "testfacility2", "testfacility2"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testFacility, Role.FACILITYADMIN);
		AuthzResolver.setRole(sess, testGroup, testFacility2, Role.FACILITYOBSERVER);
		List<Facility> result = AuthzResolver.getFacilitiesWhereUserIsInRoles(sess, null, Collections.singletonList(Role.FACILITYADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testFacility));
	}

	@Test
	public void getResourcesWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getResourcesWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Facility testFacility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0,"testfacility1","testfacility1"));
		final Resource testResource = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource", "testResource", 0, 0), testVo, testFacility);
		final Resource testResource2 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource2", "testResource2", 0, 0), testVo, testFacility);
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testResource, Role.RESOURCEADMIN);
		AuthzResolver.setRole(sess, testGroup, testResource2, Role.RESOURCEOBSERVER);
		List<Resource> result = AuthzResolver.getResourcesWhereUserIsInRoles(sess, testUser, Collections.singletonList(Role.RESOURCEADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testResource));
	}

	@Test
	public void getResourcesWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getResourcesWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Facility testFacility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0,"testfacility1","testfacility1"));
		final Resource testResource = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource", "testResource", 0, 0), testVo, testFacility);
		final Resource testResource2 = perun.getResourcesManagerBl().createResource(sess, new Resource(0, "testResource2", "testResource2", 0, 0), testVo, testFacility);
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testResource, Role.RESOURCEADMIN);
		AuthzResolver.setRole(sess, testGroup, testResource2, Role.RESOURCEOBSERVER);
		List<Resource> result = AuthzResolver.getResourcesWhereUserIsInRoles(sess, null, Collections.singletonList(Role.RESOURCEADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testResource));
	}

	@Test
	public void getGroupsWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Group testGroup2 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup2", "testg2"));
		final Group testGroup4 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup4", "testg4"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testGroup2, Role.GROUPADMIN);
		AuthzResolver.setRole(sess, testGroup, testGroup4, Role.GROUPOBSERVER);
		List<Group> result = AuthzResolver.getGroupsWhereUserIsInRoles(sess, testUser, Collections.singletonList(Role.GROUPADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testGroup2));
	}

	@Test
	public void groupMatchesUserRolesFilter() throws Exception {
		System.out.println(CLASS_NAME + "groupMatchesUserRolesFilter");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Group testGroup2 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup2", "testg2"));
		final Group testGroup4 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup4", "testg4"));

		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testGroup2, Role.GROUPADMIN);
		AuthzResolver.setRole(sess, testGroup, testGroup4, Role.GROUPOBSERVER);

		sess.getPerunPrincipal().setUser(testUser);

		List<Group> directRoles = perun.getGroupsManager().getAllGroups(sess, testVo);
		directRoles.removeIf(group -> !AuthzResolverBlImpl.groupMatchesUserRolesFilter(sess, testUser, group, List.of(Role.GROUPADMIN), List.of(RoleAssignmentType.DIRECT)));
		assertEquals(1, directRoles.size());
		assertTrue(directRoles.contains(testGroup2));

		List<Group> indirectRoles = perun.getGroupsManager().getAllGroups(sess, testVo);
		indirectRoles.removeIf(group -> !AuthzResolverBlImpl.groupMatchesUserRolesFilter(sess, testUser, group, List.of(Role.GROUPOBSERVER), List.of(RoleAssignmentType.INDIRECT)));
		assertEquals(1, indirectRoles.size());
		assertTrue(indirectRoles.contains(testGroup4));

		List<Group> allIndirectGroups = perun.getGroupsManager().getAllGroups(sess, testVo);
		allIndirectGroups.removeIf(group -> !AuthzResolverBlImpl.groupMatchesUserRolesFilter(sess, testUser, group, new ArrayList<>(), List.of(RoleAssignmentType.INDIRECT)));
		assertEquals(1, allIndirectGroups.size());
		assertTrue(allIndirectGroups.contains(testGroup4));

		List<Group> allGroupAdmins = perun.getGroupsManager().getAllGroups(sess, testVo);
		allGroupAdmins.removeIf(group -> !AuthzResolverBlImpl.groupMatchesUserRolesFilter(sess, testUser, group, List.of(Role.GROUPADMIN), new ArrayList<>()));
		assertEquals(1, allGroupAdmins.size());
		assertTrue(allGroupAdmins.contains(testGroup2));
	}

	@Test
	public void getGroupsWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Group testGroup2 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup2", "testg2"));
		final Group testGroup3 = perun.getGroupsManager().createGroup(sess, testGroup2, new Group("testGroup3", "testg3"));
		final Group testGroup4 = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup4", "testg4"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testUser, testGroup2, Role.GROUPADMIN);
		AuthzResolver.setRole(sess, testGroup, testGroup4, Role.GROUPOBSERVER);
		List<Group> result = AuthzResolver.getGroupsWhereUserIsInRoles(sess, null, Collections.singletonList(Role.GROUPADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testGroup2));
	}

	@Test
	public void getMembersWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getMembersWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member testMember = createSomeMember(testVo);
		final Member testMember2 = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);

		AuthzResolver.setRole(sess, testUser, testVo, Role.SPONSOR);
		perun.getMembersManagerBl().setSponsorshipForMember(sess, testMember2, testUser);
		List<Member> result = AuthzResolver.getMembersWhereUserIsInRoles(sess, testUser, Collections.singletonList(Role.SPONSORSHIP));

		assertEquals(1, result.size());
		assertTrue(result.contains(testMember2));
	}

	@Test
	public void getMembersWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getMembersWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final Member testMember = createSomeMember(testVo);
		final Member testMember2 = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);

		AuthzResolver.setRole(sess, testUser, testVo, Role.SPONSOR);
		perun.getMembersManagerBl().setSponsorshipForMember(sess, testMember2, testUser);
		List<Member> result = AuthzResolver.getMembersWhereUserIsInRoles(sess, null, Collections.singletonList(Role.SPONSORSHIP));

		assertEquals(1, result.size());
		assertTrue(result.contains(testMember2));
	}

	@Test
	public void getSecurityTeamsWhereUserIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getSecurityTeamsWhereUserIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final SecurityTeam testSecurityTeam = perun.getSecurityTeamsManagerBl().createSecurityTeam(sess, new SecurityTeam(0, "testSecurityTeam", "testSecurityTeam"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		sess.getPerunPrincipal().setUser(testUser);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testGroup, testSecurityTeam, Role.SECURITYADMIN);
		List<SecurityTeam> result = AuthzResolver.getSecurityTeamsWhereUserIsInRoles(sess, null, Collections.singletonList(Role.SECURITYADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testSecurityTeam));
	}

	@Test
	public void getSecurityTeamsWherePrincipalIsInRoles() throws Exception {
		System.out.println(CLASS_NAME + "getSecurityTeamsWherePrincipalIsInRoles");

		final Vo testVo = perun.getVosManager().createVo(sess, new Vo(0,"testvo1","testvo1"));
		final SecurityTeam testSecurityTeam = perun.getSecurityTeamsManagerBl().createSecurityTeam(sess, new SecurityTeam(0, "testSecurityTeam", "testSecurityTeam"));
		final Group testGroup = perun.getGroupsManager().createGroup(sess, testVo, new Group("testGroup", "testg"));
		final Member testMember = createSomeMember(testVo);
		final User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
		perun.getGroupsManager().addMember(sess, testGroup, testMember);

		AuthzResolver.setRole(sess, testGroup, testSecurityTeam, Role.SECURITYADMIN);
		List<SecurityTeam> result = AuthzResolver.getSecurityTeamsWhereUserIsInRoles(sess, testUser, Collections.singletonList(Role.SECURITYADMIN));

		assertEquals(1, result.size());
		assertTrue(result.contains(testSecurityTeam));
	}

	@Test
	public void isAnyAttributeMfaCritical() throws Exception {
		System.out.println(CLASS_NAME + "isAnyAttributeMfaCritical");

		Vo vo = perun.getVosManager().createVo(sess, new Vo(0, "testvo1", "testvo1"));
		Facility facility = setUpFacility();
		Resource resource = setUpResource(vo, facility);

		assertFalse(AuthzResolverBlImpl.isAnyObjectMfaCritical(sess, List.of(vo, facility, resource)));

		AttributeDefinition attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
		Attribute attr = new Attribute(attrDef, true);
		perun.getAttributesManagerBl().setAttribute(sess, facility, attr);

		assertTrue(AuthzResolverBlImpl.isAnyObjectMfaCritical(sess, List.of(resource, vo, facility)));
	}

	@Test
	public void mfaGenericRule() throws Exception {
		System.out.println(CLASS_NAME + "mfaGenericRule");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		boolean originalForce = BeansUtils.getCoreConfig().isEnforceMfa();
		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			assertThatExceptionOfType(MfaPrivilegeException.class).isThrownBy(
				() -> AuthzResolver.authorizedInternal(session, "test_mfa_generic", List.of())
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalForce);
		}
	}

	@Test
	public void mfaSpecificObjectRule() throws Exception {
		System.out.println(CLASS_NAME + "mfaSpecificObjectRule");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		// mark createdVo as critical
		Attribute criticalObject = perun.getAttributesManagerBl().getAttribute(sess, createdVo,
			AttributesManager.NS_VO_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
		criticalObject.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, createdVo, criticalObject);

		final Member createdMember = createSomeMember(createdVo);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		boolean originalForce = BeansUtils.getCoreConfig().isEnforceMfa();
		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			assertThatExceptionOfType(MfaPrivilegeException.class).isThrownBy(
				() -> AuthzResolver.authorizedInternal(session, "test_mfa_specific", List.of(createdVo))
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalForce);
		}
	}

	@Test
	public void mfaNotCriticalObject() throws Exception {
		System.out.println(CLASS_NAME + "mfaNotCriticalObject");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);

		boolean originalForce = BeansUtils.getCoreConfig().isEnforceMfa();
		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			assertThatNoException().isThrownBy(
				() -> AuthzResolver.authorizedInternal(session, "test_mfa_specific", List.of(createdVo))
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalForce);
		}
	}

	@Test
	public void mfaGenericSetRole() throws Exception {
		System.out.println(CLASS_NAME + "mfaGenericSetRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		boolean originalMfaForce = BeansUtils.getCoreConfig().isEnforceMfa();
		List<Map<String, String>> originalAssignmentCheck = AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).getAssignmentCheck();
		List<String> originalAdmins = new ArrayList<>(BeansUtils.getCoreConfig().getAdmins());
		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			BeansUtils.getCoreConfig().getAdmins().remove("perunTests");
			AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).setAssignmentCheck(List.of(Map.ofEntries(
				entry("MFA", "")
			)));
			// setting PERUNADMIN requires MFA globally
			assertThatExceptionOfType(MfaPrivilegeException.class).isThrownBy(
				() -> AuthzResolver.setRole(sess, createdUser, null, Role.PERUNADMIN)
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalMfaForce);
			AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).setAssignmentCheck(originalAssignmentCheck);
			BeansUtils.getCoreConfig().setAdmins(originalAdmins);
		}
	}

	@Test
	public void mfaSpecificSetRole() throws Exception {
		System.out.println(CLASS_NAME + "mfaSpecificSetRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		AttributeDefinition attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
		Attribute attr = new Attribute(attrDef, true);

		boolean originalMfaForce = BeansUtils.getCoreConfig().isEnforceMfa();
		List<Map<String, String>> originalAssignmentCheck = AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).getAssignmentCheck();
		List<String> originalAdmins = new ArrayList<>(BeansUtils.getCoreConfig().getAdmins());

		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			BeansUtils.getCoreConfig().getAdmins().remove("perunTests");
			AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).setAssignmentCheck(List.of(Map.ofEntries(
				entry("MFA", "Vo")
			)));
			// setting VOADMIN requires MFA if VO is critical object
			assertThatNoException().isThrownBy(
				() -> AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN)
			);
			assertThatNoException().isThrownBy(
				() -> AuthzResolver.unsetRole(sess, createdUser, createdVo, Role.VOADMIN)
			);

			perun.getAttributesManagerBl().setAttribute(sess, createdVo, attr);
			assertThatExceptionOfType(MfaPrivilegeException.class).isThrownBy(
				() -> AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN)
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalMfaForce);
			AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).setAssignmentCheck(originalAssignmentCheck);
			BeansUtils.getCoreConfig().setAdmins(originalAdmins);
		}
	}

	@Test
	public void mfaNotCriticalSetRole() throws Exception {
		System.out.println(CLASS_NAME + "mfaNotCriticalSetRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		AttributeDefinition attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
		Attribute attr = new Attribute(attrDef, true);

		boolean originalMfaForce = BeansUtils.getCoreConfig().isEnforceMfa();
		List<Map<String, String>> originalAssignmentCheck = AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).getAssignmentCheck();
		List<String> originalAdmins = new ArrayList<>(BeansUtils.getCoreConfig().getAdmins());

		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			perun.getAttributesManagerBl().setAttribute(sess, createdVo, attr);
			BeansUtils.getCoreConfig().getAdmins().remove("perunTests");

			// setting VOADMIN does not require MFA even if VO is critical
			AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).setAssignmentCheck(new ArrayList<>());
			assertThatNoException().isThrownBy(
				() -> AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN)
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalMfaForce);
			AuthzResolverImpl.getRoleManagementRules(Role.VOADMIN).setAssignmentCheck(originalAssignmentCheck);
			BeansUtils.getCoreConfig().setAdmins(originalAdmins);
		}
	}

	@Test
	public void mfaCriticalAssigningEntitySetRole() throws Exception {
		System.out.println(CLASS_NAME + "mfaCriticalAssigningEntitySetRole");

		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		AttributeDefinition attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
		Attribute attr = new Attribute(attrDef, true);

		boolean originalMfaForce = BeansUtils.getCoreConfig().isEnforceMfa();
		List<String> originalAdmins = new ArrayList<>(BeansUtils.getCoreConfig().getAdmins());

		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			perun.getAttributesManagerBl().setAttribute(sess, createdUser, attr);
			BeansUtils.getCoreConfig().getAdmins().remove("perunTests");
			// setting new role for critical user
			assertThatExceptionOfType(MfaPrivilegeException.class).isThrownBy(
				() -> AuthzResolver.setRole(sess, createdUser, createdVo, Role.VOADMIN)
			);
		} finally {
			BeansUtils.getCoreConfig().setEnforceMfa(originalMfaForce);
			BeansUtils.getCoreConfig().setAdmins(originalAdmins);
		}
	}

	@Test
	public void mfaCriticalRole() throws Exception {
		System.out.println(CLASS_NAME + "mfaCriticalRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "test123test123", "test123test123"));
		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		PerunSession session = getHisSession(createdMember);

		boolean originalForce = BeansUtils.getCoreConfig().isEnforceMfa();
		boolean originalCriticalRole = AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).isMfaCriticalRole();
		AuthzResolver.setRole(sess, createdUser, null, Role.PERUNADMIN);
		int originalMfaAuthTimeout = BeansUtils.getCoreConfig().getMfaAuthTimeout();
		int originalMfaAuthTimeoutPercentageForceLogIn = BeansUtils.getCoreConfig().getMfaAuthTimeoutPercentageForceLogIn();
		String originalAdditionalInfoAuthTime = session.getPerunPrincipal().getAdditionalInformations().get("authTime");

		try {
			BeansUtils.getCoreConfig().setEnforceMfa(true);
			AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).setMfaCriticalRole(true);
			BeansUtils.getCoreConfig().setMfaAuthTimeout(60);
			BeansUtils.getCoreConfig().setMfaAuthTimeoutPercentageForceLogIn(75);
			// mock auth time for this test
			session.getPerunPrincipal().getAdditionalInformations().put("authTime", Instant.now().minus(20, ChronoUnit.SECONDS).toString());
			assertThatExceptionOfType(MfaRolePrivilegeException.class).isThrownBy(
				() -> AuthzResolver.refreshAuthz(session)
			);
			AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).setMfaCriticalRole(false);
			assertThatNoException().isThrownBy(
				() -> AuthzResolver.refreshAuthz(session)
			);
		} finally {
			AuthzResolverImpl.getRoleManagementRules(Role.PERUNADMIN).setMfaCriticalRole(originalCriticalRole);
			BeansUtils.getCoreConfig().setEnforceMfa(originalForce);
			BeansUtils.getCoreConfig().setMfaAuthTimeout(originalMfaAuthTimeout);
			BeansUtils.getCoreConfig().setMfaAuthTimeoutPercentageForceLogIn(originalMfaAuthTimeoutPercentageForceLogIn);
			session.getPerunPrincipal().getAdditionalInformations().put("authTime", originalAdditionalInfoAuthTime);
		}
	}

	@Test
	public void testGroupMembershipManager() throws Exception {
		System.out.println(CLASS_NAME + "testGroupMembershipManager");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0,"test123test123","test123test123"));

		final Member createdMember = createSomeMember(createdVo);
		Group createdGroup = setUpGroup(createdVo, createdMember);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, createdGroup, Role.GROUPMEMBERSHIPMANAGER);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_groupmembershipmanager", Arrays.asList(createdGroup)));
	}

	@Test
	public void testSpregApplication() throws Exception {
		System.out.println(CLASS_NAME + "testSpregApplication");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "SpregApplicationTestVo", "SpregApplicationTestVo"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, null, Role.SPREGAPPLICATION);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_spregapplication", Arrays.asList(createdVo)));
	}

	@Test
	public void testPasswordResetManager() throws Exception {
		System.out.println(CLASS_NAME + "testPasswordResetManager");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "PasswordResetManagerTestVo", "PasswordResetManagerTestVo"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, null, Role.PASSWORDRESETMANAGER);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_passwordresetmanager", Arrays.asList(createdVo)));
	}

	@Test
	public void testProxyRole() throws Exception {
		System.out.println(CLASS_NAME + "testProxyRole");
		final Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "ProxyRoleTestVo", "ProxyRoleTestVo"));

		final Member createdMember = createSomeMember(createdVo);
		final User createdUser = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		AuthzResolver.setRole(sess, createdUser, null, Role.PROXY);

		PerunSession session = getHisSession(createdMember);
		AuthzResolver.refreshAuthz(session);
		assertTrue(AuthzResolver.authorizedInternal(session, "test_proxy_role", Arrays.asList(createdVo)));
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

	private AttributeDefinition setUpGroupAttributeDefinition() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testGroupAttr");
		attrDef.setDisplayName("test group attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		return attrDef;
	}

	private AttributeDefinition setUpFacilityAttributeDefinition() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testFacilityAttribute");
		attrDef.setDisplayName("test facility attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		return attrDef;
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

	private Member createSomeMember(final Vo createdVo) throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		final Candidate candidate = setUpCandidate("Login" + userLoginSequence++);
		final Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
		return createdMember;
	}

	private PerunSession getHisSession(final Member createdMember) {

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
