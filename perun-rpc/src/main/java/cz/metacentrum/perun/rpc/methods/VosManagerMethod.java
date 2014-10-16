package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.util.List;

public enum VosManagerMethod implements ManagerMethod {

	/*#
	 * Returns list of all VOs.
	 *
	 * @return List<VirtualOrganization> Found VOs
	 */
	getVos {
		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getVos(ac.getSession());
		}
	},

	getAllVos {
		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getAllVos(ac.getSession());
		}
	},

	/*#
	 * Deletes a VO.
	 *
	 * @param vo int VO ID
	 */
 	/*#
	 * Deletes a VO (force).
	 *
	 * @param vo int VO ID
	 * @param force int Force must be 1
	 */
	deleteVo {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("force")) {
				ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), true);
			} else {
				ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			}
			return null;
		}
	},

	/*#
	 * Creates a VO.
	 *
	 * @param vo VirtualOrganization JSON VO class
	 * @return VirtualOrganization Newly created VO
	 */
	createVo {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getVosManager().createVo(ac.getSession(), parms.read("vo", Vo.class));
		}
	},

	/*#
	 * Updates a VO.
	 *
	 * @param vo VirtualOrganization JSON VO class
	 * @return VirtualOrganization Updated VO
	 */
	updateVo {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getVosManager().updateVo(ac.getSession(), parms.read("vo", Vo.class));
		}
	},

	/*#
	 * Returns a VO by a short name.
	 *
	 * @param shortName String VO shortName
	 * @return VirtualOrganization Found VO
	 */
	getVoByShortName {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getVoByShortName(ac.getSession(), parms.readString("shortName"));
		}
	},

	/*#
	 * Returns a VO by ID.
	 *
	 * @param id int VO ID
	 * @return VirtualOrganization Found VO
	 */
	getVoById {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getVoById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Finds candidates for a VO.
	 *
	 * @param vo int VO ID
	 * @param searchString String String to search by
	 * @return List<Candidate> List of candidates
	 */
 	/*#
	 * Finds candidates for a VO. Maximum results specified.
	 *
	 * @param vo int VO ID
	 * @param searchString String String to search by
	 * @param maxNumOfResults int Maximum results
	 * @return List<Candidate> List of candidates
	 */
	findCandidates {
		@Override
		public List<Candidate> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("maxNumOfResults")) {
				return ac.getVosManager().findCandidates(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"),
						parms.readInt("maxNumOfResults"));
			} else {
				return ac.getVosManager().findCandidates(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("searchString"));
			}
		}
	},

	/*#
	 * Adds an admin to a VO.
	 *
	 * @param vo int VO ID
	 * @param user int User ID
	/*#
	 *  Adds a group admin to a VO.
	 *
	 *  @param vo int VO ID
	 *  @param authorizedGroup int Group ID
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
	 * Removes an admin from a VO.
	 *
	 * @param vo int VO ID
	 * @param user int User ID
	/*#
	 *  Removes a group admin from VO.
	 *
	 *  @param vo int VO ID
	 *  @param group int Group ID
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
	 * Get list of all user administrators for supported role and specific vo.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param perunSession
	 * @param vo
	 * @param role supported role
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given vo for supported role
	 */
	/*#
	 * Returns administrators of a VO.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
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
					role, parms.readInt("onlyDirectAdmins") == 1);
			} else {
				return ac.getVosManager().getAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Returns direct administrators of a VO.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
	 * @return List<User> VO admins
	 */
	@Deprecated
	getDirectAdmins {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			return ac.getVosManager().getDirectAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Get list of group administrators of the given VO.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param perunSession
	 * @param vo
	 * @param role
	 *
	 * @return List of groups, who are administrators of the Vo with supported role. Returns empty list if there is no VO group admin.
	 */
	/*#
	 * Returns group administrators of a VO.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
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
	 * If "onlyDirectAdmins" is "true", return only direct users of the vo for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the vo and supported role with attributes
	 */
	/*#
	 * Returns administrators of a VO.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
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
					parms.readInt("allUserAttributes") == 1,
					parms.readInt("onlyDirectAdmins") == 1);
			} else {
				return ac.getVosManager().getRichAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},
	/*#
	 * Returns administrators of a VO with additional information.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
	 * @return List<RichUser> VO admins
	 */
	@Deprecated
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
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> VO rich admins with attributes
	 */
	@Deprecated
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
	 * !!! DEPRECATED version !!!
	 *
	 * @param vo int VO ID
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> VO rich admins with attributes
	 */
	@Deprecated
	getDirectRichAdminsWithSpecificAttributes {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getVosManager().getDirectRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readList("specificAttributes", String.class));
		}
	};
}
