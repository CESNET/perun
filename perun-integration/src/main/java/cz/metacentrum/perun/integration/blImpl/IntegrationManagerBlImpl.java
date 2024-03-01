package cz.metacentrum.perun.integration.blImpl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;
import cz.metacentrum.perun.integration.dao.IntegrationManagerDao;
import cz.metacentrum.perun.integration.model.GroupMemberData;
import java.util.List;

public class IntegrationManagerBlImpl implements IntegrationManagerBl {

  private IntegrationManagerDao integrationManagerDao;
  private PerunBl perun;

  /**
   * For the given groupMember data, load all member-group attributes.
   *
   * @param sess        session
   * @param groupMember groupId with memberId
   * @return member with his member-group attributes
   */
  private List<GroupMemberAttribute> getAttributesForMemberGroup(PerunSession sess, GroupMember groupMember) {
    try {
      return perun.getAttributesManagerBl()
          .getAttributes(sess, new Member(groupMember.memberId()), new Group(groupMember.groupId)).stream()
          .map(attr -> new GroupMemberAttribute(groupMember.groupId(), groupMember.memberId(), attr)).collect(toList());
    } catch (MemberGroupMismatchException e) {
      throw new InternalErrorException("Failed to load member-group attributes.", e);
    }
  }

  @Override
  public GroupMemberData getGroupMemberData(PerunSession sess) {
    var groupMembersRelations = integrationManagerDao.getGroupMemberRelations(sess);
    var attributesByGroupIdMemberId = groupMembersRelations.stream()
        .map(groupMemberRelation -> new GroupMember(groupMemberRelation.groupId(), groupMemberRelation.memberId()))
        .distinct().flatMap(groupMember -> getAttributesForMemberGroup(sess, groupMember).stream()).collect(
            groupingBy(GroupMemberAttribute::groupId,
                groupingBy(GroupMemberAttribute::memberId, mapping(GroupMemberAttribute::attribute, toList()))));

    return new GroupMemberData(groupMembersRelations, attributesByGroupIdMemberId);
  }

  public IntegrationManagerDao getIntegrationManagerDao() {
    return integrationManagerDao;
  }

  public PerunBl getPerun() {
    return perun;
  }

  public void setIntegrationManagerDao(IntegrationManagerDao integrationManagerDao) {
    this.integrationManagerDao = integrationManagerDao;
  }

  public void setPerun(PerunBl perun) {
    this.perun = perun;
  }

  private record GroupMember(int groupId, int memberId) {
  }

  private record GroupMemberAttribute(int groupId, int memberId, Attribute attribute) {
  }
}
