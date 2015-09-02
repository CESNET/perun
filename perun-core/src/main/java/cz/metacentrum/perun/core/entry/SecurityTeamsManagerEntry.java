package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SecurityTeamsManagerBl;
import cz.metacentrum.perun.core.impl.Utils;

import java.util.List;

/**
 * Created by ondrej on 5.8.15.
 */
public class SecurityTeamsManagerEntry implements cz.metacentrum.perun.core.api.SecurityTeamsManager {

	private SecurityTeamsManagerBl securityTeamsManagerBl;
	private PerunBl perunBl;

	public SecurityTeamsManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.securityTeamsManagerBl = perunBl.getSecurityTeamsManagerBl();
	}

	public SecurityTeamsManagerEntry() {
	}

	public SecurityTeamsManagerBl getSecurityTeamsManagerBl() {
		return this.securityTeamsManagerBl;
	}
	public PerunBl getPerunBl() {
		return this.perunBl;
	}
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
	public void setSecurityTeamsManagerBl(SecurityTeamsManagerBl securityTeamsManagerBl) {
		this.securityTeamsManagerBl = securityTeamsManagerBl;
	}

	@Override
	public List<SecurityTeam> getSecurityTeams(PerunSession sess) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN)) {
			throw new PrivilegeException(sess, "getSecurityTeams");
		}

		return getSecurityTeamsManagerBl().getSecurityTeams(sess);
	}

	@Override
	public List<SecurityTeam> getAllSecurityTeams(PerunSession sess) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)
				&& !AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN)) {
			throw new PrivilegeException(sess, "getAllSecurityTeams");
		}

		return getSecurityTeamsManagerBl().getAllSecurityTeams(sess);
	}

	@Override
	public SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws PrivilegeException, InternalErrorException, SecurityTeamExistsException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamNotExists(sess, securityTeam);
		getSecurityTeamsManagerBl().checkSecurityTeamUniqueName(sess, securityTeam);
		Utils.notNull(securityTeam, "securityTeam");
		Utils.notNull(securityTeam.getName(), "securityTeam.name");

		if (securityTeam.getName().length() > 128) {
			throw new InternalErrorException("Security Team name is too long, >128 characters");
		}

		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createSecurityTeam");
		}

		return getSecurityTeamsManagerBl().createSecurityTeam(sess, securityTeam);
	}

	@Override
	public SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, SecurityTeamExistsException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getSecurityTeamsManagerBl().checkSecurityTeamUniqueName(sess, securityTeam);
		Utils.notNull(securityTeam, "securityTeam");
		Utils.notNull(securityTeam.getName(), "securityTeam.name");

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "updateSecurityTeam");
		}

		return getSecurityTeamsManagerBl().updateSecurityTeam(sess, securityTeam);
	}

	@Override
	public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "deleteSecurityTeam");
		}

		getSecurityTeamsManagerBl().deleteSecurityTeam(sess, securityTeam);
	}

	@Override
	public SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)
				&& !AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN)
				&& !(AuthzResolver.isAuthorized(sess, Role.RPC))) {
			throw new PrivilegeException(sess, "getSecurityTeamById");
		}

		return getSecurityTeamsManagerBl().getSecurityTeamById(sess, id);
	}

	@Override
	public List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "getAdmins");
		}

		return getSecurityTeamsManagerBl().getAdmins(sess, securityTeam);
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, AlreadyAdminException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getSecurityTeamsManagerBl().checkUserIsNotSecurityAdmin(sess, securityTeam, user);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		getSecurityTeamsManagerBl().addAdmin(sess, securityTeam, user);
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, AlreadyAdminException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		getSecurityTeamsManagerBl().checkGroupIsNotSecurityAdmin(sess, securityTeam, group);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		getSecurityTeamsManagerBl().addAdmin(sess, securityTeam, group);

	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserNotAdminException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		getSecurityTeamsManagerBl().checkUserIsSecurityAdmin(sess, securityTeam, user);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "removeAdmin");
		}


		getSecurityTeamsManagerBl().removeAdmin(sess, securityTeam, user);
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, GroupNotAdminException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		getSecurityTeamsManagerBl().checkGroupIsSecurityAdmin(sess, securityTeam, group);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "removeAdmin");
		}

		getSecurityTeamsManagerBl().removeAdmin(sess, securityTeam, group);
	}

	@Override
	public void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserAlreadyBlacklistedException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getSecurityTeamsManagerBl().checkUserIsNotInBlacklist(sess, securityTeam, user);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "addUserToBlacklist");
		}

		getSecurityTeamsManagerBl().addUserToBlacklist(sess, securityTeam, user);
	}

	@Override
	public void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserAlreadyRemovedException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getSecurityTeamsManagerBl().checkUserIsInBlacklist(sess, securityTeam, user);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "removeUserFromBlacklist");
		}


		getSecurityTeamsManagerBl().removeUserFromBlacklist(sess, securityTeam, user);
	}

	@Override
	public List<User> getBlacklist(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);
		getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

		if (!AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam)) {
			throw new PrivilegeException(sess, "getBlacklist");
		}

		return getSecurityTeamsManagerBl().getBlacklist(sess, securityTeam);
	}

	@Override
	public List<User> getBlacklist(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getBlacklist");
		}

		return getSecurityTeamsManagerBl().getBlacklist(sess, facility);
	}

}
