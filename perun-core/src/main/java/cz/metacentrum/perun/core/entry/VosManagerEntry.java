package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * VosManager entry logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerEntry implements VosManager {

	private final static Logger log = LoggerFactory.getLogger(VosManagerEntry.class);

	private PerunBl perunBl;
	private VosManagerBl vosManagerBl;

	/**
	 * Constructor.
	 */
	public VosManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.vosManagerBl = this.perunBl.getVosManagerBl();
	}

	public VosManagerEntry() {
	}

	@Override
	public List<Vo> getVos(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");

		// Perun admin can see everything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			return vosManagerBl.getVos(sess);
		} else {
			if(sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN) ||
			   sess.getPerunPrincipal().getRoles().hasRole(Role.VOOBSERVER) ||
					sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN)) {

				List<Vo> tempVos = vosManagerBl.getVos(sess);
				tempVos.removeIf(vo -> {
					try {
						return !AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
								!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo);
					} catch (InternalErrorException e) {
						// if we can't determine authorization prevent returning it
						return true;
					}
				});
				Set<Vo> vos = new HashSet<>(tempVos);

				// Get Vos where user is VO Observer
				for (PerunBean vo: AuthzResolver.getComplementaryObjectsForRole(sess, Role.VOOBSERVER, Vo.class)) {
					vos.add((Vo) vo);
				}

				return new ArrayList<>(vos);
			} else {
				throw new PrivilegeException(sess, "getVos");
			}
		}
	}

	@Override
	public List<Vo> getAllVos(PerunSession perunSession) throws InternalErrorException, PrivilegeException {
		Utils.notNull(perunSession, "sess");
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN)) {
			throw new PrivilegeException(perunSession, "getAllVos");
				}
		return vosManagerBl.getVos(perunSession);
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws VoNotExistsException, InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");

		// Authorization - only Perun admin can delete the VO
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteVo");
		}

		vosManagerBl.checkVoExists(sess, vo);

		vosManagerBl.deleteVo(sess, vo, forceDelete);
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");

		// Authorization - only Perun admin can delete the VO
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteVo");
		}

		vosManagerBl.checkVoExists(sess, vo);

		vosManagerBl.deleteVo(sess, vo);
	}

	@Override
	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, PrivilegeException, InternalErrorException {
		Utils.notNull(sess, "sess");
		Utils.notNull(vo, "vo");

		// Authorization - Perun admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN)) {
			throw new PrivilegeException(sess, "createVo");
		}


		if (vo.getName().length() > 128) {
			throw new InternalErrorException("VO name is too long, >128 characters");
		}

		if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,32}$")) {
			throw new InternalErrorException("Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 32 characters.");
		}

		return vosManagerBl.createVo(sess, vo);
	}

	@Override
	public Vo updateVo(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "updateVo");
		}

		if (vo.getName().length() > 128) {
			throw new InternalErrorException("VO name is too long, >128 characters");
		}

		if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,32}$")) {
			throw new InternalErrorException("Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 32 characters.");
		}

		return vosManagerBl.updateVo(sess, vo);
	}

	@Override
	public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException, InternalErrorException, PrivilegeException {
		Utils.notNull(shortName, "shortName");
		Utils.notNull(sess, "sess");
		Vo vo = vosManagerBl.getVoByShortName(sess, shortName);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getVoByShortName");
		}

		return vo;
	}

	@Override
	public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException, InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");
		Vo vo = vosManagerBl.getVoById(sess, id);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF)) {
			throw new PrivilegeException(sess, "getVoById");
				}

		return vo;
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.notNull(searchString, "searchString");
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findCandidates");
				}

		return vosManagerBl.findCandidates(sess, vo, searchString, maxNumOfResults);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.notNull(searchString, "searchString");
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findCandidates");
		}

		return vosManagerBl.findCandidates(sess, vo, searchString);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.notNull(searchString, "searchString");
		Utils.notNull(sess, "sess");
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findCandidates");
		}

		return vosManagerBl.findCandidates(sess, group, searchString);
	}

	@Override
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.notNull(searchString, "searchString");
		Utils.notNull(sess, "sess");
		Utils.notNull(vo, "vo");
		Utils.notNull(attrNames, "attrNames");

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "getCompleteCandidates");
		}

		return vosManagerBl.getCompleteCandidates(sess, vo, attrNames, searchString);
	}

	@Override
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Group group, List<String> attrNames, String searchString) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.notNull(searchString, "searchString");
		Utils.notNull(sess, "sess");
		Utils.notNull(group, "group");
		Utils.notNull(attrNames, "attrNames");

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<ExtSource> extSources;

		Vo vo = getPerunBl().getGroupsManagerBl().getVo(sess, group);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)) {
			extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);

			// null the vo so users are searched in whole perun
			vo = null;
		} else if (AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			extSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
		} else {
			throw new PrivilegeException(sess, "getCompleteCandidates");
		}

		return vosManagerBl.getCompleteCandidates(sess, vo, group, attrNames, searchString, extSources);
	}

	@Override
	public void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, UserNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		vosManagerBl.addAdmin(sess, vo, user);
	}


	@Override
	public void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		vosManagerBl.addAdmin(sess, vo, group);
	}

	@Override
	public void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotAdminException, UserNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAdmin");
		}

		vosManagerBl.removeAdmin(sess, vo, user);
	}

	@Override
	public void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, PrivilegeException, VoNotExistsException, GroupNotAdminException, GroupNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAdmin");
		}

		vosManagerBl.removeAdmin(sess, vo, group);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Vo vo, Role role, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, VoNotExistsException, RoleNotSupportedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) &&
						!role.equals(Role.VOADMIN) &&
						!role.equals(Role.VOOBSERVER)) {
			throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
		}

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return vosManagerBl.getAdmins(perunSession, vo, role, onlyDirectAdmins);
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, Role role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException, RoleNotSupportedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) &&
						!role.equals(Role.VOADMIN) &&
						!role.equals(Role.VOOBSERVER)) {
			throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
		}

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.ENGINE)) {
			throw new PrivilegeException(perunSession, "getDirectRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, vosManagerBl.getRichAdmins(perunSession, vo, role, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, Role role) throws InternalErrorException, PrivilegeException, VoNotExistsException, RoleNotSupportedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) &&
						!role.equals(Role.VOADMIN) &&
						!role.equals(Role.VOOBSERVER)) {
			throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
		}

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(perunSession, "getAdminGroups");
				}

		return vosManagerBl.getAdminGroups(perunSession, vo, role);
	}


	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getAdmins");
				}

		return vosManagerBl.getAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getDirectAdmins");
				}

		return vosManagerBl.getDirectAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getAdminGroups");
				}

		return vosManagerBl.getAdminGroups(sess, vo);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getRichAdmins");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdmins(sess, vo));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getRichAdminsWithAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithAttributes(sess, vo));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getRichAdminsWithSpecificAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getDirectRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getDirectRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
	}

	@Override
	public int getVosCount(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		return vosManagerBl.getVosCount(sess);
	}

	/**
	 * Adds role SPONSOR for user in a VO.
	 */
	@Override
	public void addSponsorRole(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "addSponsorRole");
		}
		log.debug("addSponsorRole({},{})",vo.getShortName(),user.getId());
		AuthzResolverBlImpl.setRole(sess, user, vo, Role.SPONSOR);
	}

	/**
	 * Adds role SPONSOR for group in a VO.
	 */
	@Override
	public void addSponsorRole(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "addSponsorRole");
		}
		AuthzResolverBlImpl.setRole(sess, group, vo, Role.SPONSOR);
	}

	/**
	 * Removes role SPONSOR from user in a VO.
	 */
	@Override
	public void removeSponsorRole(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "removeSponsorRole");
		}
		AuthzResolverBlImpl.unsetRole(sess, user, vo, Role.SPONSOR);
	}

	/**
	 * Removes role SPONSOR from group in a VO.
	 */
	@Override
	public void removeSponsorRole(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		vosManagerBl.checkVoExists(sess, vo);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "removeSponsorRole");
		}
		AuthzResolverBlImpl.unsetRole(sess, group, vo, Role.SPONSOR);
	}

	/**
	 * Gets the perunBl for this instance.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
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
	 * Sets the vosManagerBl for this instance.
	 *
	 * @param vosManagerBl The vosManagerBl.
	 */
	public void setVosManagerBl(VosManagerBl vosManagerBl)
	{
		this.vosManagerBl = vosManagerBl;
	}
}
