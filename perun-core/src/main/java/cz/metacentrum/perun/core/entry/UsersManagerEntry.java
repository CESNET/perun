package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginExistsException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotSpecificUserExpectedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordChangeFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDoesntMatchException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthFailedException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserExpectedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UsersManager entry logic
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 */
public class UsersManagerEntry implements UsersManager {

	private UsersManagerBl usersManagerBl;
	private PerunBl perunBl;

	public UsersManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.usersManagerBl = perunBl.getUsersManagerBl();
	}

	public UsersManagerEntry() {
	}

	/*FIXME delete this method */
	public UsersManagerImplApi getUsersManagerImpl() {
		throw new InternalErrorRuntimeException("Unsupported method!");
	}

	@Override
	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		User user = getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByUserExtSource");
		}

		return user;
	}

	@Override
	public User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getUsersManagerBl().getUserByUserExtSources(sess, userExtSources);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByUserExtSources");
		}

		return user;
	}

	@Override
	public User getUserById(PerunSession sess, int id) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getUsersManagerBl().getUserById(sess, id);

		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getUserById");
		}

		return user;

	}

	@Override
	public List<User> getSpecificUsersByUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, NotSpecificUserExpectedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		if(user.isServiceUser()) throw new NotSpecificUserExpectedException(user);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			boolean found = false;
			for (Vo vo : vos) {
				if (found = AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) break;
			}
			// if not self or vo/group admin of any of users VOs
			if (!found) {
				throw new PrivilegeException(sess, "getSpecificUsersByUser");
			}
		}
		return getUsersManagerBl().getSpecificUsersByUser(sess, user);
	}

	@Override
	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, SpecificUserExpectedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, specificUser);
		if(!specificUser.isSpecificUser()) throw new SpecificUserExpectedException(specificUser);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, specificUser) &&
			!AuthzResolver.isAuthorized(sess, Role.SPONSOR, specificUser) &&
			!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, specificUser);
			boolean found = false;
			for (Vo vo : vos) {
				if (found = AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) break;
			}
			// if not self or vo/group admin of any of users VOs
			if (!found) {
				throw new PrivilegeException(sess, "getUsersBySpecificUser");
			}
		}
		return getUsersManagerBl().getUsersBySpecificUser(sess, specificUser);
	}

	@Override
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, SpecificUserExpectedException, NotSpecificUserExpectedException, RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().checkUserExists(sess, specificUser);
		if (user.isServiceUser()) throw new NotSpecificUserExpectedException(user);
		if (user.isSponsoredUser() && specificUser.isSponsoredUser()) throw new NotSpecificUserExpectedException(specificUser);
		if (!specificUser.isSpecificUser()) throw new SpecificUserExpectedException(specificUser);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, specificUser) &&
			!AuthzResolver.isAuthorized(sess, Role.SPONSOR, specificUser)) {
			throw new PrivilegeException(sess, "removeSpecificUserOwner");
		}
		getUsersManagerBl().removeSpecificUserOwner(sess, user, specificUser);
	}

	@Override
	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, SpecificUserExpectedException, NotSpecificUserExpectedException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().checkUserExists(sess, specificUser);
		if (user.isServiceUser()) throw new NotSpecificUserExpectedException(user);
		if (user.isSponsoredUser() && specificUser.isSponsoredUser()) throw new NotSpecificUserExpectedException(specificUser);
		if (!specificUser.isSpecificUser()) throw new SpecificUserExpectedException(specificUser);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, specificUser) &&
			!AuthzResolver.isAuthorized(sess, Role.SPONSOR, specificUser)) {
			throw new PrivilegeException(sess, "addSpecificUserOwner");
		}
		getUsersManagerBl().addSpecificUserOwner(sess, user, specificUser);
	}

	@Override
	public List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getSpecificUsers");
		}
		return getUsersManagerBl().getSpecificUsers(sess);
	}

	@Override
	public User getUserByMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getUserByMember");
		}

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		return getUsersManagerBl().getUserByMember(sess, member);
	}

	@Override
	public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);

		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByExtSourceNameAndExtLogin");
		}

		return user;
	}

	@Override
	public List<User> getUsers(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsers");
		}

		return getUsersManagerBl().getUsers(sess);
	}

	@Override
	public RichUser getRichUser(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getRichUser");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUser(sess, user));
	}

	@Override
	public RichUser getRichUserWithAttributes(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getRichUserWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUserWithAttributes(sess, user));
	}

	@Override
	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsers(sess, includedSpecificUsers));
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedSpecificUsers));
	}

	@Override
	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if(users == null || users.isEmpty()) return new ArrayList<>();

		for(User user: users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersFromListOfUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersFromListOfUsers(sess, users));
	}

	@Override
	public List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if(users == null || users.isEmpty()) return new ArrayList<>();

		for(User user: users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getRichUsersWithAttributesFromListOfUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(sess, users));
	}

	@Override
	@Deprecated
	public User createUser(PerunSession sess, User user) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(user, "user");

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createUser");
		}

		// Create the user
		return getUsersManagerBl().createUser(sess, user);
	}

	@Override
	public User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner) throws InternalErrorException, RelationExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(specificUserType, "specificUserType");
		getPerunBl().getUsersManagerBl().checkUserExists(sess, owner);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, specificUser);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "Only PerunAdmin should have rights to call this method.");
		}

		//set specific user
		return getUsersManagerBl().setSpecificUser(sess, specificUser, specificUserType, owner);
	}

	@Override
	public User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(specificUserType, "specificUserType");
		getPerunBl().getUsersManagerBl().checkUserExists(sess, specificUser);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "Only PerunAdmin should have rights to call this method.");
		}

		//set specific user
		return getUsersManagerBl().unsetSpecificUser(sess, specificUser, specificUserType);
	}

	@Override
	public void deleteUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().deleteUser(sess, user);
	}

	@Override
	public void deleteUser(PerunSession sess, User user, boolean forceDelete) throws InternalErrorException, UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().deleteUser(sess, user, forceDelete);
	}

	@Override
	public User updateUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "updateUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		if(user.getLastName() == null || user.getLastName().isEmpty()) throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("User lastName can't be null. It's required attribute.");

		return getUsersManagerBl().updateUser(sess, user);
	}

	@Override
	public User updateNameTitles(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Utils.checkMaxLength("TitleBefore", user.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", user.getTitleAfter(), 40);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "updateNameTitles");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().updateNameTitles(sess, user);
	}

	@Override
	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException, UserExtSourceExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "updateUserExtSource");
		}

		getUsersManagerBl().checkUserExtSourceExistsById(sess, userExtSource.getId());

		try {
			getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);
		} catch (UserExtSourceNotExistsException ex) {
			// silently skip, since it's expected, that new value is not taken already
		}

		return getUsersManagerBl().updateUserExtSource(sess, userExtSource);

	}

	@Override
	public List<UserExtSource> getUserExtSources(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserExtSources");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getUserExtSources(sess, user);
	}

	@Override
	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getUserExtSourceById");
		}

		return getUsersManagerBl().getUserExtSourceById(sess, id);
	}

	@Override
	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException, PrivilegeException, UserExtSourceExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(userExtSource, "userExtSource");
		Utils.notNull(user, "user");
		Utils.notNull(userExtSource.getExtSource(), "ExtSource in UserExtSource");
		Utils.notNull(userExtSource.getLogin(), "Login in UserExtSource");
		Utils.notNull(userExtSource.getExtSource().getType(), "Type of ExpSource in UserExtSource");

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "addUserExtSource");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		// set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
		userExtSource.setUserId(user.getId());

		try {
			getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);
			throw new UserExtSourceExistsException("UserExtSource " + userExtSource + " already exists");
		} catch (UserExtSourceNotExistsException e) {
			// This is ok
		}

		return getUsersManagerBl().addUserExtSource(sess, user, userExtSource);
	}

	@Override
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException, UserExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "removeUserExtSource");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		// set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
		userExtSource.setUserId(user.getId());
		getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		if (userExtSource.isPersistent()) {
			throw new InternalErrorException("Given UserExtSource: " + userExtSource + " is marked as persistent. " +
					"It means it can not be removed.");
		}

		getUsersManagerBl().removeUserExtSource(sess, user, userExtSource);
	}

	@Override
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource, boolean forceDelete) throws InternalErrorException, UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException, UserExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		if (forceDelete) {

			// Authorization
			if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN, user)) {
				throw new PrivilegeException(sess, "removeUserExtSource");
			}

			getUsersManagerBl().checkUserExists(sess, user);
			// set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
			userExtSource.setUserId(user.getId());
			getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

			getUsersManagerBl().removeUserExtSource(sess, user, userExtSource);
		} else {
			removeUserExtSource(sess, user, userExtSource);
		}
	}

	@Override
	public void moveUserExtSource(PerunSession sess, User sourceUser, User targetUser, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "moveUserExtSource");
		}

		getUsersManagerBl().checkUserExists(sess, targetUser);
		getUsersManagerBl().checkUserExists(sess, sourceUser);
		// set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
		userExtSource.setUserId(sourceUser.getId());
		getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		if (userExtSource.isPersistent()) {
			throw new InternalErrorException("Given UserExtSource: " + userExtSource + " is marked as persistent. " +
					"It means it can not be removed.");
		}

		getUsersManagerBl().moveUserExtSource(sess, sourceUser, targetUser, userExtSource);
	}

	@Override
	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException, UserExtSourceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "findUserExtSourceByExtLogin");
		}

		Utils.notNull(extLogin, "extLogin");
		getPerunBl().getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		return getUsersManagerBl().getUserExtSourceByExtLogin(sess, source, extLogin);
	}

	@Override
	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getVosWhereUserIsAdmin");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getVosWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getGroupsWhereUserIsAdmin");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getGroupsWhereUserIsAdmin(sess, user);
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getGroupsWhereUserIsAdmin");
		}

		return getUsersManagerBl().getGroupsWhereUserIsAdmin(sess, vo, user);
	}

	@Override
	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getVosWhereUserIsMember");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getVosWhereUserIsMember(sess, user);
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getAllowedResources");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getUsersManagerBl().getAllowedResources(sess, facility, user);
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getAllowedResources");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getAllowedResources(sess, user);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		return getUsersManagerBl().getAssignedRichResources(sess, user);
	}

	@Override
	public List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByName");
		}

		return getUsersManagerBl().findUsers(sess, searchString);
	}

	@Override
	public List<RichUser> findRichUsers(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByName");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsers(sess, searchString));
	}

	@Override
	public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByName");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		return getUsersManagerBl().getUsersWithoutSpecificVo(sess, vo, searchString);
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByName");
		}

		return getUsersManagerBl().findUsersByName(sess, searchString);
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByName");
		}

		return getUsersManagerBl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
	}

	@Override
	public List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findUsersByExactName");
		}

		return getUsersManagerBl().findUsersByExactName(sess, searchString);
	}

	@Override
	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SERVICEUSER)) {
			throw new PrivilegeException(sess, "getUsersByAttribute");
		}

		return getUsersManagerBl().getUsersByAttribute(sess, attribute);
	}

	@Override
	public List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue)
			throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SERVICEUSER)) {
			throw new PrivilegeException(sess, "getUsersByAttribute");
		}

		getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);

		return getUsersManagerBl().getUsersByAttribute(sess, attributeName, attributeValue);
	}

	@Override
	public List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue)
			throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SERVICEUSER)) {
			throw new PrivilegeException(sess, "getUsersByAttributeValue");
		}

		getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);

		return getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, attributeValue);
	}

	@Override
	public boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		// Authorization - must be public since it's used to check anonymous users input on registration form

		return getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login);
	}

	@Override
	public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsersWithoutVoAssigned");
		}

		return getUsersManagerBl().getUsersWithoutVoAssigned(sess);
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersWithoutVoAssigned");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithoutVoAssigned(sess));
	}

	@Override
	public boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "isUserPerunAdmin");
		}

		return getUsersManagerBl().isUserPerunAdmin(sess, user);
	}

	@Override
	public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword) throws InternalErrorException,
			PrivilegeException, UserNotExistsException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "changePassword");
		}

		// Check if the login-namespace already exists and the user has a login in the login-namespace
		// Create attribute name
		String attributeName = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace;

		try {
			getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
			throw new LoginNotExistsException(e);
		}

		getUsersManagerBl().changePassword(sess, user, loginNamespace, oldPassword, newPassword, checkOldPassword);
	}

	@Override
	public void changePassword(PerunSession sess, String login , String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword) throws InternalErrorException,
			PrivilegeException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		Utils.checkPerunSession(sess);

		String attributeName = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace;

		List<User> users;
		try {
			users = getUsersManagerBl().getUsersByAttributeValue(sess, attributeName , login);
		} catch (ConsistencyErrorException e) {
			// attr def not exists by implementation in getUsersByAttributeValue
			throw new LoginNotExistsException(e);
		}
		if (users.size() > 1) throw new ConsistencyErrorException("Multiple users found for login: "+login);
		if (users.isEmpty()) throw new LoginNotExistsException("User with login: "+login+" not exists.");
		User user = users.get(0);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "changePassword");
		}

		getUsersManagerBl().changePassword(sess, user, loginNamespace, oldPassword, newPassword, checkOldPassword);
	}

	@Override
	@Deprecated
	public void createPassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "createPassword");
		}

		// Check if the login is already occupied == reserved, if not throw an exception.
		// We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
		if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
			getUsersManagerBl().createPassword(sess, userLogin, loginNamespace, password);
		} else {
			throw new PasswordCreationFailedException("Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
		}
	}

	@Override
	@Deprecated
	public void createPassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "createPassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().createPassword(sess, user, loginNamespace, password);
	}

	@Override
	public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "reserveRandomPassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().reserveRandomPassword(sess, user, loginNamespace);
	}

	@Override
	public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "reservePassword");
		}

		// Check if the login is already occupied == reserved, if not throw an exception.
		// We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
		if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
			getUsersManagerBl().reservePassword(sess, userLogin, loginNamespace, password);
		} else {
			throw new PasswordCreationFailedException("Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
		}
	}

	@Override
	public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "reservePassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().reservePassword(sess, user, loginNamespace, password);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "validatePassword");
		}

		// Check if the login is already occupied == reserved, if not throw an exception.
		// We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
		if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
			getUsersManagerBl().validatePassword(sess, userLogin, loginNamespace);
		} else {
			throw new PasswordCreationFailedException("Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
		}
	}

	@Override
	public void validatePasswordAndSetExtSources(PerunSession sess, User user, String userLogin, String loginNamespace) throws PrivilegeException, InternalErrorException, PasswordCreationFailedException, LoginNotExistsException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) && !AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "validatePasswordAndSetExtSources");
		}

		// Check if the login is already occupied == reserved, if not throw an exception.
		// We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
		if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
			getUsersManagerBl().validatePasswordAndSetExtSources(sess, user, userLogin, loginNamespace);
		} else {
			throw new PasswordCreationFailedException("Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
		}
	}

	@Override
	public void validatePassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "validatePassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().validatePassword(sess, user, loginNamespace);
	}


	@Override
	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException,
			PrivilegeException, PasswordDeletionFailedException, LoginNotExistsException, PasswordOperationTimeoutException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "deletePassword");
		}

		if (getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
			// NOT RESERVED BY ATTRIBUTE OR REGISTRAR
			throw new PasswordDeletionFailedException("Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
		} else {
			// RESERVED BY ATTRIBUTE OR REGISTRAR
			try {
				getPerunBl().getUsersManagerBl().checkReservedLogins(sess, loginNamespace, userLogin);
				// RESERVED BY ATTRIBUTE ONLY - we don't want to delete logins in use
				throw new PasswordDeletionFailedException("Login " + userLogin + " in namespace " + loginNamespace + " can't be delete, because it's already in use.");
			} catch (AlreadyReservedLoginException ex) {
				// RESERVED BY REGISTRAR AND MAYBE BY ATTRIBUTE
				// reservation by both should not occur
				getUsersManagerBl().deletePassword(sess, userLogin, loginNamespace);
			}
		}
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace, String password) throws InternalErrorException, PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(description, "description");
		Utils.notNull(loginNamespace, "loginNamespace");
		Utils.notNull(password, "password");

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "createAlternativePassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().createAlternativePassword(sess, user, description, loginNamespace, password);
	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId) throws InternalErrorException, UserNotExistsException, PasswordDeletionFailedException, PrivilegeException, LoginNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(loginNamespace, "loginNamespace");
		Utils.notNull(passwordId, "passwordId");

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "deleteAlternativePassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().deleteAlternativePassword(sess, user, loginNamespace, passwordId);
	}

	/**
	 * Gets the usersManagerBl for this instance.
	 *
	 * @return The usersManagerBl.
	 */
	public UsersManagerBl getUsersManagerBl() {
		return this.usersManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the usersManagerBl for this instance.
	 *
	 * @param usersManagerBl The usersManagerBl.
	 */
	public void setUsersManagerBl(UsersManagerBl usersManagerBl)
	{
		this.usersManagerBl = usersManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	@Override
	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers, List<String> attrsNames) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedSpecificUsers, attrsNames));

	}

	@Override
	public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN)){

			if (AuthzResolver.isAuthorized(sess, Role.SELF)) {
				// necessary when adding new owners to service accounts
				List<User> serviceIdentities = getPerunBl().getUsersManagerBl().getSpecificUsersByUser(sess, sess.getPerunPrincipal().getUser());
				if (serviceIdentities.isEmpty()) {
					// user isn't owner of any service identity
					throw new PrivilegeException(sess, "findRichUsersWithAttributes");
				}
			} else {
				throw new PrivilegeException(sess, "findRichUsersWithAttributes");
			}
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames));

	}

	@Override
	public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) { // called only internally by registrar
			throw new PrivilegeException(sess, "findRichUsersWithAttributesByExactMatch");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithAttributesByExactMatch(sess, searchString, attrNames));

	}

	@Override
	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException, PrivilegeException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "findRichUsersWithoutSpecificVoWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithoutSpecificVoWithAttributes(sess, vo, searchString, attrsName));
	}

	@Override
	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, UserNotExistsException, PrivilegeException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersWithoutVOWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithoutVoWithAttributes(sess, attrsName));
	}

	@Override
	public void setLogin(PerunSession sess, User user, String loginNamespace, String login) throws InternalErrorException, PrivilegeException, UserNotExistsException, LoginExistsException {

		// checks
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) && !user.isSpecificUser()) {
			throw new PrivilegeException(sess, "setLogin");
		}

		if (getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login)) {

			getPerunBl().getUsersManagerBl().setLogin(sess, user, loginNamespace, login);

		} else {
			throw new LoginExistsException("Login: "+login+" in namespace: "+loginNamespace+" is already in use.");
		}

	}

	@Override
	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang) throws InternalErrorException, PrivilegeException, UserNotExistsException {

		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "requestPreferredEmailChange");
		}

		getPerunBl().getUsersManagerBl().requestPreferredEmailChange(sess, url, user, email, lang);

	}

	@Override
	public String validatePreferredEmailChange(PerunSession sess, User user, String i, String m) throws InternalErrorException, UserNotExistsException, PrivilegeException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException {

		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "validatePreferredEmailChange");
		}

		// check change verification parameters

		if (m.equals(Utils.getMessageAuthenticationCode(i))) {
			return getPerunBl().getUsersManagerBl().validatePreferredEmailChange(sess, user, i, m);
		}

		throw new InternalErrorException("Can't validate preferred email change. Verification parameters doesn't match.");

	}

	@Override
	public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException {

		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getPendingPreferredEmailChanges");
		}

		return getPerunBl().getUsersManagerBl().getPendingPreferredEmailChanges(sess, user);

	}

	@Override
	public void changeNonAuthzPassword(PerunSession sess, String i, String m, String password, String lang) throws InternalErrorException, UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException {

		Utils.checkPerunSession(sess);

		if (lang == null || lang.isEmpty()) lang = "en"; // fallback to english

		int userId = Integer.parseInt(Utils.cipherInput(i,true));
		// this will make also "if exists check"
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);

		getPerunBl().getUsersManagerBl().changeNonAuthzPassword(sess, user, m, password, lang);

	}

	@Override
	public int getUsersCount(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		return getUsersManagerBl().getUsersCount(sess);
	}

	@Override
	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, PrivilegeException, UserExtSourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "updateUserExtSourceLastAccess");
		}

		getUsersManagerBl().updateUserExtSourceLastAccess(sess, userExtSource);
	}

	@Override
	public Map<String,String> generateAccount(PerunSession sess, String namespace, Map<String, String> parameters) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "generateAccount");
		}

		return getUsersManagerBl().generateAccount(sess, namespace, parameters);

	}

	@Override
	public List<RichUser> getSponsors(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(member, "member");
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "getSponsors can be called only by REGISTRAR");
		}
		List<User> sponsors = usersManagerBl.getSponsors(sess, member);
		if (attrNames == null || attrNames.isEmpty()) {
			//adds all existing atributes
			return usersManagerBl.convertRichUsersToRichUsersWithAttributes(sess, usersManagerBl.convertUsersToRichUsers(sess, sponsors));
		} else {
			//adds only selected atributes (if the list would be empty, it will return no attributes)
			return usersManagerBl.convertUsersToRichUsersWithAttributesByNames(sess, sponsors, attrNames);
		}
	}

	@Override
	public String changePasswordRandom(PerunSession sess, User user, String loginNamespace)	throws InternalErrorException, PrivilegeException, PasswordOperationTimeoutException, LoginNotExistsException, PasswordChangeFailedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException("changePasswordRandom");
		}

		return usersManagerBl.changePasswordRandom(sess, user, loginNamespace);
	}

	@Override
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException("getGroupsWhereUserIsActive");
		}

		return perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, resource, user);
	}

	@Override
	public List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user, List<String> attrNames) throws PrivilegeException, InternalErrorException {

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
			throw new PrivilegeException("getRichGroupsWhereUserIsActive");
		}

		return perunBl.getGroupsManagerBl().filterOnlyAllowedAttributes(sess,
				perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(sess,
						perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, resource, user), attrNames), null, true);

	}

	@Override
	public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException("getGroupsWhereUserIsActive");
		}

		return perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, facility, user);
	}

	@Override
	public List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user, List<String> attrNames) throws PrivilegeException, InternalErrorException {

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException("getRichGroupsWhereUserIsActive");
		}

		return perunBl.getGroupsManagerBl().filterOnlyAllowedAttributes(sess,
				perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(sess,
						perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, facility, user), attrNames), null, true);


	}

}
