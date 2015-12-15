package cz.metacentrum.perun.core.entry;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;

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

	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		User user = getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByUserExtSource");
		}

		return user;
	}

	public User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getUsersManagerBl().getUserByUserExtSources(sess, userExtSources);

		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByUserExtSources");
		}

		return user;
	}

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

	public List<User> getServiceUsersByUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, NotServiceUserExpectedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		if(user.isServiceUser()) throw new NotServiceUserExpectedException(user);

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
				throw new PrivilegeException(sess, "getServiceUsersByUser");
			}
		}
		return getUsersManagerBl().getServiceUsersByUser(sess, user);
	}

	public List<User> getUsersByServiceUser(PerunSession sess, User serviceUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, ServiceUserExpectedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, serviceUser);
		if(!serviceUser.isServiceUser()) throw new ServiceUserExpectedException(serviceUser);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, serviceUser)) {
			List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, serviceUser);
			boolean found = false;
			for (Vo vo : vos) {
				if (found = AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) break;
				if (found = AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) break;
			}
			// if not self or vo/group admin of any of users VOs
			if (!found) {
				throw new PrivilegeException(sess, "getUsersByServiceUser");
			}
		}
		return getUsersManagerBl().getUsersByServiceUser(sess, serviceUser);
	}

	public void removeServiceUserOwner(PerunSession sess, User user, User serviceUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, ServiceUserExpectedException, NotServiceUserExpectedException, RelationNotExistsException, ServiceUserMustHaveOwnerException, ServiceUserOwnerAlreadyRemovedException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().checkUserExists(sess, serviceUser);
		if(!serviceUser.isServiceUser()) throw new ServiceUserExpectedException(serviceUser);
		if(user.isServiceUser()) throw new NotServiceUserExpectedException(user);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, serviceUser)) {
			throw new PrivilegeException(sess, "removeServiceUser");
		}
		getUsersManagerBl().removeServiceUserOwner(sess, user, serviceUser);
	}

	public void addServiceUserOwner(PerunSession sess, User user, User serviceUser) throws InternalErrorException, UserNotExistsException, PrivilegeException, ServiceUserExpectedException, NotServiceUserExpectedException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().checkUserExists(sess, serviceUser);
		if(!serviceUser.isServiceUser()) throw new ServiceUserExpectedException(serviceUser);
		if(user.isServiceUser()) throw new NotServiceUserExpectedException(user);
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, serviceUser)) {
			throw new PrivilegeException(sess, "addServiceUser");
		}
		getUsersManagerBl().addServiceUserOwner(sess, user, serviceUser);
	}

	public List<User> getServiceUsers(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getServiceUsers");
		}
		return getUsersManagerBl().getServiceUsers(sess);
	}

	public User getUserByMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getUserByMember");
		}

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		return getUsersManagerBl().getUserByMember(sess, member);
	}

	public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);

		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getUserByExtSourceNameAndExtLogin");
		}

		return user;
	}

	public List<User> getUsers(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {
			throw new PrivilegeException(sess, "getUser");
		}

		return getUsersManagerBl().getUsers(sess);
	}

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

	public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedServiceUsers) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsers(sess, includedServiceUsers));
	}

	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedServiceUsers) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedServiceUsers));
	}

	public List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if(users == null || users.isEmpty()) return new ArrayList<RichUser>();

		for(User user: users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersFromListOfUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersFromListOfUsers(sess, users));
	}

	public List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if(users == null || users.isEmpty()) return new ArrayList<RichUser>();

		for(User user: users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersFromListOfUsers");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(sess, users));
	}

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

	public void deleteUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, ServiceUserAlreadyRemovedException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().deleteUser(sess, user);
	}

	public void deleteUser(PerunSession sess, User user, boolean forceDelete) throws InternalErrorException, UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, ServiceUserAlreadyRemovedException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().deleteUser(sess, user, forceDelete);
	}

	public User updateUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "updateUser");
		}

		getUsersManagerBl().checkUserExists(sess, user);
		if(user.getLastName() == null || user.getLastName().isEmpty()) throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("User lastName can't be null. It's required attribute.");

		return getUsersManagerBl().updateUser(sess, user);
	}

	public User updateNameTitles(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "updateNameTitles");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().updateNameTitles(sess, user);
	}

	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "updateUserExtSource");
		}

		getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

		return getUsersManagerBl().updateUserExtSource(sess, userExtSource);
	}

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

	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "addUserExtSourceById");
		}

		return getUsersManagerBl().getUserExtSourceById(sess, id);
	}

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

		getUsersManagerBl().removeUserExtSource(sess, user, userExtSource);
	}

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

	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getVosWhereUserIsAdmin");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getVosWhereUserIsAdmin(sess, user);
	}

	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getGroupsWhereUserIsAdmin");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getGroupsWhereUserIsAdmin(sess, user);
	}

	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getVosWhereUserIsMember");
		}

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getVosWhereUserIsMember(sess, user);
	}

	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getUsersManagerBl().getAllowedResources(sess, facility, user);
	}

	public List<Resource> getAllowedResources(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getUsersManagerBl().checkUserExists(sess, user);

		return getUsersManagerBl().getAllowedResources(sess, user);
	}

	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//TODO Authorization
		getUsersManagerBl().checkUserExists(sess, user);
		return getUsersManagerBl().getAssignedRichResources(sess, user);
	}

	public List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Probably without authorization
		return getUsersManagerBl().findUsers(sess, searchString);
	}

	public List<RichUser> findRichUsers(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Probably without authorization
		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsers(sess, searchString));
	}

	public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		return getUsersManagerBl().getUsersWithoutSpecificVo(sess, vo, searchString);
	}

	public List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Probably without authorization
		return getUsersManagerBl().findUsersByName(sess, searchString);
	}

	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Probably without authorization
		return getUsersManagerBl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
	}

	public List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Probably without authorization
		return getUsersManagerBl().findUsersByExactName(sess, searchString);
	}

	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SERVICEUSER)) {
			throw new PrivilegeException(sess, "getUsersByAttribute");
		}

		return getUsersManagerBl().getUsersByAttribute(sess, attribute);
	}

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

	public boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		// FIXME zatim nekontrolujeme
		/*
			 if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			 throw new PrivilegeException(sess, "getUsersByAttribute");
			 }
			 */

		return getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login);
	}

	public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsersWithoutVoAssigned");
		}

		return getUsersManagerBl().getUsersWithoutVoAssigned(sess);
	}

	public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersWithoutVoAssigned");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithoutVoAssigned(sess));
	}

	public void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, NotServiceUserExpectedException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExists(sess, user);

		if(user.isServiceUser()) throw new NotServiceUserExpectedException(user);
		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "makeUserPerunAdmin");
		}

		getUsersManagerBl().makeUserPerunAdmin(sess, user);
	}

	public boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "isUserPerunAdmin");
		}

		return getUsersManagerBl().isUserPerunAdmin(sess, user);
	}

	public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword) throws InternalErrorException,
			PrivilegeException, UserNotExistsException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException {
		Utils.checkPerunSession(sess);

		getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "changePassword");
		}

		// Check if the login-namesapce already exists and the user has a login in the login-namespace
		// Create attribute name
		String attributeName = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace;

		try {
			getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
		} catch (AttributeNotExistsException e) {
			throw new LoginNotExistsException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new LoginNotExistsException(e);
		}

		getUsersManagerBl().changePassword(sess, user, loginNamespace, oldPassword, newPassword, checkOldPassword);
	}

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

	public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "reserveRandomPassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getUsersManagerBl().reserveRandomPassword(sess, user, loginNamespace);
	}

	public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException {
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

	public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws InternalErrorException,
			PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) && (!(AuthzResolver.isAuthorized(sess, Role.VOADMIN) && user.isServiceUser()))) {
			throw new PrivilegeException(sess, "reservePassword");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getUsersManagerBl().reservePassword(sess, user, loginNamespace, password);
	}

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


	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException,
			PrivilegeException, PasswordDeletionFailedException, LoginNotExistsException {
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

	public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedServiceUsers, List<String> attrsNames) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedServiceUsers, attrsNames));

	}

	public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF)) {
			throw new PrivilegeException(sess, "findRichUsersWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames));

	}

	public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF)) {
			throw new PrivilegeException(sess, "findRichUsersWithAttributesByExactMatch");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithAttributesByExactMatch(sess, searchString, attrNames));

	}

	public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException, VoNotExistsException, PrivilegeException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "findRichUsersWithoutSpecificVOWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsersWithoutSpecificVoWithAttributes(sess, vo, searchString, attrsName));
	}

	public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, VoNotExistsException, UserNotExistsException, PrivilegeException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getRichUsersWithoutVOWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithoutVoWithAttributes(sess, attrsName));
	}

	public void setLogin(PerunSession sess, User user, String loginNamespace, String login) throws InternalErrorException, PrivilegeException, UserNotExistsException, LoginExistsException {

		// checks
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) && !user.isServiceUser()) {
			throw new PrivilegeException(sess, "setLogin");
		}

		if (getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login)) {

			getPerunBl().getUsersManagerBl().setLogin(sess, user, loginNamespace, login);

		} else {
			throw new LoginExistsException("Login: "+login+" in namespace: "+loginNamespace+" is already in use.");
		}

	}

	public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email) throws InternalErrorException, PrivilegeException, UserNotExistsException {

		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "requestPreferredEmailChange");
		}

		getPerunBl().getUsersManagerBl().requestPreferredEmailChange(sess, url, user, email);

	}

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
	public void changeNonAuthzPassword(PerunSession sess, String i, String m, String password) throws InternalErrorException, UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException {

		Utils.checkPerunSession(sess);

		int userId = Integer.parseInt(Utils.cipherInput(i,true));
		// this will make also "if exists check"
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);

		getPerunBl().getUsersManagerBl().changeNonAuthzPassword(sess, user, m, password);

	}

	@Override
	public int getUsersCount(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsersCount");
		}

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
}
