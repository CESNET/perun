package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			parms.stateChangingCheck();

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
	 * Vo Object must contain:
	 * name - lenght can be no more than 128 characters
	 * shortName - can contain only a-z, A-Z, 0-9, '.', '-', '_' and cannot be longer than 32 characters.
	 * Other parameters are ignored.
	 *
	 * @exampleParam vo { "name" : "My testing VO" , "shortName" : "test_vo" }
	 *
	 * @param vo Vo VO to create (value of VO's <code>id</code> is ignored and will be set internally)
	 * @throw VoExistsException When VO you try to create already exists.
	 * @return Vo Created VO with correct <code>id</code> set
	 */
	/*#
	 * Creates new VO. Caller is automatically set as VO manager.
	 *
	 * @param name String name - length can be no more than 128 characters
	 * @param shortName String shortName - can contain only a-z, A-Z, 0-9, '.', '-', '_' and cannot be longer than 32 characters.
	 * @throw VoExistsException When VO you try to create already exists.
	 * @return Vo Created VO with correct <code>id</code> set
	 * @exampleParam shortName "test_vo"
	 * @exampleParam name "My testing VO"
	 */
	createVo {
		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("vo")) {
				return ac.getVosManager().createVo(ac.getSession(), parms.read("vo", Vo.class));
			} else if (parms.contains("name") && parms.contains("shortName")) {
				String name = parms.readString("name");
				String shortName = parms.readString("shortName");
				Vo vo = new Vo(0, name, shortName);
				return ac.getVosManager().createVo(ac.getSession(), vo);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
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
			parms.stateChangingCheck();

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
	 * Find MemberCandidates for VO. MemberCandidates can be used to create new members. They are made of Candidate,
	 * RichUser and Member objects. Candidates are searched in VO's external sources (if available). RichUsers are
	 * searched in given VO and are associated with appropriate candidate and member. RichUsers for MemberCandidates
	 * may also be searched in the whole Perun.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attrNames List<String> list with names of attributes that should be find.
	 * @param searchString String Text to search by
	 * @throw VoNotExistsException When <code>id</code> of VO doesn't match any existing VO.
	 * @return List<MemberCandidate> List of MemberCandidates
	 */
	/*#
	 * Find MemberCandidates for GROUP. MemberCandidates can be used to create new members. They are made of Candidate,
	 * RichUser and Member objects. Candidates are searched in VO's or GROUP's (depends on privileges) external sources
	 * (if available). RichUsers are searched in given VO and are associated with appropriate candidate and member.
	 * RichUsers for appropriate Candidate are also searched in the whole Perun.
	 *
	 * @param group int GROUP <code>id</code>
	 * @param attrNames List<String> list with names of attributes that should be find.
	 * @param searchString String Text to search by
	 * @throw GroupNotExistsException When <code>id</code> of GROUP doesn't match any existing GROUP.
	 * @return List<MemberCandidate> List of MemberCandidates
	 */
	getCompleteCandidates {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("vo")) {
				return ac.getVosManager().getCompleteCandidates(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readList("attrNames", String.class),
						parms.readString("searchString"));
			} else {
				return ac.getVosManager().getCompleteCandidates(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.readList("attrNames", String.class),
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
			parms.stateChangingCheck();
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
	 * Add user as a sponsor for guest members of VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @throw AlreadyAdminException When User is already sponsor for VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw UserNotExistsException When User specified by <code>id</code> doesn't exists.
	 */
	/*#
	 * Add group as a sponsor of guest members of VO. All members of group will become sponsors.
	 *
	 * @param vo int VO <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
	 * @throw AlreadyAdminException When Group is already sponsor of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw GroupNotExistsException When Group specified by <code>id</code> doesn't exists.
	 */
	addSponsorRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			parms.stateChangingCheck();
			Vo vo = ac.getVoById(parms.readInt("vo"));
			if (parms.contains("user")) {
				ac.getVosManager().addSponsorRole(ac.getSession(), vo, ac.getUserById(parms.readInt("user")));
			} else {
				ac.getVosManager().addSponsorRole(ac.getSession(), vo, ac.getGroupById(parms.readInt("authorizedGroup")));
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
			parms.stateChangingCheck();
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
	 * Removes user as a sponsor. His or her sponsored members will be set as expired if the user was their last sponsor.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @throw UserNotAdminException When User is not sponsor
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw UserNotExistsException When User specified by <code>id</code> doesn't exists.
	 */
	/*#
	 * Removes group as a sponsor. All group members will cease to be sponsors, and their sponsored
	 * members will be set as expired if the group member was their last sponsor.
	 *
	 * @param vo int VO <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
	 * @throw GroupNotAdminException When Group is not manager of VO.
	 * @throw VoNotExistsException When VO specified by <code>id</code> doesn't exists.
	 * @throw GroupNotExistsException When Group specified by <code>id</code> doesn't exists.
	 */
	removeSponsorRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms)
				throws PerunException {
			parms.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getVosManager().removeSponsorRole(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getVosManager().removeSponsorRole(ac.getSession(),
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
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
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
				if (roleName == null) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Parameter role cannot be null.");
				} else {
					roleName = roleName.toUpperCase();
				}

				return ac.getVosManager().getAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					roleName, parms.readBoolean("onlyDirectAdmins"));
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
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
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
				if (roleName == null) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Parameter role cannot be null.");
				} else {
					roleName = roleName.toUpperCase();
				}

				return ac.getVosManager().getAdminGroups(ac.getSession(),
					ac.getVoById(parms.readInt("vo")), roleName);
			} else {
				return ac.getVosManager().getAdminGroups(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Get list of all richUser administrators for the vo and supported role with specific attributes.
	 *
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN, SPONSOR
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
				if (roleName == null) {
					throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Parameter role cannot be null.");
				} else {
					roleName = roleName.toUpperCase();
				}

				return ac.getVosManager().getRichAdmins(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					roleName, parms.readList("specificAttributes", String.class),
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
	},

	/*#
	 * Set ban for member on his vo. The member id is required,
	 * validityTo and description are optional. voId is ignored.
	 *
	 * @param banOnVo BanOnVo JSON object
	 * @return BanOnVo Created banOnVo
	 */
	setBan {
		@Override
		public BanOnVo call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getVosManager().setBan(ac.getSession(),
					parms.read("banOnVo", BanOnVo.class));
		}
	},

	/*#
	 * Remove vo ban with given id.
	 *
	 * @param banId int of vo ban
	 * @throw PrivilegeException insufficient permissions
	 * @throw BanNotExistsException if there is no ban with specified id
	 */
	removeBan {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getVosManager().removeBan(ac.getSession(),
					parms.readInt("banId"));

			return null;
		}
	},

	/*#
	 * Remove vo ban for member with given id.
	 *
	 * @param member int member id
	 * @throw PrivilegeException insufficient permissions
	 * @throw BanNotExistsException if there is no ban for member with given id
	 */
	removeBanForMember {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getVosManager().removeBanForMember(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));

			return null;
		}
	},

	/*#
	 * Get vo ban with given id.
	 *
	 * @param banId int id
	 * @return BanOnVo found ban
	 * @throw BanNotExistsException if there is no such ban
	 * @throw PrivilegeException insufficient permissions
	 */
	getBanById {
		@Override
		public BanOnVo call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getBanById(ac.getSession(),
					parms.readInt("banId"));
		}
	},

	/*#
	 * Get ban for given member, or null if he is not banned.
	 *
	 * @param member int member id
	 * @return BanOnVo found ban or null if the member is not banned
	 * @throw PrivilegeException insufficient permissions
	 * @throw MemberNotExistsException if there is no member with given id
	 */
	getBanForMember {
		@Override
		public BanOnVo call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getBanForMember(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));
		}
	},

	/*#
	 * Get list of all bans for vo with given id.
	 *
	 * @param vo int vo id
	 * @return List<BanOnVo> vo bans for given vo
	 * @throw PrivilegeException insufficient permissions
	 * @throw VoNotExistsException if there is no vo with given id
	 */
	getBansForVo {
		@Override
		public List<BanOnVo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getVosManager().getBansForVo(ac.getSession(),
					parms.readInt("vo"));
		}
	}
}
