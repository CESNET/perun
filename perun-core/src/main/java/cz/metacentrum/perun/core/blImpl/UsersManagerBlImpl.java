package cz.metacentrum.perun.core.blImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;

import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncFailed;
import cz.metacentrum.perun.audit.events.UserManagerEvents.AllUserExtSourcesDeletedForUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.OwnershipDisabledForSpecificUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.OwnershipEnabledForSpecificUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.OwnershipRemovedForSpecificUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserAddedToOwnersOfSpecificUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserCreated;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserDeleted;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceAddedToUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceRemovedFromUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceUpdated;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserSyncFailed;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserSyncFinishedWithErrors;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserUpdated;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;

import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.rt.*;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

/**
 * UsersManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 * @author Pavel Vyskocil vyskocilpavel@muni.cz
 */
public class UsersManagerBlImpl implements UsersManagerBl {

	private final static Logger log = LoggerFactory.getLogger(UsersManagerBlImpl.class);

	private UsersManagerImplApi usersManagerImpl;
	private PerunBl perunBl;
	private Integer maxConcurentUsersToSynchronize;
	private final PerunBeanProcessingPool<Candidate> poolOfCandidatesToBeSynchronized;
	private final ArrayList<UserSynchronizerThread> userSynchronizerThreads;

	private static final String A_USER_DEF_ALT_PASSWORD_NAMESPACE = AttributesManager.NS_USER_ATTR_DEF + ":altPasswords:";

	private static final String PASSWORD_VALIDATE = "validate";
	private static final String PASSWORD_CREATE = "create";
	private static final String PASSWORD_RESERVE = "reserve";
	private static final String PASSWORD_RESERVE_RANDOM = "reserve_random";
	private static final String PASSWORD_CHANGE = "change";
	private static final String PASSWORD_CHECK = "check";
	private static final String PASSWORD_DELETE = "delete";


	/**
	 * Constructor.
	 *
	 * @param usersManagerImpl connection pool
	 */
	public UsersManagerBlImpl(UsersManagerImplApi usersManagerImpl) {
		this.usersManagerImpl = usersManagerImpl;
		this.userSynchronizerThreads = new ArrayList<>();
		this.poolOfCandidatesToBeSynchronized = new PerunBeanProcessingPool<>();
		//set maximum concurrent users to synchronize by property
		this.maxConcurentUsersToSynchronize = BeansUtils.getCoreConfig().getUserMaxConcurentUsersToSynchronize();

	}

	@Override
	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException {
		return getUsersManagerImpl().getUserByUserExtSource(sess, userExtSource);
	}

	// FIXME do this in IMPL
	@Override
	public User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws InternalErrorException, UserNotExistsException {
		for (UserExtSource ues: userExtSources) {
			try {
				return getUsersManagerImpl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				// Ignore
			}
		}
		throw new UserNotExistsException("User with userExtSources " + userExtSources + " doesn't exists.");
	}

	@Override
	public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException {
		if ((extSourceType == null) || (login == null)) return new ArrayList<User>();

		return getUsersManagerImpl().getUsersByExtSourceTypeAndLogin(perunSession, extSourceType, login);
	}

	@Override
	public List<User> getSpecificUsersByUser(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getSpecificUsersByUser(sess, user);
	}

	@Override
	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) throws InternalErrorException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().getUsersBySpecificUser(sess, specificUser);
	}

	@Override
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationNotExistsException, SpecificUserMustHaveOwnerException, SpecificUserOwnerAlreadyRemovedException {
		this.removeSpecificUserOwner(sess, user, specificUser, false);
	}

	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser, boolean forceDelete) throws InternalErrorException, RelationNotExistsException, SpecificUserMustHaveOwnerException, SpecificUserOwnerAlreadyRemovedException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		if (user.getMajorSpecificType().equals(SpecificUserType.SERVICE)) throw new InternalErrorException("Service user can`t own another account (service or guest)!" + user);

		List<User> specificUserOwners = this.getUsersBySpecificUser(sess, specificUser);
		if(!specificUserOwners.remove(user)) throw new RelationNotExistsException("User is not the active owner of the specificUser.");

		if(!getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser)) {
			throw new RelationNotExistsException("User has no relationship to specificUser.");
		}

		try {
			// refresh authz for sponsors
			if(specificUser.isSponsoredUser()) AuthzResolverBlImpl.unsetRole(sess, user, specificUser, Role.SPONSOR);
			// refresh authz for service user owners
			if(specificUser.isServiceUser() && sess.getPerunPrincipal() != null) {
				if(user.getId() == sess.getPerunPrincipal().getUserId()) {
					AuthzResolverBlImpl.refreshAuthz(sess);
				}
			}
		} catch (UserNotAdminException ex) {
			throw new InternalErrorException("Can't remove role of sponsor for user " + user + " and sponsored user " + specificUser);
		}

		if(forceDelete) {
			//getPerunBl().getAuditer().log(sess, "{} ownership was removed for specificUser {}.", user, specificUser);
			getPerunBl().getAuditer().log(sess, new OwnershipRemovedForSpecificUser(user, specificUser));
			getUsersManagerImpl().removeSpecificUserOwner(sess, user, specificUser);
		} else {
			getPerunBl().getAuditer().log(sess, new OwnershipDisabledForSpecificUser(user, specificUser));
			getUsersManagerImpl().disableOwnership(sess, user, specificUser);
		}
	}

	@Override
	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationExistsException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		if (user.getMajorSpecificType().equals(SpecificUserType.SERVICE)) throw new InternalErrorException("Service user can`t own another account (service or guest)!" + user);
		List<User> specificUserOwners = this.getUsersBySpecificUser(sess, specificUser);
		if(specificUserOwners.remove(user)) throw new RelationExistsException("User is already the active owner of specific user.");

		if(getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser)) {
			getUsersManagerImpl().enableOwnership(sess, user, specificUser);
			getPerunBl().getAuditer().log(sess, new OwnershipEnabledForSpecificUser(user, specificUser));
		} else {
			getPerunBl().getAuditer().log(sess, new UserAddedToOwnersOfSpecificUser(user, specificUser));
			getUsersManagerImpl().addSpecificUserOwner(sess, user, specificUser);
		}

		try {
			// refresh authz for sponsors
			if(specificUser.isSponsoredUser()) AuthzResolverBlImpl.setRole(sess, user, specificUser, Role.SPONSOR);
			// refresh authz for service user owners
			if(specificUser.isServiceUser() && sess.getPerunPrincipal() != null) {
				if(user.getId() == sess.getPerunPrincipal().getUserId()) {
					AuthzResolverBlImpl.refreshAuthz(sess);
				}
			}
		} catch (AlreadyAdminException ex) {
			throw new InternalErrorException("User " + user + " is already sponsor of sponsored user " + specificUser);
		}
	}

	@Override
	public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser);
	}

	@Override
	public List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getSpecificUsers(sess);
	}

	@Override
	public User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner) throws InternalErrorException, RelationExistsException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) {
			throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		}

		if(specificUser.getMajorSpecificType().equals(specificUserType)) {
			throw new InternalErrorException("Can't set " + specificUserType.getSpecificUserType() + " for " + specificUser + ", because he has already set this flag.");
		}

		//Set specific type for user
		specificUser = getUsersManagerImpl().setSpecificUserType(sess, specificUser, specificUserType);

		//add owner for this new specific user
		this.addSpecificUserOwner(sess, owner, specificUser);

		return specificUser;
	}

	@Override
	public User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType) throws InternalErrorException {
		if(!specificUser.getMajorSpecificType().equals(specificUserType)) {
			throw new InternalErrorException("Can't unset " + specificUserType.getSpecificUserType() + " for " + specificUser + ", because he hasn't this flag yet.");
		}

		//remove all owners for this new specific user
		List<User> owners = getPerunBl().getUsersManagerBl().getUsersBySpecificUser(sess, specificUser);
		for(User owner: owners) {
			try {
				this.removeSpecificUserOwner(sess, owner, specificUser, true);
			} catch(SpecificUserMustHaveOwnerException | RelationNotExistsException | SpecificUserOwnerAlreadyRemovedException ex) {
				throw new InternalErrorException("Can't remove ownership of user " + specificUser, ex);
			}
		}

		//Unset specific type for user
		specificUser = getUsersManagerImpl().unsetSpecificUserType(sess, specificUser, specificUserType);


		return specificUser;
	}

	@Override
	public User getUserById(PerunSession sess, int id) throws InternalErrorException, UserNotExistsException {
		return getUsersManagerImpl().getUserById(sess, id);
	}

	@Override
	public User getUserByMember(PerunSession sess, Member member) throws InternalErrorException {
		if (member.getUserId() != 0) {
			try {
				// TODO If the member object will contain also User object, here can be returned directly.
				return getUsersManagerImpl().getUserById(sess, member.getUserId());
			} catch (UserNotExistsException e) {
				throw new ConsistencyErrorException("Member " + member + "has non-existin user.", e);
			}
		} else {
			return getUsersManagerImpl().getUserByMember(sess, member);
		}
	}

	@Override
	public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException {
		ExtSource extSource = perunBl.getExtSourcesManagerBl().getExtSourceByName(sess, extSourceName);
		UserExtSource userExtSource = this.getUserExtSourceByExtLogin(sess, extSource, extLogin);

		return this.getUserByUserExtSource(sess, userExtSource);
	}

	@Override
	public List<User> getUsers(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getUsers(sess);
	}

	@Override
	public RichUser getRichUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers.get(0);
	}

	@Override
	public RichUser getRichUserWithAttributes(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes =  this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes.get(0);
	}

	public RichUser getRichUserWithAllAttributes(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAllAttributes(sess, richUsers);
		return richUsersWithAttributes.get(0);
	}

	@Override
	public List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users) throws InternalErrorException {
		List<RichUser> richUsers = new ArrayList<RichUser>();

		for (User user: users) {
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			RichUser richUser = new RichUser(user, userExtSources);
			richUsers.add(richUser);
		}
		return richUsers;
	}

	@Override
	public List<RichUser> convertRichUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers)  throws InternalErrorException, UserNotExistsException {
		for (RichUser richUser: richUsers) {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, richUser.getId());
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, user);

			richUser.setUserAttributes(userAttributes);
		}

		return richUsers;
	}

	public List<RichUser> convertRichUsersToRichUsersWithAllAttributes(PerunSession sess, List<RichUser> richUsers) throws InternalErrorException, UserNotExistsException {
		for (RichUser richUser : richUsers) {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, richUser.getId());
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAllAttributes(sess, user);

			richUser.setUserAttributes(userAttributes);
		}

		return richUsers;
	}

	@Override
	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.addAll(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers;
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.addAll(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes;
	}


	@Override
	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException {
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers;
	}

	@Override
	public List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException {
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes;
	}

	@Override
	public List<RichUser> convertUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers, List<AttributeDefinition> attrsDef)  throws InternalErrorException {
		List<AttributeDefinition> usersAttributesDef = new ArrayList<AttributeDefinition>();

		for(AttributeDefinition attrd: attrsDef) {
			if(attrd.getName().startsWith(AttributesManager.NS_USER_ATTR)) usersAttributesDef.add(attrd);
			//If not, skip this attribute, it is not user Attribute
		}

		for (RichUser richUser: richUsers) {
			List<Attribute> userAttributes = new ArrayList<Attribute>();
			List<String> userAttrNames = new ArrayList<String>();
			for(AttributeDefinition ad: usersAttributesDef) {
				userAttrNames.add(ad.getName());
			}
			userAttributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, richUser, userAttrNames));

			richUser.setUserAttributes(userAttributes);
		}

		return richUsers;
	}

	@Override
	public User createUser(PerunSession sess, User user) throws InternalErrorException {

		// trim input
		if(user.getFirstName() != null) user.setFirstName(user.getFirstName().trim());
		if(user.getLastName() != null) user.setLastName(user.getLastName().trim());
		if(user.getMiddleName() != null) user.setMiddleName(user.getMiddleName().trim());
		if(user.getTitleBefore() != null) user.setTitleBefore(user.getTitleBefore().trim());
		if(user.getTitleAfter() != null) user.setTitleAfter(user.getTitleAfter().trim());

		//Convert empty strings to null
		if(user.getFirstName() != null && user.getFirstName().isEmpty()) user.setFirstName(null);
		if(user.getLastName() != null && user.getLastName().isEmpty()) user.setLastName(null);
		if(user.getMiddleName() != null && user.getMiddleName().isEmpty()) user.setMiddleName(null);
		if(user.getTitleBefore() != null && user.getTitleBefore().isEmpty()) user.setTitleBefore(null);
		if(user.getTitleAfter() != null && user.getTitleAfter().isEmpty()) user.setTitleAfter(null);

		user = getUsersManagerImpl().createUser(sess, user);
		getPerunBl().getAuditer().log(sess, new UserCreated(user));

		// Add default userExtSource
		ExtSource es;
		try {
			es = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
		} catch (ExtSourceNotExistsException e1) {
			throw new ConsistencyErrorException("Default extSource PERUN must exists! It is created in ExtSourcesManagerImpl.init function.",e1);
		}
		UserExtSource ues = new UserExtSource(es, 0, String.valueOf(user.getId()));
		try {
			this.addUserExtSourceWithPriority(sess, user, ues);
		} catch (UserExtSourceExistsException e) {
			throw new ConsistencyErrorException(e);
		}

		return user;
	}

	public User createUser(PerunSession sess, Candidate candidate) throws InternalErrorException {
		User user = new User();
		// trim input
		if (candidate.getFirstName() != null) user.setFirstName(candidate.getFirstName().trim());
		if (candidate.getLastName() != null) user.setLastName(candidate.getLastName().trim());
		if (candidate.getMiddleName() != null) user.setMiddleName(candidate.getMiddleName().trim());
		if (candidate.getTitleBefore() != null) user.setTitleBefore(candidate.getTitleBefore().trim());
		if (candidate.getTitleAfter() != null) user.setTitleAfter(candidate.getTitleAfter().trim());
		if (candidate.isSponsoredUser()) user.setSponsoredUser(true);
		if (candidate.isServiceUser()) user.setServiceUser(true);

		//Convert empty strings to null
		if(user.getFirstName() != null && user.getFirstName().isEmpty()) user.setFirstName(null);
		if(user.getLastName() != null && user.getLastName().isEmpty()) user.setLastName(null);
		if(user.getMiddleName() != null && user.getMiddleName().isEmpty()) user.setMiddleName(null);
		if(user.getTitleBefore() != null && user.getTitleBefore().isEmpty()) user.setTitleBefore(null);
		if(user.getTitleAfter() != null && user.getTitleAfter().isEmpty()) user.setTitleAfter(null);

		user = getUsersManagerImpl().createUser(sess, user);
		getPerunBl().getAuditer().log(sess, new UserCreated(user));

		// Add default userExtSource
		ExtSource es;
		try {
			es = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
		} catch (ExtSourceNotExistsException e1) {
			throw new ConsistencyErrorException("Default extSource PERUN must exists! It is created in ExtSourcesManagerImpl.init function.",e1);
		}
		UserExtSource ues = new UserExtSource(es, 0, String.valueOf(user.getId()));
		try {
			this.addUserExtSourceWithPriority(sess, user, ues);
		} catch (UserExtSourceExistsException e) {
			throw new ConsistencyErrorException(e);
		}

		return user;
	}

	@Override
	public void deleteUser(PerunSession sess, User user) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		this.deleteUser(sess, user, false);
	}

	@Override
	public void deleteUser(PerunSession sess, User user, boolean forceDelete) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		if (members != null && (members.size() > 0)) {
			if (forceDelete) {
				for (Member member: members) {
					getPerunBl().getMembersManagerBl().deleteMember(sess, member);
				}
			} else {
				throw new RelationExistsException("Members exist");
			}
		}

		//Remove all information about user on facilities (facilities contacts)
		List<ContactGroup> userContactGroups = getPerunBl().getFacilitiesManagerBl().getFacilityContactGroups(sess, user);
		if(!userContactGroups.isEmpty()) {
			if(forceDelete) {
				getPerunBl().getFacilitiesManagerBl().removeAllUserContacts(sess, user);
			} else {
				throw new RelationExistsException("User has still some facilities contacts: " + userContactGroups);
			}
		}

		if (getPerunBl().getSecurityTeamsManagerBl().isUserBlacklisted(sess, user) && forceDelete) {
			getPerunBl().getSecurityTeamsManagerBl().removeUserFromAllBlacklists(sess, user);
		} else if (getPerunBl().getSecurityTeamsManagerBl().isUserBlacklisted(sess, user) && !forceDelete) {
			throw new RelationExistsException("User is blacklisted by some security team. Deletion would cause loss of this information.");
		}

		// First delete all associated external sources to the user
		removeAllUserExtSources(sess, user);
		getPerunBl().getAuditer().log(sess, new AllUserExtSourcesDeletedForUser(user));

		// delete all authorships of users publications
		getUsersManagerImpl().removeAllAuthorships(sess, user);

		// delete all mailchange request related to user
		getUsersManagerImpl().removeAllPreferredEmailChangeRequests(sess, user);

		// delete all pwdreset request related to user
		getUsersManagerImpl().removeAllPasswordResetRequests(sess, user);

		// get all reserved logins of user
		List<Pair<String,String>> logins = getUsersManagerImpl().getUsersReservedLogins(user);

		// delete them from KDC
		for (Pair<String,String> login : logins) {
			try {
				// !! left = namespace / right = login
				this.deletePassword(sess, login.getRight(), login.getLeft());
			} catch (LoginNotExistsException e) {
				// OK - User hasn't assigned any password with this login
			} catch (PasswordDeletionFailedException | PasswordOperationTimeoutException e) {
				if (forceDelete) {
					log.error("Error during deletion of an account at {} for user {} with login {}.", new Object[]{login.getLeft(), user, login.getRight()});
				} else {
					throw new RelationExistsException("Error during deletion of an account at " + login.getLeft() +
							" for user " + user + " with login " + login.getRight() + ".");
				}
			}
		}

		// delete them from DB
		getUsersManagerImpl().deleteUsersReservedLogins(user);

		// all users applications and submitted data are deleted on cascade when "deleteUser()"

		// Remove all possible passwords associated with logins (stored in attributes)
		for (Attribute loginAttribute: getPerunBl().getAttributesManagerBl().getLogins(sess, user)) {
			try {
				this.deletePassword(sess, (String) loginAttribute.getValue(), loginAttribute.getFriendlyNameParameter());
			} catch (LoginNotExistsException e) {
				// OK - User hasn't assigned any password with this login
			} catch (PasswordDeletionFailedException | PasswordOperationTimeoutException e) {
				if (forceDelete) {
					log.error("Error during deletion of the account at {} for user {} with login {}.", new Object[]{loginAttribute.getFriendlyNameParameter(), user, (String) loginAttribute.getValue()});
				} else {
					throw new RelationExistsException("Error during deletion of the account at " + loginAttribute.getFriendlyNameParameter() +
							" for user " + user + " with login " + (String) loginAttribute.getValue() + ".");
				}
			}
		}


		// Delete assigned attributes
		// Users one
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, user);
			// User-Facilities one
			getPerunBl().getAttributesManagerBl().removeAllUserFacilityAttributes(sess, user);
		} catch(WrongAttributeValueException ex) {
			//All members are deleted => there are no required attribute => all atributes can be removed
			throw new ConsistencyErrorException(ex);
		} catch(WrongReferenceAttributeValueException ex) {
			//All members are deleted => there are no required attribute => all atributes can be removed
			throw new ConsistencyErrorException(ex);
		}

		//Remove user authz
		AuthzResolverBlImpl.removeAllUserAuthz(sess, user);
		//delete even inactive links
		usersManagerImpl.deleteSponsorLinks(sess, user);

		//Remove all users bans
		List<BanOnFacility> bansOnFacility = getPerunBl().getFacilitiesManagerBl().getBansForUser(sess, user.getId());
		for(BanOnFacility banOnFacility : bansOnFacility) {
			try {
				getPerunBl().getFacilitiesManagerBl().removeBan(sess, banOnFacility.getId());
			} catch (BanNotExistsException ex) {
				//it is ok, we just want to remove it anyway
			}
		}

		// Remove all sponsored user authz of his owners
		if(user.isSponsoredUser()) AuthzResolverBlImpl.removeAllSponsoredUserAuthz(sess, user);
		// Finally delete the user
		getUsersManagerImpl().deleteUser(sess, user);
		getPerunBl().getAuditer().log(sess, new UserDeleted(user));
	}

	@Override
	public User updateUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		//Convert user to version with no empty strings in object attributes (null instead)
		user = this.convertUserEmptyStringsInObjectAttributesIntoNull(user);

		User beforeUpdatingUser = getPerunBl().getUsersManagerBl().getUserById(sess, user.getId());
		User afterUpdatingUser = getUsersManagerImpl().updateUser(sess, user);

		//Log only when something is changed
		if(!beforeUpdatingUser.equals(afterUpdatingUser))
			getPerunBl().getAuditer().log(sess, new UserUpdated(user));
		return afterUpdatingUser;
	}

	@Override
	public User updateNameTitles(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		//Convert user to version with no empty strings in object attributes (null instead)
		user = this.convertUserEmptyStringsInObjectAttributesIntoNull(user);

		User beforeUpdatingUser = getPerunBl().getUsersManagerBl().getUserById(sess, user.getId());
		User afterUpdatingUser = getUsersManagerImpl().updateNameTitles(sess, user);

		//Log only when something is changed
		// must audit like update user since it changes same object
		if(!beforeUpdatingUser.equals(afterUpdatingUser))
			getPerunBl().getAuditer().log(sess, new UserUpdated(user));
		return afterUpdatingUser;
	}

	@Override
	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
		UserExtSource updatedUserExtSource = getUsersManagerImpl().updateUserExtSource(sess, userExtSource);
		try {
			updateUserAttributesByUserExtSources(sess, getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess,userExtSource));
		} catch (UserNotExistsException e) {
			throw new ConsistencyErrorException("User from perun not exists when should - removed during sync.", e);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException("Error during updating user attributes after UserExtSource update: {}", e);
		}
		return updatedUserExtSource;
	}

	public UserExtSource updateUserExtSourceWithoutUpdateUserAttributes(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
		return getUsersManagerImpl().updateUserExtSource(sess, userExtSource);
	}

	@Override
	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		getUsersManagerImpl().updateUserExtSourceLastAccess(sess, userExtSource);
	}

	@Override
	public List<UserExtSource> getUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getUserExtSources(sess, user);
	}

	@Override
	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceById(sess, id);
	}

	@Override
	public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) throws InternalErrorException {
		return getUsersManagerImpl().getAllUserExtSourcesByTypeAndLogin(sess, extType, extLogin);
	}

	@Override
	public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getActiveUserExtSources(sess, user);
	}

	@Override
	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException {
		// Check if the userExtSource already exists
		if(usersManagerImpl.userExtSourceExists(sess,userExtSource)) {
			throw new UserExtSourceExistsException("UserExtSource " + userExtSource + " already exists.");
		}

		// Check if userExtsource is type of IDP (special testing behavior)
		if (userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
			// If extSource of this userExtSource is type of IDP, test uniqueness of login in this extSource type for all users
			String login = userExtSource.getLogin();
			List<UserExtSource> userExtSources = getAllUserExtSourcesByTypeAndLogin(sess, ExtSourcesManager.EXTSOURCE_IDP, login);
			if(userExtSources.size() == 1) throw new InternalErrorException("ExtLogin: " + login + " is already in used for extSourceType: " + ExtSourcesManager.EXTSOURCE_IDP);
			else if(userExtSources.size() > 1) throw new ConsistencyErrorException("There are " + userExtSources.size() + "   extLogins: " + login + " for  extSourceType: " + ExtSourcesManager.EXTSOURCE_IDP);
		}

		userExtSource = getUsersManagerImpl().addUserExtSource(sess, user, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceAddedToUser(userExtSource, user));
		return userExtSource;
	}

	public UserExtSource addUserExtSourceWithPriority(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException {
		UserExtSource ues = addUserExtSource(sess, user, userExtSource);
		try {
			setLowestPriority(sess, user, ues);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException | WrongReferenceAttributeValueException e) {
			throw new IllegalArgumentException("Problem with storing priority for UserExtSource!");
		}
		return ues;
	}

	@Override
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException {
		//FIXME zkontrolovat zda na userExtSource neni navazan nejaky member
		//First get synchronizedAttributes from this userExtSource
		List<String> synchronizedAttributesFromRemovedUes = getSynchronizedAttributeListForUserExtSource(sess, userExtSource);

		//Remove all user extSource attributes before removing userExtSource
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, userExtSource);
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
			throw new InternalErrorException("Can't remove userExtSource because there is problem with removing all it's attributes.", ex);
		}
		getUsersManagerImpl().removeUserExtSource(sess, user, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceRemovedFromUser(userExtSource, user));

		//Update user attributes
		try {
			updateUserAttributesByUserExtSources(sess, user, synchronizedAttributesFromRemovedUes);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException("Error during updating user attributes after UserExtSource removed: {}", e);
		}
	}

	@Override
	public void moveUserExtSource(PerunSession sess, User sourceUser, User targetUser, UserExtSource userExtSource) throws InternalErrorException {
		List<Attribute> userExtSourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, userExtSource);
		Iterator<Attribute> iterator = userExtSourceAttributes.iterator();
		//remove all virtual attributes (we don't need to take care about them)
		while(iterator.hasNext()) {
			Attribute attribute = iterator.next();
			if(getPerunBl().getAttributesManagerBl().isVirtAttribute(sess, attribute)) iterator.remove();
		}

		//remove userExtSource
		try {
			this.removeUserExtSource(sess, sourceUser, userExtSource);
		} catch (UserExtSourceAlreadyRemovedException ex) {
			//this is little weird, will be better to report exception
			throw new InternalErrorException("UserExtSource was unexpectedly removed while moving " + userExtSource +
					" from " + sourceUser + " to " + targetUser);
		}

		//change userId for userExtSource
		userExtSource.setUserId(targetUser.getId());
		//add userExtSource to the targetUser
		try {
			userExtSource = this.addUserExtSource(sess, targetUser, userExtSource);
		} catch (UserExtSourceExistsException ex) {
			//someone moved this UserExtSource before us
			throw new InternalErrorException("Moving " + userExtSource + " from " + sourceUser + " to " + targetUser +
					" failed because someone already moved this UserExtSource.", ex);
		}

		//set all attributes back to this UserExtSource when it is already assigned to the targetUser
		try {
			getPerunBl().getAttributesManagerBl().setAttributes(sess, userExtSource, userExtSourceAttributes);
		} catch (WrongAttributeAssignmentException | WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
			throw new InternalErrorException("Moving " + userExtSource + " from " + sourceUser + " to " + targetUser +
					" failed because of problem with setting removed attributes back to the UserExtSource.", ex);
		}
	}

	@Override
	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceByExtLogin(sess, source, extLogin);
	}

	@Override
	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getVosWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getGroupsWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException {
		return getUsersManagerImpl().getGroupsWhereUserIsAdmin(sess, vo, user);
	}

	@Override
	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getVosWhereUserIsMember(sess, user);
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersWithoutVoAssigned(sess);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException  {
		return usersManagerImpl.getUsersWithoutVoAssigned(sess);
	}

	@Override
	public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {
		List<User> allSearchingUsers = new ArrayList<User>();
		List<User> allVoUsers = new ArrayList<User>();
		allSearchingUsers = this.findUsers(sess, searchString);
		allVoUsers = getUsersManagerImpl().getUsersByVo(sess, vo);
		allSearchingUsers.removeAll(allVoUsers);
		return allSearchingUsers;
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		return getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		List<Resource> allowedResources = new ArrayList<Resource>();

		List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for(Resource resource : resources) {
			if (getPerunBl().getResourcesManagerBl().isUserAssigned(sess, user, resource)) {
				allowedResources.add(resource);
			}
		}
		return allowedResources;
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, User user) throws InternalErrorException {
		//TODO do this method in more efficient way
		Set<Resource> resources = new HashSet<Resource>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member member : members) {
			if(!getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID)) {
				resources.addAll(getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member));
			}
		}
		return new ArrayList<Resource>(resources);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, User user) throws InternalErrorException {
		Set<Resource> resources = new HashSet<Resource>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		for(Member member : members) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member));
		}
		return new ArrayList<Resource>(resources);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) throws InternalErrorException {
		Set<RichResource> resources = new HashSet<RichResource>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		for(Member member : members) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getAssignedRichResources(sess, member));
		}
		return new ArrayList<RichResource>(resources);
	}

	private List<User> getUsersByVirtualAttribute(PerunSession sess, AttributeDefinition attributeDef, String attributeValue) throws InternalErrorException {
		// try to find method in attribute module
		UserVirtualAttributesModuleImplApi attributeModule = perunBl.getAttributesManagerBl().getUserVirtualAttributeModule(sess, attributeDef);
		List<User> listOfUsers = attributeModule.searchInAttributesValues((PerunSessionImpl) sess, attributeValue);

		if (listOfUsers != null) {
			return listOfUsers;
		}

		// iterate over all users
		List<User> matchedUsers = new ArrayList<User>();
		for (User user: perunBl.getUsersManagerBl().getUsers(sess)) {
			Attribute userAttribute;
			try {
				userAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, user, attributeDef.getName());
			} catch (AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			} catch (WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}
			if (userAttribute.valueContains((String) attributeValue)) {
				matchedUsers.add(user);
			}
		}
		return matchedUsers;
	}

	@Override
	public List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException {
		try {
			AttributeDefinition attributeDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);

			if (perunBl.getAttributesManagerBl().isVirtAttribute(sess, attributeDef)) {
				return this.getUsersByVirtualAttribute(sess, attributeDef, attributeValue);
			} else {
				return this.getUsersManagerImpl().getUsersByAttributeValue(sess, attributeDef, attributeValue);
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute name:'"  + attributeName + "', value:'" + attributeValue + "' not exists ", e);
		}
	}

	@Override
	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
		return this.getUsersManagerImpl().getUsersByAttribute(sess, attribute);
	}

	/**
	 * Search attributes directly in the DB only if the attr is def or opt and value is type of String, otherwise load all users and search in a loop.
	 */
	@Override
	public List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException {
		try {
			AttributeDefinition attributeDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);

			if (perunBl.getAttributesManagerBl().isVirtAttribute(sess, attributeDef)) {
				return this.getUsersByVirtualAttribute(sess, attributeDef, attributeValue);
			} else {
				Attribute attribute = new Attribute(attributeDef);
				attribute.setValue(attributeValue);

				return this.getUsersManagerImpl().getUsersByAttribute(sess, attribute);
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute name:'"  + attributeName + "', value:'" + attributeValue + "' not exists ", e);
		}
	}

	@Override
	public List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsers(sess, searchString);
	}

	@Override
	public List<RichUser> findRichUsers(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsers(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<RichUser> findRichUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByName(sess, searchString);
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException {
		// Convert to lower case
		titleBefore = titleBefore.toLowerCase();
		firstName = firstName.toLowerCase();
		middleName = middleName.toLowerCase();
		lastName = lastName.toLowerCase();
		titleAfter = titleAfter.toLowerCase();

		return this.getUsersManagerImpl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
	}

	@Override
	public List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByExactName(sess, searchString);
	}

	public List<User> findUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
	}

	@Override
	public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) throws InternalErrorException {
		return getUsersManagerImpl().getUsersByIds(sess, usersIds);
	}

	@Override
	public boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InternalErrorException {
		if (loginNamespace == null || login == null) {
			throw new InternalErrorException(new NullPointerException("loginNamespace cannot be null, nor login"));
		}

		// Create Attribute
		try {
			AttributeDefinition attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + loginNamespace);
			Attribute attribute = new Attribute(attributeDefinition);

			attribute.setValue(login);

			// Create empty user
			User user = new User();

			// Check attribute value, if the login is already occupied, then WrongAttributeValueException exception is thrown
			getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, user, attribute);

			return true;
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			return false;
		}

		//TODO Check also reserved logins in Registrar
	}
	/**
	 * Gets the usersManagerImpl for this instance.
	 *
	 * @return The usersManagerImpl.
	 */
	public UsersManagerImplApi getUsersManagerImpl() {
		return this.usersManagerImpl;
	}

	/**
	 * Gets the perunBl for this instance.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl()
	{
		return this.perunBl;
	}

	@Override
	public void checkUserExists(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		getUsersManagerImpl().checkUserExists(sess, user);
	}

	@Override
	public void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException {
		getUsersManagerImpl().checkReservedLogins(sess, namespace, login);
	}

	@Override
	public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException {
		getUsersManagerImpl().checkUserExtSourceExists(sess, userExtSource);
	}

	@Override
	public void checkUserExtSourceExistsById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException {
		getUsersManagerImpl().checkUserExtSourceExistsById(sess, id);
	}

	@Override
	public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		return getUsersManagerImpl().userExtSourceExists(sess, userExtSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().isUserPerunAdmin(sess, user);
	}

	@Override
	public RichUser filterOnlyAllowedAttributes(PerunSession sess, RichUser richUser) throws InternalErrorException {
		if(richUser == null) throw new InternalErrorException("RichUser can't be null.");
		//Filtering users attributes
		if(richUser.getUserAttributes() != null) {
			List<Attribute> userAttributes = richUser.getUserAttributes();
			List<Attribute> allowedUserAttributes = new ArrayList<Attribute>();
			for(Attribute userAttr: userAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, userAttr, richUser)) {
					userAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, userAttr, richUser));
					allowedUserAttributes.add(userAttr);
				}
			}
			richUser.setUserAttributes(allowedUserAttributes);
		}
		return richUser;
	}

	@Override
	public List<RichUser> filterOnlyAllowedAttributes(PerunSession sess, List<RichUser> richUsers) throws InternalErrorException {
		List<RichUser> filteredRichUsers = new ArrayList<RichUser>();
		if(richUsers == null || richUsers.isEmpty()) return filteredRichUsers;

		for(RichUser ru: richUsers) {
			filteredRichUsers.add(this.filterOnlyAllowedAttributes(sess, ru));
		}

		return filteredRichUsers;
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException {
		List<User> users = new ArrayList<User>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		//Get object for primaryHolder of aidingAttr
		if(perunBean != null) {
			if(perunBean instanceof Vo) vo = (Vo) perunBean;
			else if(perunBean instanceof Facility) facility = (Facility) perunBean;
			else if(perunBean instanceof Group) group = (Group) perunBean;
			else if(perunBean instanceof Member) member = (Member) perunBean;
			else if(perunBean instanceof User) user = (User) perunBean;
			else if(perunBean instanceof Host) host = (Host) perunBean;
			else if(perunBean instanceof Resource) resource = (Resource) perunBean;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribtue must have primaryHolder which is not null.");
		}

		if(group != null) {
			List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
			List<User> usersFromGroup = new ArrayList<User>();
			for(Member memberElement: members) {
				usersFromGroup.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
			for(User userElement: usersFromGroup) {
				users.add(userElement);
			}
		} else if(member != null) {
			user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			users.add(user);
		} else if(resource != null) {
			List<User> usersFromResource = getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource);
			users.addAll(usersFromResource);
		} else if(user != null) {
			users.add(user);
		} else if(host != null) {
			facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<User> usersFromHost = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
			for(User userElement: usersFromHost) {
				users.add(userElement);
			}
		} else if(facility != null) {
			List<User> usersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
			for(User userElement: usersFromFacility) {
				users.add(userElement);
			}
		} else if(vo != null) {
			List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
			List<User> usersFromVo = new ArrayList<User>();
			for(Member memberElement: members) {
				usersFromVo.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
			for(User userElement: usersFromVo) {
				users.add(userElement);
			}
		}

		return users;
	}

	/**
	 * Method which calls external program for password reservation.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 */
	@Override
	public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException {

		log.info("Reserving password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			try {
				this.managePassword(sess, PASSWORD_RESERVE_RANDOM, (String) attr.getValue(), loginNamespace, null);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			} catch (PasswordStrengthFailedRuntimeException e) {
				throw new PasswordStrengthFailedException(e);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Method which calls external program for password reservation.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 * @param password
	 */
	@Override
	public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		log.info("Reserving password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Reserve the password
		try {
			this.managePassword(sess, PASSWORD_RESERVE, (String) userLogin, loginNamespace, password);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
		} catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		} catch (PasswordStrengthFailedRuntimeException e) {
			throw new PasswordStrengthFailedException(e);
		}
	}

	/**
	 * Method which calls external program for password reservation. User and login is already known.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param password
	 */
	@Override
	public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		log.info("Reserving password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			try {
				this.managePassword(sess, PASSWORD_RESERVE, (String) attr.getValue(), loginNamespace, password);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			} catch (PasswordStrengthFailedRuntimeException e) {
				throw new PasswordStrengthFailedException(e);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Method which calls external program for password validation.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 */
	@Override
	public void validatePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException,
			PasswordCreationFailedException {
		log.info("Validating password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Validate the password
		try {
			this.managePassword(sess, PASSWORD_VALIDATE, (String) userLogin, loginNamespace, null);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
		}
	}

	/**
	 * Method which calls external program for password validation. User and login is already known.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 */
	@Override
	public void validatePassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException,
			PasswordCreationFailedException, LoginNotExistsException {
		log.info("Validating password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			try {
				this.managePassword(sess, PASSWORD_VALIDATE, (String) attr.getValue(), loginNamespace, null);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Method which calls external program for password validation. User and login is already known.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 */
	@Override
	public void validatePasswordAndSetExtSources(PerunSession sess, User user, String userLogin, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		/*
		 * FIXME This method is very badly writen - it should be rewrited or refactored
		 */

		try {
			if (loginNamespace.equals("einfra")) {
				List<String> kerberosLogins = new ArrayList<String>();

				// Set META and EINFRA userExtSources
				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "META");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@META");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EINFRA");
				ues = new UserExtSource(extSource, userLogin + "@EINFRA");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://login.ics.muni.cz/idp/shibboleth");
				ues = new UserExtSource(extSource, userLogin + "@meta.cesnet.cz");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				boolean someChange = false;
				if (!kerberosLogins.contains(userLogin + "@EINFRA")) {
					kerberosLogins.add(userLogin + "@EINFRA");
					someChange = true;
				}
				if (!kerberosLogins.contains(userLogin + "@META")) {
					kerberosLogins.add(userLogin + "@META");
					someChange = true;
				}

				if (someChange) {
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("egi-ui")) {

				List<String> kerberosLogins = new ArrayList<String>();

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EGI");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@EGI");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				if (!kerberosLogins.contains(userLogin + "@EGI")) {
					kerberosLogins.add(userLogin + "@EGI");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("sitola")) {

				List<String> kerberosLogins = new ArrayList<String>();

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "SITOLA.FI.MUNI.CZ");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@SITOLA.FI.MUNI.CZ");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				if (!kerberosLogins.contains(userLogin + "@SITOLA.FI.MUNI.CZ")) {
					kerberosLogins.add(userLogin + "@SITOLA.FI.MUNI.CZ");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("ics-muni-cz")) {

				List<String> kerberosLogins = new ArrayList<String>();

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "ICS.MUNI.CZ");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@ICS.MUNI.CZ");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				if (!kerberosLogins.contains(userLogin + "@ICS.MUNI.CZ")) {
					kerberosLogins.add(userLogin + "@ICS.MUNI.CZ");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("mu")) {

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://idp2.ics.muni.cz/idp/shibboleth");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@muni.cz");
				ues.setLoa(2);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

			} else if (loginNamespace.equals("vsup")) {

				// Add UES in their ActiveDirectory to access Perun by it
				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "AD");
				UserExtSource ues = new UserExtSource(extSource, userLogin);
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}
			} else if (loginNamespace.equals("elixir")) {

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "ELIXIR-EUROPE.ORG");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@ELIXIR-EUROPE.ORG");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				List<String> kerberosLogins = new ArrayList<String>();

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				if (!kerberosLogins.contains(userLogin + "@ELIXIR-EUROPE.ORG")) {
					kerberosLogins.add(userLogin + "@ELIXIR-EUROPE.ORG");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("einfra-services")) {

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EINFRA-SERVICES");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@EINFRA-SERVICES");
				ues.setLoa(0);

				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

				List<String> kerberosLogins = new ArrayList<String>();

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				if (!kerberosLogins.contains(userLogin + "@EINFRA-SERVICES")) {
					kerberosLogins.add(userLogin + "@EINFRA-SERVICES");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("dummy")) {
				//dummy namespace for testing, it has accompanying DummyPasswordModule that just generates random numbers
				ExtSource extSource;
				try {
					extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://dummy");
				} catch (ExtSourceNotExistsException e) {
					extSource =  new ExtSource("https://dummy",ExtSourcesManager.EXTSOURCE_IDP);
					try {
						extSource = getPerunBl().getExtSourcesManagerBl().createExtSource(sess, extSource, null);
					} catch (ExtSourceExistsException e1) {
						log.warn("impossible or race condition",e1);
					}
				}
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@dummy");
				ues.setLoa(2);
				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch(UserExtSourceExistsException ex) {
					//this is OK
				}

			}
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		validatePassword(sess, user, loginNamespace);

	}

	/**
	 * Method which calls external program for password creation.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 * @param password
	 */
	@Override
	@Deprecated
	public void createPassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException {
		log.info("Creating password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Create the password
		try {
			this.managePassword(sess, PASSWORD_CREATE, (String) userLogin, loginNamespace, password);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
		}
	}

	/**
	 * Method which calls external program for password creation. User and login is already known.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param password
	 */
	@Override
	@Deprecated
	public void createPassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException, LoginNotExistsException {
		log.info("Creating password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			try {
				this.managePassword(sess, PASSWORD_CREATE, (String) attr.getValue(), loginNamespace, password);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Method which calls external program for password deletion.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 */
	@Override
	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException, LoginNotExistsException,
			PasswordDeletionFailedException, PasswordOperationTimeoutException {
		log.info("Deleting password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Delete the password
		try {
			this.managePassword(sess, PASSWORD_DELETE, (String) userLogin, loginNamespace, null);
		} catch (PasswordDeletionFailedRuntimeException e) {
			throw new PasswordDeletionFailedException(e);
		} catch (LoginNotExistsRuntimeException e) {
			throw new LoginNotExistsException(e);
		}  catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		}
	}

	/**
	 * Method which calls external program for password change.
	 */
	@Override
	public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword)
			throws InternalErrorException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		log.info("Changing password for {} in login-namespace {}.", user, loginNamespace);

		// Get User login in loginNamespace
		Attribute userLogin;
		try {
			userLogin = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + loginNamespace);
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// Check password if it was requested
		if (checkOldPassword) {
			try {
				this.managePassword(sess, PASSWORD_CHECK, (String) userLogin.getValue(), loginNamespace, oldPassword);
			} catch (PasswordDoesntMatchRuntimeException e) {
				throw new PasswordDoesntMatchException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			}
		}

		// Change the password
		try {
			this.managePassword(sess, PASSWORD_CHANGE, (String) userLogin.getValue(), loginNamespace, newPassword);
		} catch (PasswordChangeFailedRuntimeException e) {
			throw new PasswordChangeFailedException(e);
		} catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		} catch (PasswordStrengthFailedRuntimeException e) {
			throw new PasswordStrengthFailedException(e);
		}

		//validate and set user ext sources
		try {
			this.validatePasswordAndSetExtSources(sess, user, (String) userLogin.getValue(), loginNamespace);
		} catch(PasswordCreationFailedException ex) {
			throw new PasswordChangeFailedException(ex);
		} catch(ExtSourceNotExistsException ex) {
			throw new InternalErrorException(ex);
		} catch(AttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Calls external program which do the job with the password.
	 *
	 * Return codes of the external program
	 * If password check fails then return 1
	 * If there is no handler for loginNamespace return 2
	 * If setting of the new password failed return 3
	 *
	 * @param sess
	 * @param operation
	 * @param userLogin
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 */
	protected void managePassword(PerunSession sess, String operation, String userLogin, String loginNamespace, String password) throws InternalErrorException {

		// If new PWDMGR module exists, use-it
		PasswordManagerModule module = null;

		try {
			module = getPasswordManagerModule(sess, loginNamespace);
		} catch (Exception ex) {
			// silently skip
		}

		if (module != null) {

			if (operation.equals(PASSWORD_RESERVE)) {
				try {
					module.reservePassword(sess, userLogin, password);
					return;
				} catch (Exception ex) {
					throw new PasswordCreationFailedRuntimeException("Password creation failed for " + loginNamespace + ":" + userLogin + ".");
				}
			}
			if (operation.equals(PASSWORD_RESERVE_RANDOM)) {
				try {
					module.reserveRandomPassword(sess, userLogin);
					return;
				} catch (Exception ex) {
					throw new PasswordCreationFailedRuntimeException("Password creation failed for " + loginNamespace + ":" + userLogin + ".");
				}
			}
			if (operation.equals(PASSWORD_CHECK)) {
				try {
					module.checkPassword(sess, userLogin, password);
					return;
				} catch (Exception ex) {
					throw new PasswordDoesntMatchRuntimeException("Old password doesn't match for " + loginNamespace + ":" + userLogin + ".");
				}
			}
			if (operation.equals(PASSWORD_VALIDATE)) {
				module.validatePassword(sess, userLogin);
				return;
			}
			if (operation.equals(PASSWORD_CHANGE)) {
				try {
					module.changePassword(sess, userLogin, password);
					return;
				} catch (Exception ex) {
					throw new PasswordChangeFailedRuntimeException("Password change failed for " + loginNamespace + ":" + userLogin + ".");
				}
			}
			if (operation.equals(PASSWORD_DELETE)) {
				try {
					module.deletePassword(sess, userLogin);
					return;
				} catch (Exception ex) {
					throw new PasswordDeletionFailedRuntimeException("Password deletion failed for " + loginNamespace + ":" + userLogin + ".");
				}
			}

		}

		// use good old way

		// Check validity of original password
		ProcessBuilder pb = new ProcessBuilder(BeansUtils.getCoreConfig().getPasswordManagerProgram(),
				operation, loginNamespace, userLogin);

		Process process;
		try {
			process = pb.start();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

		InputStream es = process.getErrorStream();

		if (operation.equals(PASSWORD_CHANGE) || operation.equals(PASSWORD_CHECK)  || operation.equals(PASSWORD_RESERVE)) {
			OutputStream os = process.getOutputStream();
			if (password == null || password.isEmpty()) {
				throw new EmptyPasswordRuntimeException("Password for " + loginNamespace + ":" + userLogin + " cannot be empty.");
			}
			// Write password to the stdin of the program
			PrintWriter pw = new PrintWriter(os, true);
			pw.write(password);
			pw.close();
		}

		// If non-zero exit code is returned, then try to read error output
		try {
			if (process.waitFor() != 0) {
				if (process.exitValue() == 1) {
					throw new PasswordDoesntMatchRuntimeException("Old password doesn't match for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 3) {
					throw new PasswordChangeFailedRuntimeException("Password change failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 4) {
					throw new PasswordCreationFailedRuntimeException("Password creation failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 5) {
					throw new PasswordDeletionFailedRuntimeException("Password deletion failed for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 6) {
					throw new LoginNotExistsRuntimeException("User login doesn't exists in underlying system for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 11) {
					throw new PasswordStrengthFailedRuntimeException("Password to set doesn't match expected restrictions for " + loginNamespace + ":" + userLogin + ".");
				} else if (process.exitValue() == 12) {
					throw new PasswordOperationTimeoutRuntimeException("Operation with password exceeded expected limit for " + loginNamespace + ":" + userLogin + ".");
				} else {
					// Some other error occured
					BufferedReader inReader = new BufferedReader(new InputStreamReader(es));
					StringBuffer errorMsg = new StringBuffer();
					String line;
					try {
						while ((line = inReader.readLine()) != null) {
							errorMsg.append(line);
						}
					} catch (IOException e) {
						throw new InternalErrorException(e);
					}

					throw new InternalErrorException(errorMsg.toString());
				}
			}
		} catch (InterruptedException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace, String password) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException {
		try {
			manageAlternativePassword(sess, user, PASSWORD_CREATE, loginNamespace, null, description, password);
		} catch(PasswordCreationFailedRuntimeException ex) {
			throw new PasswordCreationFailedException(ex);
		} catch(LoginNotExistsRuntimeException ex) {
			throw new LoginNotExistsException(ex);
		} catch(PasswordDeletionFailedException ex) {
			//This probably never happend, if yes, its some error in code of manageAlternativePassword method
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId) throws InternalErrorException, PasswordDeletionFailedException, LoginNotExistsException {
		try {
			manageAlternativePassword(sess, user, PASSWORD_DELETE, loginNamespace, passwordId, null, null);
		} catch(PasswordDeletionFailedRuntimeException ex) {
			throw new PasswordDeletionFailedException(ex);
		} catch(LoginNotExistsRuntimeException ex) {
			throw new LoginNotExistsException(ex);
		}
	}

	/**
	 * Calls external program which do the job with the alternative passwords.
	 *
	 * Return codes of the external program
	 * If password check fails then return 1
	 * If there is no handler for loginNamespace return 2
	 * If setting of the new password failed return 3
	 *
	 * @param sess
	 * @param operation
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 */
	protected void manageAlternativePassword(PerunSession sess, User user, String operation, String loginNamespace, String passwordId, String description, String password) throws InternalErrorException, PasswordDeletionFailedException {
		//if password id == null
		if(passwordId == null) passwordId = Long.toString(System.currentTimeMillis());

		//Prepare process builder
		ProcessBuilder pb = new ProcessBuilder(BeansUtils.getCoreConfig().getAlternativePasswordManagerProgram(), operation, loginNamespace, Integer.toString(user.getId()), passwordId);

		//Set password in Perun to attribute
		if (operation.equals(PASSWORD_CREATE)) {
			try {
				Attribute userAlternativePassword = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_ALT_PASSWORD_NAMESPACE + loginNamespace);
				Map<String,String> altPassValue = new LinkedHashMap<>();
				//Set not null value from altPassword attribute of this user
				if (userAlternativePassword.getValue() != null) altPassValue = (LinkedHashMap<String,String>) userAlternativePassword.getValue();
				//If password already exists, throw an exception
				if (altPassValue.containsKey(description)) throw new ConsistencyErrorException("Password with this description already exists. Description: " + description);
				//set new value to attribute
				altPassValue.put(description, passwordId);
				userAlternativePassword.setValue(altPassValue);
				//set new attribute with value to perun
				getPerunBl().getAttributesManagerBl().setAttribute(sess, user, userAlternativePassword);
			} catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		} else if (operation.equals(PASSWORD_DELETE)) {
			try {
				Attribute userAlternativePassword = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_ALT_PASSWORD_NAMESPACE + loginNamespace);
				Map<String,String> altPassValue = new LinkedHashMap<>();
				//Set not null value from altPassword attribute of this user
				if (userAlternativePassword.getValue() != null) altPassValue = (LinkedHashMap<String,String>) userAlternativePassword.getValue();
				//If password already exists, throw an exception
				if (!altPassValue.containsValue(passwordId)) throw new PasswordDeletionFailedException("Password not found by ID.");
				//remove key with this value from map
				Set<String> keys = altPassValue.keySet();
				description = null;
				for(String key: keys) {
					String valueOfKey = altPassValue.get(key);
					if(valueOfKey.equals(passwordId)) {
						if(description != null) throw new ConsistencyErrorException("There is more than 1 password with same ID in value for user " + user);
						description = key;
					}
				}
				if(description == null) throw new InternalErrorException("Password not found by ID.");
				altPassValue.remove(description);
				//set new value for altPassword attribute for this user
				userAlternativePassword.setValue(altPassValue);
				getPerunBl().getAttributesManagerBl().setAttribute(sess, user, userAlternativePassword);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			throw new InternalErrorException("Not supported operation " + operation);
		}

		Process process;
		try {
			process = pb.start();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

		InputStream es = process.getErrorStream();

		//Set pasword in remote system
		if (operation.equals(PASSWORD_CREATE)) {
			OutputStream os = process.getOutputStream();
			if (password == null || password.isEmpty()) {
				throw new EmptyPasswordRuntimeException("Alternative password for " + loginNamespace + " cannot be empty.");
			}
			// Write password to the stdin of the program
			PrintWriter pw = new PrintWriter(os, true);
			pw.write(password);
			pw.close();
		}

		// If non-zero exit code is returned, then try to read error output
		try {
			if (process.waitFor() != 0) {
				if (process.exitValue() == 1) {
					//throw new PasswordDoesntMatchRuntimeException("Old password doesn't match for " + loginNamespace + ":" + userLogin + ".");
					throw new InternalErrorException("Alternative password manager returns unexpected return code: " + process.exitValue());
				} else if (process.exitValue() == 3) {
					//throw new PasswordChangeFailedRuntimeException("Password change failed for " + loginNamespace + ":" + userLogin + ".");
					throw new InternalErrorException("Alternative password manager returns unexpected return code: " + process.exitValue());
				} else if (process.exitValue() == 4) {
					throw new PasswordCreationFailedRuntimeException("Alternative password creation failed for " + user + ". Namespace: " + loginNamespace + ", description: " + description + ".");
				} else if (process.exitValue() == 5) {
					throw new PasswordDeletionFailedRuntimeException("Password deletion failed for " + user + ". Namespace: " + loginNamespace + ", passwordId: " + passwordId + ".");
				} else if (process.exitValue() == 6) {
					throw new LoginNotExistsRuntimeException("User doesn't exists in underlying system for namespace " + loginNamespace + ", user: " + user + ".");
				} else if (process.exitValue() == 7) {
					throw new InternalErrorException("Problem with creating user entry in underlying system " + loginNamespace + ", user: " + user + ".");
				} else {
					// Some other error occured
					BufferedReader inReader = new BufferedReader(new InputStreamReader(es));
					StringBuffer errorMsg = new StringBuffer();
					String line;
					try {
						while ((line = inReader.readLine()) != null) {
							errorMsg.append(line);
						}
					} catch (IOException e) {
						throw new InternalErrorException(e);
					}

					throw new InternalErrorException(errorMsg.toString());
				}
			}
		} catch (InterruptedException e) {
			throw new InternalErrorException(e);
		}
	}



	@Override
	public List<RichUser> convertUsersToRichUsersWithAttributesByNames(PerunSession sess, List<User> users, List<String> attrNames) throws InternalErrorException {

		// TODO - optimzization needed - at least there should be single select on RichUser object in impl !!
		List<RichUser> result = new ArrayList<RichUser>();
		AttributesManagerBl attributesManagerBl = this.getPerunBl().getAttributesManagerBl();
		for (User u : users) {
			RichUser ru = new RichUser(u, getUserExtSources(sess, u));
			ru.setUserAttributes(attributesManagerBl.getAttributes(sess, u, attrNames));
			result.add(ru);
		}
		return result;

	}

	@Override
	public RichUser convertUserToRichUserWithAttributesByNames(PerunSession sess, User user, List<String> attrNames) throws InternalErrorException {
		AttributesManagerBl attributesManagerBl = this.getPerunBl().getAttributesManagerBl();

		RichUser richUser = new RichUser(user, getUserExtSources(sess, user));
		richUser.setUserAttributes(attributesManagerBl.getAttributes(sess, user, attrNames));

		return richUser;
	}

	@Override
	public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, findRichUsers(sess, searchString));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsers(sess, searchString), attrsName);
		}

	}

	@Override
	public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, findRichUsersByExactMatch(sess, searchString));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsersByExactMatch(sess, searchString), attrsName);
		}

	}

	@Override
	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException, VoNotExistsException{

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutSpecificVo(sess, vo, searchString)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutSpecificVo(sess, vo, searchString), attrsName);
		}
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, VoNotExistsException, UserNotExistsException{

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutVoAssigned(sess)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutVoAssigned(sess), attrsName);
		}
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		List<User> users = getUsers(sess);
		// optionally exclude specific users
		if (!includedSpecificUsers) {
			Iterator<User> it = users.iterator();
			while (it.hasNext()) {
				User u = it.next();
				if (u.isSpecificUser()) {
					it.remove();
				}
			}
		}

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, users));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, users, attrsName);
		}

	}

	@Override
	public void setLogin(PerunSession sess, User user, String loginNamespace, String login) throws InternalErrorException {

		// should always pass, since isLoginAvailable() in ENTRY does the same
		try {

			List<String> names = new ArrayList<String>();
			names.add(AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + loginNamespace);

			// will always get attribute (empty, if not set)
			List<Attribute> checked = getPerunBl().getAttributesManagerBl().getAttributes(sess, user, names);
			if (checked.size() != 1) {
				throw new InternalErrorException("User should have only one login (attribute) in namespace");
			}
			// if user already has login
			if (checked.get(0).getValue() != null) {
				throw new InternalErrorException("Can't set new login. User already has login in namespace: "+loginNamespace);
			}

			checked.get(0).setValue(login);

			getPerunBl().getAttributesManagerBl().setAttributes(sess, user, checked);

		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang) throws InternalErrorException, UserNotExistsException {

		int changeId = getUsersManagerImpl().requestPreferredEmailChange(sess, user, email);

		if (lang == null || lang.isEmpty()) lang = "en";

		String subject;
		try {
			Attribute subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, lang,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":preferredMailChangeMailSubject");
			subject = (String) subjectTemplateAttribute.getValue();
			if (subject == null) {
				subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":preferredMailChangeMailSubject");
				subject = (String) subjectTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		String message;
		try {
			Attribute messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, lang,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":preferredMailChangeMailTemplate");
			message = (String) messageTemplateAttribute.getValue();
			if (message == null) {
				messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":preferredMailChangeMailTemplate");
				message = (String) messageTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		Utils.sendValidationEmail(user, url, email, changeId, subject, message);

	}

	@Override
	public String validatePreferredEmailChange(PerunSession sess, User user, String i, String m) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {

		String email = getUsersManagerImpl().getPreferredEmailChangeRequest(sess, user, i, m);

		AttributeDefinition def = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
		Attribute a = new Attribute(def);
		a.setValue(email);

		// store attribute
		getPerunBl().getAttributesManagerBl().setAttribute(sess, user, a);

		getUsersManagerImpl().removeAllPreferredEmailChangeRequests(sess, user);

		return email;

	}

	@Override
	public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {

		List<String> list = getUsersManagerImpl().getPendingPreferredEmailChanges(sess, user);

		Attribute a = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
		if (a != null && a.getValue() != null) {
			Iterator<String> it = list.iterator();
			while (it.hasNext()) {
				String value = it.next();
				if (value.equals(BeansUtils.attributeValueToString(a))) {
					// remove pending change requests if they are already set in attribute
					it.remove();
				}
			}
		}

		return list;

	}

	@Override
	public User convertUserEmptyStringsInObjectAttributesIntoNull(User user) {
		//if user is null, return it back without change
		if(user == null) return user;

		//convert all empty strings to null
		if(user.getFirstName() != null && user.getFirstName().isEmpty()) user.setFirstName(null);
		if(user.getMiddleName() != null && user.getMiddleName().isEmpty()) user.setMiddleName(null);
		if(user.getLastName() != null && user.getLastName().isEmpty()) user.setLastName(null);

		if(user.getTitleBefore() != null && user.getTitleBefore().isEmpty()) user.setTitleBefore(null);
		if(user.getTitleAfter() != null && user.getTitleAfter().isEmpty()) user.setTitleAfter(null);

		return user;
	}

	@Override
	public void changeNonAuthzPassword(PerunSession sess, User user, String m, String password, String lang) throws InternalErrorException, UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {

		String requestId = Utils.cipherInput(m, true);
		String namespace = getUsersManagerImpl().loadPasswordResetRequest(user, Integer.parseInt(requestId));

		if (namespace.isEmpty()) throw new InternalErrorException("Password reset request is not valid anymore or doesn't existed at all for User: "+user);

		List<Attribute> logins = perunBl.getAttributesManagerBl().getLogins(sess, user);
		boolean found = false;
		for (Attribute a : logins) {
			if (a.getFriendlyNameParameter().equals(namespace)) found = true;
		}
		if (!found) throw new InternalErrorException(user.toString()+" doesn't have login in namespace: "+namespace);

		// reset password without checking old
		try {
			changePassword(sess, user, namespace, "", password, false);
		} catch (PasswordDoesntMatchException ex) {
			// shouldn't happen
			throw new InternalErrorException(ex);
		}

		// was changed - send notification to all member's emails
		Set<String> emails = new HashSet<String>();

		try {
			Attribute a = perunBl.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
			if (a != null && a.getValue() != null) {
				emails.add((String)a.getValue());
			}
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member member : members) {

			try {
				Attribute a = perunBl.getAttributesManagerBl().getAttribute(sess, member, AttributesManager.NS_MEMBER_ATTR_DEF+":mail");
				if (a != null && a.getValue() != null) {
					emails.add((String)a.getValue());
				}
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException(ex);
			}

		}

		// get template

		String subject;
		try {
			Attribute subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, lang,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetConfirmMailSubject:" + namespace);
			subject = (String) subjectTemplateAttribute.getValue();
			if (subject == null) {
				subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetConfirmMailSubject:" + namespace);
				subject = (String) subjectTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		String message;
		try {
			Attribute messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, lang,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetConfirmMailTemplate:" + namespace);
			message = (String) messageTemplateAttribute.getValue();
			if (message == null) {
				messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetConfirmMailTemplate:" + namespace);
				message = (String) messageTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		for (String email : emails) {
			Utils.sendPasswordResetConfirmationEmail(user, email, namespace, subject, message);
		}

	}

	@Override
	public int getUsersCount(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getUsersCount(sess);
	}

	@Override
	public Map<String,String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws InternalErrorException {
		return getUsersManagerImpl().generateAccount(session, namespace, parameters);
	}

	@Override
	public List<User> getSponsors(PerunSession sess, Member sponsoredMember) throws InternalErrorException {
		if(!sponsoredMember.isSponsored()) {
			throw new IllegalArgumentException("member "+sponsoredMember.getId()+" is not marked as sponsored");
		}
		return getUsersManagerImpl().getSponsors(sess, sponsoredMember);
	}

	private PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) throws InternalErrorException {
		return getUsersManagerImpl().getPasswordManagerModule(session, namespace);
	}

	@Override
	public void removeAllUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		for(UserExtSource userExtSource : getUserExtSources(sess, user)) {
			try {
				removeUserExtSource(sess, user, userExtSource);
			} catch (UserExtSourceAlreadyRemovedException ex) {
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd, List<String> excludeValueEnds) throws AttributeNotExistsException, InternalErrorException {
		AttributeDefinition adef = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
		if((!adef.getType().equals("java.lang.String")) || (!adef.getNamespace().equals(AttributesManager.NS_UES_ATTR_DEF))) {
			throw new InternalErrorException("only ues attributes of type String can be used in findUsersWithExtSourceAttributeValueEnding()");
		}
		return usersManagerImpl.findUsersWithExtSourceAttributeValueEnding(sess,attributeName,valueEnd,excludeValueEnds);
	}

	@Override
	public String changePasswordRandom(PerunSession session, User user, String loginNamespace) throws PasswordOperationTimeoutException, LoginNotExistsException, InternalErrorException, PasswordChangeFailedException {

		char[] possibleCharacters =
			    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+;:,<.>/?"
				.toCharArray();
		int count = 12;

		// FIXME - We will replace following logic once each login-namespace will implement
		// FIXME   pwd-manager module and have server side checks
		if (Objects.equals(loginNamespace, "vsup")) {
			count = 14;
			// removed O, l, specific only: +, -, *, /, .
			possibleCharacters = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789+-*/.".toCharArray();
		}

		String newRandomPassword = RandomStringUtils.random(count, 0, possibleCharacters.length - 1, false,
			false, possibleCharacters, new SecureRandom());

		try {
			changePassword(session, user, loginNamespace, null, newRandomPassword, false);
		} catch (PasswordDoesntMatchException | PasswordStrengthFailedException e) {
			// should not happen when we are not using the old password
			throw new InternalErrorException(e);
		}

		String template = getPasswordResetTemplate(session, loginNamespace);

		String userLogin;
		try {
			Attribute userLoginAttribute = getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + loginNamespace);
			userLogin = (String) userLoginAttribute.getValue();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// should not happen since the changePassword method passed
			throw new InternalErrorException(e);
		}

		return template
			.replace("{password}", StringEscapeUtils.escapeHtml4(newRandomPassword))
			.replace("{login}", StringEscapeUtils.escapeHtml4(userLogin));
	}

	/**
	 * Returns template for password reset.
	 * <p>
	 * It finds the template in entityless attribute randomPwdResetTemplate for given namespace.
	 * If that fails, it falls back to default template.
	 *
	 * @param session        session
	 * @param loginNamespace login namespace
	 * @return String representing HTML template for password reset
	 */
	private String getPasswordResetTemplate(PerunSession session, String loginNamespace) {
		String template =
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
						"<head>\n" +
						"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>\n" +
						"</head>"+
						"<body><div style=\"padding: 25px;color: black;text-align: center;\">" +
						"<h1>Password reset</h1>" +
						"<p>Password for user {login} has been reset by the administrator.<br />" +
						"The new password is <br />" +
						"<h2><b>{password}</b></h2>" +
						"</p></div>" +
						"</body>" +
						"</html>";
		try {
			Attribute templateAttribute = perunBl.getAttributesManagerBl().getAttribute(session, loginNamespace,
				AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":randomPwdResetTemplate");
			if (templateAttribute.getValue() != null) {
				template = (String) templateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException | InternalErrorException e) {
			log.warn("Failed to get template attribute for password reset in namespace {}, using default. Exception " +
				"class: {}, Exception message: {}", loginNamespace, e.getClass().getName(), e.getMessage());
		}

		return template;
	}


	public void addCandidateToPool(Candidate candidate) throws InternalErrorException {
		poolOfCandidatesToBeSynchronized.putJobIfAbsent(candidate,false);
	}

	public synchronized void reinitializeUserSynchronizerThreads(PerunSession sess) throws InternalErrorException {
		int numberOfNewlyRemovedThreads = removeInteruptedThreads();
		int numberOfNewlyCreatedThreads = 0;

		// Start new threads if there is place for them
		while (userSynchronizerThreads.size() < maxConcurentUsersToSynchronize) {
			UserSynchronizerThread thread = new UserSynchronizerThread(sess);
			thread.start();
			userSynchronizerThreads.add(thread);
			numberOfNewlyCreatedThreads++;
			log.debug("New thread for user synchronization started.");
		}

		// Save state of synchronization to the info log
		log.debug("Reinitialize UserSynchronizerThread method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'right now synchronized users with subjects'='" + poolOfCandidatesToBeSynchronized.getRunningJobs() + "', " +
				"'right now waiting users with subjects'='" + poolOfCandidatesToBeSynchronized.getWaitingJobs() + "'.");
	}

	public synchronized void synchronizeUser(PerunSession sess, Candidate candidate) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		log.debug("User synchronization started for candidate: {}", candidate);

		User user = null;
		UserExtSource ues;
		if ((ues = candidate.getUserExtSource()) != null) {
			ExtSource tmpExtSource = getPerunBl().getExtSourcesManagerBl().checkOrCreateExtSource(sess, ues.getExtSource().getName(),
					ues.getExtSource().getType());
			// Set the extSource ID
			ues.getExtSource().setId(tmpExtSource.getId());
			try {
				// Try to find the user by userExtSource
				user = getUserByExtSourceNameAndExtLogin(sess, ues.getExtSource().getName(), ues.getLogin());
			} catch (UserExtSourceNotExistsException e) {
				// This is OK, non-existent userExtSource will be assigned later
			} catch (UserNotExistsException | ExtSourceNotExistsException e) {
				// Ignore, we are only checking if the user exists
			}
		}
		// If user hasn't been found, then create him
		if (user == null) {
			user = createUser(sess, candidate);
			log.debug("User {} was created by candidate {}.", user, candidate);
		}

		// Assign missing userExtSource and update LoA
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource userExtSource : candidate.getUserExtSources()) {
				UserExtSource uesFromPerun;
				try {
					uesFromPerun = getUserExtSourceByExtLogin(sess, userExtSource.getExtSource(), userExtSource.getLogin());
					// Update LoA
					if (uesFromPerun.getLoa() != userExtSource.getLoa()) {
						uesFromPerun.setLoa(userExtSource.getLoa());
						getPerunBl().getUsersManagerBl().updateUserExtSourceWithoutUpdateUserAttributes(sess, uesFromPerun);
					}
					//Store userExtSource priority if the attribute doesn't stored yet.
					if (getUserExtSourcePriority(sess, uesFromPerun) == -1) {
						setLowestPriority(sess, user, uesFromPerun);
					}

				} catch (UserExtSourceNotExistsException e) {
					// Create userExtSource
					try {
						addUserExtSourceWithPriority(sess, user, userExtSource);
						log.debug("UserExtSource {} was added to user {}.", userExtSource, user);
					} catch (UserExtSourceExistsException e1) {
						throw new ConsistencyErrorException("Adding userExtSource which already exists: " + userExtSource);
					}
				} catch (UserExtSourceExistsException e1) {
					throw new ConsistencyErrorException("Updating login of userExtSource to value which already exists: " + userExtSource);
				}
			}
		}
		UserExtSource userExtSource = null;
		try {
			userExtSource = getUserExtSourceByExtLogin(sess, ues.getExtSource(), ues.getLogin());
		} catch (UserExtSourceNotExistsException e) {
			throw new InternalErrorException("Error during synchronize user ", e);
		}
		if (userExtSource != null) {
			//Get old synchronized attribute list
			List<String> oldSynchronizedAttributes = getSynchronizedAttributeListForUserExtSource(sess, userExtSource);

			//Store new attributes
			storeUserExtSourceStoredAttributes(sess, candidate, userExtSource);

			//Update user attributes
			updateUserAttributesByUserExtSources(sess, user, oldSynchronizedAttributes);
		}
		log.debug("User synchronization ended for candidate: {}", candidate);
	}

	public void updateUserAttributesByUserExtSources(PerunSession sess, User user, List<String> oldStoredAttributes) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		updateUserCoreAttributesByHighestPriority(sess, user);

		List<String> synchronizedAttributes = getSynchronizedAttributeListForUser(sess, user);
		synchronizedAttributes.addAll(oldStoredAttributes);

		updateUserAttributesAfterSync(sess, user, synchronizedAttributes);
	}

	public void updateUserAttributesByUserExtSources(PerunSession sess, User user) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		updateUserAttributesByUserExtSources(sess, user, new ArrayList<>());
	}

	public int getUserExtSourcePriority(PerunSession sess, UserExtSource userExtSource) throws WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException {
		Attribute priorityAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, userExtSource, UsersManager.USEREXTSOURCEPRIORITY_ATTRNAME);
		if (priorityAttribute != null && priorityAttribute.getValue() != null ) {
			return priorityAttribute.valueAsInteger();
		} else {
			return -1;
		}
	}

	public int setLowestPriority(PerunSession sess, User user, UserExtSource userExtSource) throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, AttributeNotExistsException {
		Attribute priorityAttribute = new Attribute(perunBl.getAttributesManagerBl().getAttributeDefinition(sess, UsersManager.USEREXTSOURCEPRIORITY_ATTRNAME));
		int priority = getNewLowestPriority(sess, user);
		priorityAttribute.setValue(priority);
		getPerunBl().getAttributesManagerBl().setAttribute(sess, userExtSource, priorityAttribute);
		log.debug("The priority {} was stored for userExtSource {}.", priority, userExtSource);
		return priority;
	}

	public List<String> getSynchronizedAttributeListForUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		Attribute uesStoredAttributesAttr = getUserExtSourceStoredAttributesAttr(sess, userExtSource);
		if (uesStoredAttributesAttr != null && uesStoredAttributesAttr.getValue() != null && uesStoredAttributesAttr.valueAsString() != null) {
			JSONObject storedAttributes = new JSONObject(uesStoredAttributesAttr.valueAsString());
			return new ArrayList<>(storedAttributes.keySet());
		} else {
			return new ArrayList<String>();
		}
	}


	public Attribute getUserExtSourceStoredAttributesAttr(PerunSession sess, UserExtSource userExtSource)  throws InternalErrorException {
		try {
			return getPerunBl().getAttributesManagerBl().getAttribute(sess, userExtSource, UsersManager.USEREXTSOURCESTOREDATTRIBUTES_ATTRNAME);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			return null;
		}
	}


	public void saveInformationAboutUserSynchronization(PerunSession sess, Candidate candidate, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		//get current timestamp of this synchronization
		Date currentTimestamp = new Date();
		String originalExceptionMessage = exceptionMessage;
		//If session is null, throw an exception
		if (sess == null) {
			throw new InternalErrorException("Session is null when trying to save information about synchronization. Candidate: " + candidate+ ", timestamp: " + currentTimestamp + ",message: " + exceptionMessage);
		}

		//If group is null, throw an exception
		if (candidate == null) {
			throw new InternalErrorException("Object candidate is null when trying to save information about synchronization. Timestamp: " + currentTimestamp + ", message: " + exceptionMessage);
		}

		//if exceptionMessage is empty, use "Empty message" instead
		if (exceptionMessage != null && exceptionMessage.isEmpty()) {
			exceptionMessage = "Empty message.";
			//else trim the message on 1000 characters if not null
		} else if (exceptionMessage != null && exceptionMessage.length() > 1000) {
			exceptionMessage = exceptionMessage.substring(0, 1000) + " ... message is too long, other info is in perun log file. If needed, please ask perun administrators.";
		}

		//Set correct format of currentTimestamp
		String correctTimestampString = BeansUtils.getDateFormatter().format(currentTimestamp);

		//Get both attribute definition lastSynchroTimestamp and lastSynchroState
		//Get definitions and values, set values
		Attribute lastSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_UES_ATTR_DEF + ":lastSynchronizationTimestamp"));
		Attribute lastSynchronizationState = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_UES_ATTR_DEF + ":lastSynchronizationState"));
		lastSynchronizationTimestamp.setValue(correctTimestampString);
		//if exception is null, set null to value => remove attribute instead of setting in method setAttributes
		lastSynchronizationState.setValue(exceptionMessage);

		//attributes to set
		List<Attribute> attrsToSet = new ArrayList<>();

		//null in exceptionMessage means no exception, success
		//Set lastSuccessSynchronizationTimestamp if this one is success
		if(exceptionMessage == null) {
			String attrName = AttributesManager.NS_UES_ATTR_DEF + ":lastSuccessSynchronizationTimestamp";
			try {
				Attribute lastSuccessSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_UES_ATTR_DEF + ":lastSuccessSynchronizationTimestamp"));
				lastSuccessSynchronizationTimestamp.setValue(correctTimestampString);
				attrsToSet.add(lastSuccessSynchronizationTimestamp);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save lastSuccessSynchronizationTimestamp, because there is missing attribute with name {}", attrName);
			}
		} else {
			//Log to auditer_log that synchronization failed or finished with some errors
			if(failedDueToException) {
				getPerunBl().getAuditer().log(sess, new UserSyncFailed(candidate));
				log.debug("{} synchronization failed because of {}", candidate, originalExceptionMessage);
			} else {
				getPerunBl().getAuditer().log(sess,new UserSyncFinishedWithErrors(candidate));
				log.debug("{} synchronization finished with errors: {}", candidate, originalExceptionMessage);
			}
		}

		//set lastSynchronizationState and lastSynchronizationTimestamp
		attrsToSet.add(lastSynchronizationState);
		attrsToSet.add(lastSynchronizationTimestamp);
		try {
			UserExtSource userExtSourceFromPerun = getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, candidate.getUserExtSource().getExtSource(), candidate.getUserExtSource().getLogin());
			((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttributes(sess, userExtSourceFromPerun, attrsToSet);
		} catch (UserExtSourceNotExistsException e) {
			log.error("Can't save information about user synchronization, because the userExtSource from candidate doesn't exist in Perun.");
		}
	}


	//----------- PRIVATE METHODS

	/**
	 * Store all attributes from candidate to UserExtSource attribute storedAttribibutes as JSON.
	 *
	 * @param sess PerunSession
	 * @param candidate Candidate
	 * @param userExtSource UserExtSource
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	private void storeUserExtSourceStoredAttributes(PerunSession sess, Candidate candidate, UserExtSource userExtSource) throws AttributeNotExistsException, InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Attribute userExtSourceStoredAttributesAttr = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, UsersManager.USEREXTSOURCESTOREDATTRIBUTES_ATTRNAME));
		userExtSourceStoredAttributesAttr.setValue(candidate.convertAttributesToJSON().toString());
		getPerunBl().getAttributesManagerBl().setAttribute(sess, userExtSource, userExtSourceStoredAttributesAttr);
	}

	/**
	 * Updates User attributes with the values from UserExtSources with highest priority
	 * @param sess PerunSession
	 * @param user User
	 * @param synchronizedAttributes List of attributes names for update
	 * @throws WrongAttributeAssignmentException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	private void updateUserAttributesAfterSync(PerunSession sess, User user, List<String> synchronizedAttributes) throws WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Update userAttributes from actualExtSources
		for (String attrName : synchronizedAttributes) {
			if (attrName.startsWith(AttributesManager.NS_USER_ATTR_DEF)) {
				Attribute userAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attrName);
				Attribute attribute = getUserAttributeFromUserExtSourcesWithHighestPriority(sess, user, attrName);

				if ((userAttribute.getValue() != null && !userAttribute.getValue().equals(attribute.getValue()))
						|| (userAttribute.getValue() == null && attribute.getValue() != null)) {
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, attribute);
				}
			}
		}
	}

	/**
	 * Returns list of synchronized attributes names for user
	 * @param sess PerunSession
	 * @param user User
	 * @return List of synchronized attributes names
	 * @throws InternalErrorException
	 */
	private List<String> getSynchronizedAttributeListForUser(PerunSession sess, User user) throws InternalErrorException {
		Collection<String> synchronizedAttributes = new HashSet<>();

		for (UserExtSource userExtSource : perunBl.getUsersManagerBl().getUserExtSources(sess, user) ) {
			Attribute uesStoredAttributesAttr = getUserExtSourceStoredAttributesAttr(sess, userExtSource);
			if (uesStoredAttributesAttr != null && uesStoredAttributesAttr.getValue() != null && uesStoredAttributesAttr.valueAsString() != null) {
				JSONObject storedAttributes = new JSONObject(uesStoredAttributesAttr.valueAsString());
				synchronizedAttributes.addAll(storedAttributes.keySet());
			}
		}
		return new ArrayList<>(synchronizedAttributes);
	}


	/**
	 * Returns the new lowest priority for user
	 *
	 * @param sess PerunSession
	 * @param user User
	 * @return New lowest priority
	 * @throws InternalErrorException
	 */
	private int getNewLowestPriority(PerunSession sess, User user) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		int lowestPriority = -1;
		List<UserExtSource> userExtSourceList = getPerunBl().getUsersManagerBl().getUserExtSources(sess,user);

		for (UserExtSource userExtSource : userExtSourceList) {
			int priority = getUserExtSourcePriority(sess, userExtSource);
			if (priority > lowestPriority) {
				lowestPriority = priority;
			}
		}
		return lowestPriority + 1;
	}

	/**
	 * Updates all user core attributes with userCoreAttributes from userExtSource with highest priority
	 *
	 * @param sess PerunSession
	 * @param user User
	 * @throws ConsistencyErrorException
	 */
	private void updateUserCoreAttributesByHighestPriority(PerunSession sess, User user) throws InternalErrorException {
		UserExtSource uesWithHighestPriority = getUserExtSourceWithHighestPositivePriority(sess, user);

		if (uesWithHighestPriority == null) {
			return;
		}

		Attribute storedAttribute = getUserExtSourceStoredAttributesAttr(sess, uesWithHighestPriority);

		if (storedAttribute != null && storedAttribute.valueAsString() != null) {
			JSONObject storedAttributes = new JSONObject(storedAttribute.valueAsString());
			boolean attributeChanged = false;
			String firstName = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":firstName").optString(0);
			String lastName = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":lastName").optString(0);
			String middleName = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":middleName").optString(0);
			String tittleBefore = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":tittleBefore").optString(0);
			String tittleAfter = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":tittleAfter").optString(0);
			Boolean isServiceUser = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":serviceUser").optBoolean(0);
			Boolean isSponsoredUser = storedAttributes.optJSONArray(AttributesManager.NS_USER_ATTR_CORE + ":sponsoredUser").optBoolean(0);

			if ((user.getFirstName() == null && !firstName.equals("")) || (user.getFirstName() != null && !user.getFirstName().equals(firstName))) {
				user.setFirstName(firstName);
				attributeChanged = true;
			}
			if ((user.getLastName() == null && !lastName.equals("")) || (user.getLastName() != null && !user.getLastName().equals(lastName))) {
				user.setLastName(lastName);
				attributeChanged = true;
			}
			if ((user.getMiddleName() == null && !middleName.equals("")) || (user.getMiddleName() != null && !user.getMiddleName().equals(middleName))) {
				user.setMiddleName(middleName);
				attributeChanged = true;
			}
			if ((user.getTitleBefore() == null && !tittleBefore.equals("")) || (user.getTitleBefore() != null && !user.getTitleBefore().equals(tittleBefore))) {
				user.setTitleAfter(tittleBefore);
				attributeChanged = true;
			}
			if ((user.getTitleAfter() == null && !tittleAfter.equals("")) || (user.getTitleAfter() != null && !user.getTitleAfter().equals(tittleAfter))) {
				user.setTitleBefore(tittleAfter);
				attributeChanged = true;
			}
			if (!user.isServiceUser() == isServiceUser) {
				user.setServiceUser(isServiceUser);
				attributeChanged = true;
			}
			if (!user.isSponsoredUser() == isSponsoredUser) {
				user.setSponsoredUser(isSponsoredUser);
				attributeChanged = true;
			}

			//Update user if some of user core attribute was changed
			if (attributeChanged) {
				try {
					perunBl.getUsersManagerBl().updateUser(sess, user);
				} catch (UserNotExistsException e) {
					throw new ConsistencyErrorException("User from perun not exists when should - removed during sync.", e);
				}
			}
		}
	}

	/**
	 * Returns the attribute from userExtSources with highest priority for user. If the attribute's type is ArrayList
	 * or LinkedHashMap and the overwriteAttributeList does't contains attributeName, the attribute value is merged
	 * from all userExtSources.
	 *
	 * @param sess PerunSession
	 * @param user User
	 * @param attrName Attribute name
 	 * @return Attribute
	 * @throws WrongAttributeAssignmentException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	private Attribute getUserAttributeFromUserExtSourcesWithHighestPriority(PerunSession sess, User user, String attrName) throws WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException {
		int highestPriority = Integer.MAX_VALUE;
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attrName);
		attribute.setValue(null);
		UserExtSource userExtSourceWithHighestPriority = null;

		//Get userExtSource and attribute with highest priority
		for (UserExtSource userExtSource: getPerunBl().getUsersManagerBl().getUserExtSources(sess, user)) {
			int priority = getUserExtSourcePriority(sess, userExtSource);
			if ( priority >= 0 && priority < highestPriority) {
				Attribute uesStoredAttributesAttr = getUserExtSourceStoredAttributesAttr(sess, userExtSource);

				if (uesStoredAttributesAttr != null
						&& uesStoredAttributesAttr.valueAsString() != null) {
					JSONObject storedAttributes = new JSONObject(uesStoredAttributesAttr.valueAsString());

					if (storedAttributes.opt(attrName) != null) {
						highestPriority = priority;
						attribute.setValue(getPerunBl().getAttributesManagerBl().stringToAttributeValue(storedAttributes.optJSONArray(attrName).optString(0),attribute.getType()));
						userExtSourceWithHighestPriority = userExtSource;
					}
				}
			}
		}

		//Merge attribute value for attribute with type ArrayList and LinkedHashMap if the attribute is not in overwriteAttributeList
		if ((attribute.getType().equals("java.util.ArrayList") || attribute.getType().equals("java.util.LinkedHashMap")
				&& userExtSourceWithHighestPriority != null && !getPerunBl().getExtSourcesManagerBl().getOverwriteUserAttributeList(userExtSourceWithHighestPriority.getExtSource()).contains(attrName))) {
			for (UserExtSource ues : getPerunBl().getUsersManagerBl().getUserExtSources(sess, user)) {
				if (!ues.equals(userExtSourceWithHighestPriority)) {
					Attribute uesStoredAttributesAttr = getUserExtSourceStoredAttributesAttr(sess, ues);
					if (uesStoredAttributesAttr != null && uesStoredAttributesAttr.valueAsString() != null) {
						JSONObject storedAttributes = new JSONObject(uesStoredAttributesAttr.valueAsString());
						if (storedAttributes.opt(attrName) != null && attribute.getType().equals("java.util.ArrayList")) {
							ArrayList<String> attributeValue = attribute.valueAsList();
							ArrayList<String> valueFromUes = (ArrayList<String>) getPerunBl().getAttributesManagerBl().stringToAttributeValue(storedAttributes.optJSONArray(attrName).optString(0), attribute.getType());
							if (attributeValue == null) {
								attributeValue = new ArrayList<>();
							}
							if (valueFromUes != null) {
								for (String value : valueFromUes) {
									if(!attributeValue.contains(value)) {
										attributeValue.add(value);
									}
								}
								attribute.setValue(attributeValue);
							}
						} else if (storedAttributes.opt(attrName) != null &&  attribute.getType().equals("java.util.LinkedHashMap")) {
							LinkedHashMap<String, String> attributeValue = attribute.valueAsMap();
							LinkedHashMap<String, String> valueFromUes = (LinkedHashMap<String, String>) getPerunBl().getAttributesManagerBl().stringToAttributeValue(storedAttributes.optJSONArray(attrName).optString(0), attribute.getType());
							if (attributeValue == null) {
								attributeValue = new LinkedHashMap<>();
							}
							if (valueFromUes != null) {
								for (String key : valueFromUes.keySet()) {
									if (!attributeValue.containsKey(key)) {
										attributeValue.put(key, valueFromUes.get(key));
									} else {
										log.error("Key {} is already exist. Skip.", key);
									}
								}
								attribute.setValue(attributeValue);
							}
						}
					}
				}
			}
		}
		return attribute;
	}

	/**
	 * Returns userExtSource with highest priority for user
	 *
	 * @param sess PerunSession
	 * @param user User
	 * @return UserExtSource
	 */
	private UserExtSource getUserExtSourceWithHighestPositivePriority(PerunSession sess, User user) throws InternalErrorException {
		int priority = Integer.MAX_VALUE;
		UserExtSource userExtSource = null;
		for (UserExtSource ues : getPerunBl().getUsersManagerBl().getUserExtSources(sess, user)) {
			try {
				int uesPriority = getUserExtSourcePriority(sess, ues);
				Attribute storedAttributesAttr = getUserExtSourceStoredAttributesAttr(sess, ues);
				if (uesPriority > 0 && uesPriority < priority && storedAttributesAttr != null) {
					priority = uesPriority;
					userExtSource = ues;
				}
			} catch (Exception e) {
				//Skip this userExtSource
			}
		}
		return userExtSource;
	}

	/**
	 * This function removed interupted threads
	 *
	 * @return Number of removed threads
	 */
	private int removeInteruptedThreads() {
		int numberOfNewlyRemovedThreads = 0;

		// Get the default synchronization timeout from the configuration file
		int timeout = BeansUtils.getCoreConfig().getUserSynchronizationTimeout();

		Iterator<UserSynchronizerThread> threadIterator = userSynchronizerThreads.iterator();

		while (threadIterator.hasNext()) {
			UserSynchronizerThread thread = threadIterator.next();
			long threadStart = thread.getStartTime();
			//If thread start time is 0, this thread is waiting for another job, skip it
			if (threadStart == 0) continue;

			long timeDiff = System.currentTimeMillis() - threadStart;
			//If thread was interrupted by anything, remove it from the pool of active threads
			if (thread.isInterrupted()) {
				numberOfNewlyRemovedThreads++;
				threadIterator.remove();
			} else if (timeDiff / 1000 / 60 > timeout) {
				// If the time is greater than timeout set in the configuration file (in minutes), interrupt and remove this thread from pool
				log.error("One of threads was interrupted because of timeout!");
				thread.interrupt();
				threadIterator.remove();
				numberOfNewlyRemovedThreads++;
			}
		}

		return numberOfNewlyRemovedThreads;
	}

	//----------- PRIVATE CLASSESS

	private class UserSynchronizerThread extends Thread {

		// all synchronization runs under synchronizer identity.
		private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private final PerunBl perunBl;
		private final PerunSession sess;
		private volatile long startTime;

		public UserSynchronizerThread(PerunSession sess) throws InternalErrorException {
			this.perunBl = (PerunBl) sess.getPerun();
			this.sess = perunBl.getPerunSession(pp, new PerunClient());
			//Default settings of not running thread (waiting for another User)
			this.startTime = 0;
		}

		public void run() {
			while (true) {
				//Set thread to default state (waiting for another group to synchronize)
				this.setThreadToDefaultState();

				//If this thread was interrupted, end it's running
				if(this.isInterrupted()) return;

				//text of exception if was thrown, null in exceptionMessage means "no exception, it's ok"
				String exceptionMessage = null;
				//if exception which produce fail of whole synchronization was thrown
				boolean failedDueToException = false;

				//Take another user from the pool to synchronize it
				Candidate candidate = null;
				try {
					candidate = poolOfCandidatesToBeSynchronized.takeJob();
				} catch (InterruptedException ex) {
					log.error("Thread was interrupted when trying to take another subject to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					return;
				}

				try {
					// Set the start time, so we can check the timeout of the thread
					startTime = System.currentTimeMillis();

					log.debug("Synchronization thread started synchronization for user with subject {}.", candidate);

					perunBl.getUsersManagerBl().synchronizeUser(sess, candidate);

					log.debug("Synchronization thread for candidate {} has finished in {} ms.", candidate, System.currentTimeMillis() - startTime);
				} catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException | UserExtSourceNotExistsException | ExtSourceNotExistsException e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize user with candidate ";
					log.error(exceptionMessage + candidate, e);
					exceptionMessage += "due to exception: " + e.getName() + " => " + e.getMessage();
				} finally {
					//Save information about group synchronization, this method run in new transaction
					try {
						perunBl.getUsersManagerBl().saveInformationAboutUserSynchronization(sess, candidate, failedDueToException, exceptionMessage);
					} catch (Exception ex) {
						log.error("When synchronization user with candidate {}, exception was thrown.", candidate, ex);
					}
					//Remove job from running jobs
					if(!poolOfCandidatesToBeSynchronized.removeJob(candidate)) {
						log.error("Can't remove running job for object {} from pool of running jobs because it is not containing it.", candidate);
					}

					log.debug("UserSynchronizationThread finished for candidate: {}", candidate);
				}
			}
		}

		public long getStartTime() {
			return startTime;
		}

		private void setThreadToDefaultState() {
			this.startTime = 0;
		}
	}
}
