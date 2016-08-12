package cz.metacentrum.perun.core.blImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;

import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.rt.EmptyPasswordRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordChangeFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDoesntMatchRuntimeException;
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
 */
public class UsersManagerBlImpl implements UsersManagerBl {

	private final static Logger log = LoggerFactory.getLogger(UsersManagerBlImpl.class);

	private UsersManagerImplApi usersManagerImpl;
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

	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException {
		return getUsersManagerImpl().getUserByUserExtSource(sess, userExtSource);
	}

	// FIXME do this in IMPL
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

	public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException {
		if ((extSourceType == null) || (login == null)) return new ArrayList<User>();

		return getUsersManagerImpl().getUsersByExtSourceTypeAndLogin(perunSession, extSourceType, login);
	}

	public List<User> getSpecificUsersByUser(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getSpecificUsersByUser(sess, user);
	}

	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) throws InternalErrorException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We are not support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().getUsersBySpecificUser(sess, specificUser);
	}

	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationNotExistsException, SpecificUserMustHaveOwnerException, SpecificUserOwnerAlreadyRemovedException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We are not support specific and sponsored users together yet.");
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

		getPerunBl().getAuditer().log(sess, "{} ownership was disabled for specificUser {}.", user, specificUser);
		getUsersManagerImpl().disableOwnership(sess, user, specificUser);
	}

	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationExistsException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We are not support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		if (user.getMajorSpecificType().equals(SpecificUserType.SERVICE)) throw new InternalErrorException("Service user can`t own another account (service or guest)!" + user);
		List<User> specificUserOwners = this.getUsersBySpecificUser(sess, specificUser);
		if(specificUserOwners.remove(user)) throw new RelationExistsException("User is already the active owner of specific user.");

		if(getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser)) {
			getUsersManagerImpl().enableOwnership(sess, user, specificUser);
			getPerunBl().getAuditer().log(sess, "{} ownership was enabled for specificUser {}.", user, specificUser);
		} else {
			getPerunBl().getAuditer().log(sess, "{} was added to owners of {}.", user, specificUser);
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

	public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We are not support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser);
	}

	public List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getSpecificUsers(sess);
	}

	public User getUserById(PerunSession sess, int id) throws InternalErrorException, UserNotExistsException {
		return getUsersManagerImpl().getUserById(sess, id);
	}

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

	public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException {
		ExtSource extSource = perunBl.getExtSourcesManagerBl().getExtSourceByName(sess, extSourceName);
		UserExtSource userExtSource = this.getUserExtSourceByExtLogin(sess, extSource, extLogin);

		return this.getUserByUserExtSource(sess, userExtSource);
	}

	public List<User> getUsers(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getUsers(sess);
	}

	public RichUser getRichUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers.get(0);
	}

	public RichUser getRichUserWithAttributes(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes =  this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes.get(0);
	}

	public List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users) throws InternalErrorException {
		List<RichUser> richUsers = new ArrayList<RichUser>();

		for (User user: users) {
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			RichUser richUser = new RichUser(user, userExtSources);
			richUsers.add(richUser);
		}
		return richUsers;
	}

	public List<RichUser> convertRichUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers)  throws InternalErrorException, UserNotExistsException {
		for (RichUser richUser: richUsers) {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, richUser.getId());
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, user);

			richUser.setUserAttributes(userAttributes);
		}

		return richUsers;
	}

	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.addAll(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers;
	}

	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException {
		List<User> users = new ArrayList<User>();
		users.addAll(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes;
	}


	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException {
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers;
	}

	public List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException {
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes = this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes;
	}

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
		getPerunBl().getAuditer().log(sess, "{} created.", user);

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

	public void deleteUser(PerunSession sess, User user) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, GroupOperationsException  {
		this.deleteUser(sess, user, false);
	}

	public void deleteUser(PerunSession sess, User user, boolean forceDelete) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, GroupOperationsException {
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
		getUsersManagerImpl().removeAllUserExtSources(sess, user);
		getPerunBl().getAuditer().log(sess, "All user ext sources removed for {}.", user);

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
			} catch (PasswordDeletionFailedException e) {
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
			} catch (PasswordDeletionFailedException e) {
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
		getPerunBl().getAuditer().log(sess, "{} deleted.", user);
	}

	public User updateUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		//Convert user to version with no empty strings in object attributes (null instead)
		user = this.convertUserEmptyStringsInObjectAttributesIntoNull(user);

		User beforeUpdatingUser = getPerunBl().getUsersManagerBl().getUserById(sess, user.getId());
		User afterUpdatingUser = getUsersManagerImpl().updateUser(sess, user);

		//Log only when something is changed
		if(!beforeUpdatingUser.equals(afterUpdatingUser)) getPerunBl().getAuditer().log(sess, "{} updated.", user);
		return afterUpdatingUser;
	}

	public User updateNameTitles(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		//Convert user to version with no empty strings in object attributes (null instead)
		user = this.convertUserEmptyStringsInObjectAttributesIntoNull(user);

		User beforeUpdatingUser = getPerunBl().getUsersManagerBl().getUserById(sess, user.getId());
		User afterUpdatingUser = getUsersManagerImpl().updateNameTitles(sess, user);

		//Log only when something is changed		
		// must audit like update user since it changes same object
		if(!beforeUpdatingUser.equals(afterUpdatingUser)) getPerunBl().getAuditer().log(sess, "{} updated.", user);
		return afterUpdatingUser;
	}

	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "{} updated.", userExtSource);
		return getUsersManagerImpl().updateUserExtSource(sess, userExtSource);
	}

	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		getUsersManagerImpl().updateUserExtSourceLastAccess(sess, userExtSource);
	}

	public List<UserExtSource> getUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		List<Integer> ueasIds = getUsersManagerImpl().getUserExtSourcesIds(sess, user);
		List<UserExtSource> ueas = getUsersManagerImpl().getUserExtsourcesByIds(sess, ueasIds);

		return ueas;
	}

	public List<UserExtSource> getUserExtsourcesByIds(PerunSession sess, List<Integer> ids) throws InternalErrorException {
		return getUsersManagerImpl().getUserExtsourcesByIds(sess, ids);
	}

	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceById(sess, id);
	}

	public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) throws InternalErrorException {
		return getUsersManagerImpl().getAllUserExtSourcesByTypeAndLogin(sess, extType, extLogin);
	}

	public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getActiveUserExtSources(sess, user);
	}

	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException {
		// Check if the userExtSource already exists
		try {
			getUsersManagerImpl().checkUserExtSourceExists(sess, userExtSource);
			throw new UserExtSourceExistsException("UserExtSource " + userExtSource + " already exists.");
		} catch (UserExtSourceNotExistsException e) {
			// this is OK
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
		getPerunBl().getAuditer().log(sess, "{} added to {}.", userExtSource, user);
		return userExtSource;
	}

	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException {
		//FIXME zkontrolovat zda na userExtSource neni navazan nejaky member
		getUsersManagerImpl().removeUserExtSource(sess, user, userExtSource);
		getPerunBl().getAuditer().log(sess, "{} removed from {}.", userExtSource, user);
	}

	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceByExtLogin(sess, source, extLogin);
	}

	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getVosWhereUserIsAdmin(sess, user);
	}

	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getGroupsWhereUserIsAdmin(sess, user);
	}

	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().getVosWhereUserIsMember(sess, user);
	}

	public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersWithoutVoAssigned(sess);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException  {
		return usersManagerImpl.getUsersWithoutVoAssigned(sess);
	}

	public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {
		List<User> allSearchingUsers = new ArrayList<User>();
		List<User> allVoUsers = new ArrayList<User>();
		allSearchingUsers = this.findUsers(sess, searchString);
		allVoUsers = getUsersManagerImpl().getUsersByVo(sess, vo);
		allSearchingUsers.removeAll(allVoUsers);
		return allSearchingUsers;
	}

	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		return getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);
	}

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

	public List<Resource> getAssignedResources(PerunSession sess, User user) throws InternalErrorException {
		Set<Resource> resources = new HashSet<Resource>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);

		for(Member member : members) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member));
		}
		return new ArrayList<Resource>(resources);
	}

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

	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
		return this.getUsersManagerImpl().getUsersByAttribute(sess, attribute);
	}

	/**
	 * Search attributes directly in the DB only if the attr is def or opt and value is type of String, otherwise load all users and search in a loop.
	 */
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

	public List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsers(sess, searchString);
	}

	public List<RichUser> findRichUsers(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsers(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	public List<RichUser> findRichUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	public List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByName(sess, searchString);
	}

	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException {
		// Convert to lower case
		titleBefore = titleBefore.toLowerCase();
		firstName = firstName.toLowerCase();
		middleName = middleName.toLowerCase();
		lastName = lastName.toLowerCase();
		titleAfter = titleAfter.toLowerCase();

		return this.getUsersManagerImpl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
	}

	public List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByExactName(sess, searchString);
	}

	public List<User> findUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException {
		return this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
	}

	public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) throws InternalErrorException {
		return getUsersManagerImpl().getUsersByIds(sess, usersIds);
	}

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

	public void checkUserExists(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		getUsersManagerImpl().checkUserExists(sess, user);
	}

	public void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException {
		getUsersManagerImpl().checkReservedLogins(sess, namespace, login);
	}

	public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException {
		getUsersManagerImpl().checkUserExtSourceExists(sess, userExtSource);
	}

	public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		return getUsersManagerImpl().userExtSourceExists(sess, userExtSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			AuthzResolverBlImpl.setRole(sess, user, null, Role.PERUNADMIN);
		} catch (AlreadyAdminException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		return getUsersManagerImpl().isUserPerunAdmin(sess, user);
	}

	public RichUser filterOnlyAllowedAttributes(PerunSession sess, RichUser richUser) throws InternalErrorException {
		if(richUser == null) throw new InternalErrorException("RichUser can't be null.");
		//Filtering users attributes
		if(richUser.getUserAttributes() != null) {
			List<Attribute> userAttributes = richUser.getUserAttributes();
			List<Attribute> allowedUserAttributes = new ArrayList<Attribute>();
			for(Attribute userAttr: userAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, userAttr, richUser, null)) {
					userAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, userAttr, richUser, null));
					allowedUserAttributes.add(userAttr);
				}
			}
			richUser.setUserAttributes(allowedUserAttributes);
		}
		return richUser;
	}

	public List<RichUser> filterOnlyAllowedAttributes(PerunSession sess, List<RichUser> richUsers) throws InternalErrorException {
		List<RichUser> filteredRichUsers = new ArrayList<RichUser>();
		if(richUsers == null || richUsers.isEmpty()) return filteredRichUsers;

		for(RichUser ru: richUsers) {
			filteredRichUsers.add(this.filterOnlyAllowedAttributes(sess, ru));
		}

		return filteredRichUsers;
	}

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
			List<Member> members = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
			List<User> usersFromResource = new ArrayList<User>();
			for(Member memberElement: members) {
				usersFromResource.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
			for(User userElement: usersFromResource) {
				users.add(userElement);
			}
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
	public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException {

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
	public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException {
		log.info("Reserving password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Reserve the password
		try {
			this.managePassword(sess, PASSWORD_RESERVE, (String) userLogin, loginNamespace, password);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
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
	public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PasswordCreationFailedException, LoginNotExistsException {
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

			} else if (loginNamespace.equals("sagrid")) {

				List<String> kerberosLogins = new ArrayList<String>();

				ExtSource extSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, "SAGRID");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@SAGRID");
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

				if (!kerberosLogins.contains(userLogin + "@SAGRID")) {
					kerberosLogins.add(userLogin + "@SAGRID");
					kerberosLoginsAttr.setValue(kerberosLogins);
					getPerunBl().getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}

			} else if (loginNamespace.equals("ics.muni.cz")) {

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
	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException, LoginNotExistsException,
			PasswordDeletionFailedException {
		log.info("Deleting password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Delete the password
		try {
			this.managePassword(sess, PASSWORD_DELETE, (String) userLogin, loginNamespace, null);
		} catch (PasswordDeletionFailedRuntimeException e) {
			throw new PasswordDeletionFailedException(e);
		} catch (LoginNotExistsRuntimeException e) {
			throw new LoginNotExistsException(e);
		}
	}

	/**
	 * Method which calls external program for password change.
	 */
	public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword)
			throws InternalErrorException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException {
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
			}
		}

		// Change the password
		try {
			this.managePassword(sess, PASSWORD_CHANGE, (String) userLogin.getValue(), loginNamespace, newPassword);
		} catch (PasswordChangeFailedRuntimeException e) {
			throw new PasswordChangeFailedException(e);
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
		ProcessBuilder pb = new ProcessBuilder(BeansUtils.getPropertyFromConfiguration("perun.passwordManager.program"),
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
		ProcessBuilder pb = new ProcessBuilder(BeansUtils.getPropertyFromConfiguration("perun.alternativePasswordManager.program"), operation, loginNamespace, Integer.toString(user.getId()), passwordId);

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
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
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



	public List<RichUser> convertUsersToRichUsersWithAttributesByNames(PerunSession sess, List<User> users, List<String> attrNames) throws InternalErrorException {

		// TODO - optimzization needed - at least there should be single select on RichUser object in impl !!
		List<RichUser> result = new ArrayList<RichUser>();
		for (User u : users) {
			RichUser ru = new RichUser(u, getUserExtSources(sess, u));
			ru.setUserAttributes(this.getPerunBl().getAttributesManagerBl().getAttributes(sess, u, attrNames));
			result.add(ru);
		}
		return result;

	}

	public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, findRichUsers(sess, searchString));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsers(sess, searchString), attrsName);
		}

	}

	public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, findRichUsersByExactMatch(sess, searchString));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsersByExactMatch(sess, searchString), attrsName);
		}

	}

	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException, VoNotExistsException{

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutSpecificVo(sess, vo, searchString)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutSpecificVo(sess, vo, searchString), attrsName);
		}
	}

	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, VoNotExistsException, UserNotExistsException{

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutVoAssigned(sess)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutVoAssigned(sess), attrsName);
		}
	}

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

	public List<RichUser> getAllRichUsersWithAllNonVirutalAttributes(PerunSession sess) throws InternalErrorException {
		List<Pair<User, Attribute>> usersWithNonVirtAttrs = usersManagerImpl.getAllRichUsersWithAllNonVirutalAttributes(sess);
		Map<User,List<Attribute>> sortingMap = new HashMap<User, List<Attribute>>();

		//User map for sorting users with all their attributes
		for(Pair<User, Attribute> p: usersWithNonVirtAttrs) {
			if(sortingMap.containsKey(p.getLeft())) {
				sortingMap.get(p.getLeft()).add(p.getRight());
			} else {
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(p.getRight());
				sortingMap.put(p.getLeft(), attributes);
			}
		}

		//Add extSources and VirtualAttributes
		List<RichUser> richUsersWithAttributes = new ArrayList<RichUser>();
		Set<User> usersSet = sortingMap.keySet();
		for(User u: usersSet) {
			//Without UserExtSources and VirtualAttributes
			//List<UserExtSource> ues = getPerunBl().getUsersManagerBl().getUserExtSources(sess, u);
			List<Attribute> allAttrsOfUser = sortingMap.get(u);
			RichUser ru = new RichUser(u, null, allAttrsOfUser);
			richUsersWithAttributes.add(ru);
		}

		return richUsersWithAttributes;
	}


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

	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email) throws InternalErrorException, UserNotExistsException {

		int changeId = getUsersManagerImpl().requestPreferredEmailChange(sess, user, email);
		Utils.sendValidationEmail(user, url, email, changeId);

	}

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
	public void changeNonAuthzPassword(PerunSession sess, User user, String m, String password) throws InternalErrorException, UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException {

		String requestId = Utils.cipherInput(m, true);
		String namespace = getUsersManagerImpl().loadPasswordResetRequest(user, Integer.parseInt(requestId));

		if (namespace.isEmpty()) throw new InternalErrorException("Password reset request is not valid anymore or doesn't existed at all.");

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

		for (String email : emails) {
			Utils.sendPasswordResetConfirmationEmail(user, email, namespace);
		}

	}

	public int getUsersCount(PerunSession sess) throws InternalErrorException {
		return getUsersManagerImpl().getUsersCount(sess);
	}

	@Override
	public Map<String,String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws InternalErrorException {
		return getUsersManagerImpl().generateAccount(session, namespace, parameters);
	}

	private PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) throws InternalErrorException {
		return getUsersManagerImpl().getPasswordManagerModule(session, namespace);
	}

}
