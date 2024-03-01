package cz.metacentrum.perun.core.provisioning;


import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenMemberDataNode;
import cz.metacentrum.perun.core.api.GenResourceDataNode;
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
 * <p>
 * attributes: {...hashes...}
 * hierarchy: {
 * "1": {    ** facility id **
 * members: {    ** all members on the facility **
 * "4" : 5,    ** member id : user id **
 * "6" : 7,    ** member id : user id **
 * ...
 * }
 * children: [
 * "2": {    ** resource id **
 * children: [],
 * voId: 30,
 * members: {    ** all members on the resource with id 2 **
 * "4" : 5    ** member id : user id **
 * }
 * },
 * "3": {
 * ...
 * }
 * ]
 * }
 * }
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class HierarchicalHashedDataGenerator implements HashedDataGenerator {

  private final PerunSessionImpl sess;
  private final Service service;
  private final Facility facility;
  private final GenDataProvider dataProvider;
  private final Set<Member> membersWithConsent = new HashSet<>();
  private final boolean filterExpiredMembers;
  private final boolean consentEval;

  private HierarchicalHashedDataGenerator(PerunSessionImpl sess, Service service, Facility facility,
                                          boolean filterExpiredMembers, boolean consentEval) {
    this.sess = sess;
    this.service = service;
    this.facility = facility;
    this.filterExpiredMembers = filterExpiredMembers;
    this.consentEval = consentEval;
    dataProvider = new GenDataProviderImpl(sess, service, facility);
  }

  @Override
  public HashedGenData generateData() {
    dataProvider.loadFacilityAttributes();

    List<Resource> resources =
        sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);

    if (BeansUtils.getCoreConfig().getForceConsents()) {
      List<Member> membersToEvaluate;
      if (filterExpiredMembers) {
        membersToEvaluate =
            sess.getPerunBl().getFacilitiesManagerBl().getAllowedMembersNotExpiredInGroups(sess, facility, service);
      } else {
        membersToEvaluate = sess.getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
      }
      membersWithConsent.addAll(sess.getPerunBl().getConsentsManagerBl()
          .evaluateConsents(sess, service, facility, membersToEvaluate, consentEval));
    }

    Map<Integer, GenDataNode> childNodes = resources.stream()
        .collect(toMap(Resource::getId, this::getDataForResource));

    dataProvider.getFacilityAttributesHashes();
    Map<String, Map<String, Object>> attributes = dataProvider.getAllFetchedAttributes();

    Map<Integer, Integer> memberIdsToUserIds = membersWithConsent.stream()
        .collect(toMap(Member::getId, Member::getUserId));

    GenDataNode root = new GenDataNode.Builder()
        .children(childNodes)
        .members(memberIdsToUserIds)
        .build();

    return new HashedGenData(attributes, root, facility.getId());
  }

  private GenResourceDataNode getDataForResource(Resource resource) {
    List<Member> members;
    if (filterExpiredMembers) {
      members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
    } else {
      members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
    }
    if (BeansUtils.getCoreConfig().getForceConsents()) {
      // remove the members without granted consents on required attributes
      members.removeIf(member -> !membersWithConsent.contains(member));
    } else {
      // we skipped this part if consents were required, so add them now
      membersWithConsent.addAll(members);
    }

    dataProvider.loadResourceAttributes(resource, members, true);

    dataProvider.getResourceAttributesHashes(resource, true);

    members.forEach(member -> getDataForMember(resource, member));

    Map<Integer, Integer> memberIdsToUserIds = members.stream()
        .collect(toMap(Member::getId, Member::getUserId));

    return new GenResourceDataNode.Builder()
        .members(memberIdsToUserIds)
        .voId(resource.getVoId())
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
    private boolean consentEval = false;

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

    public Builder consentEval(boolean consentEval) {
      this.consentEval = consentEval;
      return this;
    }

    public HierarchicalHashedDataGenerator build() {
      return new HierarchicalHashedDataGenerator(sess, service, facility, filterExpiredMembers, consentEval);
    }
  }
}
