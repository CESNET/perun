package cz.metacentrum.perun.core.blImpl;

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
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserUpdated;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordChangeFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDoesntMatchException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthFailedException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.EmptyPasswordRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordChangeFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDoesntMatchRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordOperationTimeoutRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordStrengthFailedRuntimeException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * UsersManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 */
public class UsersManagerBlImpl implements UsersManagerBl {

	private final static Logger log = LoggerFactory.getLogger(UsersManagerBlImpl.class);

	private final UsersManagerImplApi usersManagerImpl;
	private PerunBl perunBl;

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
		if ((extSourceType == null) || (login == null)) return new ArrayList<>();

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
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException {
		this.removeSpecificUserOwner(sess, user, specificUser, false);
	}

	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser, boolean forceDelete) throws InternalErrorException, RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException {
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
			} catch(RelationNotExistsException | SpecificUserOwnerAlreadyRemovedException ex) {
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
	public RichUser getRichUser(PerunSession sess, User user) throws InternalErrorException {
		List<User> users = new ArrayList<>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers.get(0);
	}

	@Override
	public RichUser getRichUserWithAttributes(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes =  this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes.get(0);
	}

	@Override
	public List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users) throws InternalErrorException {
		List<RichUser> richUsers = new ArrayList<>();

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

	@Override
	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException {
		List<User> users = new ArrayList<>(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers;
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<>(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes;
	}


	@Override
	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException {
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
		List<AttributeDefinition> usersAttributesDef = new ArrayList<>();

		for(AttributeDefinition attrd: attrsDef) {
			if(attrd.getName().startsWith(AttributesManager.NS_USER_ATTR)) usersAttributesDef.add(attrd);
			//If not, skip this attribute, it is not user Attribute
		}

		for (RichUser richUser: richUsers) {
			List<String> userAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: usersAttributesDef) {
				userAttrNames.add(ad.getName());
			}
			List<Attribute> userAttributes = new ArrayList<>(getPerunBl().getAttributesManagerBl().getAttributes(sess, richUser, userAttrNames));

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
			this.addUserExtSource(sess, user, ues);
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
					log.error("Error during deletion of an account at {} for user {} with login {}.", login.getLeft(), user, login.getRight());
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
					log.error("Error during deletion of the account at {} for user {} with login {}.", loginAttribute.getFriendlyNameParameter(), user, (String) loginAttribute.getValue());
				} else {
					throw new RelationExistsException("Error during deletion of the account at " + loginAttribute.getFriendlyNameParameter() +
							" for user " + user + " with login " + loginAttribute.getValue() + ".");
				}
			}
		}


		// Delete assigned attributes
		// Users one
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, user);
			// User-Facilities one
			getPerunBl().getAttributesManagerBl().removeAllUserFacilityAttributes(sess, user);
		} catch(WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
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
		UserExtSource updatedUes = getUsersManagerImpl().updateUserExtSource(sess, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
		return updatedUes;
	}

	@Override
	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		getUsersManagerImpl().updateUserExtSourceLastAccess(sess, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
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
	public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue) throws InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException {
		if(attrId <= 0) throw new InternalErrorException("Unexpected attribute Id with zero or negative value.");
		AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinitionById(sess, attrId);
		return getUserExtSourceByUniqueAttributeValue(sess, attrDef, uniqueValue);

	}

	@Override
	public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, String attrName, String uniqueValue) throws InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException {
		if(attrName == null || attrName.isEmpty()) throw new InternalErrorException("Can't find attribute, because it's name is missing.");
		AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, attrName);

		return getUserExtSourceByUniqueAttributeValue(sess, attrDef, uniqueValue);
	}

	/**
	 * Return userExtSource for specific attribute definition and unique value.
	 * If not found, throw and exception.
	 *
	 * It looks for exactly one value of the specific attribute type:
	 * - Integer -> exactly match
	 * - String -> exactly match
	 * - Map -> exactly match of "key=value"
	 * - ArrayList -> exactly match of one of the value
	 *
	 * @param sess
	 * @param attrDef attribute definition we are looking for, has to be unique and in userExtSource namespace
	 * @param uniqueValue value used for searching
	 *
	 * @return userExtSource found by attribute definition and it's unique value
	 *
	 * @throws InternalErrorException if attributeDefinition or uniqueValue is in incorrect format
	 * @throws UserExtSourceNotExistsException if userExtSource can't be found
	 */
	private UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, AttributeDefinition attrDef, String uniqueValue) throws InternalErrorException, UserExtSourceNotExistsException {
		if(!attrDef.getNamespace().startsWith(AttributesManager.NS_UES_ATTR)) throw new InternalErrorException("Attribute definition has to be from 'ues' namespace: " + attrDef);
		if(!attrDef.isUnique()) throw new InternalErrorException("Attribute definition has to be unique: " + attrDef);
		if(uniqueValue == null || uniqueValue.isEmpty()) throw new InternalErrorException("Can't find userExtSource by empty value!");

		return usersManagerImpl.getUserExtSourceByUniqueAttributeValue(sess, attrDef.getId(), uniqueValue);
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

	@Override
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException {
		//FIXME zkontrolovat zda na userExtSource neni navazan nejaky member
		//First remove all user extSource attributes before removing userExtSource
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, userExtSource);
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
			throw new InternalErrorException("Can't remove userExtSource because there is problem with removing all it's attributes.", ex);
		}
		getUsersManagerImpl().removeUserExtSource(sess, user, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceRemovedFromUser(userExtSource, user));
	}

	@Override
	public void moveUserExtSource(PerunSession sess, User sourceUser, User targetUser, UserExtSource userExtSource) throws InternalErrorException {
		List<Attribute> userExtSourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, userExtSource);
		//remove all virtual attributes (we don't need to take care about them)
		userExtSourceAttributes.removeIf(attribute -> getPerunBl().getAttributesManagerBl().isVirtAttribute(sess, attribute));

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
		List<User> allSearchingUsers = this.findUsers(sess, searchString);
		List<User> allVoUsers = getUsersManagerImpl().getUsersByVo(sess, vo);
		allSearchingUsers.removeAll(allVoUsers);
		return allSearchingUsers;
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		return getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		List<Resource> allowedResources = new ArrayList<>();

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
		Set<Resource> resources = new HashSet<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member member : members) {
			if(!getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID)) {
				resources.addAll(getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member));
			}
		}
		return new ArrayList<>(resources);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, User user) throws InternalErrorException {
		Set<Resource> resources = new HashSet<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		for(Member member : members) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member));
		}
		return new ArrayList<>(resources);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) throws InternalErrorException {
		Set<RichResource> resources = new HashSet<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		for(Member member : members) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getAssignedRichResources(sess, member));
		}
		return new ArrayList<>(resources);
	}

	private List<User> getUsersByVirtualAttribute(PerunSession sess, AttributeDefinition attributeDef, String attributeValue) throws InternalErrorException {
		// try to find method in attribute module
		UserVirtualAttributesModuleImplApi attributeModule = perunBl.getAttributesManagerBl().getUserVirtualAttributeModule(sess, attributeDef);
		List<User> listOfUsers = attributeModule.searchInAttributesValues((PerunSessionImpl) sess, attributeValue);

		if (listOfUsers != null) {
			return listOfUsers;
		}

		// iterate over all users
		List<User> matchedUsers = new ArrayList<>();
		for (User user: perunBl.getUsersManagerBl().getUsers(sess)) {
			Attribute userAttribute;
			try {
				userAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, user, attributeDef.getName());
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}
			if (userAttribute.valueContains(attributeValue)) {
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
			getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, user, attribute);

			return true;
		} catch (AttributeNotExistsException | WrongReferenceAttributeValueException | WrongAttributeAssignmentException e) {
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
			List<Attribute> allowedUserAttributes = new ArrayList<>();
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
		List<RichUser> filteredRichUsers = new ArrayList<>();
		if(richUsers == null || richUsers.isEmpty()) return filteredRichUsers;

		for(RichUser ru: richUsers) {
			filteredRichUsers.add(this.filterOnlyAllowedAttributes(sess, ru));
		}

		return filteredRichUsers;
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Group group) throws InternalErrorException {
		List<User> users = new ArrayList<>();
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for(Member memberElement: members) {
			users.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
		}
		return users;
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Member member) throws InternalErrorException {
		return Collections.singletonList(getPerunBl().getUsersManagerBl().getUserByMember(sess, member));
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Resource resource) throws InternalErrorException {
		return getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Host host) throws InternalErrorException {
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		return getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Facility facility) throws InternalErrorException {
		return getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Vo vo) throws InternalErrorException {
		List<User> users = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		for(Member memberElement: members) {
			users.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
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
			this.managePassword(sess, PASSWORD_RESERVE, userLogin, loginNamespace, password);
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
			this.managePassword(sess, PASSWORD_VALIDATE, userLogin, loginNamespace, null);
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
			switch (loginNamespace) {
				case "einfra": {
					List<String> kerberosLogins = new ArrayList<>();

					// Set META and EINFRA userExtSources
					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "META");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@META");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EINFRA");
					ues = new UserExtSource(extSource, userLogin + "@EINFRA");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://login.ics.muni.cz/idp/shibboleth");
					ues = new UserExtSource(extSource, userLogin + "@meta.cesnet.cz");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
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

					if (someChange && kerberosLoginsAttr != null) {
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "egi-ui": {

					List<String> kerberosLogins = new ArrayList<>();

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EGI");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@EGI");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					// Store also Kerberos logins
					Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
					if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
						kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
					}

					if (!kerberosLogins.contains(userLogin + "@EGI") && kerberosLoginsAttr != null) {
						kerberosLogins.add(userLogin + "@EGI");
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "sitola": {

					List<String> kerberosLogins = new ArrayList<>();

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "SITOLA.FI.MUNI.CZ");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@SITOLA.FI.MUNI.CZ");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					// Store also Kerberos logins
					Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
					if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
						kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
					}

					if (!kerberosLogins.contains(userLogin + "@SITOLA.FI.MUNI.CZ") && kerberosLoginsAttr != null) {
						kerberosLogins.add(userLogin + "@SITOLA.FI.MUNI.CZ");
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "ics-muni-cz": {

					List<String> kerberosLogins = new ArrayList<>();

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "ICS.MUNI.CZ");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@ICS.MUNI.CZ");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					// Store also Kerberos logins
					Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
					if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
						kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
					}

					if (!kerberosLogins.contains(userLogin + "@ICS.MUNI.CZ") && kerberosLoginsAttr != null) {
						kerberosLogins.add(userLogin + "@ICS.MUNI.CZ");
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "mu": {

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://idp2.ics.muni.cz/idp/shibboleth");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@muni.cz");
					ues.setLoa(2);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					break;
				}
				case "vsup": {

					// Add UES in their ActiveDirectory to access Perun by it
					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "AD");
					UserExtSource ues = new UserExtSource(extSource, userLogin);
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}
					break;
				}
				case "elixir": {

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "ELIXIR-EUROPE.ORG");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@ELIXIR-EUROPE.ORG");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					List<String> kerberosLogins = new ArrayList<>();

					// Store also Kerberos logins
					Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
					if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
						kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
					}

					if (!kerberosLogins.contains(userLogin + "@ELIXIR-EUROPE.ORG") && kerberosLoginsAttr != null) {
						kerberosLogins.add(userLogin + "@ELIXIR-EUROPE.ORG");
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "einfra-services": {

					ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "EINFRA-SERVICES");
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@EINFRA-SERVICES");
					ues.setLoa(0);

					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					List<String> kerberosLogins = new ArrayList<>();

					// Store also Kerberos logins
					Attribute kerberosLoginsAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
					if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
						kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
					}

					if (!kerberosLogins.contains(userLogin + "@EINFRA-SERVICES") && kerberosLoginsAttr != null) {
						kerberosLogins.add(userLogin + "@EINFRA-SERVICES");
						kerberosLoginsAttr.setValue(kerberosLogins);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
					}

					break;
				}
				case "dummy": {
					//dummy namespace for testing, it has accompanying DummyPasswordModule that just generates random numbers
					ExtSource extSource;
					try {
						extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "https://dummy");
					} catch (ExtSourceNotExistsException e) {
						extSource = new ExtSource("https://dummy", ExtSourcesManager.EXTSOURCE_IDP);
						try {
							extSource = getPerunBl().getExtSourcesManagerBl().createExtSource(sess, extSource, null);
						} catch (ExtSourceExistsException e1) {
							log.warn("impossible or race condition", e1);
						}
					}
					UserExtSource ues = new UserExtSource(extSource, userLogin + "@dummy");
					ues.setLoa(2);
					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
					} catch (UserExtSourceExistsException ex) {
						//this is OK
					}

					break;
				}
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
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
			this.managePassword(sess, PASSWORD_CREATE, userLogin, loginNamespace, password);
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
			this.managePassword(sess, PASSWORD_DELETE, userLogin, loginNamespace, null);
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
		} catch(ExtSourceNotExistsException | AttributeValueException ex) {
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
					StringBuilder errorMsg = new StringBuilder();
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
			} catch (WrongAttributeAssignmentException | WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
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
					StringBuilder errorMsg = new StringBuilder();
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
		List<RichUser> result = new ArrayList<>();
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
	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutSpecificVo(sess, vo, searchString)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutSpecificVo(sess, vo, searchString), attrsName);
		}
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, UserNotExistsException{

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
			users.removeIf(User::isSpecificUser);
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

			List<String> names = new ArrayList<>();
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

		} catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang) throws InternalErrorException {

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
		// If user is null, return null
		if(user == null) return null;

		//convert all empty strings to null
		if(user.getFirstName() != null && user.getFirstName().isEmpty()) user.setFirstName(null);
		if(user.getMiddleName() != null && user.getMiddleName().isEmpty()) user.setMiddleName(null);
		if(user.getLastName() != null && user.getLastName().isEmpty()) user.setLastName(null);

		if(user.getTitleBefore() != null && user.getTitleBefore().isEmpty()) user.setTitleBefore(null);
		if(user.getTitleAfter() != null && user.getTitleAfter().isEmpty()) user.setTitleAfter(null);

		return user;
	}

	@Override
	public void changeNonAuthzPassword(PerunSession sess, User user, String m, String password, String lang) throws InternalErrorException, LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {

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
		Set<String> emails = new HashSet<>();

		try {
			Attribute a = perunBl.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
			if (a != null && a.getValue() != null) {
				emails.add((String)a.getValue());
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member member : members) {

			try {
				Attribute a = perunBl.getAttributesManagerBl().getAttribute(sess, member, AttributesManager.NS_MEMBER_ATTR_DEF+":mail");
				if (a != null && a.getValue() != null) {
					emails.add((String)a.getValue());
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
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

	@Override
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user) throws InternalErrorException {

		Vo vo = getPerunBl().getResourcesManagerBl().getVo(sess, resource);
		Member voMember;
		try {
			voMember = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, user);
		} catch (MemberNotExistsException e) {
			// user is not member of VO from this Resource -> No groups allowed
			return new ArrayList<>();
		}

		// Only valid members are considered for allowed groups
		if (!Status.VALID.equals(voMember.getStatus())) return new ArrayList<>();

		List<Group> assignedGroups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource, voMember);

		// no groups of member are assigned to such resource, can't be allowed
		if (assignedGroups.isEmpty()) return new ArrayList<>();

		// get and filter groups by removing all where user is not VALID group member
		List<Group> inactiveMembersGroups = getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsInactive(sess, voMember);
		assignedGroups.removeAll(inactiveMembersGroups);

		return assignedGroups;

	}

	@Override
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user) throws InternalErrorException {

		List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		Set<Group> groups = new HashSet<>();

		for (Resource resource : resources) {
			groups.addAll(getGroupsWhereUserIsActive(sess, resource, user));
		}

		return new ArrayList<>(groups);

	}

}
