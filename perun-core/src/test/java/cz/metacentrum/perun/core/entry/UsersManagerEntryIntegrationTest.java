package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import java.util.ArrayList;

/**
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UsersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private User user;                             // our User
        private User serviceUser1;
        private User serviceUser2;
        private Vo vo;
	String userFirstName = "";
	String userLastName = "";
	String extLogin = "";              // his login in external source
	String extLogin2 = ""; 
	String extSourceName = "LDAPMETA";        // real ext source with his login
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final UserExtSource userExtSource = new UserExtSource();   // create new User Ext Source
	private UsersManager usersManager;


	@Before
	public void setUp() throws Exception {

		usersManager = perun.getUsersManager();
		// set random name and logins during every setUp method
		userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source
		extLogin2 = Long.toHexString(Double.doubleToLongBits(Math.random()));
		setUpUser();
		setUpUserExtSource();                
                vo = setUpVo();
                setUpServiceUser1ForUser(vo);
                setUpServiceUser2ForUser(vo);

	}

	@Test
	public void createUser() throws Exception {
		System.out.println("UsersManager.createUser");

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
	public void getUserById() throws Exception {
		System.out.println("UsersManager.getUserById");

		User secondUser = usersManager.getUserById(sess, user.getId());
		assertNotNull(secondUser);
		assertEquals("both users should be the same",user,secondUser);

	}
        
        @Test
	public void getAllRichUsersWithAllNonVirutalAttributes() throws Exception {
		System.out.println("UsersManager.getAllUsersWithAllNonVirutalAttributes");

		List<RichUser> richUsers = new ArrayList<RichUser>();
                richUsers.addAll(perun.getUsersManagerBl().getAllRichUsersWithAllNonVirutalAttributes(sess));
                
                assertTrue(richUsers.size() > 0);
	}
        

	@Test (expected=UserNotExistsException.class)
	public void getUserByIdWhenUserNotExist() throws Exception {
		System.out.println("UsersManager.getUserByIdWhenUserNotExist");

		usersManager.getUserById(sess, 0);
		// shouldn't find user

	}

	@Test
	public void getUsers() throws Exception {
		System.out.println("UsersManager.getUsers");

		List<User> users = usersManager.getUsers(sess);
		assertNotNull(users);
		assertTrue(users.size() > 0);
		assertTrue(users.contains(user));

	}
        
        @Test
        public void getServiceUsers() throws Exception {
                System.out.println("UsersManager.getServiceUsers");
                
                List<User> users = usersManager.getServiceUsers(sess);
                assertTrue(users.contains(serviceUser1));
                assertTrue(users.contains(serviceUser2));
        }
        
        @Test
        public void getUsersByServiceUser1() throws Exception {
                System.out.println("UsersManager.getUsersByServiceUser1");
                
                List<User> users = usersManager.getUsersByServiceUser(sess, serviceUser1);
                assertTrue(users.contains(user));
                assertTrue(users.size() == 1);
        }

        @Test
        public void getUsersByServiceUser2() throws Exception {
                System.out.println("UsersManager.getUsersByServiceUser2");
                
                List<User> users = usersManager.getUsersByServiceUser(sess, serviceUser2);
                assertTrue(users.contains(user));
                assertTrue(users.size() == 1);
        }
        
        @Test
        public void getServiceUsersByUser() throws Exception {
                System.out.println("UsersManager.getServiceUsersByUser");
                
                List<User> users = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(users.contains(serviceUser1));
                assertTrue(users.contains(serviceUser2));
                assertTrue(users.size() == 2);
        }
        
        @Test
        public void modifyOwnership() throws Exception {
                System.out.println("UsersManager.modifyOwnership");
                
                usersManager.removeServiceUserOwner(sess, user, serviceUser1);
                
                List<User> users = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(users.contains(serviceUser2));
                assertTrue(users.size() == 1);
                
                usersManager.removeServiceUserOwner(sess, user, serviceUser2);
                users = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(users.isEmpty());
                
                usersManager.addServiceUserOwner(sess, user, serviceUser1);
                users = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(users.contains(serviceUser1));
                assertTrue(users.size() == 1);
                
                usersManager.addServiceUserOwner(sess, user, serviceUser2);
                users = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(users.contains(serviceUser1));
                assertTrue(users.contains(serviceUser2));
                assertTrue(users.size() == 2);
        }
        
        @Test (expected= RelationNotExistsException.class)
        public void removeNotExistingOwnership() throws Exception {
                System.out.println("UsersManager.removeNotExistingOwnership");
            
                Member member = setUpMember(vo);
                User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);
                
                usersManager.removeServiceUserOwner(sess, userOfMember, serviceUser1);
        }
        
        @Test (expected= RelationNotExistsException.class)
        public void removeOwnershipTwiceInRow() throws Exception {
                System.out.println("UsersManager.removeOwnershipTwiceInRow");
            
                usersManager.removeServiceUserOwner(sess, user, serviceUser1);
                usersManager.removeServiceUserOwner(sess, user, serviceUser1);
        }
        
        @Test (expected= RelationExistsException.class)
        public void addExistingOwnership() throws Exception {
                System.out.println("UsersManager.addExistingOwnership");
            
                usersManager.addServiceUserOwner(sess, user, serviceUser1);
                
        }
        
        @Test (expected= RelationExistsException.class)
        public void addOwnershipTwiceInRow() throws Exception {
                System.out.println("UsersManager.addOwnershipTwiceInRow");
            
                Member member = setUpMember(vo);
                User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);
                
                usersManager.addServiceUserOwner(sess, userOfMember, serviceUser1);
                usersManager.addServiceUserOwner(sess, userOfMember, serviceUser1);
        }
        
        @Test
        public void disableExistingOwnership() throws Exception {
                System.out.println("UsersManager.disableExistingOwnership");
            
                Member member = setUpMember(vo);
                User userOfMember = perun.getUsersManagerBl().getUserByMember(sess, member);
                assertTrue(!perun.getUsersManagerBl().serviceUserOwnershipExists(sess, userOfMember, serviceUser1));
                assertTrue(!perun.getUsersManagerBl().serviceUserOwnershipExists(sess, userOfMember, serviceUser2));
                
                usersManager.addServiceUserOwner(sess, userOfMember, serviceUser1);
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, userOfMember, serviceUser1));
                
                usersManager.addServiceUserOwner(sess, userOfMember, serviceUser2);
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, userOfMember, serviceUser2));
                
                List<User> serviceUsers = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(serviceUsers.contains(serviceUser1));
                assertTrue(serviceUsers.contains(serviceUser2));
                assertTrue(serviceUsers.size() == 2);
                
                usersManager.removeServiceUserOwner(sess, user, serviceUser1);
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, user, serviceUser1));
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, user, serviceUser2));
                serviceUsers = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(serviceUsers.contains(serviceUser2));
                assertTrue(serviceUsers.size() == 1);
                
                usersManager.removeServiceUserOwner(sess, user, serviceUser2);
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, user, serviceUser1));
                assertTrue(perun.getUsersManagerBl().serviceUserOwnershipExists(sess, user, serviceUser2));
                serviceUsers = usersManager.getServiceUsersByUser(sess, user);
                assertTrue(serviceUsers.isEmpty());
        }
        
	@Test
	public void updateUser() throws Exception {
		System.out.println("UsersManager.updateUser");

		user.setFirstName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		user.setMiddleName("");
		user.setLastName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		user.setTitleBefore("");
		user.setTitleAfter("");

		User updatedUser = usersManager.updateUser(sess, user);
		assertNotNull(updatedUser);
		assertEquals("users should be the same after update in DB",user,updatedUser);

	}

	@Test (expected=UserNotExistsException.class)
	public void updateWhenUserNotExists() throws Exception {
		System.out.println("UsersManager.updateWhenUserNotExists");

		usersManager.updateUser(sess, new User());

	}

	@Test (expected=UserNotExistsException.class)
	public void deleteUser() throws Exception {
		System.out.println("UsersManager.deleteUser");

		usersManager.deleteUser(sess, user, true);  // force delete
		usersManager.getUserById(sess, user.getId());
		// should be unable to get deleted user by his id

	}

	@Test (expected=UserNotExistsException.class)
	public void deleteUserWhenUserNotExists() throws Exception {
		System.out.println("UsersManager.deleteUserWhenUserNotExists");

		usersManager.deleteUser(sess, new User(), true);  // force delete
		// shouldn't find user
	}

	@Test
	public void addUserExtSource() throws Exception {
		System.out.println("UsersManager.addUserExtSource");

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
		System.out.println("UsersManager.addUserExtSourceWhenUserExtSourceAlreadyExists");

		usersManager.addUserExtSource(sess, user, userExtSource);
	}


	@Test (expected=UserNotExistsException.class)
	public void addUserExtSourceWhenUserNotExists() throws Exception {
		System.out.println("UsersManager.addUserExtSourceWhenUserNotExists");

		usersManager.addUserExtSource(sess, new User(), userExtSource);
		// shouldn't find user
	}

	@Test
	public void getUserByUserExtSource() throws Exception {
		System.out.println("UsersManager.getUserByUserExtSource");

		User secondUser = usersManager.getUserByUserExtSource(sess, userExtSource);
		assertEquals("users should be the same from both ext sources",user, secondUser);

	}

	@Test
	public void getUserByExtSourceNameAndExtLogin() throws Exception {
		System.out.println("UsersManager.getUserByExtSourceNameAndExtLogin");

		String extSourceName = userExtSource.getExtSource().getName();
		String extLogin = userExtSource.getLogin();
		User secondUser = usersManager.getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);
		assertEquals("users should be the same from both ext sources",user, secondUser);

	}

	@Test
	public void getUserExtSourceByExtLogin() throws Exception {
		System.out.println("UsersManager.getUserExtSourceByExtLogin");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		UserExtSource returnedUserExtSource = usersManager.getUserExtSourceByExtLogin(sess, externalSource, extLogin);
		assertEquals("both ext source should be the same", userExtSource, returnedUserExtSource);
		// check if both user ext sources are the same.

	}
        
        //TODO: for this test is needed to add creating login in registrar database
        /*
        @Test (expected=AlreadyReservedLoginException.class)
	public void isAlreadyReservedLogin() throws Exception {
		System.out.println("UsersManager.isAlreadyReservedLogin");

                String namespace = "einfra";
                String login = "martin_svehla";
                perun.getUsersManagerBl().checkReservedLogins(sess, namespace, login);
	}
        */      
        
	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceByExtLoginWhenExtLoginNotExists() throws Exception {
		System.out.println("UsersManager.getUserExtSourceByExtLoginWhenExtLoginNotExists");

		ExtSource externalSource = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		usersManager.getUserExtSourceByExtLogin(sess, externalSource, "");
		// shouldn't find UserExtSource (based on valid ext source and invalid login)
	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void getUserExtSourceByExtLoginWhenExtSourceNotExists() throws Exception {
		System.out.println("UsersManager.getUserExtSourceByExtLoginWhenExtSourceNotExists");

		usersManager.getUserExtSourceByExtLogin(sess, new ExtSource(), "");

	}

	@Test
	public void getUserExtSources() throws Exception {
		System.out.println("UsersManager.getUserExtSources");

		List<UserExtSource> userExtSources = usersManager.getUserExtSources(sess, user);
		assertNotNull(userExtSources);
		assertTrue(userExtSources.size() == 2);
		// our user should have only two ext source, one we we added and the default one

	}

	@Test (expected=UserNotExistsException.class)
	public void getUserExtSourcesWhenUserNotExists() throws Exception {
		System.out.println("UsersManager.getUserExtSourcesWhenUserNotExists");

		usersManager.getUserExtSources(sess, new User());
		// shouldn't find user

	}

	@Test
	public void getUserExtSourceById() throws Exception {
		System.out.println("UsersManager.getUserExtSourceById");

		int id = userExtSource.getId();
		UserExtSource retUserExtSource = usersManager.getUserExtSourceById(sess, id);
		// get user ext source base on our user ext source ID
		assertNotNull("unable to get ext source by its ID", retUserExtSource);
		assertEquals("both user ext sources should be the same", userExtSource, retUserExtSource);

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void getUserExtSourceByIdWhenExtSourceNotExists() throws Exception {
		System.out.println("UsersManager.getUserExtSourceByIdWhenExtSourceNotExists");

		usersManager.getUserExtSourceById(sess, 0);
		// shouldn't find ext source

	}

	@Test (expected=UserExtSourceNotExistsException.class)
	public void removeUserExtSource() throws Exception {
		System.out.println("UsersManager.removeUserExtSource");

		usersManager.removeUserExtSource(sess, user, userExtSource);

		usersManager.getUserExtSourceById(sess, userExtSource.getId());
		// shloudn't get removed user ext source from DB

	}

	@Test (expected=UserNotExistsException.class)
	public void removeUserExtSourceWhenUserNotExist() throws Exception {
		System.out.println("UsersManager.removeUserExtSourceWhenUserNotExist");

		usersManager.removeUserExtSource(sess, new User(), userExtSource);
		// shouldn't find user

	}

	@Test
	public void getUserByMember() throws Exception {
		System.out.println("UsersManager.getUserByMember");

		Member member = setUpMember(vo);

		User firstUser = usersManager.getUserByMember(sess, member);
		assertNotNull("unable to get user by member from DB",firstUser);
		User secondUser = usersManager.getUserById(sess,firstUser.getId());
		assertEquals("both users should be the same",firstUser,secondUser);

	}

	@Test (expected=MemberNotExistsException.class)
	public void getUserByMemberWhenMemberNotExist() throws Exception {
		System.out.println("UsersManager.getUserByMemberWhenMemberNotExist");

		usersManager.getUserByMember(sess, new Member());
		// shouldn't find member

	}

	@ Test
	public void getVosWhereUserIsAdmin() throws Exception {
		System.out.println("UsersManager.getVosWhereUserIsAdmin");

		Member member = setUpMember(vo);
                User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		perun.getVosManager().addAdmin(sess, vo, user);

		List<Vo> vos = usersManager.getVosWhereUserIsAdmin(sess, user);
		assertTrue("our user should be admin in one VO", vos.size() >= 1);

	}

	@Test (expected=UserNotExistsException.class)
	public void getVosWhereUserIsAdminWhenUserNotExist() throws Exception {
		System.out.println("UsersManager.getVosWhereUserIsAdminWhenUserNotExist");

		usersManager.getVosWhereUserIsAdmin(sess, new User());
		// shouldn't find user
	}

	@ Test
	public void getGroupsWhereUserIsAdmin() throws Exception {
		System.out.println("UsersManager.getGroupsWhereUserIsAdmin");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);
		User returnedUser = usersManager.getUserByMember(sess, member);

		List<Group> groups = usersManager.getGroupsWhereUserIsAdmin(sess, returnedUser);
		assertTrue("our user should be admin in one group", groups.size() >= 1);
		assertTrue("created group is not between them",groups.contains(group));

	}

	@Test (expected=UserNotExistsException.class)
	public void getGroupsWhereUserIsAdminWhenUserNotExist() throws Exception {
		System.out.println("UsersManager.getGroupsWhereUserIsAdminWhenUserNotExist");

		usersManager.getGroupsWhereUserIsAdmin(sess, new User());
		// shouldn't find user
	}

	@ Test
	public void getVosWhereUserIsMember() throws Exception {
		System.out.println("UsersManager.getVosWhereUserIsMember");

		Member member = setUpMember(vo);

		User returnedUser = usersManager.getUserByMember(sess, member);
		List<Vo> vos = usersManager.getVosWhereUserIsMember(sess, returnedUser);
		assertTrue("our user should be member of one VO", vos.size() >= 1);

	}

	@Test (expected=UserNotExistsException.class)
	public void getVosWhereUserIsMemberWhenUserNotExist() throws Exception {
		System.out.println("UsersManager.getVosWhereUserIsMemberWhenUserNotExist");

		usersManager.getVosWhereUserIsMember(sess, new User());
		// shouldn't find user
	}

	@Test
	public void getAllowedResources() throws Exception {
		System.out.println("UsersManager.getAllowedResources");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);

		Facility facility = new Facility();
		facility.setName("UsersManagerTestFacility");
		facility.setType("Testing");
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
		perun.getResourcesManager().assignGroupToResource(sess, group, resource);
		// create resource, assign group with our member

		User user = usersManager.getUserByMember(sess, member);
		// get user from member with assigned resource
		List<Resource> resources = usersManager.getAllowedResources(sess, facility, user);
		assertTrue("our user should have allowed resource", resources.size() >= 1);
		assertTrue("created resource should be allowed",resources.contains(resource));

	}

	//FIXME az bude odstranen Grouper
  @Ignore
	@Test
	public void findUsers() throws Exception {
		System.out.println("UsersManager.findUsers");

		// TODO otestovat hledani podle loginu i emailu

		// Create second user
		User user2 = new User();
		user2.setFirstName(userFirstName+"2");
		user2.setMiddleName("");
		user2.setLastName(userLastName); // Different last name from the default user in the test
		user2.setTitleBefore("");
		user2.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user2));
		// create new user in database
		usersForDeletion.add(user2);
		// save user for deletion after testing

		List<User> users = usersManager.findUsers(sess, userFirstName+""+userLastName);
		// This search must contain at least one result
		assertTrue("results must contain at least one user", users.size() >= 1);
		// And must contain the user
		assertTrue("results must contain user", users.contains(user));

		users = usersManager.findUsers(sess, userLastName);
		// This search must contain at least two results
		assertTrue("results must contain at least two users", users.size() >= 2);
		assertTrue("results must contain user and user2", users.contains(user) && users.contains(user2));
	}

	//FIXME az bude odstranen Grouper
	@Ignore
	@Test
	public void findUsersByNameFullText() throws Exception {
		System.out.println("UsersManager.findUsersByNameFullText");

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

		List<User> users = usersManager.findUsersByName(sess, userFirstName +" "+ userLastName);
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
		System.out.println("UsersManager.findUsersByNameUsingExactFields");

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
		System.out.println("UsersManager.getUsersByAttribute");

		// Check if the attribute already exists
		Attribute attr;
		AttributeDefinition attrDef;
		try {
			attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:user:attribute-def:opt:user_test_attribute");
		} catch (AttributeNotExistsException e) {
			// Attribute doesn't exist, so create it
			attrDef = new AttributeDefinition();
			attrDef.setNamespace("urn:perun:user:attribute-def:opt");
			attrDef.setFriendlyName("user_test_attribute");
			attrDef.setType(String.class.getName());

			attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		} 

		attr = new Attribute(attrDef);
		attr.setValue("UserAttribute");

		// Set the attribute to the user
		perun.getAttributesManagerBl().setAttribute(sess, user, attr);

		assertTrue("results must contain user", usersManager.getUsersByAttribute(sess, attr).contains(user));
	}


	// PRIVATE METHODS -------------------------------------------------------------


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
        
        private void setUpServiceUser1ForUser(Vo vo) throws Exception {
               Candidate candidate = setUpCandidateForServiceUser1();
               
               List<User> owners = new ArrayList<User>();
               owners.add(user);
               
               Member serviceMember = perun.getMembersManagerBl().createServiceMemberSync(sess, vo, candidate, owners);
		// set first candidate as member of test VO
		assertNotNull("No member created", serviceMember);
                serviceUser1 = usersManager.getUserByMember(sess, serviceMember);
		usersForDeletion.add(serviceUser1);
        }
        
        private void setUpServiceUser2ForUser(Vo vo) throws Exception {
               Candidate candidate = setUpCandidateForServiceUser2();
               
               List<User> owners = new ArrayList<User>();
               owners.add(user);
               
               Member serviceMember = perun.getMembersManagerBl().createServiceMemberSync(sess, vo, candidate, owners);
		// set first candidate as member of test VO
		assertNotNull("No member created", serviceMember);
                serviceUser2 = usersManager.getUserByMember(sess, serviceMember);
		usersForDeletion.add(serviceUser2);
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
		assertEquals("both VOs should be the same",newVo,returnedVo);

		ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		// get real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		return returnedVo;

	}

	private Member setUpMember(Vo vo) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(usersManager.getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Group setUpGroup(Vo vo, Member member) throws Exception {

		Group group = new Group("UserManagerTestGroup","");
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
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}
        
        private Candidate setUpCandidateForServiceUser1() {
                Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("(Service)");
		candidate.setId(0);
                candidate.setMiddleName("");
		candidate.setLastName("testingServiceUser01");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;
        }
        
        private Candidate setUpCandidateForServiceUser2() {
                Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("(Service)");
		candidate.setId(0);
                candidate.setMiddleName("");
		candidate.setLastName("testingServiceUser02");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;
        }
}
