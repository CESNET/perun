package cz.metacentrum.perun.core.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.TestUtils.TestConsumer;
import cz.metacentrum.perun.TestUtils.TestSupplier;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicy;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.BlockedLogin;
import cz.metacentrum.perun.core.api.BlockedLoginsOrderColumn;
import cz.metacentrum.perun.core.api.BlockedLoginsPageQuery;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleObject;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.SortingOrder;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.UsersOrderColumn;
import cz.metacentrum.perun.core.api.UsersPageQuery;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DeletionNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginExistsException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsNotBlockedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceOnlyRoleAssignedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.blImpl.UsersManagerBlImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;

import java.util.UUID;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests of UsersManager.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UsersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "UsersManager.";
	private static final String ATTR_UES_O = "o";
	private static final String ATTR_UES_CN = "cn";
	private static final String URN_ATTR_USER_PREFERRED_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
	private static final String URN_ATTR_UES_O = AttributesManager.NS_UES_ATTR_DEF + ':' + ATTR_UES_O;
	private static final String URN_ATTR_UES_CN = AttributesManager.NS_UES_ATTR_DEF + ':' + ATTR_UES_CN;

	private static final String defaultBlockedLogin = "perunEngine";
	private static final String globallyBlockedLogin = "globalLogin";
	private static final String namespaceBlockedLogin = "namespaceLogin";

	private User user;           // our User
	private User anonymizedUser;
	private User serviceUser1;
	private User serviceUser2;
	private User sponsoredUser;
	private Vo vo;
	String userFirstName = "";
	String userLastName = "";
	String anonymizedUserFirstName = "AnonymFirstName";
	String anonymizedUserLastName = "AnonymLastName";
	String extLogin = "";        // his login in external source
	String extLogin2 = "";
	final String extSourceName = "UserManagerEntryIntegrationTest";
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final UserExtSource userExtSource = new UserExtSource();   // create new User Ext Source
	private UsersManager usersManager;

	@Before
	public void setUp() throws Exception {

		usersManager = perun.getUsersManager();
		// set random name and logins during every setUp method
		userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));   // his login in external source
		extLogin2 = Long.toHexString(Double.doubleToLongBits(Math.random()));
		vo = setUpVo();
		setUpUser();
		setUpAnonymizedUser();
		setUpUserExtSource();
		setUpSpecificUser1ForUsers(vo);
		setUpSpecificUser2ForUser(vo);
		setUpSponsoredUserForVo(vo);
	}

	@Test
	public void createUser() throws Exception {
		System.out.println(CLASS_NAME + "createUser");

		user = new User();
		user.setFirstName(userFirstName);
		user.setMiddleName("");
		user.setLastName(userLastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));
		// create new user in database
		assertTrue("user id must be greater than zero", user.getId() > 0);
		usersForDeletion.add(user);

	}

	@Test
	public void createUserSetsUUID() throws Exception {
		System.out.println(CLASS_NAME + "createUserSetsUUID");

		user = new User();
		user.setFirstName(userFirstName);
		user.setLastName(userLastName);

		User createdUser = perun.getUsersManagerBl().createUser(sess, user);

		assertThat(createdUser.getUuid()).isNotNull();
		assertThat(createdUser.getUuid().version()).isEqualTo(4);
	}

	@Test
	public void getUserById() throws Exception {
		System.out.println(CLASS_NAME + "getUserById");

		User secondUser = usersManager.getUserById(sess, user.getId());
		assertNotNull(secondUser);
		assertEquals("both users should be the same",user,secondUser);
		assertThat(secondUser.getUuid()).isNotNull();
		assertThat(secondUser.getUuid().version()).isEqualTo(4);
	}


	@Test (expected=UserNotExistsException.class)
	public void getUserByIdWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getUserByIdWhenUserNotExist");

		usersManager.getUserById(sess, 0);
		// shouldn't find user

	}

	@Test
	public void getUsers() throws Exception {
		System.out.println(CLASS_NAME + "getUsers");

		List<User> users = usersManager.getUsers(sess);
		assertNotNull(users);
		assertTrue(users.size() > 0);
		assertTrue(users.contains(user));

	}

	@Test
	public void getSpecificUsers() throws Exception {
		System.out.println(CLASS_NAME + "getServiceUsers");

		List<User> users = usersManager.getSpecificUsers(sess);
		assertTrue(users.contains(serviceUser1));
		assertTrue(users.contains(serviceUser2));
	}

	@Test
	public void setAndUnsetSpecificUser() throws Exception {
		System.out.println(CLASS_NAME + "setAndUnsetSpecificUser");
		setUpUser();
		User owner = user;

		assertTrue("User should be service user", serviceUser1.isServiceUser());
		usersManager.unsetSpecificUser(sess, serviceUser1, SpecificUserType.SERVICE);
		User user2 = usersManager.getUserById(sess, serviceUser1.getId());
		assertTrue("User shouldn't be service user", !user2.isServiceUser());
		usersManager.setSpecificUser(sess, user2, SpecificUserType.SERVICE, owner);
		user2 = usersManager.getUserById(sess, user2.getId());
		assertTrue("User should be service user again", user2.isServiceUser());
		List<User> owners = usersManager.getUsersBySpecificUser(sess, user2);
		assertTrue("There should be just our owner", owners.size() == 1 && owners.contains(owner));
	}

	@Test
	public void unsetServiceUserWithServiceOnlyRole() throws Exception {
		System.out.println(CLASS_NAME + "unsetServiceUserWithServiceOnlyRole");
		setUpUser();

		assertTrue("User should be service user", serviceUser1.isServiceUser());
		AuthzResolverBlImpl.setRole(sess, serviceUser1, null, Role.EXEMPTEDFROMMFA);
		assertThrows(ServiceOnlyRoleAssignedException.class,
				() -> usersManager.unsetSpecificUser(sess, serviceUser1, SpecificUserType.SERVICE));
		AuthzResolverBlImpl.unsetRole(sess, serviceUser1, null, Role.EXEMPTEDFROMMFA);
	}

	@Test
	public void getUsersByServiceUser1() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByServiceUser1");

		List<User> users = usersManager.getUsersBySpecificUser(sess, serviceUser1);
		assertTrue(users.contains(user));
		assertTrue(users.size() == 2);
	}

	@Test
	public void getUsersByServiceUser2() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByServiceUser2");

		List<User> users = usersManager.getUsersBySpecificUser(sess, serviceUser2);
		assertTrue(users.contains(user));
		assertTrue(users.size() == 1);
	}

	@Test
	public void getUnanonymizedUsersBySpecificUser1() throws Exception {
		System.out.println(CLASS_NAME + "getUnanonymizedUsersBySpecificUser1");

		List<User> users = usersManager.getUnanonymizedUsersBySpecificUser(sess, serviceUser1);
		assertTrue(users.contains(user));
		assertFalse(users.contains(anonymizedUser));
		assertTrue(users.size() == 1);
	}

	@Test
	public void getSpecificUsersByUser() throws Exception {
		System.out.println(CLASS_NAME + "getServiceUsersByUser");

		List<User> users = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(users.contains(serviceUser1));
		assertTrue(users.contains(serviceUser2));
		assertTrue(users.size() == 2);
	}

	@Test
	public void modifyOwnership() throws Exception {
		System.out.println(CLASS_NAME + "modifyOwnership");

		usersManager.removeSpecificUserOwner(sess, user, serviceUser1);

		List<User> users = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(users.contains(serviceUser2));
		assertTrue(users.size() == 1);

		usersManager.removeSpecificUserOwner(sess, user, serviceUser2);
		users = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(users.isEmpty());

		usersManager.addSpecificUserOwner(sess, user, serviceUser1);
		users = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(users.contains(serviceUser1));
		assertTrue(users.size() == 1);

		usersManager.addSpecificUserOwner(sess, user, serviceUser2);
		users = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(users.contains(serviceUser1));
		assertTrue(users.contains(serviceUser2));
		assertTrue(users.size() == 2);
	}

	@Test (expected= RelationNotExistsException.class)
	public void removeNotExistingOwnership() throws Exception {
		System.out.println(CLASS_NAME + "removeNotExistingOwnership");

		Member member = setUpMember(vo);
		User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);

		usersManager.removeSpecificUserOwner(sess, userOfMember, serviceUser1);
	}

	@Test (expected= RelationNotExistsException.class)
	public void removeOwnershipTwiceInRow() throws Exception {
		System.out.println(CLASS_NAME + "removeOwnershipTwiceInRow");

		usersManager.removeSpecificUserOwner(sess, user, serviceUser1);
		usersManager.removeSpecificUserOwner(sess, user, serviceUser1);
	}

	@Test (expected= RelationExistsException.class)
	public void addExistingOwnership() throws Exception {
		System.out.println(CLASS_NAME + "addExistingOwnership");

		usersManager.addSpecificUserOwner(sess, user, serviceUser1);

	}

	@Test (expected= RelationExistsException.class)
	public void addOwnershipTwiceInRow() throws Exception {
		System.out.println(CLASS_NAME + "addOwnershipTwiceInRow");

		Member member = setUpMember(vo);
		User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);

		usersManager.addSpecificUserOwner(sess, userOfMember, serviceUser1);
		usersManager.addSpecificUserOwner(sess, userOfMember, serviceUser1);
	}

	@Test
	public void disableExistingOwnership() throws Exception {
		System.out.println(CLASS_NAME + "disableExistingOwnership");

		Member member = setUpMember(vo);
		User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);
		assertTrue(!perun.getUsersManagerBl().specificUserOwnershipExists(sess, userOfMember, serviceUser1));
		assertTrue(!perun.getUsersManagerBl().specificUserOwnershipExists(sess, userOfMember, serviceUser2));

		usersManager.addSpecificUserOwner(sess, userOfMember, serviceUser1);
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, userOfMember, serviceUser1));

		usersManager.addSpecificUserOwner(sess, userOfMember, serviceUser2);
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, userOfMember, serviceUser2));

		List<User> specificUsers = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(specificUsers.contains(serviceUser1));
		assertTrue(specificUsers.contains(serviceUser2));
		assertTrue(specificUsers.size() == 2);

		usersManager.removeSpecificUserOwner(sess, user, serviceUser1);
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, user, serviceUser1));
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, user, serviceUser2));
		specificUsers = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(specificUsers.contains(serviceUser2));
		assertTrue(specificUsers.size() == 1);

		usersManager.removeSpecificUserOwner(sess, user, serviceUser2);
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, user, serviceUser1));
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, user, serviceUser2));
		assertTrue(perun.getUsersManagerBl().specificUserOwnershipExists(sess, user, serviceUser2));
		specificUsers = usersManager.getSpecificUsersByUser(sess, user);
		assertTrue(specificUsers.isEmpty());
	}

	@Test
	public void updateUser() throws Exception {
		System.out.println(CLASS_NAME + "updateUser");

		user.setFirstName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		user.setMiddleName("");
		user.setLastName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		user.setTitleBefore("");
		user.setTitleAfter("");

		User updatedUser = usersManager.updateUser(sess, user);
		assertNotNull(updatedUser);
		assertEquals("users should be the same after update in DB",user,updatedUser);

		User gettingUser = usersManager.getUserById(sess, updatedUser.getId());
		assertEquals("users should be the same after updated in DB and getting from DB",gettingUser,updatedUser);

	}

	@Test (expected=UserNotExistsException.class)
	public void updateWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "updateWhenUserNotExists");

		usersManager.updateUser(sess, new User());

	}

	@Test
	public void updateUserWithNullValues() throws Exception {
		System.out.println(CLASS_NAME + "updateUserWithNullValues");

		user.setFirstName(null);
		user.setLastName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		user.setMiddleName(null);
		user.setTitleBefore(null);
		user.setTitleAfter(null);
		User updatedUser = usersManager.updateUser(sess, user);
		User gettingUser = usersManager.getUserById(sess, updatedUser.getId());
		assertNotNull(updatedUser);
		assertEquals("users should be the same after update in DB", gettingUser, updatedUser);
	}

	@Test (expected=cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException.class)
	public void updateUserWithNullValueInLastName() throws Exception {
		System.out.println(CLASS_NAME + "updateUserWithNullValueInLastName");

		user.setFirstName(null);
		user.setLastName(null);
		User updateUser = usersManager.updateUser(sess, user);
	}

	@Test
	public void deleteUser() throws Exception {
		System.out.println(CLASS_NAME + "deleteUser");

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			usersManager.deleteUser(sess, user, true);
			assertThatExceptionOfType(UserNotExistsException.class).isThrownBy(
				() -> usersManager.getUserById(sess, user.getId()));
		} finally {
			// set userDeletionForced back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
		}

	}

	@Test
	public void deleteUserWithPersistentLogin() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserWithPersistentLogin");

		ExtSource extSource = new ExtSource("https://login.bbmri-eric.eu/idp/", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace("urn:perun:user:attribute-def:virt");
		attrDef.setFriendlyName("login-namespace:bbmri-persistent");
		attrDef.setType(String.class.getName());
		perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		AttributeDefinition attrDefShadow = new AttributeDefinition();
		attrDefShadow.setNamespace("urn:perun:user:attribute-def:def");
		attrDefShadow.setFriendlyName("login-namespace:bbmri-persistent-shadow");
		attrDefShadow.setType(String.class.getName());
		perun.getAttributesManagerBl().createAttribute(sess, attrDefShadow);

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			usersManager.deleteUser(sess, user, true);

			assertThatExceptionOfType(UserNotExistsException.class).isThrownBy(
				() -> usersManager.getUserById(sess, user.getId()));
		} finally {
			// set userDeletionForced back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
		}
	}

	@Test
	public void deleteUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserWhenUserNotExists");

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			assertThatExceptionOfType(UserNotExistsException.class).isThrownBy(
				() -> usersManager.deleteUser(sess, new User(), true));
		} finally {
			// set userDeletionForced back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
		}
	}

	@Test
	public void deleteUserNotSupported() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserNotSupported");

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Disable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(false);
			assertThatExceptionOfType(DeletionNotSupportedException.class).isThrownBy(
				() -> usersManager.deleteUser(sess, user));
		} finally {
			// set userDeletionForced back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
		}
	}

	@Test
	public void deleteUserAndCheckBlockedLogins() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserAndCheckBlockedLogins");

		// Create logins for user in 3 namespaces
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:namespace1");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin));
		attrLogin.setValue("login1");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin);

		Attribute attrLogin2 = new Attribute();
		attrLogin2.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin2.setFriendlyName("login-namespace:namespace2");
		attrLogin2.setType(String.class.getName());
		attrLogin2 = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin2));
		attrLogin2.setValue("login2");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin2);

		// This attribute will not be in attributesToKeep, so should NOT be blocked during deletion of user
		Attribute attrLogin3 = new Attribute();
		attrLogin3.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin3.setFriendlyName("login-namespace:namespace3");
		attrLogin3.setType(String.class.getName());
		attrLogin3 = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin3));
		attrLogin3.setValue("login3");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin3);

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			List<String> attrsToKeep = new ArrayList<>();
			// Define exactly two attributes to keep
			attrsToKeep.add(attrLogin.getName());
			attrsToKeep.add(attrLogin2.getName());
			BeansUtils.getCoreConfig().setAttributesToKeep(attrsToKeep);
			usersManager.deleteUser(sess, user, true);
		} finally {
			// set userDeletionForced and attributesToKeep back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
			BeansUtils.getCoreConfig().setAttributesToKeep(Collections.emptyList());
		}

		assertTrue(usersManager.isLoginBlockedForNamespace(sess, "login1", "namespace1", true));
		assertFalse(usersManager.isLoginBlockedForNamespace(sess, "login1", "namespace2", true));
		assertTrue(usersManager.isLoginBlockedForNamespace(sess, "login2", "namespace2", true));
		assertFalse(usersManager.isLoginBlockedForNamespace(sess, "login2", "namespace1", true));
		// login3 in namespace3 should NOT be blocked
		assertFalse(usersManager.isLoginBlockedForNamespace(sess, "login3", "namespace3", true));

		assertEquals("The number of blocked logins should be 2.", 2, usersManager.getAllBlockedLoginsInNamespaces(sess).size());

		assertThatExceptionOfType(UserNotExistsException.class).isThrownBy(
			() -> perun.getUsersManager().getUserById(sess, user.getId()));
	}

	@Test
	public void deleteUserAndCheckBlockedAllLogins() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserAndCheckBlockedAllLogins");

		// Create logins for user in 3 namespaces
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:namespace1");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin));
		attrLogin.setValue("login1");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin);

		Attribute attrLogin2 = new Attribute();
		attrLogin2.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin2.setFriendlyName("login-namespace:namespace2");
		attrLogin2.setType(String.class.getName());
		attrLogin2 = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin2));
		attrLogin2.setValue("login2");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin2);

		Attribute attrLogin3 = new Attribute();
		attrLogin3.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin3.setFriendlyName("login-namespace:namespace3");
		attrLogin3.setType(String.class.getName());
		attrLogin3 = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin3));
		attrLogin3.setValue("login3");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin3);

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			List<String> attrsToKeep = new ArrayList<>();
			attrsToKeep.add("urn:perun:user:attribute-def:def:login-namespace:*");
			BeansUtils.getCoreConfig().setAttributesToKeep(attrsToKeep);
			usersManager.deleteUser(sess, user, true);
		} finally {
			// set userDeletionForced and attributesToKeep back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
			BeansUtils.getCoreConfig().setAttributesToKeep(Collections.emptyList());
		}

		assertTrue(usersManager.isLoginBlockedForNamespace(sess, "login1", "namespace1", true));
		assertFalse(usersManager.isLoginBlockedForNamespace(sess, "login1", "namespace2", true));
		assertTrue(usersManager.isLoginBlockedForNamespace(sess, "login2", "namespace2", true));
		assertFalse(usersManager.isLoginBlockedForNamespace(sess, "login2", "namespace1", true));
		assertTrue(usersManager.isLoginBlockedForNamespace(sess, "login3", "namespace3", true));

		assertEquals("The number of blocked logins should be 3.", 3, usersManager.getAllBlockedLoginsInNamespaces(sess).size());

		assertThatExceptionOfType(UserNotExistsException.class).isThrownBy(
			() -> perun.getUsersManager().getUserById(sess, user.getId()));
	}

	@Test
	public void blockedLoginsAndRelatedUserIds() throws Exception {
		System.out.println(CLASS_NAME + "blockedLoginsAndRelatedUserIds");

		String login = "login1";
		String namespace = "namespace1";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);

		// user id should NOT be stored with block login - method should return null
		assertNull(usersManager.getRelatedUserIdByBlockedLoginInNamespace(sess, login, namespace));

		// Create login for user in namespace1
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:namespace2");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(perun.getAttributesManager().createAttribute(sess, attrLogin));
		attrLogin.setValue("login2");
		perun.getAttributesManager().setAttribute(sess, user, attrLogin);

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			BeansUtils.getCoreConfig().setAttributesToKeep(Collections.singletonList(attrLogin.getName()));
			usersManager.deleteUser(sess, user, true);
		} finally {
			// set userDeletionForced and attributesToKeep back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
			BeansUtils.getCoreConfig().setAttributesToKeep(Collections.emptyList());
		}

		// check that user id is correctly stored with the block login
		assertEquals(user.getId(), usersManager.getRelatedUserIdByBlockedLoginInNamespace(sess, "login2", "namespace2").intValue());
		assertThatExceptionOfType(LoginIsNotBlockedException.class).isThrownBy(
			() -> usersManager.getRelatedUserIdByBlockedLoginInNamespace(sess, "login3", "namespace2"));
	}

	@Test
	public void anonymizeUserNotSupported() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserNotSupported");

		boolean originalUserDeletionForced = BeansUtils.getCoreConfig().getUserDeletionForced();
		try {
			// Enable deletion of users
			BeansUtils.getCoreConfig().setUserDeletionForced(true);
			assertThatExceptionOfType(AnonymizationNotSupportedException.class).isThrownBy(
				() -> usersManager.anonymizeUser(sess, user, false));
		} finally {
			// set userDeletionForced back to the original value
			BeansUtils.getCoreConfig().setUserDeletionForced(originalUserDeletionForced);
		}
	}

	@Test
	public void anonymizeUser() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUser");

		// set preferredMail and phone attributes
		Attribute preferredMail = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
		preferredMail.setValue("mail@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, user, preferredMail);
		Attribute phone = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
		phone.setValue("+420555555");
		perun.getAttributesManagerBl().setAttribute(sess, user, phone);

		List<String> originalAttributesToKeep = BeansUtils.getCoreConfig().getAttributesToKeep();
		try {
			// configure attributesToKeep so it contains only 1 attribute - preferredMail
			BeansUtils.getCoreConfig().setAttributesToKeep(Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));
			usersManager.anonymizeUser(sess, user, false);
		} finally {
			// set attributesToKeep back to the original attributes
			BeansUtils.getCoreConfig().setAttributesToKeep(originalAttributesToKeep);
		};

		User updatedUser = perun.getUsersManagerBl().getUserById(sess, user.getId());
		String anonymizationAttrName = AttributesManager.NS_USER_ATTR_VIRT + ":anonymized";
		Attribute anonymizationAttribute = perun.getAttributesManagerBl().getAttribute(sess, user, anonymizationAttrName);

		assertTrue("User should be marked as anonymized (attribute value).", (boolean) anonymizationAttribute.getValue());
		assertTrue("Firstname should be null or empty.", updatedUser.getFirstName() == null || updatedUser.getFirstName().isEmpty());
		assertTrue("Lastname should be null or empty.", updatedUser.getLastName() == null || updatedUser.getLastName().isEmpty());

		Attribute updatedPreferredMail = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
		Attribute updatedPhone = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
		assertEquals("PreferredMail attribute should be kept untouched.", updatedPreferredMail, preferredMail);
		assertNull("Phone attribute should be deleted.", updatedPhone.getValue());
	}

	@Test
	public void anonymizeUserWithForce() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWithForce");

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);

		usersManager.anonymizeUser(sess, user, true);

		assertThatExceptionOfType(MemberNotExistsException.class)
			.isThrownBy(() -> usersManager.getUserByMember(sess, member));
	}

	@Test
	public void anonymizeUserWithExistingRelation() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWithExistingRelation");

		perun.getMembersManagerBl().createMember(sess, vo, user);

		assertThatExceptionOfType(RelationExistsException.class)
			.isThrownBy(() -> usersManager.anonymizeUser(sess, user, false));
	}

	@Test(expected=UserNotExistsException.class)
	public void anonymizeUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWhenUserNotExists");

		usersManager.anonymizeUser(sess, new User(), false);
		// shouldn't find user
	}

	@Test
	public void anonymizeUserWhenAnonymizationNotSupported() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWhenAnonymizationNotSupported");

		// create dummy attribute
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("dummy-test");
		attrLogin.setType(String.class.getName());
		perun.getAttributesManager().createAttribute(sess, attrLogin);

		// set dummy attribute
		Attribute dummy = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":dummy-test");
		dummy.setValue("dummy");
		perun.getAttributesManagerBl().setAttribute(sess, user, dummy);

		List<String> originalAttributesToAnonymize = BeansUtils.getCoreConfig().getAttributesToAnonymize();
		try {
			// configure attributesToAnonymize so it contains only 1 attribute - dummy-test
			BeansUtils.getCoreConfig().setAttributesToAnonymize(Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":dummy-test"));
			assertThatExceptionOfType(AnonymizationNotSupportedException.class).isThrownBy(
				() -> usersManager.anonymizeUser(sess, user, false));
		} finally {
			// set attributesToAnonymize back to the original attributes
			BeansUtils.getCoreConfig().setAttributesToAnonymize(originalAttributesToAnonymize);
		}
	}

	@Test
	public void addIDPExtSourcesWithSameLogin() throws Exception {
		System.out.println(CLASS_NAME + "addIDPExtSourcesWithSameLogin");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ExtSource ext2 = new ExtSource("test2", ExtSourcesManagerEntry.EXTSOURCE_IDP);

		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);
		ext2 = perun.getExtSourcesManagerBl().createExtSource(sess, ext2, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		UserExtSource ues2 = new UserExtSource(ext2, 1, "testExtLogin@test");

		// should be allowed since user is the same
		usersManager.addUserExtSource(sess, user, ues1);
		usersManager.addUserExtSource(sess, user, ues2);

	}

	@Test (expected=InternalErrorException.class)
	public void addIDPExtSourcesWithSameLoginDifferentUser() throws Exception {
		System.out.println(CLASS_NAME + "addIDPExtSourcesWithSameLoginDifferentUser");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ExtSource ext2 = new ExtSource("test2", ExtSourcesManagerEntry.EXTSOURCE_IDP);

		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);
		ext2 = perun.getExtSourcesManagerBl().createExtSource(sess, ext2, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		UserExtSource ues2 = new UserExtSource(ext2, 1, "testExtLogin@test");

		// should fail since there are different users
		usersManager.addUserExtSource(sess, user, ues1);
		usersManager.addUserExtSource(sess, sponsoredUser, ues2);

	}

	@Test
	public void addIDPExtSourcesWithSameLoginDifferentUserDuplicates() throws Exception {
		System.out.println(CLASS_NAME + "addIDPExtSourcesWithSameLoginDifferentUserDuplicates");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ExtSource ext2 = new ExtSource("test2", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ExtSource ext3 = new ExtSource("test3", ExtSourcesManagerEntry.EXTSOURCE_IDP);

		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);
		ext2 = perun.getExtSourcesManagerBl().createExtSource(sess, ext2, null);
		ext3 = perun.getExtSourcesManagerBl().createExtSource(sess, ext3, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		UserExtSource ues2 = new UserExtSource(ext2, 1, "testExtLogin@test");
		UserExtSource ues3 = new UserExtSource(ext3, 1, "testExtLogin@test");

		usersManager.addUserExtSource(sess, user, ues1);
		usersManager.addUserExtSource(sess, user, ues2);
		// should fail since there is different user using these identities (multiple times)
		assertThatExceptionOfType(InternalErrorException.class)
			.isThrownBy(() -> usersManager.addUserExtSource(sess, sponsoredUser, ues3));

	}

	@Test
	public void addUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "addUserExtSource");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);

		UserExtSource userExtSource2 = new UserExtSource();
		userExtSource2.setLogin(extLogin2);
		userExtSource2.setExtSource(externalSource);

		UserExtSource returnedUserExtSource = usersManager.addUserExtSource(sess, user, userExtSource2);
		assertNotNull(returnedUserExtSource);
		assertTrue(returnedUserExtSource.getId() > 0);
		assertEquals("Both User Ext Sources should be the same",userExtSource2, returnedUserExtSource);

	}

	@Test
	public void addUserExtSourceWithAttributes() throws Exception {
		System.out.println(CLASS_NAME + "addUserExtSourceWithAttributes");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		Attribute attribute = createUserExtSourceAttribute("testAttribute", String.class.getName(), "testValue", true);
		List<Attribute> uesAttributes = List.of(attribute);

		UserExtSource userExtSource2 = new UserExtSource();
		userExtSource2.setLogin(extLogin2);
		userExtSource2.setExtSource(externalSource);

		RichUserExtSource returnedRichUserExtSource = usersManager.addUserExtSourceWithAttributes(sess, user, userExtSource2, uesAttributes);
		assertNotNull(returnedRichUserExtSource);
		assertTrue(returnedRichUserExtSource.asUserExtSource().getId() > 0);
		assertEquals("Both User Ext Sources should be the same",userExtSource2, returnedRichUserExtSource.asUserExtSource());
		assertEquals(1, returnedRichUserExtSource.getAttributes().size());
	}

	@Test (expected=UserExtSourceExistsException.class)
	public void addUserExtSourceWhenUserExtSourceAlreadyExists() throws Exception {
		System.out.println(CLASS_NAME + "addUserExtSourceWhenUserExtSourceAlreadyExists");

		usersManager.addUserExtSource(sess, user, userExtSource);
	}


	@Test (expected=UserNotExistsException.class)
	public void addUserExtSourceWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addUserExtSourceWhenUserNotExists");

		usersManager.addUserExtSource(sess, new User(), userExtSource);
		// shouldn't find user
	}

	@Test
	public void updateUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "updateUserExtSource");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		ues1 = usersManager.addUserExtSource(sess, user, ues1);

		ues1.setLoa(2);
		usersManager.updateUserExtSource(sess, ues1);

		UserExtSource retrievedUes = usersManager.getUserExtSourceById(sess, ues1.getId());
		Assert.assertTrue("LoA was not updated", retrievedUes.getLoa() == ues1.getLoa());

		ues1.setLogin("changedTestExtLogin@test");
		usersManager.updateUserExtSource(sess, ues1);

		retrievedUes = usersManager.getUserExtSourceById(sess, ues1.getId());
		Assert.assertTrue("Login was not updated", Objects.equals(retrievedUes.getLogin(),ues1.getLogin()));

	}

	@Test (expected = UserExtSourceExistsException.class)
	public void updateUserExtSourceWhenExists() throws Exception {
		System.out.println(CLASS_NAME + "updateUserExtSourceWhenExists");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		usersManager.addUserExtSource(sess, user, ues1);
		UserExtSource ues2 = new UserExtSource(ext1, 1, "testExtLogin2@test");
		ues2 = usersManager.addUserExtSource(sess, user, ues2);

		ues2.setLogin("testExtLogin@test");
		usersManager.updateUserExtSource(sess, ues2);

	}

	@Test
	public void getUserByUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getUserByUserExtSource");

		User secondUser = usersManager.getUserByUserExtSource(sess, userExtSource);
		assertEquals("users should be the same from both ext sources",user, secondUser);

	}

	@Test
	public void getUserByExtSourceNameAndExtLogin() throws Exception {
		System.out.println(CLASS_NAME + "getUserByExtSourceNameAndExtLogin");

		String extSourceName = userExtSource.getExtSource().getName();
		String extLogin = userExtSource.getLogin();
		User secondUser = usersManager.getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);
		assertEquals("users should be the same from both ext sources",user, secondUser);

	}

	@Test
	public void getActiveUserExtSources() throws Exception {
		System.out.println(CLASS_NAME + "getActiveUserExtSources");
		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		UserExtSource userExtSource = usersManager.getUserExtSourceByExtLogin(sess, externalSource, extLogin);

		List<UserExtSource> ues = perun.getUsersManagerBl().getActiveUserExtSources(sess, user);
		assertTrue(ues.contains(userExtSource));
	}

	@Test
	public void getActiveUserExtSourcesIfEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getActiveUserExtSources");

		User emptyUser = setUpEmptyUser();
		List<UserExtSource> ues = perun.getUsersManagerBl().getUserExtSources(sess, emptyUser);
		for(UserExtSource uExtSource: ues) {
			perun.getUsersManagerBl().removeUserExtSource(sess, emptyUser, uExtSource);
		}

		ues = perun.getUsersManagerBl().getActiveUserExtSources(sess, emptyUser);
		assertTrue(ues.isEmpty());
	}

	@Test
	public void getUserExtSourceByExtLogin() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByExtLogin");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		UserExtSource returnedUserExtSource = usersManager.getUserExtSourceByExtLogin(sess, externalSource, extLogin);
		assertEquals("both ext source should be the same", userExtSource, returnedUserExtSource);
		// check if both user ext sources are the same.

	}

	//TODO: for this test is needed to add creating login in registrar database
	/*
		 @Test (expected=AlreadyReservedLoginException.class)
		 public void isAlreadyReservedLogin() throws Exception {
		 System.out.println(CLASS_NAME + "isAlreadyReservedLogin");

		 String namespace = "einfra";
		 String login = "martin_svehla";
		 perun.getUsersManagerBl().checkReservedLogins(sess, namespace, login);
		 }
		 */

	@Test
	public void getAllBlockedLoginsInNamespaces() throws Exception {
		System.out.println(CLASS_NAME + "getAllBlockedLoginsInNamespaces");

		String login = "login";
		String login2 = "login2";
		String namespace = "namespace";
		String namespace2 = "namespace2";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), null);
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login2), namespace2);

		List<BlockedLogin> listOfBlockedLogins = perun.getUsersManager().getAllBlockedLoginsInNamespaces(sess);

		assertEquals(listOfBlockedLogins.size(), 3);
		assertEquals(listOfBlockedLogins.get(0).getLogin(), login);
		assertNull(listOfBlockedLogins.get(0).getNamespace());
		assertEquals(listOfBlockedLogins.get(1).getLogin(), login);
		assertEquals(listOfBlockedLogins.get(1).getNamespace(), namespace);
		assertEquals(listOfBlockedLogins.get(2).getLogin(), login2);
		assertEquals(listOfBlockedLogins.get(2).getNamespace(), namespace2);
	}

	@Test
	public void isLoginBlocked() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlocked");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlockedGlobally = perun.getUsersManager().isLoginBlocked(sess, globalLogin, false);

		assertTrue(isLoginBlockedGlobally);

		isLoginBlockedGlobally = perun.getUsersManager().isLoginBlocked(sess, globalLogin.toUpperCase(), false);

		// should be true, even if we should not ignore case because global logins are always case-insensitive
		assertTrue(isLoginBlockedGlobally);

		String namespaceLogin = "loginNamespace";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceLogin), "namespace");
		boolean isLoginBlocked = perun.getUsersManager().isLoginBlocked(sess, namespaceLogin, false);

		assertTrue(isLoginBlocked);

		isLoginBlocked = perun.getUsersManager().isLoginBlocked(sess, namespaceLogin.toUpperCase(), false);

		// should be false, if we do NOT ignore case
		assertFalse(isLoginBlocked);
	}

	@Test
	public void isLoginBlockedIgnoreCase() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlocked");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlockedGlobally = perun.getUsersManager().isLoginBlocked(sess, globalLogin, false);

		assertTrue(isLoginBlockedGlobally);

		String namespaceLogin = "loginNamespace";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceLogin), "namespace");
		isLoginBlockedGlobally = perun.getUsersManager().isLoginBlocked(sess, namespaceLogin, false);

		assertTrue(isLoginBlockedGlobally);
	}

	@Test
	public void isLoginBlockedGlobally() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlockedGlobally");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlockedGlobally = perun.getUsersManager().isLoginBlockedGlobally(sess, globalLogin);

		assertTrue(isLoginBlockedGlobally);

		String namespaceLogin = "loginNamespace";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceLogin), "namespace");
		isLoginBlockedGlobally = perun.getUsersManager().isLoginBlockedGlobally(sess, namespaceLogin);

		assertFalse(isLoginBlockedGlobally);
	}

	@Test
	public void isLoginBlockedGloballyCaseInsensitive() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlockedGlobally");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlockedGlobally = perun.getUsersManager().isLoginBlockedGlobally(sess, globalLogin.toUpperCase());

		assertTrue(isLoginBlockedGlobally);
	}

	@Test
	public void isLoginBlockedForNamespace() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlockedForNamespace");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlockedGlobally = perun.getUsersManager().isLoginBlockedForNamespace(sess, globalLogin, null, false);

		assertTrue(isLoginBlockedGlobally);

		isLoginBlockedGlobally = perun.getUsersManager().isLoginBlockedForNamespace(sess, globalLogin.toUpperCase(), null, false);

		// should be true, since globally blocked logins are case-insensitive
		assertTrue(isLoginBlockedGlobally);

		String namespaceLogin = "loginNamespace";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceLogin), "namespace");
		boolean isLoginBlockedForNamespace = perun.getUsersManager().isLoginBlockedForNamespace(sess, namespaceLogin, "namespace", false);

		assertTrue(isLoginBlockedForNamespace);

		isLoginBlockedForNamespace = perun.getUsersManager().isLoginBlockedForNamespace(sess, namespaceLogin.toUpperCase(), "namespace", false);

		// should be false, if we do NOT ignore case
		assertFalse(isLoginBlockedForNamespace);

		isLoginBlockedForNamespace = perun.getUsersManager().isLoginBlockedForNamespace(sess, namespaceLogin, "namespace_test", false);

		assertFalse(isLoginBlockedForNamespace);
	}

	@Test
	public void isLoginBlockedForNamespaceIgnoreCase() throws Exception {
		System.out.println(CLASS_NAME + "isLoginBlockedForNamespace");

		String globalLogin = "login";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globalLogin), null);
		boolean isLoginBlocked = perun.getUsersManager().isLoginBlockedForNamespace(sess, globalLogin.toUpperCase(), null, true);

		assertTrue(isLoginBlocked);

		String namespaceLogin = "loginNamespace";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceLogin), "namespace");
		isLoginBlocked = perun.getUsersManager().isLoginBlockedForNamespace(sess, namespaceLogin.toUpperCase(), "namespace", true);

		assertTrue(isLoginBlocked);

		isLoginBlocked = perun.getUsersManager().isLoginBlockedForNamespace(sess, namespaceLogin.toUpperCase(), "namespace_test", true);

		assertFalse(isLoginBlocked);
	}

	@Test
	public void blockAndUnblockLogin() throws Exception {
		System.out.println(CLASS_NAME + "blockAndUnblockLogin");

		String login = "login";
		String namespace = "namespace";

		assertFalse(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, null, false));

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), null);
		assertTrue(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, null, false));

		perun.getUsersManager().unblockLogins(sess, Collections.singletonList(login), null);
		assertFalse(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, null, false));

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		assertTrue(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, namespace, false));

		perun.getUsersManager().unblockLogins(sess, Collections.singletonList(login), namespace);
		assertFalse(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, namespace, false));
	}

	@Test
	public void bulkBlockAndUnblockLogins() throws Exception {
		System.out.println(CLASS_NAME + "getAllBlockedLoginsInNamespaces");

		String login = "login";
		String login2 = "login2";
		String login3 = "login3";
		String namespace = "namespace";

		perun.getUsersManager().blockLogins(sess, Arrays.asList(login, login2, login3), namespace);

		List<BlockedLogin> listOfBlockedLogins = perun.getUsersManager().getAllBlockedLoginsInNamespaces(sess);
		assertEquals(listOfBlockedLogins.size(), 3);
		assertEquals(listOfBlockedLogins.get(0).getLogin(), login);
		assertEquals(listOfBlockedLogins.get(0).getNamespace(), namespace);
		assertEquals(listOfBlockedLogins.get(1).getLogin(), login2);
		assertEquals(listOfBlockedLogins.get(1).getNamespace(), namespace);
		assertEquals(listOfBlockedLogins.get(2).getLogin(), login3);
		assertEquals(listOfBlockedLogins.get(2).getNamespace(), namespace);

		perun.getUsersManager().unblockLogins(sess, Arrays.asList(login, login2), namespace);

		listOfBlockedLogins = perun.getUsersManager().getAllBlockedLoginsInNamespaces(sess);

		assertEquals(listOfBlockedLogins.size(), 1);
		assertEquals(listOfBlockedLogins.get(0).getLogin(), login3);
		assertEquals(listOfBlockedLogins.get(0).getNamespace(), namespace);
	}

	@Test
	public void unblockLoginsById() throws Exception {
		System.out.println(CLASS_NAME + "unblockLoginsById");
		String login1 = "login";
		String login2 = "login2";
		String namespace = "namespace";

		perun.getUsersManager().blockLogins(sess, Arrays.asList(login1, login2), namespace);

		assertEquals(2, perun.getUsersManager().getAllBlockedLoginsInNamespaces(sess).size());

		int id1 = perun.getUsersManagerBl().getIdOfBlockedLogin(sess, login1, namespace);
		int id2 = perun.getUsersManagerBl().getIdOfBlockedLogin(sess, login2, namespace);

		perun.getUsersManager().unblockLoginsById(sess, Arrays.asList(id1, id2));

		assertEquals(0, perun.getUsersManager().getAllBlockedLoginsInNamespaces(sess).size());
	}

	@Test (expected=LoginIsAlreadyBlockedException.class)
	public void blockAlreadyBlockedLogin() throws Exception {
		System.out.println(CLASS_NAME + "blockAlreadyBlockedLogin");

		String login = "login";
		String namespace = "namespace";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		assertTrue(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, namespace, false));

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		// shouldn't block already blocked login twice
	}

	@Test (expected=LoginIsAlreadyBlockedException.class)
	public void blockAlreadyGloballyBlockedLogin() throws Exception {
		System.out.println(CLASS_NAME + "blockAlreadyGloballyBlockedLogin");

		String login = "login";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), null);
		assertTrue(perun.getUsersManager().isLoginBlockedForNamespace(sess, login, null, false));

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), null);
		// shouldn't block already globally blocked login twice
	}

	@Test (expected=LoginIsNotBlockedException.class)
	public void unblockNotBlockedLogin() throws Exception {
		System.out.println(CLASS_NAME + "unblockNotBlockedLogin");

		perun.getUsersManager().unblockLogins(sess, Collections.singletonList("login"), "namespace");
	}

	@Test (expected=LoginExistsException.class)
	public void blockAlreadyUsedLogin() throws Exception {
		System.out.println(CLASS_NAME + "blockAlreadyUsedLogin");

		setUpUser("firstName", "lastName");
		setUpNamespaceAttribute();
		Attribute namespace = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:dummy");
		String login = "existingLogin";
		namespace.setValue(login);
		perun.getAttributesManagerBl().setAttribute(sess, user, namespace);

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), "dummy");
		// shouldn't block already used login
	}

	@Test (expected=LoginExistsException.class)
	public void blockAlreadyReservedLogin() throws Exception {
		System.out.println(CLASS_NAME + "blockAlreadyReservedLogin");

		String login = "reservedLogin";
		String namespace = "dummy";


		UsersManagerImplApi usersManagerImplApi = mock(UsersManagerImplApi.class);
		UsersManagerBlImpl usersManagerBlImpl = new UsersManagerBlImpl(usersManagerImplApi);
		usersManagerBlImpl.setPerunBl(mock(PerunBl.class, RETURNS_DEEP_STUBS));
		UsersManagerBlImpl spyUserManagerBlImpl = spy(usersManagerBlImpl);

		when(spyUserManagerBlImpl.getUsersManagerImpl().isLoginReserved(any(), eq(namespace), eq(login), anyBoolean())).thenReturn(true);

		spyUserManagerBlImpl.blockLogins(sess, Collections.singletonList(login), namespace, null);
		// shouldn't block already reserved login
	}

	@Test
	public void getBlockedLoginsPage_all() throws Exception {
		System.out.println(CLASS_NAME + "getBlockedLoginsPage_all");

		String login = "login1";
		String login2 = "login2";
		String namespace = "namespace";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login2), null);

		BlockedLoginsPageQuery query = new BlockedLoginsPageQuery(10, 0, SortingOrder.ASCENDING, BlockedLoginsOrderColumn.LOGIN);

		Paginated<BlockedLogin> blockedLogins = usersManager.getBlockedLoginsPage(sess, query);
		assertNotNull(blockedLogins);
		assertEquals(2, blockedLogins.getData().size());
		assertEquals(blockedLogins.getData().get(0), new BlockedLogin(login, namespace));
		assertEquals(blockedLogins.getData().get(1), new BlockedLogin(login2, null));
	}

	@Test
	public void getBlockedLoginPage_searchString() throws Exception {
		System.out.println(CLASS_NAME + "getBlockedLoginPage_searchString");

		String login = "login";
		String login2 = "other";
		String namespace = "namespace";

		perun.getUsersManager().blockLogins(sess, List.of(login, login2), namespace);

		BlockedLoginsPageQuery query = new BlockedLoginsPageQuery(10, 0, SortingOrder.ASCENDING, BlockedLoginsOrderColumn.LOGIN, "login");

		Paginated<BlockedLogin> blockedLogins = usersManager.getBlockedLoginsPage(sess, query);
		assertNotNull(blockedLogins);
		assertEquals(1, blockedLogins.getData().size());
		assertEquals(blockedLogins.getData(), Collections.singletonList(new BlockedLogin(login, namespace)));
	}

	@Test
	public void getBlockedLoginPage_filterByNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getBlockedLoginPage_filterByNamespace");

		String login = "login1";
		String login2 = "login2";
		String namespace = "namespace";
		String namespace2 = "other";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login2), namespace2);

		BlockedLoginsPageQuery query = new BlockedLoginsPageQuery(10, 0, SortingOrder.ASCENDING, BlockedLoginsOrderColumn.NAMESPACE, Collections.singletonList(namespace));

		Paginated<BlockedLogin> blockedLogins = usersManager.getBlockedLoginsPage(sess, query);
		assertNotNull(blockedLogins);
		assertEquals(1, blockedLogins.getData().size());
		assertEquals(blockedLogins.getData(), Collections.singletonList(new BlockedLogin(login, namespace)));
	}

	@Test
	public void getBlockedLoginPage_orderByNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getBlockedLoginPage_orderByNamespace");

		String login = "login";
		String login2 = "login2";
		String namespace = "second";
		String namespace2 = "first";

		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login), namespace);
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(login2), namespace2);

		BlockedLoginsPageQuery query = new BlockedLoginsPageQuery(10, 0, SortingOrder.ASCENDING, BlockedLoginsOrderColumn.NAMESPACE);

		Paginated<BlockedLogin> blockedLogins = usersManager.getBlockedLoginsPage(sess, query);
		assertNotNull(blockedLogins);
		assertEquals(2, blockedLogins.getData().size());
		assertEquals(blockedLogins.getData().get(0), new BlockedLogin(login2, namespace2));
		assertEquals(blockedLogins.getData().get(1), new BlockedLogin(login, namespace));
	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceByExtLoginWhenExtLoginNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByExtLoginWhenExtLoginNotExists");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		usersManager.getUserExtSourceByExtLogin(sess, externalSource, "");
		// shouldn't find UserExtSource (based on valid ext source and invalid login)
	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void getUserExtSourceByExtLoginWhenExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByExtLoginWhenExtSourceNotExists");

		usersManager.getUserExtSourceByExtLogin(sess, new ExtSource(), "");

	}

	@Test
	public void getUserExtSources() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSources");

		List<UserExtSource> userExtSources = usersManager.getUserExtSources(sess, user);
		assertNotNull(userExtSources);
		assertTrue(userExtSources.size() == 2);
		// our user should have only two ext source, one we we added and the default one

	}

	@Test (expected=UserNotExistsException.class)
	public void getUserExtSourcesWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourcesWhenUserNotExists");

		usersManager.getUserExtSources(sess, new User());
		// shouldn't find user

	}

	@Test
	public void getUserExtSourceById() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceById");

		int id = userExtSource.getId();
		UserExtSource retUserExtSource = usersManager.getUserExtSourceById(sess, id);
		// get user ext source base on our user ext source ID
		assertNotNull("unable to get ext source by its ID", retUserExtSource);
		assertEquals("both user ext sources should be the same", userExtSource, retUserExtSource);

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceByIdWhenExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByIdWhenExtSourceNotExists");

		usersManager.getUserExtSourceById(sess, 0);
		// shouldn't find ext source

	}

	@Test
	public void getUserExtSourcesByIds() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourcesByIds");

		List<UserExtSource> userExtSources = usersManager.getUserExtSourcesByIds(sess, Collections.singletonList(userExtSource.getId()));
		assertEquals(userExtSources.size(), 1);
		assertTrue(userExtSources.contains(userExtSource));

		// create another ues
		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		UserExtSource anotherUes = perun.getUsersManager().addUserExtSource(sess, user, new UserExtSource(externalSource, extLogin2));
		assertNotNull(anotherUes);
		userExtSources = usersManager.getUserExtSourcesByIds(sess, Arrays.asList(userExtSource.getId(), anotherUes.getId()));
		assertEquals(userExtSources.size(), 2);
		assertTrue(userExtSources.contains(userExtSource));
		assertTrue(userExtSources.contains(anotherUes));

		userExtSources = usersManager.getUserExtSourcesByIds(sess, Collections.singletonList(anotherUes.getId()));
		assertEquals(userExtSources.size(), 1);
		assertTrue(userExtSources.contains(anotherUes));
	}

	@Test
	public void getUserExtSourceByListValue() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByListValue");

		List<String> listValue = new ArrayList<>();
		listValue.add("A-VALUE");
		listValue.add("B-VALUE");
		listValue.add("C-VALUE");

		Attribute attribute = createUserExtSourceAttribute("testAttribute", ArrayList.class.getName(), listValue, true);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		for(String value : listValue) {
			UserExtSource returnedUserExtSource = perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), value);
			assertEquals(userExtSource, returnedUserExtSource);
		}
	}

	@Test
	public void getUserExtSourceByMapValue() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByMapValue");

		Map<String, String> mapValue = new LinkedHashMap<>();
		mapValue.put("A-KEY", "A-VALUE");
		mapValue.put("B-KEY", "B-VALUE");
		mapValue.put("C-KEY", "C-VALUE");

		Attribute attribute = createUserExtSourceAttribute("testAttribute", LinkedHashMap.class.getName(), mapValue, true);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		for(String key : mapValue.keySet()) {
			String uniqueValue = key + "=" + mapValue.get(key);
			UserExtSource returnedUserExtSource = perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), uniqueValue);
			assertEquals(userExtSource, returnedUserExtSource);
		}
	}

	@Test
	public void getUserExtSourceByStringValue() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByStringValue");

		Attribute attribute = createUserExtSourceAttribute("testAttribute", String.class.getName(), "testValue", true);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		UserExtSource returnedUserExtSource = perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), attribute.valueAsString());
		assertEquals(userExtSource, returnedUserExtSource);
	}

	@Test
	public void getUserExtSourceByIntegerValue() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByIntegerValue");

		Attribute attribute = createUserExtSourceAttribute("testAttribute", Integer.class.getName(), 77, true);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		UserExtSource returnedUserExtSource = perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), attribute.valueAsInteger().toString());
		assertEquals(userExtSource, returnedUserExtSource);
	}

	@Test
	public void getUserExtSourceByBooleanValue() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByBooleanValue");

		Attribute attribute = createUserExtSourceAttribute("testAttribute", Boolean.class.getName(), true, true);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		UserExtSource returnedUserExtSource = perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), attribute.valueAsBoolean().toString());
		assertEquals(userExtSource, returnedUserExtSource);
	}

	@Test (expected=InternalErrorException.class)
	public void getUserExtSourceByNonUniqueAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUserExtSourceByNonUniqueAttribute");

		Attribute attribute = createUserExtSourceAttribute("testAttribute");
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		perun.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attribute.getId(), attribute.valueAsString());
	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSource");

		usersManager.removeUserExtSource(sess, user, userExtSource);

		usersManager.getUserExtSourceById(sess, userExtSource.getId());
		// shloudn't get removed user ext source from DB

	}

	@Test
	public void bulkRemoveUserExtSources() throws Exception {
		System.out.println(CLASS_NAME + "bulkRemoveUserExtSources");


		ExtSource newExtSource = new ExtSource("test2", ExtSourcesManager.EXTSOURCE_INTERNAL);
		ExtSource es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		// get and create real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, vo, es);

		UserExtSource ues = new UserExtSource();
		ues.setExtSource(newExtSource);
		// put real external source into user's external source
		ues.setLogin(extLogin);
		usersManager.addUserExtSource(sess, user, ues);

		assertThat(usersManager.getUserExtSources(sess, user)).contains(userExtSource, ues);

		usersManager.removeUserExtSources(sess, user, List.of(userExtSource, ues), false);

		assertThat(usersManager.getUserExtSources(sess, user)).doesNotContain(userExtSource, ues);
	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeUserExtSourceWithAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceWithAttribute");

		//Attribute 1
		String name = "testingUEAttribute1";
		Attribute userExtSourceAttribute1 = this.createUserExtSourceAttribute(name);
		userExtSourceAttribute1.setValue(name);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, userExtSourceAttribute1);
		//Attribute 1
		name = "testingUEAttribute2";
		Attribute userExtSourceAttribute2 = this.createUserExtSourceAttribute(name);
		userExtSourceAttribute2.setValue(name);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, userExtSourceAttribute2);

		usersManager.removeUserExtSource(sess, user, userExtSource);

		usersManager.getUserExtSourceById(sess, userExtSource.getId());
		// shloudn't get removed user ext source from DB
	}

	@Test
	public void moveUserExtSource() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceWithAttribute");

		//TargetUser
		User targetUser = setUpEmptyUser();

		usersManager.moveUserExtSource(sess, user, targetUser, userExtSource);

		UserExtSource returnedUserExtSource = usersManager.getUserExtSourceById(sess, userExtSource.getId());
		assertEquals("returned user extSource should be assigned to the targetUser", targetUser.getId(), returnedUserExtSource.getUserId());
	}

	@Test
	public void moveUserExtSourceWithAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceWithAttribute");

		//Attribute 1
		String name = "testingUEAttribute1";
		Attribute userExtSourceAttribute1 = this.createUserExtSourceAttribute(name);
		userExtSourceAttribute1.setValue(name);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, userExtSourceAttribute1);
		//Attribute 1
		name = "testingUEAttribute2";
		Attribute userExtSourceAttribute2 = this.createUserExtSourceAttribute(name);
		userExtSourceAttribute2.setValue(name);
		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, userExtSourceAttribute2);
		//TargetUser
		User targetUser = setUpEmptyUser();

		usersManager.moveUserExtSource(sess, user, targetUser, userExtSource);

		UserExtSource returnedUserExtSource = usersManager.getUserExtSourceById(sess, userExtSource.getId());
		assertEquals("returned user extSource should be assigned to the targetUser", targetUser.getId(), returnedUserExtSource.getUserId());
	}

	@Test (expected=UserNotExistsException.class)
	public void removeUserExtSourceWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourceWhenUserNotExist");

		usersManager.removeUserExtSource(sess, new User(), userExtSource);
		// shouldn't find user

	}

	@Test (expected=InternalErrorException.class)
	public void removeUserExtSourcePersistent() throws Exception {
		System.out.println(CLASS_NAME + "removeUserExtSourcePersistent");

		// Assuming ExtSource PERUN is persistent (set as property)
		ExtSource extSource = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "PERUN");
		List<UserExtSource> userExtSources = usersManager.getUserExtSources(sess, user);
		for (UserExtSource ues : userExtSources) {
			if (ues.getExtSource().equals(extSource)) {
				usersManager.removeUserExtSource(sess, user, ues);
				break;
			}
		}

	}

	@Test
	public void getUserByMember() throws Exception {
		System.out.println(CLASS_NAME + "getUserByMember");

		Member member = setUpMember(vo);

		User firstUser = usersManager.getUserByMember(sess, member);
		assertNotNull("unable to get user by member from DB", firstUser);
		User secondUser = usersManager.getUserById(sess,firstUser.getId());
		assertEquals("both users should be the same", firstUser, secondUser);

	}

	@Test (expected=MemberNotExistsException.class)
	public void getUserByMemberWhenMemberNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getUserByMemberWhenMemberNotExist");

		usersManager.getUserByMember(sess, new Member());
		// shouldn't find member

	}

	@ Test
	public void getVosWhereUserIsAdmin() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsAdmin");

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		perun.getVosManager().addAdmin(sess, vo, user);

		List<Vo> vos = usersManager.getVosWhereUserIsAdmin(sess, user);
		assertTrue("our user should be admin in one VO", vos.size() >= 1);

	}

	@ Test
	public void getVosWhereUserIsNotAdminButHisGroupIs() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsNotAdminButHisGroupIs");

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getVosManager().addAdmin(sess, vo, group);

		List<Vo> vos = usersManager.getVosWhereUserIsAdmin(sess, user);
		assertTrue("our user should be admin in one VO", vos.size() >= 1);

	}

	@Test (expected=UserNotExistsException.class)
	public void getVosWhereUserIsAdminWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsAdminWhenUserNotExist");

		usersManager.getVosWhereUserIsAdmin(sess, new User());
		// shouldn't find user
	}

	@ Test
	public void getGroupsWhereUserIsAdmin() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereUserIsAdmin");

		Member member = setUpMember(vo);
		User returnedUser = usersManager.getUserByMember(sess, member);

		Group group1 = setUpGroup(vo, member, "testGroup1");
		Group group2 = setUpGroup(vo, member, "testGroup2");
		Group group3 = setUpGroup(vo, member, "testGroup3");
		perun.getGroupsManager().removeAdmin(sess, group3, returnedUser);
		perun.getGroupsManager().addAdmin(sess, group3, group2);
		Group group4 = setUpGroup(vo, member, "testGroup4");
		perun.getGroupsManager().removeAdmin(sess, group4, returnedUser);

		Vo vo2 = new Vo(0, "voForTest2", "voForTest2");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);
		Member member2 = setUpMember(vo2);
		Group group5 = setUpGroup(vo2, member2, "testGroup5");

		List<Group> groups = usersManager.getGroupsWhereUserIsAdmin(sess, returnedUser);
		assertTrue("our user should be admin at least in 4 groups", groups.size() >= 4);
		assertTrue("created group1 should be between returned groups and it is not", groups.contains(group1));
		assertTrue("created group2 should be between returned groups and it is not", groups.contains(group2));
		assertTrue("created group3 should be between returned groups and it is not", groups.contains(group3));
		assertTrue("created group5 should be between returned groups and it is not", groups.contains(group5));
		assertTrue("created group4 should not be between returned groups and it is", !groups.contains(group4));
	}

	@ Test
	public void getGroupsWhereUserIsAdminWithSelectedVo() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereUserIsAdminWithSelectedVo");

		Member member = setUpMember(vo);
		User returnedUser = usersManager.getUserByMember(sess, member);

		Group group1 = setUpGroup(vo, member, "testGroup1");
		Group group2 = setUpGroup(vo, member, "testGroup2");
		Group group3 = setUpGroup(vo, member, "testGroup3");
		perun.getGroupsManager().removeAdmin(sess, group3, returnedUser);
		perun.getGroupsManager().addAdmin(sess, group3, group2);
		Group group4 = setUpGroup(vo, member, "testGroup4");
		perun.getGroupsManager().removeAdmin(sess, group4, returnedUser);

		Vo vo2 = new Vo(0, "voForTest2", "voForTest2");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);
		Member member2 = setUpMember(vo2);
		Group group5 = setUpGroup(vo2, member2, "testGroup5");

		List<Group> groups = usersManager.getGroupsWhereUserIsAdmin(sess, vo, returnedUser);
		assertTrue("our user should be admin at least in 4 groups", groups.size() >= 3);
		assertTrue("created group1 should be between returned groups and it is not", groups.contains(group1));
		assertTrue("created group2 should be between returned groups and it is not", groups.contains(group2));
		assertTrue("created group3 should be between returned groups and it is not", groups.contains(group3));
		assertTrue("created group5 should not be between returned groups and it is", !groups.contains(group5));
		assertTrue("created group4 should not be between returned groups and it is", !groups.contains(group4));
	}

	@Test (expected=UserNotExistsException.class)
	public void getGroupsWhereUserIsAdminWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereUserIsAdminWhenUserNotExist");

		usersManager.getGroupsWhereUserIsAdmin(sess, new User());
		// shouldn't find user
	}

	@ Test
	public void getVosWhereUserIsMember() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsMember");

		Member member = setUpMember(vo);

		User returnedUser = usersManager.getUserByMember(sess, member);
		List<Vo> vos = usersManager.getVosWhereUserIsMember(sess, returnedUser);
		assertTrue("our user should be member of one VO", vos.size() >= 1);

	}

	@Test (expected=UserNotExistsException.class)
	public void getVosWhereUserIsMemberWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getVosWhereUserIsMemberWhenUserNotExist");

		usersManager.getVosWhereUserIsMember(sess, new User());
		// shouldn't find user
	}

	@Test
	public void getAllowedResources() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedResources");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Owner owner = new Owner();
		owner.setName("UsersManagerTestOwner");
		owner.setContact("testingOwner");
		owner.setType(OwnerType.technical);
		perun.getOwnersManager().createOwner(sess, owner);
		perun.getFacilitiesManager().addOwner(sess, facility, owner);

		Resource resource = new Resource();
		resource.setName("UsersManagerTestResource");
		resource.setDescription("Testovaci");
		resource = perun.getResourcesManager().createResource(sess, resource, vo, facility);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);
		// create resource, assign group with our member

		User user = usersManager.getUserByMember(sess, member);
		// get user from member with assigned resource
		List<Resource> resources = usersManager.getAllowedResources(sess, facility, user);
		assertTrue("our user should have allowed resource", resources.size() >= 1);
		assertTrue("created resource should be allowed",resources.contains(resource));

	}


    @Test
    public void getUserAssignments() throws Exception {
      System.out.println(CLASS_NAME + "getUserAssignments");

      Member member = setUpMember(vo);
      User user = usersManager.getUserByMember(sess, member);
      Group group = setUpGroup(vo, member);

      Facility facility = setUpFacility();
      Resource resource = setUpResource(facility, vo);
      Resource resource2 = setUpResource(facility, vo);

      perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);
      perun.getResourcesManager().assignGroupToResource(sess, group, resource2, false, false, false);

      Map<Facility, List<Resource>> res = perun.getUsersManager().getUserAssignments(sess, user);
      assertThat(res.keySet()).containsExactly(facility);
      assertThat(res.get(facility)).containsOnly(resource2, resource);
    }

	@Test
	public void getAssociatedResources() throws Exception {
		System.out.println(CLASS_NAME + "getAssociatedResources");

		Member member = setUpMember(vo);
		User user = usersManager.getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);

		Facility facility = setUpFacility();
		Resource resource = setUpResource(facility, vo);

		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, true, false);

		List<Resource> resources = perun.getUsersManagerBl().getAssociatedResources(sess, user);
		assertThat(resources).containsExactly(resource);

	}

	@Test
	public void getAssociatedResourcesForFacility() throws Exception {
		System.out.println(CLASS_NAME + "getAssociatedResourcesForFacility");

		Member member = setUpMember(vo);
		User user = usersManager.getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);

		Facility facility = setUpFacility();
		Resource resource = setUpResource(facility, vo);

		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, true, false);

		List<Resource> resources = perun.getUsersManagerBl().getAssociatedResources(sess, facility, user);
		assertThat(resources).containsExactly(resource);

	}

	@Test
	public void findUsers() throws Exception {
		System.out.println(CLASS_NAME + "findUsers");

		// Create second user
		User user2 = new User();
		// Different first name from the default user in the test, contains a space
		user2.setFirstName(new StringBuilder(userFirstName).append('2').insert(userFirstName.length() / 2, ' ').toString());
		user2.setMiddleName("");
		user2.setLastName(userLastName);
		user2.setTitleBefore("");
		user2.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user2));
		// create new user in database
		usersForDeletion.add(user2);
		// save user for deletion after testing

		List<User> users = usersManager.findUsers(sess, userFirstName + " " + userLastName);
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		// And must contain the user
		assertTrue("results must contain user", users.contains(user));

		users = usersManager.findUsers(sess, userLastName);
		// This search must contain at least two results
		assertTrue("results must contain at least two users", users.size() >= 2);
		assertTrue("results must contain user and user2", users.contains(user) && users.contains(user2));

		users = usersManager.findUsers(sess, userLastName + " " + userFirstName);
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		assertTrue("results must contain user", users.contains(user));

		// Search with a space in first name
		users = usersManager.findUsers(sess, user2.getFirstName());
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		assertTrue("results must contain user2", users.contains(user2));
	}

	@Test
	public void findUsersByNameFullText() throws Exception {
		System.out.println(CLASS_NAME + "findUsersByNameFullText");

		// Create second user
		User user2 = new User();
		user2.setFirstName(userFirstName);
		user2.setMiddleName("");
		user2.setLastName(userLastName+"2"); // Different last name from the default user in the test
		user2.setTitleBefore("");
		user2.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user2));
		// create new user in database
		usersForDeletion.add(user2);
		// save user for deletion after testing

		List<User> users = usersManager.findUsersByName(sess, userFirstName + " " + userLastName);
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		// And must contain the user
		assertTrue("results must contain user", users.contains(user));

		users = usersManager.findUsersByName(sess, userFirstName);
		// This search must contain at least two results
		assertTrue("results must contain at least two users", users.size() >= 2);
		assertTrue("results must contain user and user2", users.contains(user) && users.contains(user2));
	}

	@Test
	public void findUsersByNameUsingExactFields() throws Exception {
		System.out.println(CLASS_NAME + "findUsersByNameUsingExactFields");

		// Create second user
		User user2 = new User();
		user2.setFirstName(userFirstName);
		user2.setMiddleName("");
		user2.setLastName(userLastName+"2"); // Different last name from the default user in the test
		user2.setTitleBefore("");
		user2.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user2));
		// create new user in database
		usersForDeletion.add(user2);
		// save user for deletion after testing

		List<User> users = usersManager.findUsersByName(sess, "", userFirstName, "", userLastName, "");
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		// And must contain the user
		assertTrue("results must contain user", users.contains(user));

		users = usersManager.findUsersByName(sess, "", userFirstName, "", "", "");
		// This search must contain at least two results
		assertTrue("results must contain at least two users", users.size() >= 2);
		assertTrue("results must contain user and user2", users.contains(user) && users.contains(user2));
	}

	@Test
	public void getUsersByAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttribute");

		// Check if the attribute already exists
		Attribute attr;
		AttributeDefinition attrDef;
		try {
			attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:user:attribute-def:opt:user_test_attribute");
		} catch (AttributeNotExistsException e) {
			// Attribute doesn't exist, so create it
			attrDef = new AttributeDefinition();
			attrDef.setNamespace("urn:perun:user:attribute-def:opt");
			attrDef.setFriendlyName("user-test-attribute");
			attrDef.setType(String.class.getName());

			attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		}

		attr = new Attribute(attrDef);
		attr.setValue("UserAttribute");

		// Set the attribute to the user
		perun.getAttributesManagerBl().setAttribute(sess, user, attr);

		assertTrue("results must contain user", usersManager.getUsersByAttribute(sess, attr).contains(user));
	}

	@Test
	public void getUsersByAttributeFilter() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeFilter");

		Attribute attr = setUpAttribute();
		attr.setValue("value");

		User user1 = setUpUser("User1", "Test");
		User user2 = setUpUser("User2", "Test");
		perun.getMembersManagerBl().createMember(sess, vo, user2);

		perun.getAttributesManagerBl().setAttribute(sess, user1, attr);
		perun.getAttributesManagerBl().setAttribute(sess, user2, attr);

		sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.VOADMIN, vo));

		List<User> result = usersManager.getUsersByAttribute(sess, attr);

		assertTrue(result.contains(user2));
		assertFalse(result.contains(user1));
	}

	@Test
	public void getUsersByAttributeWithValueFilter() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeValueFilter");

		User user1 = setUpUser("User1", "Test");
		User user2 = setUpUser("User2", "Test");
		User user3 = setUpUser("User3", "Test");
		perun.getMembersManagerBl().createMember(sess, vo, user2);
		perun.getMembersManagerBl().createMember(sess, vo, user3);

		Attribute attr = setUpAttribute();
		attr.setValue("value1");
		perun.getAttributesManagerBl().setAttribute(sess, user1, attr);
		perun.getAttributesManagerBl().setAttribute(sess, user2, attr);
		attr.setValue("value2");
		perun.getAttributesManagerBl().setAttribute(sess, user3, attr);

		sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.VOADMIN, vo));

		List<User> result = usersManager.getUsersByAttribute(sess, attr.getName(), "value1");

		assertEquals(1, result.size());
		assertTrue(result.contains(user2));
	}

	@Test
	public void findUsersByExactName() throws Exception {
		System.out.println(CLASS_NAME + "findUsersByExactName");

		String searchString = user.getFirstName()+user.getLastName();
		List<User> users = perun.getUsersManager().findUsersByExactName(sess, searchString);
		assertTrue("No users found for exact match!", !users.isEmpty());
		assertTrue("Test user not found in results!", users.contains(user));

		// we shouldn't find anybody using substring
		searchString = searchString.substring(0, searchString.length()-3);
		users = perun.getUsersManager().findUsersByExactName(sess, searchString);
		assertTrue("Test user found in results when shouldn't!", !users.contains(user));
		assertTrue("Some user found using substring when we shouldn't find anybody!", users.isEmpty());

	}

	@Test
	public void findRichUsersWithAttributesByExactMatch() throws Exception {
		System.out.println(CLASS_NAME + "findRichUsersWithAttributesByExactMatch");

		// Create second user
		User user2 = new User();
		// Different first name from the default user in the test, contains a space
		user2.setFirstName(new StringBuilder(userFirstName).append('2').insert(userFirstName.length() / 2, ' ').toString());
		user2.setMiddleName("");
		user2.setLastName(userLastName);
		user2.setTitleBefore("");
		user2.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user2));
		// create new user in database
		usersForDeletion.add(user2);
		// save user for deletion after testing

		ArrayList<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:user:attribute-def:def:preferredMail");

		String searchString = user.getFirstName() + " " + user.getLastName();
		List<RichUser> users = perun.getUsersManager().findRichUsersWithAttributesByExactMatch(sess, searchString, attrNames);
		assertTrue("No users found for exact match!", !users.isEmpty());

		searchString = user2.getFirstName() + " " + user2.getLastName();
		users = perun.getUsersManager().findRichUsersWithAttributesByExactMatch(sess, searchString, attrNames);
		assertTrue("Results must contain user2!", users.contains(user2));
		assertTrue("Results can't contain user!", !users.contains(user));
	}

	@Test
	public void getUsersCount() throws Exception {
		System.out.println(CLASS_NAME + "getUsersCount");

		setUpUser();
		int count = perun.getUsersManager().getUsersCount(sess);
		assertTrue(count>0);
	}

	@Test
	public void getUsersByIds() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByIds");

		List<Integer> ids = new ArrayList<>();
		Set<User> users = new HashSet<>();

		for (int i = 1; i < 1002; i++) {
			User user2 = new User();
			user2.setFirstName(userFirstName+i);
			perun.getUsersManagerBl().createUser(sess, user2);
			ids.add(user2.getId());
			users.add(user2);
		}

		assertEquals(users, new HashSet<>(perun.getUsersManager().getUsersByIds(sess, ids)));
	}

	@Test
	public void getRichUsersByIds() throws Exception {
		System.out.println(CLASS_NAME + "getRichUsersByIds");

		RichUser richUser = new RichUser(user, perun.getUsersManager().getUserExtSources(sess, user));
		List<RichUser> richUsers = perun.getUsersManager().getRichUsersByIds(sess, Collections.singletonList(user.getId()));
		assertThat(richUsers).containsExactlyInAnyOrder(richUser);
		assertThat(richUsers.get(0).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser.getUserExtSources());

		User user2 = new User();
		user2.setFirstName(userFirstName + "2");
		user2 = perun.getUsersManagerBl().createUser(sess, user2);
		RichUser richUser2 = new RichUser(user2, perun.getUsersManager().getUserExtSources(sess, user2));

		richUsers = perun.getUsersManager().getRichUsersByIds(sess, Arrays.asList(user.getId(), user2.getId()));
		assertThat(richUsers).containsExactlyInAnyOrder(richUser, richUser2);
		assertThat(richUsers.get(richUsers.indexOf(richUser)).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser.getUserExtSources());
		assertThat(richUsers.get(richUsers.indexOf(richUser2)).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser2.getUserExtSources());
	}

	@Test
	public void getRichUsersWithAttributesByIds() throws Exception {
		System.out.println(CLASS_NAME + "getRichUsersWithAttributesByIds");

		RichUser richUser = new RichUser(user, perun.getUsersManager().getUserExtSources(sess, user), perun.getAttributesManager().getAttributes(sess, user));
		List<RichUser> richUsers = perun.getUsersManager().getRichUsersByIds(sess, Collections.singletonList(user.getId()));
		assertThat(richUsers).containsExactlyInAnyOrder(richUser);
		assertThat(richUsers.get(0).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser.getUserExtSources());

		User user2 = new User();
		user2.setFirstName(userFirstName + "2");
		user2 = perun.getUsersManagerBl().createUser(sess, user2);
		RichUser richUser2 = new RichUser(user2, perun.getUsersManager().getUserExtSources(sess, user2), perun.getAttributesManager().getAttributes(sess, user2));

		richUsers = perun.getUsersManager().getRichUsersWithAttributesByIds(sess, Arrays.asList(user.getId(), user2.getId()));
		assertThat(richUsers).containsExactlyInAnyOrder(richUser, richUser2);
		assertThat(richUsers.get(richUsers.indexOf(richUser)).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser.getUserExtSources());
		assertThat(richUsers.get(richUsers.indexOf(richUser2)).getUserExtSources()).containsExactlyInAnyOrderElementsOf(richUser2.getUserExtSources());
		assertThat(richUsers.get(richUsers.indexOf(user)).getUserAttributes()).containsExactlyInAnyOrderElementsOf(richUser.getUserAttributes());
		assertThat(richUsers.get(richUsers.indexOf(user2)).getUserAttributes()).containsExactlyInAnyOrderElementsOf(richUser2.getUserAttributes());
	}

	@Test
	public void getGroupsWhereUserIsActive() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereUserIsActive(resource/facility)");

		Member member = setUpMember(vo);
		User u = perun.getUsersManager().getUserByMember(sess, member);

		Facility f = new Facility(0, "name", "description");
		f = perun.getFacilitiesManager().createFacility(sess, f);

		Resource r = new Resource(0, "name", "description", f.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, f);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);

		// more groups case

		List<Group> groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have only one group", groups.size() == 1);
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have only one group", groups.size() == 1);

		perun.getMembersManager().setStatus(sess, member, Status.EXPIRED);
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have no groups, since member should be VO expired", groups.isEmpty());
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have no groups, since member should be VO expired", groups.isEmpty());

		perun.getMembersManager().setStatus(sess, member, Status.VALID);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have 2 groups", groups.size() == 2);
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have 2 groups", groups.size() == 2);

		perun.getGroupsManager().setMemberGroupStatus(sess, member, g1, MemberGroupStatus.EXPIRED);

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have 1 group since in one should be expired", groups.size() == 1);
		assertTrue("Should be a G1 group.", groups.contains(g2));
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have 1 group since in one should be expired", groups.size() == 1);
		assertTrue("Should be a G1 group.", groups.contains(g2));

		// more resources case

		Resource r2 = new Resource(0, "name2", "description2", f.getId());
		r2 = perun.getResourcesManager().createResource(sess, r2, vo, f);

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have 1 group since in one should be expired", groups.size() == 1);
		assertTrue("Should be a G1 group.", groups.contains(g2));
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have 1 group since in one should be expired", groups.size() == 1);
		assertTrue("Should be a G1 group.", groups.contains(g2));
		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r2, u);
		assertTrue("Should be empty since there are no groups on R2 resource.", groups.size() == 0);

		perun.getResourcesManager().removeGroupFromResource(sess, g2, r);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r2, false, false, false);

		perun.getGroupsManager().setMemberGroupStatus(sess, member, g1, MemberGroupStatus.VALID);

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, f, u);
		assertTrue("Should have 2 groups", groups.size() == 2);

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r, u);
		assertTrue("Should have 1 group on R1", groups.size() == 1);
		assertTrue("Should be a G1 group.", groups.contains(g1));

		groups = perun.getUsersManager().getGroupsWhereUserIsActive(sess, r2, u);
		assertTrue("Should have 1 group on R2", groups.size() == 1);
		assertTrue("Should be a G2 group.", groups.contains(g2));

	}

	@Test
	public void convertAttributesToJSON() {
		System.out.println(CLASS_NAME + "convertAttributesToJSON");

		Candidate candidate = new Candidate(user, userExtSource);
		candidate.setAttributes(Collections.singletonMap(perun.getAttributesManager().NS_USER_ATTR + ":attribute", "value"));

		JSONObject jsonObject = candidate.convertAttributesToJson();

		assertEquals(8, jsonObject.length());
		assertEquals("value", jsonObject.getJSONArray(perun.getAttributesManager().NS_USER_ATTR + ":attribute").getString(0));
		assertEquals(userFirstName, jsonObject.getJSONArray(perun.getAttributesManager().NS_USER_ATTR_CORE + ":firstName").getString(0));
	}

	@Test
	public void convertAttributesWithNullToJSON() {
		System.out.println(CLASS_NAME + "convertAttributesWithNullToJSON");

		Candidate candidate = new Candidate(user, userExtSource);
		candidate.setAttributes(Collections.singletonMap(perun.getAttributesManager().NS_USER_ATTR + ":attribute", null));

		JSONObject jsonObject = candidate.convertAttributesToJson();

		assertEquals(8, jsonObject.length());
		assertTrue(jsonObject.getJSONArray(perun.getAttributesManager().NS_USER_ATTR + ":attribute").isNull(0));
	}

	@Test
	public void getRichUserExtSourcesReturnsCorrectAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getRichUserExtSourcesReturnsCorrectAttributes");

		testGetRichUserExtSourceAttributes(
			() -> perun.getUsersManager().getRichUserExtSources(sess, user, Collections.singletonList(URN_ATTR_UES_CN)),
			(rues) -> {
				assertThat(rues).isNotNull();
				assertThat(rues.getAttributes())
					.anySatisfy(a -> assertThat(a.getFriendlyName()).isEqualTo(ATTR_UES_CN));
			},
			ATTR_UES_CN
		);
	}

	@Test
	public void getRichUserExtSourcesDoesNotReturnNotSpecifiedAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getRichUserExtSourcesDoesNotReturnNotSpecifiedAttribute");

		testGetRichUserExtSourceAttributes(
			() -> perun.getUsersManager().getRichUserExtSources(sess, user, Collections.singletonList(URN_ATTR_UES_O)),
			(rues) -> {
				assertThat(rues).isNotNull();
				assertThat(rues.getAttributes())
					.noneSatisfy(a -> assertThat(a.getFriendlyName()).isEqualTo(ATTR_UES_CN));
			},
			ATTR_UES_O
		);
	}

	@Test
	public void getRichUserExtSourcesReturnsAllAttributesForNull() throws Exception {
		System.out.println(CLASS_NAME + "getRichUserExtSourcesReturnsAllAttributesForNull");

		testGetRichUserExtSourceAttributes(
			() -> perun.getUsersManagerBl().getRichUserExtSources(sess, user, null),
			(rues) -> {
				assertThat(rues).isNotNull();
				assertThat(rues.getAttributes()).isNotEmpty();
			},
			ATTR_UES_O
		);
	}

	@Test
	public void getRichUserExtSourcesReturnsNoAttributesForEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getRichUserExtSourcesReturnsNoAttributesForEmpty");

		testGetRichUserExtSourceAttributes(
			() -> perun.getUsersManagerBl().getRichUserExtSources(sess, user, Collections.emptyList()),
			(rues) -> {
				assertThat(rues).isNotNull();
				assertThat(rues.getAttributes()).isEmpty();
			},
			ATTR_UES_O
		);
	}

	@Test
	public void findUserById() {
		System.out.println(CLASS_NAME + "findUserById");

		List<User> users = perun.getUsersManagerBl().findUsers(sess, String.valueOf(user.getId()));
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));
	}

	@Test
	public void findUserByNames() {
		System.out.println(CLASS_NAME + "findUserByNames");

		List<User> users = perun.getUsersManagerBl().findUsers(sess, user.getFirstName());
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));

		users = perun.getUsersManagerBl().findUsers(sess, user.getLastName());
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));
	}

	@Test
	public void findUserByUuid() {
		System.out.println(CLASS_NAME + "findUserByUuid");

		List<User> users = perun.getUsersManagerBl().findUsers(sess, user.getUuid().toString());
		assertThat(users).containsExactly(user);
	}

	@Test
	public void findUserByMemberAttribute() throws Exception {
		System.out.println(CLASS_NAME + "findUserByMemberAttribute");

		Member member = setUpMember(vo);

		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		// add member attribute to CoreConfig
		List<String> attributes = BeansUtils.getCoreConfig().getAttributesToSearchUsersAndMembersBy();
		attributes.add("urn:perun:member:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace("urn:perun:member:attribute-def:def");
		attrDef.setFriendlyName("test");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("login");
		perun.getAttributesManagerBl().setAttribute(sess, member, attribute);

		List<User> users = perun.getUsersManagerBl().findUsers(sess, "login");
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));

		// reset CoreConfig to previous state
		attributes.remove("urn:perun:member:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);
	}

	@Test
	public void findUserByUserAttribute() throws Exception {
		System.out.println(CLASS_NAME + "findUserByUserAttribute");

		// add user attribute to CoreConfig
		List<String> attributes = BeansUtils.getCoreConfig().getAttributesToSearchUsersAndMembersBy();
		attributes.add("urn:perun:user:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace("urn:perun:user:attribute-def:def");
		attrDef.setFriendlyName("test");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("login");

		perun.getAttributesManagerBl().setAttribute(sess, user, attribute);

		List<User> users = perun.getUsersManagerBl().findUsers(sess, "login");
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));

		// reset CoreConfig to previous state
		attributes.remove("urn:perun:user:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);
	}

	@Test
	public void findUserByUserExtSourceAttribute() throws Exception {
		System.out.println(CLASS_NAME + "findUserByUserExtSourceAttribute");

		// add userExtSource attribute to CoreConfig
		List<String> attributes = BeansUtils.getCoreConfig().getAttributesToSearchUsersAndMembersBy();
		attributes.add("urn:perun:ues:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace("urn:perun:ues:attribute-def:def");
		attrDef.setFriendlyName("test");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("login");

		perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);

		List<User> users = perun.getUsersManagerBl().findUsers(sess, "login");
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));

		// reset CoreConfig to previous state
		attributes.remove("urn:perun:ues:attribute-def:def:test");
		BeansUtils.getCoreConfig().setAttributesToSearchUsersAndMembersBy(attributes);
	}

	@Test
	public void findUserByUserExtSourceLogin() {
		System.out.println(CLASS_NAME + "findUserByUserExtSourceLogin");

		List<User> users = perun.getUsersManagerBl().findUsers(sess, extLogin);
		assertEquals(1, users.size());
		assertEquals(user, users.get(0));
	}

	@Test
	public void testCreateServiceUser() throws Exception {
		System.out.println(CLASS_NAME + "testCreateServiceUser");

		Candidate candidate = setUpCandidateForSpecificUser1();

		User createdUser = usersManager.createServiceUser(sess, candidate, Collections.emptyList());

		createdUser = usersManager.getUserById(sess, createdUser.getId());

		assertThat(createdUser).isEqualToComparingOnlyGivenFields(candidate, "firstName", "lastName");
		assertThat(createdUser.isServiceUser());
	}

	@Test
	public void testCreateServiceUserSetsAttributes() throws Exception {
		System.out.println(CLASS_NAME + "testCreateServiceUserSetsAttributes");

		Candidate candidate = setUpCandidateForSpecificUser1();
		Map<String, String> attrs = new HashMap<>();
		String value = "asdf@sdf.df";
		attrs.put(URN_ATTR_USER_PREFERRED_MAIL, value);
		candidate.setAttributes(attrs);

		User createdUser = usersManager.createServiceUser(sess, candidate, Collections.emptyList());

		Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, createdUser, URN_ATTR_USER_PREFERRED_MAIL);

		assertThat(attr.getValue()).isEqualTo(value);
	}

	@Test
	public void testCreateServiceUserSetsUes() throws Exception {
		System.out.println(CLASS_NAME + "testCreateServiceUserSetsUes");

		Candidate candidate = setUpCandidateForSpecificUser1();

		User createdUser = usersManager.createServiceUser(sess, candidate, Collections.emptyList());

		UserExtSource candidateUes = candidate.getUserExtSource();
		User userByUes = usersManager.getUserByExtSourceNameAndExtLogin(sess, candidateUes.getExtSource().getName(),
				candidateUes.getLogin());

		assertThat(createdUser).isEqualByComparingTo(userByUes);
	}

	@Test
	public void testCreateServiceUserFailsForAlreadyExistingUes() throws Exception {
		System.out.println(CLASS_NAME + "testCreateServiceUserFailsForAlreadyExistingUes");

		Candidate candidate = setUpCandidateForSpecificUser1();

		usersManager.createServiceUser(sess, candidate, Collections.emptyList());

		assertThatExceptionOfType(UserExtSourceExistsException.class)
				.isThrownBy(() -> usersManager.createServiceUser(sess, candidate, Collections.emptyList()));
	}

	@Test
	public void getSponsors() throws Exception {
		System.out.println(CLASS_NAME + "getSponsors");

		setUpNamespaceAttribute();

		String email = "email@sdf.sd";

		AttributeDefinition emailAD = perun.getAttributesManagerBl()
				.getAttributeDefinition(sess, URN_ATTR_USER_PREFERRED_MAIL);
		Attribute emailAttribute = new Attribute(emailAD);
		emailAttribute.setValue(email);

		perun.getAttributesManagerBl().setAttribute(sess, user, emailAttribute);

		Member member = perun.getMembersManagerBl().getMemberByUser(sess, vo, sponsoredUser);

		LocalDate validity = LocalDate.now();
		perun.getMembersManagerBl().updateSponsorshipValidity(sess, member, user, validity);
		Member sponsoredMember = perun.getMembersManagerBl().getMemberByUser(sess, vo, sponsoredUser);
		List<Sponsor> sponsors = usersManager
				.getSponsorsForMember(sess, sponsoredMember, Collections.singletonList(URN_ATTR_USER_PREFERRED_MAIL));

		assertThat(sponsors)
				.hasSize(1);

		assertThat(sponsors.get(0).getUser())
				.isEqualTo(user);

		assertThat(sponsors.get(0).getValidityTo())
				.isEqualTo(validity);

		assertThat(sponsors.get(0).getUserAttributes())
				.hasSize(1);

		assertThat(sponsors.get(0).getUserAttributes().get(0).getName())
				.isEqualTo(URN_ATTR_USER_PREFERRED_MAIL);

		assertThat(sponsors.get(0).getUserAttributes().get(0).valueAsString())
				.isEqualTo(email);
	}

	@Test
	public void getUsersPage_all() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_all");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		UsersPageQuery query = new UsersPageQuery(10, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertTrue(users.getData().size() > 1);
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_searchString() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_searchString");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "jane");

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_orderByName() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_orderByName");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		UsersPageQuery query = new UsersPageQuery(10, 0, SortingOrder.ASCENDING, UsersOrderColumn.NAME, "smith");

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(users.getData().get(0), usersManager.getRichUser(sess, user2));
		assertEquals(users.getData().get(1), usersManager.getRichUser(sess, user));
	}

	@Test
	public void getUsersPage_withoutVo() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_withoutVo");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		UsersPageQuery query = new UsersPageQuery(10, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertTrue(users.getData().size() > 1);
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_withAttributes() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_withAttributes");

		User user = setUpUser("john", "smith");

		AttributeDefinition prefMailAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, URN_ATTR_USER_PREFERRED_MAIL);
		Attribute prefMail = new Attribute(prefMailAttrDef);
		prefMail.setValue("mail@mail.com");

		perun.getAttributesManagerBl().setAttribute(sess, user, prefMail);

		UsersPageQuery query = new UsersPageQuery(1, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "smith");

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of(URN_ATTR_USER_PREFERRED_MAIL));
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user)));
		assertThat(users.getData().get(0).getUserAttributes()).containsOnly(prefMail);
	}

	@Test
	public void getUsersPage_userHasMembersInMultipleVos() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_userHasMembersInMultipleVos");

		User user = setUpUser("jane", "smith");

		Vo newVo = new Vo(1, "UserManagerTestV1o", "UMTestVo1");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		Member member = perun.getMembersManagerBl().createMember(sess, returnedVo, user);

		newVo = new Vo(2, "UserManagerTestV2o", "UMTestVo2");
		returnedVo = perun.getVosManager().createVo(sess, newVo);
		member = perun.getMembersManagerBl().createMember(sess, returnedVo, user);

		newVo = new Vo(3, "UserManagerTestV3o", "UMTestVo3");
		returnedVo = perun.getVosManager().createVo(sess, newVo);
		member = perun.getMembersManagerBl().createMember(sess, returnedVo, user);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "jane");

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user)));
	}

	@Test
	public void getUsersPage_facilitySearchString() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilitySearchString");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "jane", facility.getId());

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_facility() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facility");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId());

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_facilityOnlyAllowed() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityOnlyAllowed");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
		perun.getMembersManagerBl().setStatus(sess, member2, Status.VALID);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_facilityVo() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityVo");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), vo.getId(), null, null);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_facilityVoOnlyAllowed() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityVoOnlyAllowed");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
		perun.getMembersManagerBl().setStatus(sess, member2, Status.VALID);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), vo.getId(), null, null, true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_facilityResource() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityResource");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId());

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_facilityResourceOnlyAllowed() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityResourceOnlyAllowed");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
		perun.getMembersManagerBl().setStatus(sess, member2, Status.VALID);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId(), true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_facilityResourceConsent() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityResourceConsent");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManagerBl().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		Consent consent2 = new Consent(-11, user2.getId(), perun.getConsentsManagerBl().getConsentHubByName(sess, facility.getName()), new ArrayList<>());

		perun.getConsentsManagerBl().createConsent(sess, consent1);
		perun.getConsentsManagerBl().createConsent(sess, consent2);


		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId(), false, List.of(ConsentStatus.UNSIGNED));

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));

		perun.getConsentsManagerBl().changeConsentStatus(sess, consent1, ConsentStatus.GRANTED);
		
		query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId(), false, List.of(ConsentStatus.GRANTED));

		users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user)));

		query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId(), false, List.of(ConsentStatus.GRANTED,  ConsentStatus.UNSIGNED));

		users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));

		query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, null, r.getId(), false, List.of(ConsentStatus.REVOKED));

		users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(0, users.getData().size());
		assertEquals(0, users.getTotalCount());


	}

	@Test
	public void getUsersPage_facilityVoService() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityVoService");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Service service = new Service(0, "dummy_service");
		service = perun.getServicesManagerBl().createService(sess, service);

		perun.getResourcesManagerBl().assignService(sess, r, service);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), vo.getId(), service.getId(), null);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_facilityVoServiceOnlyAllowed() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityVoServiceOnlyAllowed");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Service service = new Service(0, "dummy_service");
		service = perun.getServicesManagerBl().createService(sess, service);

		perun.getResourcesManagerBl().assignService(sess, r, service);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, vo, user2);

		perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
		perun.getMembersManagerBl().setStatus(sess, member2, Status.VALID);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(vo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), vo.getId(), service.getId(), null, true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersPage_facilityService() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityService");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Vo newVo = new Vo(2, "UserManagerTestV2o", "UMTestVo2");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);

		Resource r2 = new Resource(1, "name1", "description1", facility.getId());
		r2 = perun.getResourcesManager().createResource(sess, r2, returnedVo, facility);

		Service service = new Service(0, "dummy_service");
		service = perun.getServicesManagerBl().createService(sess, service);

		perun.getResourcesManagerBl().assignService(sess, r, service);
		perun.getResourcesManagerBl().assignService(sess, r2, service);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, returnedVo, user2);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(returnedVo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r2, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, service.getId(), null);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(2, users.getData().size());
		assertEquals(2, users.getTotalCount());
		assertTrue(users.getData().containsAll(usersManager.getRichUsersByIds(sess, List.of(user.getId(), user2.getId()))));
	}

	@Test
	public void getUsersPage_facilityServiceOnlyAllowed() throws Exception {
		System.out.println(CLASS_NAME + "getUsersPage_facilityServiceOnlyAllowed");

		User user = setUpUser("john", "smith");
		User user2 = setUpUser("jane", "smith");

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource r = new Resource(0, "name", "description", facility.getId());
		r = perun.getResourcesManager().createResource(sess, r, vo, facility);

		Vo newVo = new Vo(2, "UserManagerTestV2o", "UMTestVo2");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);

		Resource r2 = new Resource(1, "name1", "description1", facility.getId());
		r2 = perun.getResourcesManager().createResource(sess, r2, returnedVo, facility);

		Service service = new Service(0, "dummy_service");
		service = perun.getServicesManagerBl().createService(sess, service);

		perun.getResourcesManagerBl().assignService(sess, r, service);
		perun.getResourcesManagerBl().assignService(sess, r2, service);

		Member member = perun.getMembersManagerBl().createMember(sess, vo, user);
		Member member2 = perun.getMembersManagerBl().createMember(sess, returnedVo, user2);

		perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
		perun.getMembersManagerBl().setStatus(sess, member2, Status.VALID);

		Group g1 = setUpGroup(vo, member, "group1");
		Group g2 = setUpGroup(returnedVo, member2, "group2");

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false, false, false);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r2, false, false, false);

		UsersPageQuery query = new UsersPageQuery(3, 0, SortingOrder.ASCENDING, UsersOrderColumn.ID, "", facility.getId(), null, service.getId(), null, true);

		Paginated<RichUser> users = usersManager.getUsersPage(sess, query, List.of());
		assertNotNull(users);
		assertEquals(1, users.getData().size());
		assertEquals(1, users.getTotalCount());
		assertTrue(users.getData().contains(usersManager.getRichUser(sess, user2)));
	}

	@Test
	public void getUsersByAttributeValue_string() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeValue_string");

		User user = setUpUser("john", "smith");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:user:attribute-def:def");
		attr.setFriendlyName("getUsersByAttributeValueTest");
		attr.setType(String.class.getName());
		attr.setDisplayName("getUsersByAttributeValueTest");
		attr.setDescription("getUsersByAttributeValueTest");

		AttributeDefinition attrDef = perun.getAttributesManager().createAttribute(sess, attr);
		Attribute attribute = new Attribute(attrDef, "element1");
		perun.getAttributesManagerBl().setAttribute(sess, user, attribute);

		String attributeName = attr.getNamespace() + ":" + attr.getFriendlyName();

		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element1"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element12"))
			.isEmpty();

		perun.getAttributesManagerBl().setAttribute(sess, user, new Attribute(attrDef, "value@1_with,wei/rd:chars"));
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "value@1_with,wei/rd:chars"))
			.containsExactly(user);
	}

	@Test
	public void getUsersByAttributeValueFilter() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeValueFilter");

		User user1 = setUpUser("User1", "Test");
		User user2 = setUpUser("User2", "Test");
		User user3 = setUpUser("User3", "Test");
		perun.getMembersManagerBl().createMember(sess, vo, user2);
		perun.getMembersManagerBl().createMember(sess, vo, user3);

		Attribute attr = setUpAttribute();
		attr.setValue("value1");
		perun.getAttributesManagerBl().setAttribute(sess, user1, attr);
		perun.getAttributesManagerBl().setAttribute(sess, user2, attr);
		attr.setValue("value2");
		perun.getAttributesManagerBl().setAttribute(sess, user3, attr);

		sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.VOADMIN, vo));

		List<User> result = usersManager.getUsersByAttributeValue(sess, attr.getName(), "value1");

		assertEquals(1, result.size());
		assertTrue(result.contains(user2));
	}

	@Test
	public void getUsersByAttributeValue_list() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeValue_list");

		User user = setUpUser("john", "smith");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:user:attribute-def:def");
		attr.setFriendlyName("getUsersByAttributeValueTest");
		attr.setType(ArrayList.class.getName());
		attr.setDisplayName("getUsersByAttributeValueTest");
		attr.setDescription("getUsersByAttributeValueTest");

		AttributeDefinition attrDef = perun.getAttributesManager().createAttribute(sess, attr);
		Attribute attribute = new Attribute(attrDef, new ArrayList<>(List.of("element1", "ah,oj,", "middle@element", "value@1_with/weird:char,s", "lašt.eĺement")));
		perun.getAttributesManagerBl().setAttribute(sess, user, attribute);

		String attributeName = attr.getNamespace() + ":" + attr.getFriendlyName();

		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element1"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "middle@element"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "lašt.eĺement"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "ah,oj,"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "value@1_with/weird:char,s"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "element12"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "mymiddle@element"))
			.isEmpty();
		// substrings between commas shouldn't get matched either
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "ah"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "oj"))
			.isEmpty();
	}

	@Test
	public void getUsersByAttributeValue_map() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByAttributeValue_map");

		User user = setUpUser("john", "smith");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:user:attribute-def:def");
		attr.setFriendlyName("getUsersByAttributeValueTest");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDisplayName("getUsersByAttributeValueTest");
		attr.setDescription("getUsersByAttributeValueTest");

		AttributeDefinition attrDef = perun.getAttributesManager().createAttribute(sess, attr);
		Attribute attribute = new Attribute(attrDef, new LinkedHashMap<>(Map.of("key1", "value@1_with,wei/rd:chars", "key@2", "last val:ue, with ň")));
		perun.getAttributesManagerBl().setAttribute(sess, user, attribute);

		String attributeName = attr.getNamespace() + ":" + attr.getFriendlyName();

		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "key1"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "value@1_with,wei/rd:chars"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "key@2"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "last val:ue, with ň"))
			.containsExactly(user);
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "key"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "mykey1"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "key@21"))
			.isEmpty();
		// substrings between comma and colon or colon and comma shouldn't get matched either
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "wei/rd"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "ue"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "ue,"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, ":ue,"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "chars"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, ":chars"))
			.isEmpty();
		assertThat(perun.getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, "chars,"))
			.isEmpty();
	}

	@Test(expected = LoginIsAlreadyBlockedException.class)
	public void testCheckBlockedLoginDefault() throws Exception {
		System.out.println("testCheckBlockedLoginDefault");
		perun.getUsersManagerBl().checkBlockedLogins(sess, "admin-meta", defaultBlockedLogin, false);
	}

	@Test(expected = LoginIsAlreadyBlockedException.class)
	public void testCheckBlockedLoginGlobalCaseInsensitive() throws Exception {
		System.out.println("testCheckBlockedLoginGlobalCaseInsensitive");

		String namespace = "admin-meta";

		// block login globally
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globallyBlockedLogin), null);

		// check if login in specific namespace can be used (check for globally blocked logins as well)
		perun.getUsersManagerBl().checkBlockedLogins(sess, namespace, globallyBlockedLogin.toUpperCase(), true);
	}

	@Test(expected = LoginIsAlreadyBlockedException.class)
	public void testCheckBlockedLoginGlobal() throws Exception {
		System.out.println("testCheckBlockedLoginGlobal");

		String namespace = "admin-meta";

		// block login globally
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(globallyBlockedLogin), null);

		// check if login in specific namespace can be used (check for globally blocked logins as well)
		perun.getUsersManagerBl().checkBlockedLogins(sess, namespace, globallyBlockedLogin, false);
	}

	@Test(expected = LoginIsAlreadyBlockedException.class)
	public void testCheckBlockedLoginInNamespaceIgnoreCase() throws Exception {
		System.out.println("testCheckBlockedLoginInNamespaceIgnoreCase");

		String namespace = "admin-meta";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceBlockedLogin), namespace);
		perun.getUsersManagerBl().checkBlockedLogins(sess, namespace, namespaceBlockedLogin.toUpperCase(), true);
	}

	@Test(expected = LoginIsAlreadyBlockedException.class)
	public void testCheckBlockedLoginInNamespace() throws Exception {
		System.out.println("testCheckBlockedLoginInNamespace");

		String namespace = "admin-meta";
		perun.getUsersManager().blockLogins(sess, Collections.singletonList(namespaceBlockedLogin), namespace);
		perun.getUsersManagerBl().checkBlockedLogins(sess, namespace, namespaceBlockedLogin, false);
	}

	@Test
	public void getUserRelations() throws Exception {
		System.out.println("getUserRelations");

		User admin = setUpUser("Admin", "Test");

		Vo adminVo = perun.getVosManagerBl().createVo(sess, new Vo(1111, "adminVo", "adminVo"));
		perun.getMembersManagerBl().createMember(sess, adminVo, user);
		AuthzResolverBlImpl.setRole(sess, admin, adminVo, Role.VOADMIN);
		Group otherGroup = perun.getGroupsManagerBl().createGroup(sess, adminVo, new Group("otherGroup", ""));
		perun.getGroupsManagerBl()
				.addMember(sess, otherGroup, perun.getMembersManagerBl().getMemberByUser(sess, adminVo, user));

		Vo otherVo = perun.getVosManagerBl().createVo(sess, new Vo(2222, "otherVo", "otherVo"));
		perun.getMembersManagerBl().createMember(sess, otherVo, user);
		Group notAdminGroup = perun.getGroupsManagerBl().createGroup(sess, otherVo, new Group("notAdminGroup", ""));
		perun.getGroupsManagerBl()
				.addMember(sess, notAdminGroup, perun.getMembersManagerBl().getMemberByUser(sess, otherVo, user));
		Group adminGroup = perun.getGroupsManagerBl().createGroup(sess, otherVo, new Group("adminGroup", ""));
		perun.getGroupsManagerBl()
				.addMember(sess, adminGroup, perun.getMembersManagerBl().getMemberByUser(sess, adminVo, user));
		AuthzResolverBlImpl.setRole(sess, admin, adminGroup, Role.GROUPADMIN);

		Vo notAdminVo = perun.getVosManagerBl().createVo(sess, new Vo(3333, "notAdminVo", "notAdminVo"));
		perun.getMembersManagerBl().createMember(sess, notAdminVo, user);

		PerunSession sess2 = new PerunSessionImpl(
				perun,
				new PerunPrincipal("getUserRelationsTest", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
						ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient()
		);
		sess2.getPerunPrincipal().setUser(admin);

		Map<String, List<PerunBean>> relations = perun.getUsersManager().getUserRelations(sess2, user);

		assertEquals(2, relations.get("vos").size());
		assertTrue(relations.get("vos").stream().map(PerunBean::getId).toList()
						   .containsAll(List.of(adminVo.getId(), otherVo.getId())));

		assertEquals(2, relations.get("groups").size());
		assertTrue(relations.get("groups").stream().map(PerunBean::getId).toList()
						   .containsAll(List.of(adminGroup.getId(), otherGroup.getId())));
	}

	@Test
	public void getUserRelationsSelf() throws Exception {
		System.out.println("getUserRelationsSelf");

		User testUser = setUpUser("User", "Test");

		Vo testVo = perun.getVosManagerBl().createVo(sess, new Vo(1111, "testVo", "testVo"));
		perun.getMembersManagerBl().createMember(sess, testVo, testUser);

		Group testGroup = perun.getGroupsManagerBl().createGroup(sess, testVo, new Group("testGroup", ""));
		perun.getGroupsManagerBl().addMember(sess, testGroup, perun.getMembersManagerBl().getMemberByUser(sess, testVo, testUser));

		PerunSession sess2 = new PerunSessionImpl(
				perun,
				new PerunPrincipal("getUserRelationsSelfTest", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
						ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient()
		);
		sess2.getPerunPrincipal().setUser(testUser);

		Map<String, List<PerunBean>> relations = perun.getUsersManager().getUserRelations(sess2, testUser);

		assertEquals(1, relations.get("vos").size());
		assertEquals(testVo.getId(), relations.get("vos").get(0).getId());

		assertEquals(1, relations.get("groups").size());
		assertEquals(testGroup.getId(), relations.get("groups").get(0).getId());
	}

	// PRIVATE METHODS -------------------------------------------------------------

	/**
	 * This method is used to test attributes of returned richUserExtSource from given call.
	 *
	 * First, this method creates attributes for given names. Then the method executes
	 * the given getRichUserExtSourceCall and finds the tested rues. Then calls the ruesValidation.
	 *
	 * @param getRichUserExtSourceCall call that returns richUserExtSources
	 * @param ruesValidation validation of returned richUserExtSource
	 * @param attrNamesToSetup names of ues attributes that will be set up for the tested ues
	 * @throws Exception any exception
	 */
	private void testGetRichUserExtSourceAttributes(
		TestSupplier<List<RichUserExtSource>> getRichUserExtSourceCall,
		TestConsumer<RichUserExtSource> ruesValidation,
		String... attrNamesToSetup
	) throws Exception {

		// set up ues attributes
		for (String attrName : attrNamesToSetup) {
			Attribute attribute = createUserExtSourceAttribute(attrName);
			perun.getAttributesManagerBl().setAttribute(sess, userExtSource, attribute);
		}

		// get richUserExtSources and find the one with set attribute
		RichUserExtSource desiredRues = null;
		List<RichUserExtSource> richUserExtSources = getRichUserExtSourceCall.getThrows();
		for (RichUserExtSource richUserExtSource : richUserExtSources) {
			if (richUserExtSource.asUserExtSource().equals(userExtSource)) {
				desiredRues = richUserExtSource;
			}
		}

		// validate assertions
		ruesValidation.acceptThrows(desiredRues);
	}

	private void setUpUser() throws Exception {

		user = new User();
		user.setFirstName(userFirstName);
		user.setMiddleName("");
		user.setLastName(userLastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));
		// create new user in database
		usersForDeletion.add(user);
		// save user for deletion after testing
	}

	private void setUpAnonymizedUser() throws Exception {

		anonymizedUser = new User();
		anonymizedUser.setFirstName(anonymizedUserFirstName);
		anonymizedUser.setMiddleName("");
		anonymizedUser.setLastName(anonymizedUserLastName);
		anonymizedUser.setTitleBefore("");
		anonymizedUser.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, anonymizedUser));
		perun.getUsersManagerBl().anonymizeUser(sess, anonymizedUser, true);
		// create new user in database
		usersForDeletion.add(anonymizedUser);
		// save user for deletion after testing
	}

	private User setUpUser(String firstName, String lastName) throws Exception {

		User user = new User();
		user.setFirstName(firstName);
		user.setMiddleName("");
		user.setLastName(lastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));
		// create new user in database
		usersForDeletion.add(user);
		// save user for deletion after testing
		return user;
	}

	private User setUpEmptyUser() throws Exception {

		User usr = new User();
		usr.setFirstName(userFirstName);
		usr.setMiddleName("");
		usr.setLastName(userLastName);
		usr.setTitleBefore("");
		usr.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, usr));
		// create new user in database
		usersForDeletion.add(usr);
		// save user for deletion after testing
		return usr;
	}

	private void setUpSpecificUser1ForUsers(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateForSpecificUser1();

		List<User> owners = new ArrayList<>();
		owners.add(user);
		owners.add(anonymizedUser);

		Member serviceMember = perun.getMembersManagerBl().createServiceMember(sess, vo, candidate, owners);
		perun.getMembersManagerBl().validateMember(sess, serviceMember);
		// set first candidate as member of test VO
		assertNotNull("No member created", serviceMember);
		serviceUser1 = usersManager.getUserByMember(sess, serviceMember);
		usersForDeletion.add(serviceUser1);
	}

	private void setUpSpecificUser2ForUser(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateForSpecificUser2();

		List<User> owners = new ArrayList<>();
		owners.add(user);

		Member serviceMember = perun.getMembersManagerBl().createServiceMember(sess, vo, candidate, owners);
		perun.getMembersManagerBl().validateMember(sess, serviceMember);
		// set first candidate as member of test VO
		assertNotNull("No member created", serviceMember);
		serviceUser2 = usersManager.getUserByMember(sess, serviceMember);
		usersForDeletion.add(serviceUser2);
	}

	private void setUpSponsoredUserForVo(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateForSponsoredUser();

		AuthzResolverBlImpl.setRole(sess, user, vo, Role.SPONSOR);
		Member sponsoredMember = perun.getMembersManagerBl().createMember(sess, vo, candidate);
		perun.getMembersManagerBl().setSponsorshipForMember(sess, sponsoredMember, user);
		perun.getMembersManagerBl().validateMember(sess, sponsoredMember);
		// set first candidate as member of test VO
		assertNotNull("No member created", sponsoredMember);
		sponsoredUser = usersManager.getUserByMember(sess, sponsoredMember);
		usersForDeletion.add(sponsoredUser);
	}

	private void setUpUserExtSource() throws Exception {

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		// gets real external source object from database
		userExtSource.setExtSource(externalSource);
		// put real external source into user's external source
		userExtSource.setLogin(extLogin);
		// set users login in his ext source
		assertNotNull(usersManager.addUserExtSource(sess, user, userExtSource));
		// create new user ext source in database

	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);
		newVo.setId(returnedVo.getId());
		assertEquals("both VOs should be the same",newVo,returnedVo);
		ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
		ExtSource es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		// get and create real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		return returnedVo;

	}

	private Member setUpMember(Vo vo) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate();
		Member member = perun.getMembersManagerBl().createMember(sess, vo, candidate); // candidates.get(0)
		perun.getMembersManagerBl().validateMember(sess, member);
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(usersManager.getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Group setUpGroup(Vo vo, Member member) throws Exception {
		return setUpGroup(vo, member, "UserManagerTestGroup");
	}

	private Group setUpGroup(Vo vo, Member member, String groupName) throws Exception {
		Group group = new Group(groupName,"");
		group = perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().addMember(sess, group, member);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		perun.getGroupsManager().addAdmin(sess, group, user);
		return group;
	}

	private Candidate setUpCandidate(){

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private Candidate setUpCandidateForSpecificUser1() {
		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("(Service)");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("testingServiceUser01");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		candidate.setServiceUser(true);
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}

	private Candidate setUpCandidateForSpecificUser2() {
		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("(Service)");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("testingServiceUser02");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		candidate.setServiceUser(true);
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}

	private Candidate setUpCandidateForSponsoredUser() {
		Candidate candidate = new Candidate();
		candidate.setFirstName("Sponsored");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("User01");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		candidate.setServiceUser(false);
		candidate.setSponsoredUser(true);
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}

	private Attribute createUserExtSourceAttribute(String name) throws Exception {
		return this.createUserExtSourceAttribute(name, String.class.getName(), "Testing value", false);
	}

	private Attribute createUserExtSourceAttribute(String name, String type, Object value, boolean unique) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		attrDef.setDescription(name);
		attrDef.setFriendlyName(name);
		attrDef.setType(type);
		attrDef.setUnique(unique);
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue(value);
		return attribute;
	}

	private void setUpNamespaceAttribute() throws Exception {
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:dummy");
		attrLogin.setType(String.class.getName());
		perun.getAttributesManager().createAttribute(sess, attrLogin);
	}

	private Facility setUpFacility() throws Exception {
		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		return perun.getFacilitiesManager().createFacility(sess, facility);
	}

	private Resource setUpResource(Facility facility, Vo vo) throws Exception {
		Resource resource = new Resource();
		resource.setName(UUID.randomUUID().toString());
		resource.setDescription("Testovaci");
		return perun.getResourcesManager().createResource(sess, resource, vo, facility);
	}

	private Attribute setUpAttribute() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace("urn:perun:user:attribute-def:opt");
		attrDef.setFriendlyName("user-test-attribute");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		List<AttributePolicyCollection> collections = perun.getAttributesManagerBl().getAttributePolicyCollections(sess, attrDef.getId());
		collections.add(new AttributePolicyCollection(-1, attrDef.getId(), AttributeAction.READ, List.of(new AttributePolicy(0, Role.VOADMIN, RoleObject.Vo, -1))));
		perun.getAttributesManagerBl().setAttributePolicyCollections(sess, collections);

		return new Attribute(attrDef);
	}
}
