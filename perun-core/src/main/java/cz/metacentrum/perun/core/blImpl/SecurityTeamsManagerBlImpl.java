package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
import java.util.List;

/**
 * Created by ondrej on 12.8.15.
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
	public SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamExistsException, InternalErrorException {
		return getSecurityTeamsManagerImpl().createSecurityTeam(sess, securityTeam);
	}

	@Override
	public SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotExistsException {
		return getSecurityTeamsManagerImpl().updateSecurityTeam(sess, securityTeam);
	}

	@Override
	public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamNotExistsException, InternalErrorException {
		getSecurityTeamsManagerImpl().deleteSecurityTeam(sess, securityTeam);
	}

	@Override
	public SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws SecurityTeamNotExistsException, InternalErrorException {
		return getSecurityTeamsManagerImpl().getSecurityTeamById(sess, id);
	}

	@Override
	public List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		return getSecurityTeamsManagerImpl().getAdmins(sess, securityTeam);
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException {
		AuthzResolverBlImpl.addAdmin(sess, securityTeam, user);
		getPerunBl().getAuditer().log(sess, "{} was added as security admin of {}.", user, securityTeam);
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, AlreadyAdminException {
		AuthzResolverBlImpl.addAdmin(sess, securityTeam, group);
		getPerunBl().getAuditer().log(sess, "{} was added as security admins of {}.", group, securityTeam);
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException {
		AuthzResolverBlImpl.removeAdmin(sess, securityTeam, user);
		getPerunBl().getAuditer().log(sess, "{} was removed from security admins of {}.", user, securityTeam);
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, GroupNotAdminException {
		AuthzResolverBlImpl.removeAdmin(sess, securityTeam, group);
		getPerunBl().getAuditer().log(sess, "{} was removed from security admins of {}.", group, securityTeam);
	}

	@Override
	public void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException {
		getSecurityTeamsManagerImpl().addUserToBlacklist(sess, securityTeam, user);
	}

	@Override
	public void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException {
		getSecurityTeamsManagerImpl().removeUserFromBlacklist(sess, securityTeam, user);
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
