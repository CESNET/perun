package cz.metacentrum.perun.integration.blImpl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;
import cz.metacentrum.perun.integration.dao.IntegrationManagerImplApi;
import cz.metacentrum.perun.integration.model.GroupMembers;
import cz.metacentrum.perun.integration.model.GroupMemberRelations;
import cz.metacentrum.perun.integration.model.MemberWithAttributes;

import java.util.HashSet;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class IntegrationManagerBlImpl implements IntegrationManagerBl {

	private IntegrationManagerImplApi integrationManagerImplApi;
	private PerunBl perun;

	@Override
	public List<GroupMemberRelations> getGroupMemberRelations(PerunSession sess) {
		var groupMembersRelations = integrationManagerImplApi.getGroupMemberRelations(sess);
		return groupMembersRelations.stream()
			.map(groupMembers -> loadMemberGroupAttributes(sess, groupMembers))
			.collect(toList());
	}

	/**
	 * For the given group members, load all group-member attributes.
	 *
	 * @param sess session
	 * @param groupMembers group with members for which the attributes will be loaded
	 * @return group with members and their member-group attributes
	 */
	private GroupMemberRelations loadMemberGroupAttributes(PerunSession sess, GroupMembers groupMembers) {
		var membersWithAttributes = groupMembers.memberIds().stream()
			.map(memberId -> getAttributesForMemberGroup(sess, memberId, groupMembers.groupId()))
			.collect(toSet());
		return new GroupMemberRelations(groupMembers.groupId(), membersWithAttributes);
	}

	/**
	 * For the given member and group, load all member-group attributes.
	 *
	 * @param sess session
	 * @param memberId member id
	 * @param groupId group id
	 * @return member with his member-group attributes
	 */
	private MemberWithAttributes getAttributesForMemberGroup(PerunSession sess, Integer memberId, Integer groupId) {
		try {
			var attrs = perun.getAttributesManagerBl().getAttributes(sess, new Member(memberId), new Group(groupId));
			return new MemberWithAttributes(memberId, new HashSet<>(attrs));
		} catch (MemberGroupMismatchException e) {
			throw new InternalErrorException(e);
		}
	}

	public IntegrationManagerImplApi getIntegrationManagerImplApi() {
		return integrationManagerImplApi;
	}

	public void setIntegrationManagerImplApi(IntegrationManagerImplApi integrationManagerImplApi) {
		this.integrationManagerImplApi = integrationManagerImplApi;
	}

	public PerunBl getPerun() {
		return perun;
	}

	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}
}
