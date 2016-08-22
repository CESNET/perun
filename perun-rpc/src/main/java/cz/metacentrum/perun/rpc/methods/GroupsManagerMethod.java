package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Attribute;
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
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum GroupsManagerMethod implements ManagerMethod {

	/*#
	 * Creates a subgroup of a group.
	 *
	 * @param parentGroup int Parent Group <code>id</code>
	 * @param group Group JSON Group class
	 * @return Group Newly created group
	 */
	/*#
	 * Creates a new group in a VO.
	 *
	 * @param vo int Parent VO <code>id</code>
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
	 * Create union of two groups, where "operandGroup" is technically set as subgroup of "resultGroup".
	 * Members from "operandGroup" are added to "resultGroup" as INDIRECT members. Union is honored also
	 * in all group member changing operations.
	 *
	 * @param resultGroup int <code>id</code> of Group to have included "operandGroup"
	 * @param operandGroup int <code>id</code> of Group to be included into "resultGroup"
	 * @return Group Result group
	 */
	createGroupUnion {

		@Override
		public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getGroupsManager().createGroupUnion(ac.getSession(),
						ac.getGroupById(parms.readInt("resultGroup")),
						ac.getGroupById(parms.readInt("operandGroup")));
		}
	},

	/*#
	 * Deletes a group. Group is not deleted, if contains members or is assigned to any resource.
	 *
	 * @param group int Group <code>id</code>
	 */
	/*#
	 * Forcefully deletes a group (remove all group members, remove group from resources).
	 *
	 * @param group int Group <code>id</code>
	 * @param force boolean If true use force delete.
	 */
	deleteGroup {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if(parms.contains("force") && parms.readBoolean("force")) {
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
	 * Forcefully deletes a list of groups (remove all group members, remove group from resources).
	 *
	 * @param groups int[] Array of Group IDs
	 * @param forceDelete boolean If true use force delete.
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
					parms.readBoolean("forceDelete"));
			return null;
		}
	},

	/*#
	 * Removes union of two groups, when "operandGroup" is technically removed from subgroups of "resultGroup".
	 * Members from "operandGroup" are removed from "resultGroup" if they were INDIRECT members sourcing from this group only.
	 *
	 * @param resultGroup int <code>id</code> of Group to have removed "operandGroup" from subgroups
	 * @param operandGroup int <code>id</code> of Group to be removed from "resultGroup" subgroups
	 */
	removeGroupUnion {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getGroupsManager().removeGroupUnion(ac.getSession(),
					ac.getGroupById(parms.readInt("resultGroup")),
					ac.getGroupById(parms.readInt("operandGroup")));
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
	 * Returns a group by <code>id</code>.
	 *
	 * @param id int Group <code>id</code>
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
	 * IMPORTANT: need to use full name of group (ex. 'toplevel:a:b', not the shortname which is in this example 'b')
	 *
	 * @param vo int VO <code>id</code>
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
	 * Return all operand groups for specified result groups (all INCLUDED groups).
	 * If "reverseDirection" is TRUE than return all result groups for specified operand group (where group is INCLUDED).
	 *
	 * @param group int <code>id</code> of Group to get groups in union.
	 * @param reverseDirection boolean FALSE (default) return INCLUDED groups / TRUE = return groups where INCLUDED
	 * @return List<Group> List of groups in union relation.
	 */
	getGroupUnions {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGroupsManager().getGroupUnions(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readBoolean("reverseDirection"));
		}
	},

	/*#
	 * Adds a member to a group.
	 *
	 * @param group int Group <code>id</code>
	 * @param member int Member <code>id</code>
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
	 * @param group int Group <code>id</code>
	 * @param member int Member <code>id</code>
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
	 * @param group int Group <code>id</code>
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
	 * @param group int Group <code>id</code>
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
	 * @param group int Group <code>id</code>
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
	 * @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 * @param group int Child group <code>id</code>
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
	 * @param group int Group <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Adds an group admin to a group.
	 *
	 * @param group int Group <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
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
	 * @param group int Group <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Removes a group admin of a group.
	 *
	 * @param group int Group <code>id</code>
	 * @param authorizedGroup int Group <code>id</code>
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
	 * Get list of all group administrators for supported role and specific group.
	 *
	 * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * @param group int Group <code>id</code>
	 * @param onlyDirectAdmins int if == true, get only direct user administrators (if == false, get both direct and indirect)
	 *
	 * @return List<User> list of all group administrators of the given group for supported role
	 */
	/*#
	 * Returns administrators of a group.
	 *
	 * @deprecated
	 * @param group int Group <code>id</code>
	 * @return List<User> Group admins
	 */
	getAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("onlyDirectAdmins")) {
				return ac.getGroupsManager().getAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getGroupsManager().getAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
			}
		}
	},

	/*#
	 * Returns direct administrators of a group.
	 *
	 * @deprecated
	 * @param group int Group <code>id</code>
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
	 * Returns administrator groups of a group.
	 *
	 * @param group int Group <code>id</code>
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
	 * Get list of all richUser administrators for the group and supported role with specific attributes.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * If "onlyDirectAdmins" is == true, return only direct admins of the group for supported role with specific attributes.
	 * If "allUserAttributes" is == true, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param allUserAttributes int if == true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins int if == true, get only direct group administrators (if false, get both direct and indirect)
	 *
	 * @return List<RichUser> list of RichUser administrators for the group and supported role with attributes
	 */
	/*#
	* Get all Group admins as RichUsers
	*
	* @deprecated
	* @param group int Group <code>id</code>
	* @return List<RichUser> admins
	*/
	getRichAdmins {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("onlyDirectAdmins")) {
				return ac.getGroupsManager().getRichAdmins(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("specificAttributes", String.class),
								parms.readBoolean("allUserAttributes"),
								parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getGroupsManager().getRichAdmins(ac.getSession(),
					ac.getGroupById(parms.readInt("group")));
			}
		}
	},

	/*#
	* Get all Group admins as RichUsers with all their non-null user attributes
	*
	* @deprecated
	* @param group int Group <code>id</code>
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
	* @deprecated
	* @param group int Group <code>id</code>
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
	* @deprecated
	* @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 * @param vo int VO <code>id</code>
	 * @return int Groups count
	 */
	/*#
	 * Gets count of all groups.

	 * @return int groups count
	 */
	getGroupsCount {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				return ac.getGroupsManager().getGroupsCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			} else {
				return ac.getGroupsManager().getGroupsCount(ac.getSession());
			}
		}
	},

	/*#
	 * Returns subgroups count of a group.
	 *
	 * @param parentGroup int Parent group <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 * @param group int Group <code>id</code>
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
	 * @param group int Group <code>id</code>
	 * @return Vo Parent VO
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
	 * @param group int Child group <code>id</code>
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
	 * @param group int Child group <code>id</code>
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
	 * @param group int Child group <code>id</code>
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
	 * @param member int Member <code>id</code>
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
	 * @param member int Member <code>id</code>
	 * @param attribute Attribute attribute object with value
	 * @return List<Group> Groups of the member
	 */
	getMemberGroupsByAttribute {
		
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException{
			
			return ac.getGroupsManager().getMemberGroupsByAttribute(ac.getSession(),
					ac.getMemberById(parms.readInt("member")),parms.read("attribute", Attribute.class));
		}
	},

	/*#
	 * Returns all RichGroups containing selected attributes
	 *
	 * @param vo int <code>id</code> of vo
	 * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
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
	 * Returns RichSubGroups from parent group containing selected attributes (only 1 level sub groups).
	 *
	 * @param group int <code>id</code> of group
	 * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
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
	 * Returns all AllRichSubGroups from parent group containing selected attributes (all level subgroups).
	 *
	 * @param group int <code>id</code> of group
	 * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
	 * @return List<RichGroup> RichGroups containing selected attributes
	 */
	getAllRichSubGroupsWithAttributesByNames {

		@Override
		public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getAllRichSubGroupsWithAttributesByNames(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					parms.readList("attrNames", String.class));
		}
	},

	/*#
	 * Returns RichGroup selected by id containing selected attributes
	 *
	 * @param groupId int <code>id</code> of group
	 * @param attrNames List<String> if attrNames is null method will return RichGroup containing all attributes
	 * @return List<RichGroup> RichGroups containing selected attributes
	 */
	getRichGroupByIdWithAttributesByNames {

		@Override
		public RichGroup call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getRichGroupByIdWithAttributesByNames(ac.getSession(),
					parms.readInt("groupId"),
					parms.readList("attrNames", String.class));
		}
	},

	/*#
	 * Returns all groups of specific member including group "members".
	 *
	 * @param member int <code>id</code> of member
	 * @return List<Group> Groups of member
	 */
	getAllMemberGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getGroupsManager().getAllMemberGroups(ac.getSession(),
					ac.getMemberById(parms.readInt("member")));
		}
	};
}
