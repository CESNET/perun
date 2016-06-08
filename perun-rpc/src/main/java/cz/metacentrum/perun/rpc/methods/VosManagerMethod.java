package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.core.api.exceptions.RpcException;

import java.util.List;

public enum VosManagerMethod implements ManagerMethod {

	/*#
	 * Return list of VOs caller has relation with (is manager of VO, is manager of group in VO etc.).
	 * @return List<Vo> Found VOs
	 */
	getVos {
		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getVos(ac.getSession());
		}
	},

	/*#
     * Return list of all VOs in Perun.
     *
     * @return List<Vo> Found VOs
     */
	getAllVos {
		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getAllVos(ac.getSession());
		}
	},

	/*#
	 * Deletes a VO. If VO contain members, group or resources, it's not deleted and exception is thrown.
	 *
	 * @param vo int VO <code>id</code>
	 * @throw RelationExistsException When VO has members, groups or resources.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 */
 	/*#
	 * Deletes a VO. If <code>force == true</code> then VO is deleted including members, groups and resources.
	 * Otherwise only empty VO is deleted or exception is thrown.
	 *
	 * @param vo int VO <code>id</code>
	 * @param force boolean Force must be true
	 * @throw RelationExistsException When VO has members, groups or resources and <code>force == false</code> is set.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 */
	deleteVo {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("force")) {
				ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readBoolean("force"));
			} else {
				ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			}
			return null;
		}
	},

	/*#
	 * Creates new VO. Caller is automatically set as VO manager.
	 *
	 * @param vo Vo VO to create (value of VO's <code>id</code> is ignored and will be set internally)
	 * @throw VoExistsException When VO you try to create already exists.
	 * @return Vo Created VO with correct <code>id</code> set
	 */
	createVo {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getVosManager().createVo(ac.getSession(), parms.read("vo", Vo.class));
		}
	},

	/*#
	 * Updates a VO. Only <code>name</code> parameter is updated. VO to updated is determined by <code>id</code>
	 * parameter of passed VO object.
	 *
	 * @param vo Vo VO to update with modified params
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @return Vo Updated VO
	 */
	updateVo {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getVosManager().updateVo(ac.getSession(), parms.read("vo", Vo.class));
		}
	},

	/*#
	 * Returns a VO by its short name.
	 *
	 * @param shortName String VO shortName
	 * @throw VoNotExistsException When VO specified by short name doesn't exists.
	 * @return Vo Found VO
	 */
	getVoByShortName {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getVoByShortName(ac.getSession(), parms.readString("shortName"));
		}
	},

	/*#
	 * Returns a VO by its <code>id</code>.
	 *
	 * @param id int VO <code>id</code>
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @return Vo Found VO
	 */
	getVoById {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getVoById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Find candidates for VO. Candidates can be used to create new members. Candidates are searched
	 * in VOs external sources (if available). Candidates, which are already members of VO are never
	 * returned even if they match searchString.
	 *
	 * @param vo int VO <code>id</code>
	 * @param searchString String Text to search by
	 * @throw VoNotExistsException When <code>id</code> of VO doesn't match any existing VO.
	 * @return List<Candidate> List of Candidates
	 */
 	/*#
	 * Find candidates for VO with specified maximum number of results. Candidates can be used to create new members.
	 * Candidates are searched in VOs external sources (if available). Candidates, which are already members of VO are never
	 * returned even if they match searchString.
	 *
	 * @param vo int VO <code>id</code>
	 * @param searchString String Text to search by
	 * @param maxNumOfResults int Number of maximum results
	 * @throw VoNotExistsException When <code>id</code> of VO doesn't match any existing VO.
	 * @return List<Candidate> List of Candidates
	 */
	/*#
	 * Find candidates for Group. Candidates can be used to create new VO and Group members. Candidates are searched
	 * in Groups external sources (if available). Candidates, which are already members of VO are never
	 * returned even if they match searchString.
	 *
	 * @param group int Group <code>id</code>
	 * @param searchString String Text to search by
	 * @throw GroupNotExistsException When <code>id</code> of Group doesn't match any existing Group.
	 * @return List<Candidate> List of Candidates
	 */
	findCandidates {
		@Override
		public List<Candidate> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("maxNumOfResults")) {
				return ac.getVosManager().findCandidates(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"),
						parms.readInt("maxNumOfResults"));
			}  else if (parms.contains("group")) {
				return ac.getVosManager().findCandidates(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.readString("searchString"));
			}else {
				return ac.getVosManager().findCandidates(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"));
			}
		}
	},

	/*#
	 * Gets count of all vos.

	 * @return int vos count
	 */
	getVosCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getVosCount(ac.getSession());
		}
	},

	/*#
	 * Add user as a manager of VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @throw AlreadyAdminException When User is already manager of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw UserNotExistsException When User specified by <code>id</code> doesn't exists.
	 */
	/*#
	 * Add group as a manager of VO. All members of group will become VO managers.
	 * It means, that who can manage group will also control VO managers (by managing group membership).
	 *
	 * @param vo int VO <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
	 * @throw AlreadyAdminException When Group is already manager of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw GroupNotExistsException When Group specified by <code>id</code> doesn't exists.
	 */
	addAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getVosManager().addAdmin(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getVosManager().addAdmin(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Removes user from managers of VO. Please note, that user can keep management rights if they
	 * are provided by group membership and group is assigned as manager of VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @throw UserNotAdminException When User is not manager of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw UserNotExistsException When User specified by <code>id</code> doesn't exists.
	 */
	/*#
	 * Removes group from managers of VO. Please note, that users can keep their management rights if they
	 * are provided by concrete manager role assignment to selected users.
	 *
	 * @param vo int VO <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
	 * @throw GroupNotAdminException When Group is not manager of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw GroupNotExistsException When Group specified by <code>id</code> doesn't exists.
	 */
	removeAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getVosManager().removeAdmin(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getVosManager().removeAdmin(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Get list of all vo administrators for supported role and specific vo.
	 *
	 * If onlyDirectAdmins is true, return only direct admins of the vo for supported role.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param vo int VO <code>id</code>
	 * @param role String supported role name
	 * @param onlyDirectAdmins boolean if true, get only direct VO administrators (if false, get both direct and indirect)
	 *
	 * @return List<User> list of all user administrators of the given vo for supported role
	 */
	/*#
	 * Returns administrators of a VO.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @return List<User> VO admins
	 */
	getAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("role")) {
				String roleName = parms.readString("role");
				Role role;
				try {
					role = Role.valueOf(roleName);
				} catch (IllegalArgumentException ex) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
				}

				return ac.getVosManager().getAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					role, parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getVosManager().getAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Returns direct administrators of a VO.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @return List<User> VO admins
	 */
	getDirectAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getDirectAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Get list of administrator groups of the given VO.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param vo int VO <code>id</code>
	 * @param role String Role name
	 *
	 * @return List<Group> List of groups, who are administrators of the VO with supported role. Returns empty list if there is no VO group admin.
	 */
	/*#
	 * Returns group administrators of a VO.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @return List<User> VO admins
	 */
	getAdminGroups {
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("role")) {
				String roleName = parms.readString("role");
				Role role;
				try {
					role = Role.valueOf(roleName);
				} catch (IllegalArgumentException ex) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
				}

				return ac.getVosManager().getAdminGroups(ac.getSession(),
					ac.getVoById(parms.readInt("vo")), role);
			} else {
				return ac.getVosManager().getAdminGroups(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Get list of all richUser administrators for the vo and supported role with specific attributes.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * If "onlyDirectAdmins" is == true, return only direct admins of the vo for supported role with specific attributes.
	 * If "allUserAttributes" is == true, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param vo int VO Id
	 * @param role String role name
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param allUserAttributes boolean if == true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins boolean if == true, get only direct vo administrators (if false, get both direct and indirect)
	 *
	 * @return List<RichUser> list of RichUser administrators for the vo and supported role with attributes
	 */
	/*#
	 * Returns administrators of a VO.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @return List<RichUser> VO admins
	 */
	getRichAdmins {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("role")) {
				String roleName = parms.readString("role");
				Role role;
				try {
					role = Role.valueOf(roleName);
				} catch (IllegalArgumentException ex) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
				}

				return ac.getVosManager().getRichAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					role, parms.readList("specificAttributes", String.class),
					parms.readBoolean("allUserAttributes"),
					parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getVosManager().getRichAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},
	/*#
	 * Returns administrators of a VO with additional information.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @return List<RichUser> VO admins
	 */
	getRichAdminsWithAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getRichAdminsWithAttributes(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Returns administrators of a VO with additional information.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> VO rich admins with attributes
	 */
	getRichAdminsWithSpecificAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getVosManager().getRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	 * Returns administrators of a VO, which are directly assigned
	 * with additional information.
	 *
	 * @deprecated
	 * @param vo int VO <code>id</code>
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> VO rich admins with attributes
	 */
	getDirectRichAdminsWithSpecificAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getVosManager().getDirectRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readList("specificAttributes", String.class));
		}
	};
}
