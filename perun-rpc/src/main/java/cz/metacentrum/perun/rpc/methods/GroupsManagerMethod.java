package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum GroupsManagerMethod implements ManagerMethod {

	/*#
	 * Creates a subgroup of a group.
	 *
	 * @param parentGroup int Parent Group ID
	 * @param group Group JSON Group class
	 * @return Group Newly created group
	 */
	/*#
	 * Creates a new group in a VO.
	 *
	 * @param vo int Parent VO ID
	 * @param group Group JSON Group class
	 * @return Group Newly created group
	 */
	createGroup {

		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("parentGroup")) {
				return ac.getGroupsManager().createGroup(ac.getSession(),
						ac.getGroupById(parms.readInt("parentGroup")),
						parms.read("group", Group.class));
			} else if (parms.contains("vo")) {
				return ac.getGroupsManager().createGroup(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.read("group", Group.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or parentGroup");
			}
		}
	},

	/*#
	 * Deletes a group.
	 *
	 * @param group int Group ID
	 */
	/*#
	 * Deletes a group (force).
	 *
	 * @param group int Group ID
	 * @param force int Force must be 1
	 */
	deleteGroup {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if(parms.contains("force") && parms.readInt("force") == 1) {
				ac.getGroupsManager().deleteGroup(ac.getSession(),
						ac.getGroupById(parms.readInt("group")), true);
				return null;
			} else {
				ac.getGroupsManager().deleteGroup(ac.getSession(),
						ac.getGroupById(parms.readInt("group")), false);
				return null;
			}
		}
	},
	
	/*#
	 * Delete groups (force).
	 *
	 * @param groups list of groups
	 * @param forceDelete int 
	 */
	deleteGroups {
		
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			
			//TODO: optimalizovat?
			int[] ids = parms.readArrayOfInts("groups");
			List<Group> groups = new ArrayList<>(ids.length);
			for (int i : ids) {
				groups.add(ac.getGroupById(i));
			}
			
			ac.getGroupsManager().deleteGroups(ac.getSession(),
					groups,
					parms.readInt("forceDelete") == 1);
			return null;
		}
	},
	
	/*#
	 * Updates a group.
	 *
	 * @param group Group JSON Group class
	 * @return Group Updated group
	 */
	updateGroup {

		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getGroupsManager().updateGroup(ac.getSession(),
					parms.read("group", Group.class));
		}
	},

	/*#
	 * Returns a group by ID.
	 *
	 * @param id int Group ID
	 * @return Group Found group
	 */
	getGroupById {

		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupById(parms.readInt("id"));
		}
	},

	/*#
	 * Returns a group by VO and Group name.
	 *
	 * @param vo int VO ID
	 * @param name String Group name
	 * @return Group Found group
	 */
	getGroupByName {

		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupByName(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readString("name"));
		}
	},

	/*#
	 * Adds a member to a group.
	 *
	 * @param group int Group ID
	 * @param member int Member ID
	 */
	addMember {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getGroupsManager().addMember(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					ac.getMemberById(parms.readInt("member")));
			return null;
		}
	},

	/*#
	 * Removes a member from a group.
	 *
	 * @param group int Group ID
	 * @param member int Member ID
	 */
	removeMember {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getGroupsManager().removeMember(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					ac.getMemberById(parms.readInt("member")));
			return null;
		}
	},

	/*#
	 * Returns members of a group.
	 *
	 * @param group int Group ID
	 * @return List<Member> Group members
	 */
	getGroupMembers {

		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getGroupMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));

		}
	},

	/*#
	 * Returns members of a group.
	 * RichMember contains User object.
	 *
	 * @param group int Group ID
	 * @return List<RichMember> Group members
	 */
	getGroupRichMembers {

		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupRichMembers(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns members of a group.
	 * RichMember contains User object and attributes.
	 *
	 * @param group int Group ID
	 * @return List<RichMember> Group members
	 */
	getGroupRichMembersWithAttributes {

		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupRichMembersWithAttributes(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns count of group members.
	 *
	 * @param group int Group ID
	 * @return int Members count
	 */
	getGroupMembersCount {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupMembersCount(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns all groups in a VO.
	 *
	 * @param vo int VO ID
	 * @return List<Group> Groups
	 */
	getAllGroups {
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getAllGroups(ac.getSession(), ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Returns all groups in a VO by a hierarchy.
	 * Example: [Group => [Group => [Group => []], Group => []]]
	 *
	 * @param vo int VO ID
	 * @return List<Object> Groups with subgroups
	 */
	getAllGroupsWithHierarchy {
		@Override
		public List<Object> call(ApiCaller ac, Deserializer parms) throws PerunException {
			List<Object> convertedGroups = new ArrayList<Object>();
			// Every list must contain as a first field the group object which represents the group. First list contains null on the first position.
			convertedGroups.add(0, null);

			Map<Group, Object> groups = ac.getGroupsManager().getAllGroupsWithHierarchy(ac.getSession(), ac.getVoById(parms.readInt("vo")));

			for (Group group: groups.keySet()) {
				convertedGroups.add(ac.convertGroupsWithHierarchy(group, (Map<Group, Object>) groups.get(group)));
			}
			return convertedGroups;
		}
	},

	/*#
	 * Returns a parent group of a group.
	 *
	 * @param group int Child group ID
	 * @return Group Parent group
	 */
	getParentGroup {
		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getParentGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns subgroups of a group.
	 *
	 * @param parentGroup int Group id
	 * @return List<Group> Child groups
	 */
	getSubGroups {
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getSubGroups(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")));
		}
	},

	/*#
	 * Adds an admin to a group.
	 *
	 * @param group int Group ID
	 * @param user int User ID
	 */
	/*#
	 * Adds an group admin to a group.
	 *
	 * @param group int Group ID
	 * @param authorizedGroup int Group ID
	 */
	addAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getGroupsManager().addAdmin(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getGroupsManager().addAdmin(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Removes an admin of a group.
	 *
	 * @param group int Group ID
	 * @param user int User ID
	 */
	/*#
	 * Removes a group admin of a group.
	 *
	 * @param group int Group ID
	 * @param authorizedGroup int Group ID
	 */
	removeAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getGroupsManager().removeAdmin(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getGroupsManager().removeAdmin(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Returns administrators of a group.
	 *
	 * @param group int Group ID
	 * @return List<User> Group admins
	 */
	getAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns direct administrators of a group.
	 *
	 * @param group int Group ID
	 * @return List<User> Group admins
	 */
	getDirectAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getDirectAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns group administrators of a group.
	 *
	 * @param group int Group ID
	 * @return List<Group> admins
	 */
	getAdminGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getAdminGroups(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	* Get all Group admins as RichUsers
	*
	* @param group int Group ID
	* @return List<RichUser> admins
	*/
	getRichAdmins {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getRichAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	* Get all Group admins as RichUsers with all their non-null user attributes
	*
	* @param group int Group ID
	* @return List<RichUser> admins with attributes
	*/
	getRichAdminsWithAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getRichAdminsWithAttributes(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	* Get all Group admins as RichUsers with specific attributes (from user namespace)
	*
	* @param group int Group ID
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> admins with attributes
	*/
	getRichAdminsWithSpecificAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	* Get all Group admins, which are assigned directly,
	*  as RichUsers with specific attributes (from user namespace)
	*
	* @param group int Group ID
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> direct admins with attributes
	*/
	getDirectRichAdminsWithSpecificAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getDirectRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	 * Returns direct descendant groups of a VO.
	 *
	 * @param vo int VO ID
	 * @return List<Group> Children groups
	 */
	getGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getGroups(ac.getSession(), ac.getVoById(parms.readInt("vo")));

		}
	},

	/*#
	 * Returns groups count in a VO.
	 *
	 * @param vo int VO ID
	 * @return int Groups count
	 */
	getGroupsCount {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupsCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Returns subgroups count of a group.
	 *
	 * @param parentGroup int Parent group ID
	 * @return int Subgroups count
	 */
	getSubGroupsCount {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getSubGroupsCount(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")));
		}
	},

	/*#
	 * Delete all groups in a VO.
	 *
	 * @param vo int VO ID
	 */
	deleteAllGroups {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getGroupsManager().deleteAllGroups(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
			return null;
		}
	},

	/*#
	 * Forces group synchronization.
	 *
	 * @param group int Group ID
	 */
	forceGroupSynchronization {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getGroupsManager().forceGroupSynchronization(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
			return null;
		}
	},

	/*#
	 * Returns parent VO of a group.
	 *
	 * @param group int Group ID
	 * @return VirtualOrganization Parent VO
	 */
	getVo {

		@Override
		public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getVo(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns members of a parent group.
	 *
	 * @param group int Child group ID
	 * @return List<Member> Parent group members
	 */
	getParentGroupMembers {

		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getParentGroupMembers(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns members of a parent group.
	 * RichMember contains User object.
	 *
	 * @param group int Child group ID
	 * @return List<RichMember> Parent group members
	 */
	getParentGroupRichMembers {

		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getParentGroupRichMembers(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns members of a parent group.
	 * RichMember contains User object and attributes.
	 *
	 * @param group int Child group ID
	 * @return List<RichMember> Parent group members
	 */
	getParentGroupRichMembersWithAttributes {

		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getParentGroupRichMembersWithAttributes(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
		}
	},

	/*#
	 * Returns groups for a member.
	 *
	 * @param member int Member ID
	 * @return List<Group> Groups of the member
	 */
	getMemberGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getMemberGroups(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));
		}
	},
	
	/*#
	 * Returns groups with specific attribute for a member.
	 * 
	 * @param member int Member ID
	 * @param 
	 * return List<Group> Groups of the member
	 */
	getMemberGroupsByAttribute {
		
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException{
			
			return ac.getGroupsManager().getMemberGroupsByAttribute(ac.getSession(),
					ac.getMemberById(parms.readInt("member")),parms.read("attribute", Attribute.class));
		}
	},

	/*#
	 * Return all RichGroups containing selected attributes
	 *
	 * @param vo
	 * @param attrNames if attrNames is null mothod will return RichGroups containing all attributes
	 * @return List<RichGroup> RichGroups containing selected attributes
	 */
	getAllRichGroupsWithAttributesByNames {

		@Override
		public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getAllRichGroupsWithAttributesByNames(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readList("attrNames", String.class));
		}
	},

	/*#
	 * Return all RichGroups containing selected attributes
	 *
	 * @param group
	 * @param attrNames if attrNames is null mothod will return RichGroups containing all attributes
	 * @return List<RichGroup> RichGroups containing selected attributes
	 */
	getRichSubGroupsWithAttributesByNames {

		@Override
		public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getRichSubGroupsWithAttributesByNames(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readList("attrNames", String.class));
		}
	},

	/*#
	 * Return RichGroup containing selected attributes
	 *
	 * @param groupId
	 * @param attrNames if attrNames is null mothod will return RichGroup containing all attributes
	 * @return List<RichGroup> RichGroups containing selected attributes
	 */
	getRichGroupsWithAttributesByNames {

		@Override
		public RichGroup call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getRichGroupByIdWithAttributesByNames(ac.getSession(),
					parms.readInt("groupId"),
					parms.readList("attrNames", String.class));
		}
	},
	
	getAllMemberGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getAllMemberGroups(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));
		}
	};
}
