package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.TestUtils.TestConsumer;
import cz.metacentrum.perun.TestUtils.TestSupplier;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests of UsersManager.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UsersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "UsersManager.";
	private final static String ATTR_UES_O = "o";
	private final static String ATTR_UES_CN = "cn";
	private final static String URN_ATTR_USER_PREFERRED_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
	private final static String URN_ATTR_UES_O = AttributesManager.NS_UES_ATTR_DEF + ':' + ATTR_UES_O;
	private final static String URN_ATTR_UES_CN = AttributesManager.NS_UES_ATTR_DEF + ':' + ATTR_UES_CN;

	private User user;           // our User
	private User serviceUser1;
	private User serviceUser2;
	private User sponsoredUser;
	private Vo vo;
	String userFirstName = "";
	String userLastName = "";
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
		setUpUserExtSource();
		setUpSpecificUser1ForUser(vo);
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
	public void getUsersByServiceUser1() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByServiceUser1");

		List<User> users = usersManager.getUsersBySpecificUser(sess, serviceUser1);
		assertTrue(users.contains(user));
		assertTrue(users.size() == 1);
	}

	@Test
	public void getUsersByServiceUser2() throws Exception {
		System.out.println(CLASS_NAME + "getUsersByServiceUser2");

		List<User> users = usersManager.getUsersBySpecificUser(sess, serviceUser2);
		assertTrue(users.contains(user));
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

	@Test (expected=UserNotExistsException.class)
	public void deleteUser() throws Exception {
		System.out.println(CLASS_NAME + "deleteUser");

		usersManager.deleteUser(sess, user, true);  // force delete
		usersManager.getUserById(sess, user.getId());
		// should be unable to get deleted user by his id

	}

	@Test (expected=UserNotExistsException.class)
	public void deleteUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteUserWhenUserNotExists");

		usersManager.deleteUser(sess, new User(), true);  // force delete
		// shouldn't find user
	}

	@Test
	public void anonymizeUser() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUser");

		List<String> originalAttributesToKeep = BeansUtils.getCoreConfig().getAttributesToKeep();
		// configure attributesToKeep so it contains only 1 attribute - preferredMail
		BeansUtils.getCoreConfig().setAttributesToKeep(Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));

		// set preferredMail and phone attributes
		Attribute preferredMail = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
		preferredMail.setValue("mail@mail.com");
		perun.getAttributesManagerBl().setAttribute(sess, user, preferredMail);
		Attribute phone = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
		phone.setValue("+420555555");
		perun.getAttributesManagerBl().setAttribute(sess, user, phone);

		usersManager.anonymizeUser(sess, user);

		// set attributesToKeep back to the original attributes
		BeansUtils.getCoreConfig().setAttributesToKeep(originalAttributesToKeep);

		User updatedUser = perun.getUsersManagerBl().getUserById(sess, user.getId());
		assertTrue("Firstname should be null or empty.", updatedUser.getFirstName() == null || updatedUser.getFirstName().isEmpty());
		assertTrue("Lastname should be null or empty.", updatedUser.getLastName() == null || updatedUser.getLastName().isEmpty());

		Attribute updatedPreferredMail = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
		Attribute updatedPhone = perun.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
		assertEquals("PreferredMail attribute should be kept untouched.", updatedPreferredMail, preferredMail);
		assertNull("Phone attribute should be deleted.", updatedPhone.getValue());
	}

	@Test(expected=UserNotExistsException.class)
	public void anonymizeUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWhenUserNotExists");

		usersManager.anonymizeUser(sess, new User());
		// shouldn't find user
	}

	@Test
	public void anonymizeUserWhenAnonymizationNotSupported() throws Exception {
		System.out.println(CLASS_NAME + "anonymizeUserWhenAnonymizationNotSupported");

		List<String> originalAttributesToAnonymize = BeansUtils.getCoreConfig().getAttributesToAnonymize();
		// configure attributesToAnonymize so it contains only 1 attribute - dummy-test
		BeansUtils.getCoreConfig().setAttributesToAnonymize(Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":dummy-test"));

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

		try {
			usersManager.anonymizeUser(sess, user);
			// anonymizeUser() should have thrown AnonymizationNotSupportedException
			fail();
		} catch (AnonymizationNotSupportedException ex) {
			// this is expected
		} finally {
			// set attributesToAnonymize back to the original attributes
			BeansUtils.getCoreConfig().setAttributesToAnonymize(originalAttributesToAnonymize);
		}
	}

	@Test (expected=InternalErrorException.class)
	public void addIDPExtSourcesWithSameLogin() throws Exception {
		System.out.println(CLASS_NAME + "addIDPExtSourcesWithSameLogin");

		ExtSource ext1 = new ExtSource("test1", ExtSourcesManagerEntry.EXTSOURCE_IDP);
		ExtSource ext2 = new ExtSource("test2", ExtSourcesManagerEntry.EXTSOURCE_IDP);

		ext1 = perun.getExtSourcesManagerBl().createExtSource(sess, ext1, null);
		ext2 = perun.getExtSourcesManagerBl().createExtSource(sess, ext2, null);

		UserExtSource ues1 = new UserExtSource(ext1, 1, "testExtLogin@test");
		UserExtSource ues2 = new UserExtSource(ext2, 1, "testExtLogin@test");


		usersManager.addUserExtSource(sess, user, ues1);
		usersManager.addUserExtSource(sess, user, ues2);
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
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false);
		// create resource, assign group with our member

		User user = usersManager.getUserByMember(sess, member);
		// get user from member with assigned resource
		List<Resource> resources = usersManager.getAllowedResources(sess, facility, user);
		assertTrue("our user should have allowed resource", resources.size() >= 1);
		assertTrue("created resource should be allowed",resources.contains(resource));

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

		perun.getResourcesManager().assignGroupToResource(sess, g1, r, false);

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
		perun.getResourcesManager().assignGroupToResource(sess, g2, r, false);

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
		perun.getResourcesManager().assignGroupToResource(sess, g2, r2, false);

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

		JSONObject jsonObject = candidate.convertAttributesToJSON();

		assertEquals(8, jsonObject.length());
		assertEquals("value", jsonObject.getJSONArray(perun.getAttributesManager().NS_USER_ATTR + ":attribute").getString(0));
		assertEquals(userFirstName, jsonObject.getJSONArray(perun.getAttributesManager().NS_USER_ATTR_CORE + ":firstName").getString(0));
	}

	@Test
	public void convertAttributesWithNullToJSON() {
		System.out.println(CLASS_NAME + "convertAttributesWithNullToJSON");

		Candidate candidate = new Candidate(user, userExtSource);
		candidate.setAttributes(Collections.singletonMap(perun.getAttributesManager().NS_USER_ATTR + ":attribute", null));

		JSONObject jsonObject = candidate.convertAttributesToJSON();

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

	private void setUpSpecificUser1ForUser(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateForSpecificUser1();

		List<User> owners = new ArrayList<>();
		owners.add(user);

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
}
