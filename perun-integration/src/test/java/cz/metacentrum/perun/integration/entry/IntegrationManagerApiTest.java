package cz.metacentrum.perun.integration.entry;

import static org.assertj.core.api.Assertions.assertThat;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.integration.apiImpl.IntegrationManagerApiImpl;
import cz.metacentrum.perun.integration.model.GroupMemberRelation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-integration.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public class IntegrationManagerApiTest {

  @Autowired
  private IntegrationManagerApiImpl integrationManagerApiImpl;

  @Autowired
  private PerunBl perun;

  private PerunSession sess;

  private Vo vo;
  private Group group;
  private User user;
  private Member member;

  @Before
  public void setUp() throws Exception {
    final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    sess = perun.getPerunSession(pp, new PerunClient());

    vo = perun.getVosManagerBl().createVo(sess, new Vo(0, "test-vo", "test-vo"));
    group = perun.getGroupsManagerBl().createGroup(sess, vo, new Group("test-group", "test-group"));
    user = perun.getUsersManagerBl().createUser(sess, new User(0, "John", "Doe", "", "", ""));
    member = perun.getMembersManagerBl().createMember(sess, vo, user);
  }

  @Test
  public void getGroupMembersRelations_returnsDirectMember() throws Exception {
    perun.getGroupsManagerBl().addMember(sess, group, member);

    var groupMemberData = integrationManagerApiImpl.getGroupMemberData(sess);

    var expectedRelation = directRelation(group.getId(), member.getId(), user.getId(), group.getShortName());

    assertThat(groupMemberData.relations())
        .contains(expectedRelation);
  }

  @Test
  public void getGroupMembersRelations_returnsExpiredMember() throws Exception {
    perun.getGroupsManagerBl().addMember(sess, group, member);
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member, group);

    var groupMemberData = integrationManagerApiImpl.getGroupMemberData(sess);

    var expectedRelation =
        directRelation(group.getId(), member.getId(), user.getId(), group.getShortName(), MemberGroupStatus.EXPIRED);

    assertThat(groupMemberData.relations())
        .contains(expectedRelation);
  }

  @Test
  public void getGroupMembersRelations_returnsMultipleRelations() throws Exception {
    var subgroup = perun.getGroupsManagerBl().createGroup(sess, group, new Group("subgroup", "ss"));
    perun.getGroupsManagerBl().addMember(sess, group, member);
    perun.getGroupsManagerBl().addMember(sess, subgroup, member);

    var groupMemberData = integrationManagerApiImpl.getGroupMemberData(sess);

    var directRelation = directRelation(group.getId(), member.getId(), user.getId(), group.getShortName());
    var indirectRelation =
        inDirectRelation(group.getId(), subgroup.getId(), member.getId(), user.getId(), group.getShortName());

    assertThat(groupMemberData.relations())
        .contains(directRelation)
        .contains(indirectRelation);
  }

  @Test
  public void getGroupMembersRelations_returnsIndirectMember() throws Exception {
    var subgroup = perun.getGroupsManagerBl().createGroup(sess, group, new Group("subgroup", "ss"));
    perun.getGroupsManagerBl().addMember(sess, subgroup, member);

    var groupMemberData = integrationManagerApiImpl.getGroupMemberData(sess);

    var expectedRelation =
        inDirectRelation(group.getId(), subgroup.getId(), member.getId(), user.getId(), group.getShortName());

    assertThat(groupMemberData.relations())
        .contains(expectedRelation);
  }

  @Test
  public void getGroupMembersRelations_returnsMemberGroupAttribute() throws Exception {
    var attrDef = createMemberGroupAttr("attr1");
    var attr = new Attribute(attrDef);
    attr.setValue("ahoj");

    perun.getGroupsManagerBl().addMember(sess, group, member);
    perun.getAttributesManagerBl().setAttribute(sess, member, group, attr);

    var groupMemberData = integrationManagerApiImpl.getGroupMemberData(sess);

    var returnedAttributes = groupMemberData.groupMemberAttributes();
    assertThat(returnedAttributes.get(group.getId()))
        .isNotNull();
    assertThat(returnedAttributes.get(group.getId()).get(member.getId()))
        .isNotNull()
        .contains(attr);
  }

  private GroupMemberRelation directRelation(int groupId, int memberId, int userId, String groupName) {
    return directRelation(groupId, memberId, userId, groupName, MemberGroupStatus.VALID);
  }

  private GroupMemberRelation directRelation(int groupId, int memberId, int userId, String groupName,
                                             MemberGroupStatus status) {
    return new GroupMemberRelation(groupId, memberId, userId, groupId, groupName, 0, status, MembershipType.DIRECT);
  }

  private GroupMemberRelation inDirectRelation(int groupId, int sourceGroupId, int memberId, int userId,
                                               String groupName) {
    return inDirectRelation(groupId, sourceGroupId, memberId, userId, groupName, MemberGroupStatus.VALID);
  }

  private GroupMemberRelation inDirectRelation(int groupId, int sourceGroupId, int memberId, int userId,
                                               String groupName, MemberGroupStatus status) {
    return new GroupMemberRelation(groupId, memberId, userId, sourceGroupId, groupName, 0, status,
        MembershipType.INDIRECT);
  }

  private AttributeDefinition createMemberGroupAttr(String friendlyName) throws Exception {
    var attrDef = new AttributeDefinition();
    attrDef.setFriendlyName(friendlyName);
    attrDef.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
    attrDef.setType(String.class.getName());
    return perun.getAttributesManagerBl().createAttribute(sess, attrDef);
  }
}
