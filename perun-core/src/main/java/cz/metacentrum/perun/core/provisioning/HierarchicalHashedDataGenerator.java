package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenMemberDataNode;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Generates data in format:
 *
 * attributes: {...hashes...}
 * hierarchy: {
 *   "1": {    ** facility id **
 *     members: {    ** all members on the facility **
 *        "4" : 5,    ** member id : user id **
 *        "6" : 7,    ** member id : user id **
 *       ...
 *     }
 *     children: [
 *       "2": {    ** resource id **
 *         children: []
 *         members: {    ** all members on the resource with id 2 **
 *           "4" : 5    ** member id : user id **
 *         }
 *       },
 *       "3": {
 *         ...
 *       }
 *     ]
 *   }
 * }
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class HierarchicalHashedDataGenerator implements HashedDataGenerator {

	private final PerunSessionImpl sess;
	private final Service service;
	private final Facility facility;
	private final GenDataProvider dataProvider;
	private final Set<Member> allMembers = new HashSet<>();
	private final boolean filterExpiredMembers;

	private HierarchicalHashedDataGenerator(PerunSessionImpl sess, Service service, Facility facility,
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

		List<Resource> resources =
				sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);

		Map<Integer, GenDataNode> childNodes = resources.stream()
				.collect(toMap(Resource::getId, this::getDataForResource));

		dataProvider.getFacilityAttributesHashes();
		Map<String, Map<String, Object>> attributes = dataProvider.getAllFetchedAttributes();

		Map<Integer, Integer> memberIdsToUserIds = allMembers.stream()
				.collect(toMap(Member::getId, Member::getUserId));

		GenDataNode root = new GenDataNode.Builder()
				.children(childNodes)
				.members(memberIdsToUserIds)
				.build();

		return new HashedGenData(attributes, root, facility.getId());
	}

	private GenDataNode getDataForResource(Resource resource) {
		List<Member> members;
		if (filterExpiredMembers) {
			members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
		} else {
			members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		}
		allMembers.addAll(members);

		dataProvider.loadResourceAttributes(resource, members, false);

		dataProvider.getResourceAttributesHashes(resource, false);

		members.forEach(member -> getDataForMember(resource, member));

		Map<Integer, Integer> memberIdsToUserIds = members.stream()
				.collect(toMap(Member::getId, Member::getUserId));

		return new GenDataNode.Builder()
				.members(memberIdsToUserIds)
				.build();
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

		public HierarchicalHashedDataGenerator build() {
			return new HierarchicalHashedDataGenerator(sess, service, facility, filterExpiredMembers);
		}
	}
}
