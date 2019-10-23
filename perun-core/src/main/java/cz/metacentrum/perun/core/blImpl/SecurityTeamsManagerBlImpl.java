package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminAddedForSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminGroupAddedForSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminGroupRemovedFromSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminRemovedFromSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.SecurityTeamCreated;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.SecurityTeamDeleted;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.SecurityTeamUpdated;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.UserAddedToBlackListOfSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.UserRemovedFromBlackListOfSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.UserRemovedFromBlacklists;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SecurityTeamsManagerBl;
import cz.metacentrum.perun.core.implApi.SecurityTeamsManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamsManagerBlImpl implements SecurityTeamsManagerBl {

	private final static Logger log = LoggerFactory.getLogger(SecurityTeamsManagerBlImpl.class);

	private final SecurityTeamsManagerImplApi securityTeamsManagerImpl;
	private PerunBl perunBl;

	public SecurityTeamsManagerBlImpl(SecurityTeamsManagerImplApi securityTeamsManagerImpl) {
		this.securityTeamsManagerImpl = securityTeamsManagerImpl;
	}

	@Override
	public List<SecurityTeam> getSecurityTeams(PerunSession sess) throws InternalErrorException {
		if (AuthzResolverBlImpl.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)) {
			return getSecurityTeamsManagerImpl().getAllSecurityTeams(sess);
		} else if (AuthzResolverBlImpl.hasRole(sess.getPerunPrincipal(), Role.SECURITYADMIN)) {

			List<SecurityTeam> securityTeams = new ArrayList<>();

			// Get SecurityTeams where user is Admin
			for (PerunBean st: AuthzResolver.getComplementaryObjectsForRole(sess, Role.SECURITYADMIN, SecurityTeam.class)) {
				securityTeams.add((SecurityTeam) st);
			}

			return securityTeams;
		} else {
			throw new InternalErrorException("Wrong Entry. Should throw PrivilegeException");
		}
	}

	@Override
	public List<SecurityTeam> getAllSecurityTeams(PerunSession sess) throws InternalErrorException {
		return getSecurityTeamsManagerImpl().getAllSecurityTeams(sess);
	}

	@Override
	public SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		securityTeam = getSecurityTeamsManagerImpl().createSecurityTeam(sess, securityTeam);
		getPerunBl().getAuditer().log(sess, new SecurityTeamCreated(securityTeam));

		// set creator as security team admin
		User user = sess.getPerunPrincipal().getUser();
		if(user != null) {   //user can be null in tests
			try {
				AuthzResolverBlImpl.setRole(sess, user, securityTeam, Role.SECURITYADMIN);
			} catch (AlreadyAdminException e) {
				throw new ConsistencyErrorException("Newly created securityTeam already have an admin.", e);
			}
		}

		return securityTeam;
	}

	@Override
	public SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotExistsException {
		securityTeam = getSecurityTeamsManagerImpl().updateSecurityTeam(sess, securityTeam);
		getPerunBl().getAuditer().log(sess, new SecurityTeamUpdated(securityTeam));
		return securityTeam;
	}

	@Override
	public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam, boolean forceDelete) throws SecurityTeamNotExistsException, InternalErrorException, RelationExistsException {

		// remove all users from blacklist, which were blacklisted by this security team.
		List<User> blacklist = getSecurityTeamsManagerImpl().getBlacklist(sess, Collections.singletonList(securityTeam));
		if (!blacklist.isEmpty() && !forceDelete) {
			throw new RelationExistsException("SecurityTeam has blacklisted users.");
		}
		for (User blacklistedUser : blacklist) {
			// calling BL will make auditer message about user to appear.
			getPerunBl().getSecurityTeamsManagerBl().removeUserFromBlacklist(sess, securityTeam, blacklistedUser);
		}

		// remove security team from all facilities
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAssignedFacilities(sess, securityTeam);
		if (!facilities.isEmpty() && !forceDelete) {
			throw new RelationExistsException("SecurityTeam is assigned to some facilities.");
		}
		for (Facility facility : facilities) {
			// calling BL will make auditer message about facility to appear.
			getPerunBl().getFacilitiesManagerBl().removeSecurityTeam(sess, facility, securityTeam);
		}

		getSecurityTeamsManagerImpl().deleteSecurityTeam(sess, securityTeam);
		getPerunBl().getAuditer().log(sess,new SecurityTeamDeleted(securityTeam));
	}

	@Override
	public SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws SecurityTeamNotExistsException, InternalErrorException {
		return getSecurityTeamsManagerImpl().getSecurityTeamById(sess, id);
	}

	@Override
	public SecurityTeam getSecurityTeamByName(PerunSession sess, String name) throws SecurityTeamNotExistsException, InternalErrorException {
		return getSecurityTeamsManagerImpl().getSecurityTeamByName(sess, name);
	}

	@Override
	public List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam, boolean onlyDirectAdmins) throws InternalErrorException {
		if(onlyDirectAdmins) {
			return getSecurityTeamsManagerImpl().getDirectAdmins(sess, securityTeam);
		} else {
			return getSecurityTeamsManagerImpl().getAdmins(sess, securityTeam);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		return getSecurityTeamsManagerImpl().getAdminGroups(sess, securityTeam);
	}

	@Override
	public void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user, String description) throws InternalErrorException {
		getSecurityTeamsManagerImpl().addUserToBlacklist(sess, securityTeam, user, description);
		getPerunBl().getAuditer().log(sess, new UserAddedToBlackListOfSecurityTeam(user, securityTeam, description));
	}

	@Override
	public void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException {
		getSecurityTeamsManagerImpl().removeUserFromBlacklist(sess, securityTeam, user);
		getPerunBl().getAuditer().log(sess, new UserRemovedFromBlackListOfSecurityTeam(user, securityTeam));
	}

	@Override
	public void removeUserFromAllBlacklists(PerunSession sess, User user) throws InternalErrorException {
		getSecurityTeamsManagerImpl().removeUserFromAllBlacklists(sess, user);
		getPerunBl().getAuditer().log(sess, new UserRemovedFromBlacklists(user));
	}

	@Override
	public List<User> getBlacklist(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		List<SecurityTeam> wrapper = new ArrayList<>();
		wrapper.add(securityTeam);
		return getSecurityTeamsManagerImpl().getBlacklist(sess, wrapper);
	}

	@Override
	public List<User> getBlacklist(PerunSession sess, Facility facility) throws InternalErrorException {
		List<SecurityTeam> securityTeams = perunBl.getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
		return getSecurityTeamsManagerImpl().getBlacklist(sess, securityTeams);
	}

	@Override
	public List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		List<SecurityTeam> wrapper = new ArrayList<>();
		wrapper.add(securityTeam);
		return getSecurityTeamsManagerImpl().getBlacklistWithDescription(sess, wrapper);
	}

	@Override
	public List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, Facility facility) throws InternalErrorException {
		List<SecurityTeam> securityTeams = perunBl.getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
		return getSecurityTeamsManagerImpl().getBlacklistWithDescription(sess, securityTeams);
	}

	@Override
	public void checkSecurityTeamExists(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamNotExistsException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkSecurityTeamExists(sess, securityTeam);
	}

	@Override
	public void checkSecurityTeamNotExists(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamExistsException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkSecurityTeamNotExists(sess, securityTeam);
	}

	@Override
	public void checkSecurityTeamUniqueName(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamExistsException {
		getSecurityTeamsManagerImpl().checkSecurityTeamUniqueName(sess, securityTeam);
	}

	@Override
	public void checkUserIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkUserIsNotSecurityAdmin(sess, securityTeam, user);
	}

	@Override
	public void checkUserIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkUserIsSecurityAdmin(sess, securityTeam, user);
	}

	@Override
	public void checkGroupIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkGroupIsNotSecurityAdmin(sess, securityTeam, group);
	}

	@Override
	public void checkGroupIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws GroupNotAdminException, InternalErrorException {
		getSecurityTeamsManagerImpl().checkGroupIsSecurityAdmin(sess, securityTeam, group);
	}

	@Override
	public void checkUserIsNotInBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws UserAlreadyBlacklistedException, InternalErrorException {
		if (isUserBlacklisted(sess, securityTeam, user)) {
			throw new UserAlreadyBlacklistedException("User "+user+" is already in blacklist of security team "+securityTeam);
		}
	}

	@Override
	public void checkUserIsInBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws UserAlreadyRemovedException, InternalErrorException {
		if (!isUserBlacklisted(sess, securityTeam, user)) {
			throw new UserAlreadyRemovedException("User "+user+" is not in blacklist of security team "+securityTeam);
		}
	}

	@Override
	public boolean isUserBlacklisted(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException {
		return getSecurityTeamsManagerImpl().isUserBlacklisted(sess, securityTeam, user);
	}

	@Override
	public boolean isUserBlacklisted(PerunSession sess, User user) throws InternalErrorException {
		return getSecurityTeamsManagerImpl().isUserBlacklisted(sess, user);
	}

	/**
	 * Gets the securityTeamsManagerImpl.
	 *
	 * @return The securityTeamsManagerImpl.
	 */
	public SecurityTeamsManagerImplApi getSecurityTeamsManagerImpl() {
		return this.securityTeamsManagerImpl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}
}
