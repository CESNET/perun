package cz.metacentrum.perun.core.provisioning;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenMemberDataNode;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates data in format:
 *
 * attributes: {...hashes...}
 * hierarchy: {
 *    ** facility **
 *    hashes: [...hashes...]
 *    members: []
 *    children: [
 *      {
 *        ** resource1 **
 *        hashes: [...hashes...]
 *        children: [
 *          {
 *            ** group A **
 *            hashes: [...hashes...]
 *            members: [...group members...]
 *            children: []
 *          },
 *          {
 *            ** group B **
 *            ...
 *          }
 *        ]
 *        members: [
 *          {
 *            ** member 1 **
 *            hashes: [...hashes...]
 *          },
 *          {
 *            ** member 2 **
 *            ...
 *          }
 *        ]
 *      },
 *      {
 *        ** resource2 **
 *        ...
 *      }
 *    ]
 * }
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GroupsHashedDataGenerator implements HashedDataGenerator {

	private final PerunSessionImpl sess;
	private final Service service;
	private final Facility facility;
	private final GenDataProvider dataProvider;
	private final boolean filterExpiredMembers;

	private GroupsHashedDataGenerator(PerunSessionImpl sess, Service service, Facility facility,
	                                 boolean filterExpiredMembers) {
		this.sess = sess;
		this.service = service;
		this.facility = facility;
		this.filterExpiredMembers = filterExpiredMembers;
		dataProvider = new GenDataProviderImpl(sess, service, facility);
	}

	@Override
	public HashedGenData generateData() {
		dataProvider.loadFacilityAttributes();

		List<Resource> resources = sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);

		List<GenDataNode> childNodes = resources.stream()
				.map(this::getDataForResource)
				.collect(Collectors.toList());

		List<String> facilityAttrHashes = dataProvider.getFacilityAttributesHashes();
		Map<String, Map<String, Object>> attributes = dataProvider.getAllFetchedAttributes();

		GenDataNode root = new GenDataNode.Builder()
				.hashes(facilityAttrHashes)
				.children(childNodes)
				.build();

		return new HashedGenData(attributes, root);
	}

	private GenDataNode getDataForResource(Resource resource) {
		List<Member> members;
		if (filterExpiredMembers) {
			members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
		} else {
			members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		}

		dataProvider.loadResourceAttributes(resource, members, true);

		List<String> resourceAttrHashes = dataProvider.getResourceAttributesHashes(resource, true);

		List<GenMemberDataNode> memberNodes = members.stream()
				.map(member -> getDataForMember(resource, member))
				.collect(Collectors.toList());

		List<Group> assignedGroups = sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		dataProvider.loadGroupsAttributes(resource, assignedGroups);

		List<GenDataNode> groupNodes = assignedGroups.stream()
				.map(group -> getDataForGroup(resource, group))
				.collect(Collectors.toList());

		return new GenDataNode.Builder()
				.hashes(resourceAttrHashes)
				.children(groupNodes)
				.members(memberNodes)
				.build();
	}

	private GenDataNode getDataForGroup(Resource resource, Group group) {
		List<String> groupAttrHashes = dataProvider.getGroupAttributesHashes(resource, group);

		// This used to be the old way but we dont actually need it
/*		List<GenDataNode> subGroupNodes;
		if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
			List<Group> subGroups = sess.getPerunBl().getGroupsManagerBl().getSubGroups(sess, group);
			dataProvider.loadGroupsAttributes(resource, subGroups);
			subGroupNodes = subGroups.stream()
					.map(subGroup -> getDataForGroup(resource, subGroup))
					.collect(Collectors.toList());
		} else {
			subGroupNodes = new ArrayList<>();
		}*/

		List<Member> members;
		if (filterExpiredMembers) {
			members = sess.getPerunBl().getGroupsManagerBl().getActiveGroupMembers(sess, group);
		} else {
			members = sess.getPerunBl().getGroupsManagerBl().getGroupMembersExceptInvalidAndDisabled(sess, group);
		}

		dataProvider.loadMemberGroupAttributes(group, members);

		List<GenMemberDataNode> memberNodes = members.stream()
				.map(member -> getDataForMember(resource, member, group))
				.collect(Collectors.toList());

		return new GenDataNode.Builder()
				.hashes(groupAttrHashes)
//				.children(subGroupNodes)
				.members(memberNodes)
				.build();
	}

	private GenMemberDataNode getDataForMember(Resource resource, Member member, Group group) {
		List<String> memberAttrHashes = dataProvider.getMemberAttributesHashes(resource, member, group);

		return new GenMemberDataNode(memberAttrHashes);
	}

	private GenMemberDataNode getDataForMember(Resource resource, Member member) {
		List<String> memberAttrHashes = dataProvider.getMemberAttributesHashes(resource, member);

		return new GenMemberDataNode(memberAttrHashes);
	}

	public static class Builder {
		private PerunSessionImpl sess;
		private Service service;
		private Facility facility;
		private boolean filterExpiredMembers = false;

		public Builder sess(PerunSessionImpl sess) {
			this.sess = sess;
			return this;
		}

		public Builder service(Service service) {
			this.service = service;
			return this;
		}

		public Builder facility(Facility facility) {
			this.facility = facility;
			return this;
		}

		public Builder filterExpiredMembers(boolean filterExpiredMembers) {
			this.filterExpiredMembers = filterExpiredMembers;
			return this;
		}

		public GroupsHashedDataGenerator build() {
			return new GroupsHashedDataGenerator(sess, service, facility, filterExpiredMembers);
		}
	}
}
