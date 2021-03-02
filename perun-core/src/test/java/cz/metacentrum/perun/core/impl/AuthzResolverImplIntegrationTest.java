package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

public class AuthzResolverImplIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "AttributesManagerImplIntegrationTest.";
	private AuthzResolverImplApi authzResolverImpl;
	private static Vo createdVo;
	private static final User user1 = new User(1, "", "", "", "", "");
	private static final User user2 = new User(2, "", "", "", "", "");
	private static final User user3 = new User(3, "", "", "", "", "");
	private static final User user4 = new User(4, "", "", "", "", "");


	@Before
	public void setUp() throws Exception {
		authzResolverImpl = (AuthzResolverImplApi) ReflectionTestUtils.getField(
			AuthzResolverBlImpl.class, "authzResolverImpl"
		);

		createdVo = perun.getVosManagerBl().createVo(sess, new Vo(1, "", ""));
		perun.getUsersManagerBl().createUser(sess, user1);
		perun.getUsersManagerBl().createUser(sess, user2);
		perun.getUsersManagerBl().createUser(sess, user3);
		perun.getUsersManagerBl().createUser(sess, user4);
	}

	@Test
	public void setRole() throws Exception {
		System.out.println(CLASS_NAME + "setRole");

		Map<String, Integer> mapping = prepareMapping(user1);

		authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
		AuthzRoles userRoles = AuthzResolverBlImpl.getUserRoles(sess, user1);

		assertTrue(userRoles.hasRole(Role.VOADMIN, createdVo));
	}

	@Test
	public void unsetRole() throws Exception {
		System.out.println(CLASS_NAME + "unsetRole");

		Map<String, Integer> mapping = prepareMapping(user2);

		authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
		authzResolverImpl.unsetRole(sess, mapping, Role.VOADMIN);

		assertFalse(AuthzResolverBlImpl.getUserRoleNames(sess, user2).contains(Role.VOADMIN));
	}

	@Test(expected = RoleNotSetException.class)
	public void unsetRoleWhichIsNotSet() throws Exception {
		System.out.println(CLASS_NAME + "unsetRoleWhichIsNotSet");

		Map<String, Integer> mapping = prepareMapping(user3);

		authzResolverImpl.unsetRole(sess, mapping, Role.VOADMIN);
	}

	@Test(expected = RoleAlreadySetException.class)
	public void setRoleWhichIsAlreadySet() throws Exception {
		System.out.println(CLASS_NAME + "setRoleWhichIsAlreadySet");

		Map<String, Integer> mapping = prepareMapping(user4);

		authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
		authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
	}

	private Map<String, Integer> prepareMapping(User user) {
		Map<String, Integer> mapping = new HashMap<>();

		mapping.put("user_id", user.getId());
		mapping.put("vo_id", createdVo.getId());
		mapping.put("role_id", authzResolverImpl.getRoleId(Role.VOADMIN));

		return mapping;
	}
}
