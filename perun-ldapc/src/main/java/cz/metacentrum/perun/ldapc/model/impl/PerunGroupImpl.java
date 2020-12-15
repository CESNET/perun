package cz.metacentrum.perun.ldapc.model.impl;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class PerunGroupImpl extends AbstractPerunEntry<Group> implements PerunGroup {

	private final static Logger log = LoggerFactory.getLogger(PerunGroupImpl.class);

	@Autowired
	private PerunVO vo;
	@Autowired
	@Lazy
	private PerunUser user;
	@Autowired
	private PerunVO perunVO;
	@Autowired
	private PerunFacility perunFacility;

	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrPerunUniqueGroupName,
				PerunAttribute.PerunAttributeNames.ldapAttrDescription);
	}

	@Override
	protected List<PerunAttribute<Group>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> group.getName()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunGroupId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> String.valueOf(group.getId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunUniqueGroupName,
						PerunAttributeDesc.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> vo.getVoShortName(group.getVoId()) + ":" + group.getName()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId,
						PerunAttributeDesc.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> String.valueOf(group.getVoId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> group.getDescription()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroup,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> group.getParentGroupId() == null ? null : this.addBaseDN(this.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getParentGroupId()))).toString()
						// PerunAttributeNames.ldapAttrPerunGroupId + "=" + group.getParentGroupId().toString() + "," + PerunAttributeNames.ldapAttrPerunVoId + "=" + group.getVoId() + "," + ldapProperties.getLdapBase()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroupId,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> group.getParentGroupId() == null ? null : group.getParentGroupId().toString()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrUuid,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>) (group, attrs) -> group.getUuid().toString()
				)
		);

	}

	public void addGroup(Group group) {
		addEntry(group);
	}

	public void addGroupAsSubGroup(Group group, Group parentGroup) {
		//This method has the same implementation like 'addGroup'
		addGroup(group);
	}

	public void removeGroup(Group group) {
		Name groupDN = buildDN(group);
		Name fullGroupDN = this.addBaseDN(groupDN);
		DirContextOperations groupEntry = findByDN(groupDN);
		String[] uniqueMembers = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if (uniqueMembers != null)
			for (String memberDN : uniqueMembers) {
				DirContextOperations memberEntry = user.findByDN(LdapNameBuilder.newInstance(memberDN).build());
				memberEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, fullGroupDN.toString());
				ldapTemplate.modifyAttributes(memberEntry);
			}

		deleteEntry(group);
	}

	@Override
	public void updateGroup(Group group) {
		modifyEntry(group);
	}

	public void addMemberToGroup(Member member, Group group) {
		//Add member to group
		Name groupDN = buildDN(group);
		DirContextOperations groupEntry = findByDN(groupDN);
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		Name fullMemberDN = addBaseDN(memberDN);
		if (isMember(groupEntry, fullMemberDN)) return;

		groupEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, fullMemberDN.toString());
		ldapTemplate.modifyAttributes(groupEntry);

		//Add member to vo if this group is membersGroup
		if (group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Add info to vo
			vo.addMemberToVO(group.getVoId(), member);
		}
		//Add group info to member
		// user->add('memberOf' => groupDN)
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, addBaseDN(groupDN).toString());
		ldapTemplate.modifyAttributes(userEntry);
	}

	public void removeMemberFromGroup(Member member, Group group) {
		//Remove member from group
		Name groupDN = buildDN(group);
		DirContextOperations groupEntry = findByDN(groupDN);
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		Name fullMemberDN = addBaseDN(memberDN);
		if (!isMember(groupEntry, fullMemberDN)) return;

		groupEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, fullMemberDN.toString());
		ldapTemplate.modifyAttributes(groupEntry);

		//Remove member from vo if this group is membersGroup
		if (group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Remove info from vo
			vo.removeMemberFromVO(group.getVoId(), member);
		}
		//Remove group info from member
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, addBaseDN(groupDN).toString());
		ldapTemplate.modifyAttributes(userEntry);
	}

	@Override
	public void addAsVoAdmin(Group group, Vo vo) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo, voDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromVoAdmins(Group group, Vo vo) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo, voDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void addAsGroupAdmin(Group group, Group group2) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name groupDN = addBaseDN(getEntryDN(String.valueOf(group2.getVoId()), String.valueOf(group2.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup, groupDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromGroupAdmins(Group group, Group group2) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name groupDN = addBaseDN(getEntryDN(String.valueOf(group2.getVoId()), String.valueOf(group2.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup, groupDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void addAsFacilityAdmin(Group group, Facility facility) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility, facilityDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromFacilityAdmins(Group group, Facility facility) {
		DirContextOperations entry = findByDN(buildDN(group));
		Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility, facilityDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	protected void doSynchronizeMembers(DirContextOperations groupEntry, List<Member> members) {
		List<Name> memberList = new ArrayList<Name>(members.size());
		for (Member member : members) {
			memberList.add(addBaseDN(user.getEntryDN(String.valueOf(member.getUserId()))));
		}
		groupEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, memberList.stream().map(name -> name.toString()).toArray(String[]::new));
	}

	protected void doSynchronizeResources(DirContextOperations groupEntry, List<Resource> resources) {
		groupEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, resources.stream().map(resource -> String.valueOf(resource.getId())).toArray(String[]::new));
	}

	private void doSynchronizeAdminRoles(DirContextOperations entry, List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup,
				admin_groups.stream()
						.map(group -> addBaseDN(getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))))
						.toArray(Name[]::new)
		);
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo,
				admin_vos.stream()
						.map(vo -> addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId()))))
						.toArray(Name[]::new)
		);
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility,
				admin_facilities.stream()
						.map(facility -> addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId()))))
						.toArray(Name[]::new)
		);
	}

	@Override
	public void synchronizeGroup(Group group, Iterable<Attribute> attrs, List<Member> members, List<Resource> resources,
	                             List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		SyncOperation syncOp = beginSynchronizeEntry(group, attrs);
		doSynchronizeMembers(syncOp.getEntry(), members);
		doSynchronizeResources(syncOp.getEntry(), resources);
		doSynchronizeAdminRoles(syncOp.getEntry(), admin_groups, admin_vos, admin_facilities);
		commitSyncOperation(syncOp);
	}

	@Override
	public void synchronizeMembers(Group group, List<Member> members) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		doSynchronizeMembers(groupEntry, members);
		ldapTemplate.modifyAttributes(groupEntry);
		// user attributes are set when synchronizing users
	}

	@Override
	public void synchronizeResources(Group group, List<Resource> resources) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		doSynchronizeResources(groupEntry, resources);
		ldapTemplate.modifyAttributes(groupEntry);
	}

	@Override
	public void synchronizeAdminRoles(Group group, List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		doSynchronizeAdminRoles(groupEntry, admin_groups, admin_vos, admin_facilities);
		ldapTemplate.modifyAttributes(groupEntry);
	}

	public boolean isMember(Member member, Group group) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		Name userDN = addBaseDN(user.getEntryDN(String.valueOf(member.getUserId())));
		return isMember(groupEntry, userDN);
	}


	@Deprecated
	public List<String> getAllUniqueMembersInGroup(int groupId, int voId) {
		Pattern userIdPattern = Pattern.compile("[0-9]+");
		List<String> uniqueMembers = new ArrayList<String>();
		DirContextOperations groupEntry = findById(String.valueOf(groupId), String.valueOf(voId));
		String[] uniqueGroupInformation = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if (uniqueGroupInformation != null) {
			for (String s : uniqueGroupInformation) {
				Matcher userIdMatcher = userIdPattern.matcher(s);
				if (userIdMatcher.find())
					uniqueMembers.add(s.substring(userIdMatcher.start(), userIdMatcher.end()));
			}
		}
		return uniqueMembers;
	}

	@Override
	protected void mapToContext(Group group, DirContextOperations context) {
		context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.objectClassPerunGroup);
		mapToContext(group, context, getAttributeDescriptions());
	}

	@Override
	protected Name buildDN(Group group) {
		return getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
	}

	/**
	 * Get Group DN using VoId and GroupId.
	 *
	 * @param id IDs of VO and Group
	 * @return DN in String
	 */
	@Override
	public Name getEntryDN(String... id) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, id[0])
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunGroupId, id[1])
				.build();
	}

	private boolean isMember(DirContextOperations groupEntry, Name userDN) {
		String[] memberOfInformation = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if (memberOfInformation != null) {
			for (String s : memberOfInformation) {
				Name memberDN = LdapNameBuilder.newInstance(s).build();
				if (memberDN.compareTo(userDN) == 0)
					// TODO should probably cross-check the user.memberOf attribute
					return true;
			}
		}
		return false;
	}

	@Override
	public List<Name> listEntries() {
		return ldapTemplate.search(query().
						where("objectclass").is(PerunAttribute.PerunAttributeNames.objectClassPerunGroup),
				getNameMapper());
	}


}
