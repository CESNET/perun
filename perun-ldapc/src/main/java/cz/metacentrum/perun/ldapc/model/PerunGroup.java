package cz.metacentrum.perun.ldapc.model;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunGroup extends PerunEntry<Group> {

	/**
	 * Add group to LDAP.
	 *
	 * @param group group from Perun
	 *
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void addGroup(Group group) throws InternalErrorException;

	/**
	 * Remove group from LDAP
	 *
	 * @param group group from Perun
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void removeGroup(Group group) throws InternalErrorException;

	public void updateGroup(Group group) throws InternalErrorException;
	
	/**
	 * The same behavior like method 'addGroup'.
	 * Only call method 'addGroup' with group
	 *
	 * @param group group from Perun (then call addGroup(group) )
	 * @param parentGroup (is not used now, can be null) IMPORTANT
	 *
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void addGroupAsSubGroup(Group group, Group parentGroup) throws InternalErrorException;


	//-----------------------------MEMBER METHODS---------------------------------
	/**
	 * Add member to group in LDAP.
	 * It means add attribute to member and add attribute to group.
	 * If this group is 'members' group, add member attribute to the vo (of group) too.
	 *
	 * @param member the member
	 * @param group the group
	 *
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void addMemberToGroup(Member member, Group group) throws InternalErrorException;

	/**
	 * Remove member from group in LDAP.
	 * It means remove attribute from member and remove attribute from group.
	 * If this group is 'member' group, remove member attribute for vo (of group) too.
	 *
	 * @param member
	 * @param group
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void removeMemberFromGroup(Member member, Group group) throws InternalErrorException;

	/**
	 * Return true if member has already attribute 'memberOf' for this group in LDAP
	 *
	 * @param member the member
	 * @param group the group
	 * @return true if attribute 'memberOf' exists for this group, false if not
	 */
	public boolean isMember(Member member, Group group);

	/**
	 * Get all 'uniqueMember' values of group in LDAP.
	 *
	 * @param groupId group Id
	 * @param voId vo Id
	 * @return list of uniqueMember values
	 */
	@Deprecated
	public List<String> getAllUniqueMembersInGroup(int groupId, int voId);

	public void addAsVoAdmin(Group group, Vo vo);
	
	public void removeFromVoAdmins(Group group, Vo vo);
	
	public void addAsGroupAdmin(Group group, Group group2);
	
	public void removeFromGroupAdmins(Group group, Group group2);
	
	public void addAsFacilityAdmin(Group group, Facility facility);
	
	public void removeFromFacilityAdmins(Group group, Facility facility);
	
	public void synchronizeGroup(Group group, List<Member> members, List<Resource> resources, 
			List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) throws InternalErrorException;
	
	public void synchronizeMembers(Group group, List<Member> members);

	public void synchronizeResources(Group group, List<Resource> resources);

	public void synchronizeAdminRoles(Group group, List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities);
}
