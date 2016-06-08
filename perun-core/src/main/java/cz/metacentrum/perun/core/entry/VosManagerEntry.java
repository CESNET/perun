package cz.metacentrum.perun.core.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Group;
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
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * VosManager entry logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerEntry implements VosManager {

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

	public List<Vo> getVos(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.notNull(sess, "sess");

		// Perun admin can see everything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			return vosManagerBl.getVos(sess);
		} else {
			if(sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN) ||
			   sess.getPerunPrincipal().getRoles().hasRole(Role.VOOBSERVER) ||
					sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN)) {

				Set<Vo> vos = new HashSet<Vo>();

				// Get Vos where user is VO Admin
				for (PerunBean vo: AuthzResolver.getComplementaryObjectsForRole(sess, Role.VOADMIN, Vo.class)) {
					vos.add((Vo) vo);
				}

				// Get Vos where user is VO Observer
				for (PerunBean vo: AuthzResolver.getComplementaryObjectsForRole(sess, Role.VOOBSERVER, Vo.class)) {
					vos.add((Vo) vo);
				}

				// Get Vos where user has an group admin right on some of the group
				for(PerunBean group: AuthzResolver.getComplementaryObjectsForRole(sess, Role.GROUPADMIN, Group.class)) {
					try {
						vos.add(vosManagerBl.getVoById(sess, ((Group) group).getVoId()));
					} catch (VoNotExistsException e) {
						throw new ConsistencyErrorException("User has group admin role for group from non-existent VO id:" + ((Group) group).getVoId(), e);
					}
				}

				return new ArrayList<Vo>(vos);
			} else {
				throw new PrivilegeException(sess, "getVos");
			}
		}
	}

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

	public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException {
		Utils.notNull(sess, "sess");

		// Authorization - only Perun admin can delete the VO
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteVo");
		}

		vosManagerBl.checkVoExists(sess, vo);

		vosManagerBl.deleteVo(sess, vo, forceDelete);
	}

	public void deleteVo(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException {
		Utils.notNull(sess, "sess");

		// Authorization - only Perun admin can delete the VO
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteVo");
		}

		vosManagerBl.checkVoExists(sess, vo);

		vosManagerBl.deleteVo(sess, vo);
	}

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

	public List<User> getAdmins(PerunSession perunSession, Vo vo, Role role, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, VoNotExistsException, RoleNotSupportedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) && 
						!(role.equals(Role.VOADMIN) &&
						!(role.equals(role.VOOBSERVER)))) {
			throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
		}

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return vosManagerBl.getAdmins(perunSession, vo, role, onlyDirectAdmins);
	}

	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, Role role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException, RoleNotSupportedException {
		Utils.notNull(perunSession, "perunSession");
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) && 
						!(role.equals(Role.VOADMIN) &&
						!(role.equals(role.VOOBSERVER)))) {
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

	public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, Role role) throws InternalErrorException, PrivilegeException, VoNotExistsException, RoleNotSupportedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(role, "role");
		vosManagerBl.checkVoExists(perunSession, vo);

		//Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
		if(!role.equals(Role.TOPGROUPCREATOR) && 
						!(role.equals(Role.VOADMIN) &&
						!(role.equals(role.VOOBSERVER)))) {
			throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
		}

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(perunSession, "getAdminGroups");
				}

		return vosManagerBl.getAdminGroups(perunSession, vo, role);
	}


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

	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getRichAdmins");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdmins(sess, vo));
	}

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

	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.notNull(sess, "sess");
		vosManagerBl.checkVoExists(sess, vo);

		//  Authorization - Vo admin required
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getRichAdminsWithSpecificAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
	}

	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
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
	public int getVosCount(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		return vosManagerBl.getVosCount(sess);
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
