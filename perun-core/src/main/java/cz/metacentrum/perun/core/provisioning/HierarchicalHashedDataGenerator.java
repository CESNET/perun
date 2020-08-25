package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenMemberDataNode;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

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
 *        children: []
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
public class HierarchicalHashedDataGenerator implements HashedDataGenerator {

	private final PerunSessionImpl sess;
	private final Service service;
	private final Facility facility;
	private final GenDataProvider dataProvider;
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

		dataProvider.loadResourceAttributes(resource, members, false);

		List<String> resourceAttrHashes = dataProvider.getResourceAttributesHashes(resource, false);

		List<GenMemberDataNode> childNodes = members.stream()
				.map(member -> getDataForMember(resource, member))
				.collect(Collectors.toList());

		return new GenDataNode.Builder()
				.hashes(resourceAttrHashes)
				.members(childNodes)
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
