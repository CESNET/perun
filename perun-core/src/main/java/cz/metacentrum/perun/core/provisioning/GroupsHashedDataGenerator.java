package cz.metacentrum.perun.core.provisioning;

import static java.util.stream.Collectors.toMap;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenMemberDataNode;
import cz.metacentrum.perun.core.api.GenResourceDataNode;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates data in format:
 * <p>
 * attributes: {...hashes...} hierarchy: { "1": {    ** facility id ** members: {    ** all members on the facility **
 * "4" : 5,    ** member id : user id ** "6" : 7,    ** member id : user id ** ... } children: [ "2": {    ** resource
 * id ** voId: 99, children: [ "89": {    ** group id ** "children": {}, "members": { "91328": 57986, "91330": 60838 } }
 * ], "members": {    ** all members on the resource with id 2 ** "91328": 57986, "91330": 60838 } }, "3": { ... } ] }
 * }
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GroupsHashedDataGenerator implements HashedDataGenerator {

  private final PerunSessionImpl sess;
  private final Service service;
  private final Facility facility;
  private final GenDataProvider dataProvider;
  private final boolean filterExpiredGroupMembers;
  private final boolean filterExpiredVoMembers;
  private final boolean filterBannedMembers;
  private final Set<Member> membersWithConsent = new HashSet<>();
  private final boolean consentEval;
  private final int taskRunId;
  private static final Logger LOG = LoggerFactory.getLogger(GroupsHashedDataGenerator.class);

  private GroupsHashedDataGenerator(PerunSessionImpl sess, Service service, Facility facility,
                                    boolean filterExpiredGroupMembers,
                                    boolean filterExpiredVoMembers, boolean filterBannedMembers,
                                    boolean consentEval, int taskRunId) {
    this.sess = sess;
    this.service = service;
    this.facility = facility;
    this.filterExpiredGroupMembers = filterExpiredGroupMembers;
    this.filterExpiredVoMembers = filterExpiredVoMembers;
    this.filterBannedMembers = filterBannedMembers;
    this.consentEval = consentEval;
    this.taskRunId = taskRunId;
    dataProvider = new GenDataProviderImpl(sess, service, facility);
  }

  @Override
  public HashedGenData generateData() {
    dataProvider.loadFacilityAttributes();

    List<Resource> resources =
        sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);

    if (BeansUtils.getCoreConfig().getForceConsents()) {
      List<Member> membersToEvaluate;
      membersToEvaluate = sess.getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);

      membersWithConsent.addAll(sess.getPerunBl().getConsentsManagerBl()
          .evaluateConsents(sess, service, facility, membersToEvaluate, consentEval));
    }

    Map<Integer, GenDataNode> childNodes = resources.stream().collect(toMap(Resource::getId, this::getDataForResource));

    dataProvider.getFacilityAttributesHashes();
    Map<String, Map<String, Object>> attributes = dataProvider.getAllFetchedAttributes();

    Map<Integer, Integer> memberIdsToUserIds =
        membersWithConsent.stream().collect(toMap(Member::getId, Member::getUserId));

    GenDataNode root = new GenDataNode.Builder().children(childNodes).members(memberIdsToUserIds).build();

    LOG.info("Task run {} provisioning to {} by service {} includes the following users: {}", taskRunId,
        facility.getName(), service.getName(), new HashSet<>(memberIdsToUserIds.values()));

    return new HashedGenData(attributes, root, facility.getId());
  }

  private GenDataNode getDataForGroup(Resource resource, Group group) {
    dataProvider.getGroupAttributesHashes(resource, group);

    List<Member> members;
    if (filterExpiredGroupMembers) {
      members = sess.getPerunBl().getGroupsManagerBl().getActiveGroupMembers(sess, group);
      // previous method for active group members doesn't care about VO status, so we must remove INVALID and
      // DISABLED VO members
      members.removeIf(
          member -> member.getStatus().equals(Status.INVALID) || member.getStatus().equals(Status.DISABLED));
    } else {
      members = sess.getPerunBl().getGroupsManagerBl().getGroupMembersExceptInvalidAndDisabled(sess, group);
    }
    if (filterBannedMembers) {
      // still have to filter facility bans here since they get filtered through `getAllowedMembers` only with consents
      members.removeIf(member -> sess.getPerunBl().getFacilitiesManagerBl().banExists(sess, member.getUserId(),
          facility.getId()));
      members.removeIf(member -> sess.getPerunBl().getVosManagerBl().isMemberBanned(sess, member.getId()));
      members.removeIf(member -> sess.getPerunBl().getResourcesManagerBl().banExists(sess, member.getId(),
          resource.getId()));
    }
    members.removeIf(
          member -> filterExpiredVoMembers && member.getStatus().equals(Status.EXPIRED));
    if (BeansUtils.getCoreConfig().getForceConsents()) {
      // remove the members without granted consents on required attributes
      members.removeIf(member -> !membersWithConsent.contains(member));
    }

    dataProvider.loadMemberGroupAttributes(group, members);

    // This has to be called so the hashes are loaded!!!
    members.forEach(member -> getDataForMember(resource, member, group));

    Map<Integer, Integer> memberIdsToUserIds = members.stream().collect(toMap(Member::getId, Member::getUserId));

    return new GenDataNode.Builder().members(memberIdsToUserIds).build();
  }

  private GenMemberDataNode getDataForMember(Resource resource, Member member) {
    List<String> memberAttrHashes = dataProvider.getMemberAttributesHashes(resource, member);

    return new GenMemberDataNode(memberAttrHashes);
  }

  private GenMemberDataNode getDataForMember(Resource resource, Member member, Group group) {
    List<String> memberAttrHashes = dataProvider.getMemberAttributesHashes(resource, member, group);

    return new GenMemberDataNode(memberAttrHashes);
  }

  private GenResourceDataNode getDataForResource(Resource resource) {
    List<Member> members;
    if (filterExpiredGroupMembers) {
      members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembersNotExpiredInGroups(sess, resource);
    } else {
      members = sess.getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
    }
    if (filterBannedMembers) {
      members.removeIf(member -> sess.getPerunBl().getFacilitiesManagerBl().banExists(sess, member.getUserId(),
          facility.getId()));
      members.removeIf(member -> sess.getPerunBl().getVosManagerBl().isMemberBanned(sess, member.getId()));
      members.removeIf(member -> sess.getPerunBl().getResourcesManagerBl().banExists(sess, member.getId(),
          resource.getId()));
    }
    members.removeIf(
          member -> filterExpiredVoMembers && member.getStatus().equals(Status.EXPIRED));
    if (BeansUtils.getCoreConfig().getForceConsents()) {
      // remove the members without granted consents on required attributes
      members.removeIf(member -> !membersWithConsent.contains(member));
    } else {
      // we skipped this part if consents were required, so add them now
      membersWithConsent.addAll(members);
    }

    dataProvider.loadResourceAttributes(resource, members, true);

    dataProvider.getResourceAttributesHashes(resource, true);

    // This must be called so the hashes are added!!
    members.forEach(member -> getDataForMember(resource, member));

    Map<Integer, Integer> memberIdsToUserIds = members.stream().collect(toMap(Member::getId, Member::getUserId));

    List<Group> assignedGroups = sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
    dataProvider.loadGroupsAttributes(resource, assignedGroups);

    Map<Integer, GenDataNode> groupNodes =
        assignedGroups.stream().collect(toMap(Group::getId, group -> getDataForGroup(resource, group)));

    return new GenResourceDataNode.Builder().children(groupNodes).voId(resource.getVoId()).members(memberIdsToUserIds)
        .build();
  }

  public static class Builder {
    private PerunSessionImpl sess;
    private Service service;
    private Facility facility;
    private boolean filterExpiredGroupMembers = false;
    private boolean filterExpiredVoMembers = false;
    private boolean filterBannedMembers = false;
    private boolean consentEval = false;
    private int taskRunId;


    public GroupsHashedDataGenerator build() {
      return new GroupsHashedDataGenerator(sess, service, facility, filterExpiredGroupMembers,
          filterExpiredVoMembers, filterBannedMembers, consentEval, taskRunId);
    }

    public Builder consentEval(boolean consentEval) {
      this.consentEval = consentEval;
      return this;
    }

    public Builder facility(Facility facility) {
      this.facility = facility;
      return this;
    }

    public Builder filterExpiredGroupMembers(boolean filterExpiredGroupMembers) {
      this.filterExpiredGroupMembers = filterExpiredGroupMembers;
      return this;
    }

    public Builder filterExpiredVoMembers(boolean filterExpiredVoMembers) {
      this.filterExpiredVoMembers = filterExpiredVoMembers;
      return this;
    }

    public Builder filterBannedMembers(boolean filterBannedMembers) {
      this.filterBannedMembers = filterBannedMembers;
      return this;
    }

    public Builder service(Service service) {
      this.service = service;
      return this;
    }

    public Builder taskRunId(int taskRunId) {
      this.taskRunId = taskRunId;
      return this;
    }

    public Builder sess(PerunSessionImpl sess) {
      this.sess = sess;
      return this;
    }
  }
}
