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
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordChangeFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDoesntMatchException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkExpiredException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkNotValidException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
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
import cz.metacentrum.perun.core.impl.modules.pwdmgr.GenericPasswordManagerModule;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	private final static Set<String> extSourcesWithMultipleIdentifiers = BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();


	/**
	 * Constructor.
	 *
	 * @param usersManagerImpl connection pool
	 */
	public UsersManagerBlImpl(UsersManagerImplApi usersManagerImpl) {
		this.usersManagerImpl = usersManagerImpl;
	}

	@Override
	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws UserNotExistsException {
		return getUsersManagerImpl().getUserByUserExtSource(sess, userExtSource);
	}

	// FIXME do this in IMPL
	@Override
	public User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws UserNotExistsException {
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
	public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) {
		if ((extSourceType == null) || (login == null)) return new ArrayList<>();

		return getUsersManagerImpl().getUsersByExtSourceTypeAndLogin(perunSession, extSourceType, login);
	}

	@Override
	public List<User> getSpecificUsersByUser(PerunSession sess, User user) {
		return getUsersManagerImpl().getSpecificUsersByUser(sess, user);
	}

	@Override
	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().getUsersBySpecificUser(sess, specificUser);
	}

	@Override
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException {
		this.removeSpecificUserOwner(sess, user, specificUser, false);
	}

	@Override
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser, boolean forceDelete) throws RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException {
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
			if(specificUser.isSponsoredUser()) AuthzResolverBlImpl.removeSpecificUserOwner(sess, specificUser, user);
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
	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws RelationExistsException {
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
			if(specificUser.isSponsoredUser()) AuthzResolverBlImpl.addSpecificUserOwner(sess, specificUser, user);
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
	public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) {
		if(specificUser.isServiceUser() && specificUser.isSponsoredUser()) throw new InternalErrorException("We don't support specific and sponsored users together yet.");
		if(specificUser.getMajorSpecificType().equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Incorrect type of specification for specific user!" + specificUser);
		return getUsersManagerImpl().specificUserOwnershipExists(sess, user, specificUser);
	}

	@Override
	public List<User> getSpecificUsers(PerunSession sess) {
		return getUsersManagerImpl().getSpecificUsers(sess);
	}

	@Override
	public User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner) throws RelationExistsException {
		if(specificUserType.equals(SpecificUserType.SPONSORED)) {
			throw new InternalErrorException("We don't support sponsored users anymore.");
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
	public User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType) {
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
	public User getUserById(PerunSession sess, int id) throws UserNotExistsException {
		return getUsersManagerImpl().getUserById(sess, id);
	}

	@Override
	public User getUserByMember(PerunSession sess, Member member) {
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
	public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException {
		ExtSource extSource = perunBl.getExtSourcesManagerBl().getExtSourceByName(sess, extSourceName);
		UserExtSource userExtSource = this.getUserExtSourceByExtLogin(sess, extSource, extLogin);

		return this.getUserByUserExtSource(sess, userExtSource);
	}

	@Override
	public List<User> getUsers(PerunSession sess) {
		return getUsersManagerImpl().getUsers(sess);
	}

	@Override
	public RichUser getRichUser(PerunSession sess, User user) {
		List<User> users = new ArrayList<>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return richUsers.get(0);
	}

	@Override
	public RichUser getRichUserWithAttributes(PerunSession sess, User user) throws UserNotExistsException {
		List<User> users = new ArrayList<>();
		users.add(user);
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		List<RichUser> richUsersWithAttributes =  this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
		return richUsersWithAttributes.get(0);
	}

	@Override
	public List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users) {
		List<RichUser> richUsers = new ArrayList<>();

		for (User user: users) {
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			RichUser richUser = new RichUser(user, userExtSources);
			richUsers.add(richUser);
		}
		return richUsers;
	}

	@Override
	public List<RichUser> convertRichUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers)  throws UserNotExistsException {
		for (RichUser richUser: richUsers) {
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richUser);

			richUser.setUserAttributes(userAttributes);
		}

		return richUsers;
	}

	@Override
	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) {
		List<User> users = new ArrayList<>(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		return this.convertUsersToRichUsers(sess, users);
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws UserNotExistsException {
		List<User> users = new ArrayList<>(this.getUsers(sess));
		if(!includedSpecificUsers) users.removeAll(this.getSpecificUsers(sess));
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
	}

	@Override
	public List<RichUser> getRichUsersByIds(PerunSession sess, List<Integer> ids) {
		List<User> users = this.getUsersByIds(sess, ids);
		return this.convertUsersToRichUsers(sess, users);
	}

	@Override
	public List<RichUser> getRichUsersWithAttributesByIds(PerunSession sess, List<Integer> ids) throws UserNotExistsException {
		List<User> users = this.getUsersByIds(sess, ids);
		return this.getRichUsersWithAttributesFromListOfUsers(sess, users);
	}

	@Override
	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) {
		return this.convertUsersToRichUsers(sess, users);
	}

	@Override
	public List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws UserNotExistsException {
		List<RichUser> richUsers = this.convertUsersToRichUsers(sess, users);
		return this.convertRichUsersToRichUsersWithAttributes(sess, richUsers);
	}

	@Override
	public List<RichUser> convertUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers, List<AttributeDefinition> attrsDef) {
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
	public User createUser(PerunSession sess, User user) {

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
	public void deleteUser(PerunSession sess, User user) throws RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		this.deleteUser(sess, user, false);
	}

	@Override
	public void deleteUser(PerunSession sess, User user, boolean forceDelete) throws RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		try {
			this.deleteUser(sess, user, forceDelete, false);
		} catch (AnonymizationNotSupportedException ex) {
			//this shouldn't happen with 'anonymizedInstead' set to false
			throw new InternalErrorException(ex);
		}
	}

	private void deleteUser(PerunSession sess, User user, boolean forceDelete, boolean anonymizeInstead) throws RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, AnonymizationNotSupportedException {
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
			} catch (InvalidLoginException e) {
				throw new InternalErrorException("We are deleting login of user, but its syntax is not allowed by namespace configuration.", e);
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

		// Remove all possible passwords associated with logins (stored in attributes)
		for (Attribute loginAttribute: getPerunBl().getAttributesManagerBl().getLogins(sess, user)) {
			try {
				this.deletePassword(sess, (String) loginAttribute.getValue(), loginAttribute.getFriendlyNameParameter());
			} catch (LoginNotExistsException e) {
				// OK - User hasn't assigned any password with this login
			} catch (InvalidLoginException e) {
				throw new InternalErrorException("We are deleting login of user, but its syntax is not allowed by namespace configuration.", e);
			} catch (PasswordDeletionFailedException | PasswordOperationTimeoutException e) {
				if (forceDelete) {
					log.error("Error during deletion of the account at {} for user {} with login {}.", loginAttribute.getFriendlyNameParameter(), user, loginAttribute.getValue());
				} else {
					throw new RelationExistsException("Error during deletion of the account at " + loginAttribute.getFriendlyNameParameter() +
							" for user " + user + " with login " + loginAttribute.getValue() + ".");
				}
			}
		}


		// Delete, keep or anonymize assigned attributes
		try {
			// User-Facilities one
			getPerunBl().getAttributesManagerBl().removeAllUserFacilityAttributes(sess, user);

			// Users one
			if (anonymizeInstead) {
				List<String> attributesToAnonymize = BeansUtils.getCoreConfig().getAttributesToAnonymize();
				List<String> attributesToKeep = BeansUtils.getCoreConfig().getAttributesToKeep();
				List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, user);
				for (Attribute attribute : userAttributes) {
					// Skip core and virtual attributes
					if (getPerunBl().getAttributesManagerBl().isCoreAttribute(sess, attribute) ||
					    getPerunBl().getAttributesManagerBl().isVirtAttribute(sess, attribute)) {
						continue;
					}
					// Skip attributes configured to keep untouched
					if (attributesToKeep.contains(attribute.getName()) ||
						// Attributes like 'login-namespace:mu' are configured as 'login-namespace:*'
						(!attribute.getFriendlyNameParameter().isEmpty() &&
						 attributesToKeep.contains(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName() + ":*"))) {
						continue;
					}
					// Anonymize configured attributes
					if (attributesToAnonymize.contains(attribute.getName()) ||
						(!attribute.getFriendlyNameParameter().isEmpty() &&
						 attributesToAnonymize.contains(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName() + ":*"))) {

						Attribute anonymized = getPerunBl().getAttributesManagerBl().getAnonymizedValue(sess, user, attribute);
						getPerunBl().getAttributesManagerBl().setAttribute(sess, user, anonymized);
					} else {
						// Delete remaining attributes
						getPerunBl().getAttributesManagerBl().removeAttribute(sess, user, attribute);
					}
				}
			} else {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, user);
			}
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException | WrongAttributeAssignmentException ex) {
			//All members are deleted => there are no required attributes => all attributes can be removed
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
		if (anonymizeInstead) {
			getUsersManagerImpl().anonymizeUser(sess, user);

			// delete all users applications and submitted data, this is needed only when 'anonymizeInstead'
			// because applications are deleted on cascade when user's row is deleted in DB
			getUsersManagerImpl().deleteUsersApplications(user);
		} else {
			// Finally delete the user
			getUsersManagerImpl().deleteUser(sess, user);
			getPerunBl().getAuditer().log(sess, new UserDeleted(user));
		}
	}

	@Override
	public void anonymizeUser(PerunSession sess, User user) throws RelationExistsException, AnonymizationNotSupportedException {
		try {
			this.deleteUser(sess, user, false, true);
		} catch (MemberAlreadyRemovedException | UserAlreadyRemovedException | SpecificUserAlreadyRemovedException ex) {
			//this shouldn't happen with 'anonymizedInstead' set to true
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public User updateUser(PerunSession sess, User user) throws UserNotExistsException {
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
	public User updateNameTitles(PerunSession sess, User user) throws UserNotExistsException {
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
	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws UserExtSourceExistsException {
		UserExtSource updatedUes = getUsersManagerImpl().updateUserExtSource(sess, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
		return updatedUes;
	}

	@Override
	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) {
		getUsersManagerImpl().updateUserExtSourceLastAccess(sess, userExtSource);
		getPerunBl().getAuditer().log(sess, new UserExtSourceUpdated(userExtSource));
	}

	@Override
	public List<UserExtSource> getUserExtSources(PerunSession sess, User user) {
		return getUsersManagerImpl().getUserExtSources(sess, user);
	}

	@Override
	public List<RichUserExtSource> getRichUserExtSources(PerunSession sess, User user, List<String> attrsNames) {
		return getUserExtSources(sess, user).stream()
				.map(ues -> new RichUserExtSource(ues,
						attrsNames == null ? getPerunBl().getAttributesManagerBl().getAttributes(sess, ues)
								: getPerunBl().getAttributesManagerBl().getAttributes(sess, ues, attrsNames)))
				.collect(Collectors.toList());
	}

	@Override
	public List<RichUserExtSource> filterOnlyAllowedAttributesForRichUserExtSources(PerunSession sess, List<RichUserExtSource> richUserExtSources) {
		richUserExtSources.forEach(rues -> rues.setAttributes(
				AuthzResolverBlImpl.filterNotAllowedAttributes(sess, rues.asUserExtSource(), rues.getAttributes())));
		return richUserExtSources;
	}

	@Override
	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceById(sess, id);
	}

	@Override
	public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue) throws AttributeNotExistsException, UserExtSourceNotExistsException {
		if(attrId <= 0) throw new InternalErrorException("Unexpected attribute Id with zero or negative value.");
		AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinitionById(sess, attrId);
		return getUserExtSourceByUniqueAttributeValue(sess, attrDef, uniqueValue);

	}

	@Override
	public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, String attrName, String uniqueValue) throws AttributeNotExistsException, UserExtSourceNotExistsException {
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
	private UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, AttributeDefinition attrDef, String uniqueValue) throws UserExtSourceNotExistsException {
		if(!attrDef.getNamespace().startsWith(AttributesManager.NS_UES_ATTR)) throw new InternalErrorException("Attribute definition has to be from 'ues' namespace: " + attrDef);
		if(!attrDef.isUnique()) throw new InternalErrorException("Attribute definition has to be unique: " + attrDef);
		if(uniqueValue == null || uniqueValue.isEmpty()) throw new InternalErrorException("Can't find userExtSource by empty value!");

		return usersManagerImpl.getUserExtSourceByUniqueAttributeValue(sess, attrDef.getId(), uniqueValue);
	}

	@Override
	public UserExtSource getUserExtSourceFromMultipleIdentifiers(PerunSession sess, PerunPrincipal principal) throws UserExtSourceNotExistsException {
		String additionalIdentifiers = principal.getAdditionalInformations().get(ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
		if (additionalIdentifiers == null) {
			throw new InternalErrorException("Entry " + ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME + " is not defined in the principal's additional information. Either it was not provided by external source used for sign-in or the mapping configuration is wrong.");
		}
		UserExtSource ues = null;
		for(String identifier : additionalIdentifiers.split(MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX)) {
			try {
				ues = perunBl.getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME, identifier);
				log.debug("UserExtSource found using additional identifiers: " + ues);
				break;
			} catch (UserExtSourceNotExistsException ex) {
				//try to find user ext source using different identifier in the next iteration of for cycle
			} catch (AttributeNotExistsException ex) {
				String errorMessage = "Mandatory attribute is not defined: ".concat(ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
				log.error(errorMessage);
				throw new InternalErrorException(errorMessage, ex);
			}
		}
		if (ues == null) throw new UserExtSourceNotExistsException("User ext source was not found. Searched value is any from \"" + additionalIdentifiers + "\" in " + ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
		return ues;
	}

	@Override
	public List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids) {
		return getUsersManagerImpl().getUserExtSourcesByIds(sess, ids);
	}

	@Override
	public User getUserByExtSourceInformation(PerunSession sess, PerunPrincipal principal) throws UserExtSourceNotExistsException, UserNotExistsException, ExtSourceNotExistsException {
		String shibIdentityProvider = principal.getAdditionalInformations().get(ORIGIN_IDENTITY_PROVIDER_KEY);
		if(shibIdentityProvider != null && extSourcesWithMultipleIdentifiers.contains(shibIdentityProvider)) {
			UserExtSource ues = getUserExtSourceFromMultipleIdentifiers(sess, principal);
			return getUserByUserExtSource(sess, ues);
		} else {
			return getUserByExtSourceNameAndExtLogin(sess, principal.getExtSourceName(), principal.getActor());
		}
	}

	@Override
	public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) {
		return getUsersManagerImpl().getAllUserExtSourcesByTypeAndLogin(sess, extType, extLogin);
	}

	@Override
	public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) {
		return getUsersManagerImpl().getActiveUserExtSources(sess, user);
	}

	@Override
	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws UserExtSourceExistsException {
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
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws UserExtSourceAlreadyRemovedException {
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
	public void moveUserExtSource(PerunSession sess, User sourceUser, User targetUser, UserExtSource userExtSource) {
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
	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws UserExtSourceNotExistsException {
		return getUsersManagerImpl().getUserExtSourceByExtLogin(sess, source, extLogin);
	}

	@Override
	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) {
		return getUsersManagerImpl().getVosWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) {
		return getUsersManagerImpl().getGroupsWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user) {
		return getUsersManagerImpl().getGroupsWhereUserIsAdmin(sess, vo, user);
	}

	@Override
	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) {
		return getUsersManagerImpl().getVosWhereUserIsMember(sess, user);
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws UserNotExistsException {
		List<User> users = this.getUsersWithoutVoAssigned(sess);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<User> getUsersWithoutVoAssigned(PerunSession sess) {
		return usersManagerImpl.getUsersWithoutVoAssigned(sess);
	}

	@Override
	public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) {
		List<User> allSearchingUsers = this.findUsers(sess, searchString);
		List<User> allVoUsers = getUsersManagerImpl().getUsersByVo(sess, vo);
		allSearchingUsers.removeAll(allVoUsers);
		return allSearchingUsers;
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) {
		return getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) {
		return getUsersManagerImpl().getAssignedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, User user) {
		return getUsersManagerImpl().getAllowedResources(sess, user);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, User user) {
		return getUsersManagerImpl().getAssignedResources(sess, user);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) {
		return getUsersManagerImpl().getAssignedRichResources(sess, user);
	}

	private List<User> getUsersByVirtualAttribute(PerunSession sess, AttributeDefinition attributeDef, String attributeValue) {
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
	public List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue) {
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
	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) {
		return this.getUsersManagerImpl().getUsersByAttribute(sess, attribute);
	}

	/**
	 * Search attributes directly in the DB only if the attr is def or opt and value is type of String, otherwise load all users and search in a loop.
	 */
	@Override
	public List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue) {
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
	public List<User> findUsers(PerunSession sess, String searchString) {
		return this.getUsersManagerImpl().findUsers(sess, searchString);
	}

	@Override
	public List<RichUser> findRichUsers(PerunSession sess, String searchString) throws UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsers(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<RichUser> findRichUsersByExactMatch(PerunSession sess, String searchString) throws UserNotExistsException {
		List<User> users = this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
		return this.convertRichUsersToRichUsersWithAttributes(sess, this.convertUsersToRichUsers(sess, users));
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String searchString) {
		return this.getUsersManagerImpl().findUsersByName(sess, searchString);
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) {
		// Convert to lower case
		titleBefore = titleBefore.toLowerCase();
		firstName = firstName.toLowerCase();
		middleName = middleName.toLowerCase();
		lastName = lastName.toLowerCase();
		titleAfter = titleAfter.toLowerCase();

		return this.getUsersManagerImpl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
	}

	@Override
	public List<User> findUsersByExactName(PerunSession sess, String searchString) {
		return this.getUsersManagerImpl().findUsersByExactName(sess, searchString);
	}

	public List<User> findUsersByExactMatch(PerunSession sess, String searchString) {
		return this.getUsersManagerImpl().findUsersByExactMatch(sess, searchString);
	}

	@Override
	public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) {
		return getUsersManagerImpl().getUsersByIds(sess, usersIds);
	}

	@Override
	public boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InvalidLoginException {
		if (loginNamespace == null || login == null) {
			throw new InternalErrorException(new NullPointerException("loginNamespace cannot be null, nor login"));
		}

		try {

			// fake attribute
			AttributeDefinition attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + loginNamespace);
			Attribute attribute = new Attribute(attributeDefinition);

			attribute.setValue(login);

			// Create empty user
			User user = new User();

			// check if login is allowed (has valid syntax and is not prohibited)
			getPasswordManagerModule(sess,loginNamespace).checkLoginFormat(sess, login);

			// Check attribute value, if the login is already occupied, then WrongReferenceAttributeValueException exception is thrown
			getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, user, attribute);

			return true;
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			return false;
		}

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
	public void checkUserExists(PerunSession sess, User user) throws UserNotExistsException {
		getUsersManagerImpl().checkUserExists(sess, user);
	}

	@Override
	public void checkReservedLogins(PerunSession sess, String namespace, String login) throws AlreadyReservedLoginException {
		getUsersManagerImpl().checkReservedLogins(sess, namespace, login);
	}

	@Override
	public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws UserExtSourceNotExistsException {
		getUsersManagerImpl().checkUserExtSourceExists(sess, userExtSource);
	}

	@Override
	public void checkUserExtSourceExistsById(PerunSession sess, int id) throws UserExtSourceNotExistsException {
		getUsersManagerImpl().checkUserExtSourceExistsById(sess, id);
	}

	@Override
	public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) {
		return getUsersManagerImpl().userExtSourceExists(sess, userExtSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public boolean isUserPerunAdmin(PerunSession sess, User user) {
		return getUsersManagerImpl().isUserPerunAdmin(sess, user);
	}

	@Override
	public RichUser filterOnlyAllowedAttributes(PerunSession sess, RichUser richUser) {
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
	public List<RichUser> filterOnlyAllowedAttributes(PerunSession sess, List<RichUser> richUsers) {
		List<RichUser> filteredRichUsers = new ArrayList<>();
		if(richUsers == null || richUsers.isEmpty()) return filteredRichUsers;

		for(RichUser ru: richUsers) {
			filteredRichUsers.add(this.filterOnlyAllowedAttributes(sess, ru));
		}

		return filteredRichUsers;
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Group group) {
		List<User> users = new ArrayList<>();
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for(Member memberElement: members) {
			users.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
		}
		return users;
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Member member) {
		return Collections.singletonList(getPerunBl().getUsersManagerBl().getUserByMember(sess, member));
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Resource resource) {
		return getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Host host) {
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		return getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Facility facility) {
		return getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getUsersByPerunBean(PerunSession sess, Vo vo) {
		List<User> users = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		for(Member memberElement: members) {
			users.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
		}
		return users;
	}

	@Override
	public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException {

		log.info("Reserving password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
			try {
				module.reserveRandomPassword(sess, attr.valueAsString());
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			} catch (PasswordStrengthFailedRuntimeException e) {
				throw new PasswordStrengthFailedException(e);
			} catch (InvalidLoginException e) {
				throw e;
			} catch (Exception ex) {
				// fallback for exception compatibility
				throw new PasswordCreationFailedException("Password creation failed for " + loginNamespace + ":" + attr.valueAsString() + ".", ex);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws
			PasswordCreationFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException {
		log.info("Reserving password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Reserve the password
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
		try {
			module.reservePassword(sess, userLogin, password);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
		} catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		} catch (PasswordStrengthFailedRuntimeException e) {
			throw new PasswordStrengthFailedException(e);
		} catch (InvalidLoginException | PasswordStrengthException e) {
			throw e;
		} catch (Exception ex) {
			// fallback for exception compatibility
			throw new PasswordCreationFailedException("Password creation failed for " + loginNamespace + ":" + userLogin + ".", ex);
		}
	}

	@Override
	public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws
			PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException {
		log.info("Reserving password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Create the password
			PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
			try {
				module.reservePassword(sess, attr.valueAsString(), password);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			} catch (PasswordStrengthFailedRuntimeException e) {
				throw new PasswordStrengthFailedException(e);
			} catch (InvalidLoginException | PasswordStrengthException e) {
				throw e;
			} catch (Exception ex) {
				// fallback for exception compatibility
				throw new PasswordCreationFailedException("Password creation failed for " + loginNamespace + ":" + attr.valueAsString() + ".", ex);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, String loginNamespace) throws
			PasswordCreationFailedException, InvalidLoginException {
		log.info("Validating password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Validate the password
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
		try {
			module.validatePassword(sess, userLogin, null);
		} catch (PasswordCreationFailedRuntimeException e) {
			throw new PasswordCreationFailedException(e);
		}
	}

	@Override
	public void validatePassword(PerunSession sess, User user, String loginNamespace) throws
			PasswordCreationFailedException, LoginNotExistsException, InvalidLoginException {
		log.info("Validating password for {} in login-namespace {}.", user, loginNamespace);

		// Get login.
		try {
			Attribute attr = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace);

			if (attr.getValue() == null) {
				throw new LoginNotExistsException("Attribute containing login has empty value. Namespace: " + loginNamespace);
			}

			// Validate the password
			PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
			try {
				module.validatePassword(sess, attr.valueAsString(), user);
			} catch (PasswordCreationFailedRuntimeException e) {
				throw new PasswordCreationFailedException(e);
			}
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws LoginNotExistsException,
			PasswordDeletionFailedException, PasswordOperationTimeoutException, InvalidLoginException {
		log.info("Deleting password for {} in login-namespace {}.", userLogin, loginNamespace);

		// Delete the password
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
		try {
			module.deletePassword(sess, userLogin);
		} catch (PasswordDeletionFailedRuntimeException e) {
			throw new PasswordDeletionFailedException(e);
		} catch (LoginNotExistsRuntimeException e) {
			throw new LoginNotExistsException(e);
		}  catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		} catch (InvalidLoginException e) {
			throw e;
		} catch (Exception ex) {
			// fallback for exception compatibility
			throw new PasswordDeletionFailedException("Password deletion failed for " + loginNamespace + ":" + userLogin + ".", ex);
		}
	}

	@Override
	public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword)
			throws LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException {
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

		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);

		// Check password if it was requested
		if (checkOldPassword) {
			try {
				module.checkPassword(sess, userLogin.valueAsString(), oldPassword);
			} catch (PasswordDoesntMatchRuntimeException e) {
				throw new PasswordDoesntMatchException(e);
			} catch (PasswordOperationTimeoutRuntimeException e) {
				throw new PasswordOperationTimeoutException(e);
			} catch (Exception ex) {
				// fallback for exception compatibility
				throw new PasswordDoesntMatchException("Old password doesn't match for " + loginNamespace + ":" + userLogin + ".", ex);
			}
		}

		// Change the password
		try {
			module.changePassword(sess, userLogin.valueAsString(), newPassword);
		} catch (PasswordChangeFailedRuntimeException e) {
			throw new PasswordChangeFailedException(e);
		} catch (PasswordOperationTimeoutRuntimeException e) {
			throw new PasswordOperationTimeoutException(e);
		} catch (PasswordStrengthFailedRuntimeException e) {
			throw new PasswordStrengthFailedException(e);
		} catch (InvalidLoginException | PasswordStrengthException e) {
			throw e;
		} catch (Exception ex) {
			// fallback for exception compatibility
			throw new PasswordChangeFailedException("Password change failed for " + loginNamespace + ":" + userLogin + ".", ex);
		}

		//validate and set user ext sources
		try {
			this.validatePassword(sess, user, loginNamespace);
		} catch(PasswordCreationFailedException ex) {
			throw new PasswordChangeFailedException(ex);
		}
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace, String password) throws PasswordCreationFailedException, LoginNotExistsException, PasswordStrengthException {

		String passwordId = Long.toString(System.currentTimeMillis());
		log.info("Creating alternative password for {} in login-namespace {} with description {} and passwordId {}.", user, loginNamespace, description, passwordId);

		try {
			Attribute userAlternativePassword = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_ALT_PASSWORD_NAMESPACE + loginNamespace);
			Map<String,String> altPassValue = new LinkedHashMap<>();
			//Set not null value from altPassword attribute of this user
			if (userAlternativePassword.getValue() != null) altPassValue = userAlternativePassword.valueAsMap();
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

		// actually create password in the backend
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);

		try {
			module.createAlternativePassword(sess, user, passwordId, password);
		} catch(PasswordCreationFailedRuntimeException ex) {
			throw new PasswordCreationFailedException(ex);
		} catch(LoginNotExistsRuntimeException ex) {
			throw new LoginNotExistsException(ex);
		} catch (PasswordStrengthException e) {
			throw e;
		} catch (Exception ex) {
			// fallback for exception compatibility
			throw new PasswordCreationFailedException("Alternative password creation failed for " + loginNamespace + ":" + passwordId + " of "+user+".", ex);
		}
	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId) throws PasswordDeletionFailedException, LoginNotExistsException {
		log.info("Deleting alternative password for {} in login-namespace {} with passwordId {}.", user, loginNamespace, passwordId);

		try {
			Attribute userAlternativePassword = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_ALT_PASSWORD_NAMESPACE + loginNamespace);
			Map<String,String> altPassValue = new LinkedHashMap<>();
			//Set not null value from altPassword attribute of this user
			if (userAlternativePassword.getValue() != null) altPassValue = userAlternativePassword.valueAsMap();
			//If password already exists, throw an exception
			if (!altPassValue.containsValue(passwordId)) throw new PasswordDeletionFailedException("Password not found by ID.");
			//remove key with this value from map
			Set<String> keys = altPassValue.keySet();
			String description = null;
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

		// actually delete password in the backend
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);

		try {
			module.deleteAlternativePassword(sess, user, passwordId);
		} catch(PasswordDeletionFailedRuntimeException ex) {
			throw new PasswordDeletionFailedException(ex);
		} catch(LoginNotExistsRuntimeException ex) {
			throw new LoginNotExistsException(ex);
		} catch (Exception ex) {
			// fallback for exception compatibility
			throw new PasswordDeletionFailedException("Alternative password deletion failed for " + loginNamespace + ":" + passwordId + " of "+user+".", ex);
		}
	}

	@Override
	public List<RichUser> convertUsersToRichUsersWithAttributesByNames(PerunSession sess, List<User> users, List<String> attrNames) {

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
	public RichUser convertUserToRichUserWithAttributesByNames(PerunSession sess, User user, List<String> attrNames) {
		AttributesManagerBl attributesManagerBl = this.getPerunBl().getAttributesManagerBl();

		RichUser richUser = new RichUser(user, getUserExtSources(sess, user));
		richUser.setUserAttributes(attributesManagerBl.getAttributes(sess, user, attrNames));

		return richUser;
	}

	@Override
	public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrsName) throws UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return findRichUsers(sess, searchString);
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsers(sess, searchString), attrsName);
		}

	}

	@Override
	public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrsName) throws UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return findRichUsersByExactMatch(sess, searchString);
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, findUsersByExactMatch(sess, searchString), attrsName);
		}

	}

	@Override
	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws UserNotExistsException {

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutSpecificVo(sess, vo, searchString)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutSpecificVo(sess, vo, searchString), attrsName);
		}
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws UserNotExistsException{

		if(attrsName == null || attrsName.isEmpty()) {
			return convertRichUsersToRichUsersWithAttributes(sess, convertUsersToRichUsers(sess, getUsersWithoutVoAssigned(sess)));
		} else {
			return convertUsersToRichUsersWithAttributesByNames(sess, getUsersWithoutVoAssigned(sess), attrsName);
		}
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers, List<String> attrsName) throws UserNotExistsException {

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
	public void setLogin(PerunSession sess, User user, String loginNamespace, String login) {

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
	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang, String path) {

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

		Utils.sendValidationEmail(user, url, email, changeId, subject, message, path);

	}

	@Override
	public String validatePreferredEmailChange(PerunSession sess, User user, String i, String m) throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {

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
	public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws WrongAttributeAssignmentException, AttributeNotExistsException {

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
	public void checkPasswordResetRequestIsValid(PerunSession sess, User user, String m) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
		int requestId = Integer.parseInt(Utils.cipherInput(m, true));

		getUsersManagerImpl().checkPasswordResetRequestIsValid(sess, user, requestId);
	}

	@Override
	public void changeNonAuthzPassword(PerunSession sess, User user, String m, String password, String lang) throws LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException, PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {

		String requestId = Utils.cipherInput(m, true);
		Pair<String,String> resetRequest = getUsersManagerImpl().loadPasswordResetRequest(sess, user, Integer.parseInt(requestId));

		String namespace = resetRequest.getLeft();
		String mail = resetRequest.getRight();

		List<Attribute> logins = perunBl.getAttributesManagerBl().getLogins(sess, user);
		String login = null;
		for (Attribute a : logins) {
			if (a.getFriendlyNameParameter().equals(namespace)) {
				login = a.valueAsString();
				break;
			}
		}
		if (login == null) throw new InternalErrorException(user.toString()+" doesn't have login in namespace: "+namespace);

		// reset password without checking old
		try {
			changePassword(sess, user, namespace, "", password, false);
		} catch (PasswordDoesntMatchException ex) {
			// shouldn't happen
			throw new InternalErrorException(ex);
		}

		// was changed - send notification to all member's emails
		Set<String> emails = new HashSet<>();

		// add mail used for reset request
		if (mail != null && !mail.isEmpty()) emails.add(mail);

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
			Utils.sendPasswordResetConfirmationEmail(user, email, namespace, login, subject, message);
		}

	}

	@Override
	public void checkPasswordResetRequestIsValid(PerunSession sess, String token) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
		getUsersManagerImpl().checkPasswordResetRequestIsValid(sess, UUID.fromString(token));
	}

	@Override
	public void changeNonAuthzPassword(PerunSession sess, String token, String password, String lang) throws LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException, PasswordResetLinkExpiredException, PasswordResetLinkNotValidException, UserNotExistsException {
		Map<String, Object> request = getUsersManagerImpl().loadPasswordResetRequest(sess, UUID.fromString(token));

		User user = perunBl.getUsersManagerBl().getUserById(sess, (Integer) request.get("user_id"));
		String namespace = (String) request.get("namespace");
		String mail = (String) request.get("mail");

		List<Attribute> logins = perunBl.getAttributesManagerBl().getLogins(sess, user);
		String login = null;
		for (Attribute a : logins) {
			if (a.getFriendlyNameParameter().equals(namespace)) {
				login = a.valueAsString();
				break;
			}
		}
		if (login == null) throw new InternalErrorException(user.toString()+" doesn't have login in namespace: "+namespace);

		// reset password without checking old
		try {
			changePassword(sess, user, namespace, "", password, false);
		} catch (PasswordDoesntMatchException ex) {
			// shouldn't happen
			throw new InternalErrorException(ex);
		}

		// was changed - send notification to all member's emails
		Set<String> emails = new HashSet<>();

		// add mail used for reset request
		if (mail != null && !mail.isEmpty()) emails.add(mail);

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
			Utils.sendPasswordResetConfirmationEmail(user, email, namespace, login, subject, message);
		}

	}


	@Override
	public int getUsersCount(PerunSession sess) {
		return getUsersManagerImpl().getUsersCount(sess);
	}

	@Override
	public Map<String,String> generateAccount(PerunSession sess, String loginNamespace, Map<String, String> parameters) throws PasswordStrengthException {
		PasswordManagerModule module = getPasswordManagerModule(sess, loginNamespace);
		return module.generateAccount(sess, parameters);
	}

	@Override
	public List<User> getSponsors(PerunSession sess, Member sponsoredMember) {
		if(!sponsoredMember.isSponsored()) {
			throw new IllegalArgumentException("member "+sponsoredMember.getId()+" is not marked as sponsored");
		}
		return getUsersManagerImpl().getSponsors(sess, sponsoredMember);
	}

	@Override
	public PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) {
		PasswordManagerModule module = getUsersManagerImpl().getPasswordManagerModule(session, namespace);
		if (module == null) {
			log.info("Password manager module for '{}' not found. Loading 'generic' password manager module instead.", namespace);
			module = getUsersManagerImpl().getPasswordManagerModule(session, "generic");
			if (module instanceof GenericPasswordManagerModule) {
				// set proper login-namespace to the generic module
				((GenericPasswordManagerModule) module).setActualLoginNamespace(namespace);
			}
		}
		if (module == null) {
			log.error("No password manager module found by the class loader for both '{}' and 'generic' namespaces.", namespace);
			throw new InternalErrorException("No password manager module implementation found by the class loader for both '"+namespace+"' and 'generic' namespaces.");
		}
		return module;
	}

	@Override
	public void removeAllUserExtSources(PerunSession sess, User user) {
		for(UserExtSource userExtSource : getUserExtSources(sess, user)) {
			try {
				removeUserExtSource(sess, user, userExtSource);
			} catch (UserExtSourceAlreadyRemovedException ex) {
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd, List<String> excludeValueEnds) throws AttributeNotExistsException {
		AttributeDefinition adef = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
		if((!adef.getType().equals("java.lang.String")) || (!adef.getNamespace().equals(AttributesManager.NS_UES_ATTR_DEF))) {
			throw new InternalErrorException("only ues attributes of type String can be used in findUsersWithExtSourceAttributeValueEnding()");
		}
		return usersManagerImpl.findUsersWithExtSourceAttributeValueEnding(sess,attributeName,valueEnd,excludeValueEnds);
	}

	@Override
	public String changePasswordRandom(PerunSession session, User user, String namespace) throws PasswordOperationTimeoutException, LoginNotExistsException, PasswordChangeFailedException, InvalidLoginException, PasswordStrengthException {

		// first check if user has login in specified namespace!
		String userLogin;
		try {
			Attribute userLoginAttribute = getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + namespace);
			userLogin = (String) userLoginAttribute.getValue();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// should not happen since the changePassword method passed
			log.error("Unexpected exception when re-seting password to randomly generated for user {} in {}", user, namespace, e);
			throw new InternalErrorException(e);
		}

		if (userLogin == null) {
			log.warn("User {} has no login in {} namespace.", user, namespace);
			throw new LoginNotExistsException("User has no login in "+namespace+" namespace.");
		}

		// generate and change password
		PasswordManagerModule module = getPasswordManagerModule(session, namespace);
		String newRandomPassword = module.generateRandomPassword(session, userLogin);

		try {
			changePassword(session, user, namespace, null, newRandomPassword, false);
		} catch (PasswordDoesntMatchException | PasswordStrengthFailedException e) {
			// should not happen when we are not using the old password and have good password generated
			log.error("Unexpected exception when re-seting password to randomly generated for login {} in {}", userLogin, namespace, e);
			throw new InternalErrorException(e);
		}

		// create template to return
		String template = getPasswordResetTemplate(session, namespace);
		return template
				.replace("{password}", StringEscapeUtils.escapeHtml4(newRandomPassword))
				.replace("{login}", StringEscapeUtils.escapeHtml4(userLogin));
	}

	@Override
	public void checkPasswordStrength(PerunSession sess, String password, String namespace) throws PasswordStrengthException {
		getPasswordManagerModule(sess, namespace).checkPasswordStrength(sess, null, password);
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
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user) {

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
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user) {

		List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		Set<Group> groups = new HashSet<>();

		for (Resource resource : resources) {
			groups.addAll(getGroupsWhereUserIsActive(sess, resource, user));
		}

		return new ArrayList<>(groups);

	}

	@Override
	public User createUser(PerunSession sess, Candidate candidate) throws UserExtSourceExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		checkThatCandidateUesesDontExist(sess, candidate);

		User user = createUserFromCandidate(candidate);
		user = getPerunBl().getUsersManagerBl().createUser(sess, user);

		addMissingCandidatesUes(sess, user, candidate);

		setCandidateAttributes(sess, user, candidate);

		log.info("Created user: {}", user);
		return user;
	}

	@Override
	public User createServiceUser(PerunSession sess, Candidate candidate, List<User> owners) throws WrongAttributeAssignmentException, UserExtSourceExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException, AttributeNotExistsException {
		candidate.setServiceUser(true);

		User serviceUser = createUser(sess, candidate);

		for(User owner: owners) {
			try {
				getPerunBl().getUsersManagerBl().addSpecificUserOwner(sess, owner, serviceUser);
			} catch (RelationExistsException ex) {
				throw new InternalErrorException(ex);
			}
		}

		log.info("Created service user: {}", serviceUser);
		return serviceUser;
	}


	/**
	 * Creates a User object from given candidate.
	 *
	 * @param candidate candidate
	 * @return created User object
	 */
	private User createUserFromCandidate(Candidate candidate) {
		User user = new User();
		user.setFirstName(candidate.getFirstName());
		user.setLastName(candidate.getLastName());
		user.setMiddleName(candidate.getMiddleName());
		user.setTitleAfter(candidate.getTitleAfter());
		user.setTitleBefore(candidate.getTitleBefore());
		user.setServiceUser(candidate.isServiceUser());
		user.setSponsoredUser(candidate.isSponsoredUser());
		return user;
	}

	/**
	 * Check that none of the given userExtSources exist. If so, the UserExtSourceExistsException
	 * is thrown.
	 *
	 * @param sess session
	 * @param candidate candidate
	 * @throws UserExtSourceExistsException if some of the given userExtSources already exist.
	 */
	private void checkThatCandidateUesesDontExist(PerunSession sess, Candidate candidate) throws UserExtSourceExistsException {
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource ues : candidate.getUserExtSources()) {
				// Check if the extSource exists
				ExtSource tmpExtSource = getPerunBl().getExtSourcesManagerBl()
						.checkOrCreateExtSource(sess, ues.getExtSource().getName(), ues.getExtSource().getType());
				// Set the extSource ID
				ues.getExtSource().setId(tmpExtSource.getId());
				try {
					// Try to find the user by userExtSource
					User user = getPerunBl().getUsersManagerBl()
							.getUserByExtSourceNameAndExtLogin(sess, ues.getExtSource().getName(), ues.getLogin());
					if (user != null) {
						throw new UserExtSourceExistsException(ues);
					}
				} catch (UserExtSourceNotExistsException | UserNotExistsException | ExtSourceNotExistsException e) {
					// This is OK, we don't want it to exist
				}
			}
		}
	}

	/**
	 * For given user, set user extsources from candiate, which have not been set before.
	 *
	 * @param sess session
	 * @param user user
	 * @param candidate candidate to take userExtSources
	 */
	private void addMissingCandidatesUes(PerunSession sess, User user, Candidate candidate) {
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource userExtSource : candidate.getUserExtSources()) {
				try {
					UserExtSource currentUserExtSource = getPerunBl().getUsersManagerBl()
							.getUserExtSourceByExtLogin(sess, userExtSource.getExtSource(), userExtSource.getLogin());
					// Update LoA
					currentUserExtSource.setLoa(userExtSource.getLoa());
					getPerunBl().getUsersManagerBl().updateUserExtSource(sess, currentUserExtSource);
				} catch (UserExtSourceNotExistsException e) {
					// Create userExtSource
					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, userExtSource);
					} catch (UserExtSourceExistsException e1) {
						throw new ConsistencyErrorException("Adding userExtSource which already exists: " +
								userExtSource, e1);
					}
				} catch (UserExtSourceExistsException e1) {
					throw new ConsistencyErrorException("Updating login of userExtSource to value which already" +
							" exists: " + userExtSource, e1);
				}
			}
		}
	}

	/**
	 * For given user, set user attributes from given candidate.
	 * Can set only user-def and user-opt attributes.
	 *
	 * @param sess session
	 * @param user user
	 * @param candidate candidate to take attributes
	 * @throws AttributeNotExistsException if some of the given attributes dont exist
	 * @throws WrongAttributeAssignmentException if some of the given attributes have unsupported namespace
	 * @throws WrongReferenceAttributeValueException if some of the given attribute value cannot be set because of
	 *                                               some other attribute constraint
	 * @throws WrongAttributeValueException if some of the given attribute value is invalid
	 */
	private void setCandidateAttributes(PerunSession sess, User user, Candidate candidate) throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		if (candidate.getAttributes() == null) {
			return;
		}
		List<Attribute> attributesToSet = new ArrayList<>();
		AttributesManagerBl attrsManager = perunBl.getAttributesManagerBl();
		for (String attributeName: candidate.getAttributes().keySet()) {
			if (!attributeName.startsWith(AttributesManager.NS_USER_ATTR_DEF) &&
			    !attributeName.startsWith(AttributesManager.NS_USER_ATTR_OPT)) {
				throw new WrongAttributeAssignmentException("Cannot set non-(user DEF/OPT) attribute: " + attributeName);
			}
			AttributeDefinition definition = attrsManager.getAttributeDefinition(sess, attributeName);
			Attribute attribute = new Attribute(definition);
			attribute.setValue(attrsManager
					.stringToAttributeValue(candidate.getAttributes().get(attributeName), attribute.getType()));
				attributesToSet.add(attribute);
		}
		attrsManager.setAttributes(sess, user, attributesToSet);
	}

}
