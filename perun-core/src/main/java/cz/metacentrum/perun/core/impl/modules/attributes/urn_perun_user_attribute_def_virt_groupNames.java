package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForMultipleUsers;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupUpdated;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberExpiredInGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberValidatedInGroup;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberDisabled;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberInvalidated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberSuspended;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberValidated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 * Contains all group names of the user
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_groupNames extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  protected static final RowMapper<Pair<String, String>> ROW_MAPPER = new RowMapper<Pair<String, String>>() {
    @Override
    public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
      String voShortName = rs.getString("vo_short_name");
      String groupName = rs.getString("group_name");
      return new Pair<>(voShortName, groupName);
    }
  };
  private static final String FRIENDLY_NAME = "groupNames";
  private static final String A_U_V_GROUP_NAMES = AttributesManager.NS_USER_ATTR_VIRT + ":" + FRIENDLY_NAME;

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("groupNames");
    attr.setDisplayName("Group names");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Full names of groups which the user is a member.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    Set<String> groupNames = new TreeSet<>();
    List<Pair<String, String>> names;
    try {
      names = sess.getPerunBl().getDatabaseManagerBl().getJdbcPerunTemplate().query(
          "SELECT" + " DISTINCT vos.short_name AS vo_short_name, groups.name AS group_name" + " FROM" + " members" +
          " JOIN vos ON vos.id = members.vo_id AND members.user_id = ? AND members.status = ?" +
          " JOIN groups_members ON groups_members.member_id = members.id AND groups_members.source_group_status =" +
          " ?" + " JOIN groups ON groups_members.group_id = groups.id" + " ORDER BY vo_short_name, group_name",
          ROW_MAPPER, user.getId(), Status.VALID.getCode(), MemberGroupStatus.VALID.getCode());
    } catch (EmptyResultDataAccessException e) {
      names = new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    for (Pair<String, String> one : names) {
      String voShortName = one.getLeft();
      groupNames.add(voShortName);
      if (!VosManager.MEMBERS_GROUP.equals(one.getRight())) {
        String groupName = one.getRight();
        groupNames.add(voShortName + ":" + groupName);
      }
    }
    attribute.setValue(new ArrayList<>(groupNames));
    return attribute;
  }

  private AuditEvent resolveEvent(PerunSessionImpl sess, Member member) throws AttributeNotExistsException {
    User user = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
    AttributeDefinition attributeDefinition =
        sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_U_V_GROUP_NAMES);
    return new AttributeChangedForUser(new Attribute(attributeDefinition), user);
  }

  private AuditEvent resolveEventGroupUpdated(PerunSessionImpl sess, List<Member> members) throws
      AttributeNotExistsException {
    List<User> users = new ArrayList<>();
    UsersManagerBl usersManagerBl = sess.getPerunBl().getUsersManagerBl();
    for (Member member : members) {
      users.add(usersManagerBl.getUserByMember(sess, member));
    }
    AttributeDefinition attributeDefinition =
        sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_U_V_GROUP_NAMES);
    return new AttributeChangedForMultipleUsers(new Attribute(attributeDefinition), users);
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl sess, AuditEvent message)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    List<AuditEvent> resolvingMessages = new ArrayList<>();
    if (message == null) {
      return resolvingMessages;
    }

    if (message instanceof DirectMemberAddedToGroup) {
      resolvingMessages.add(resolveEvent(sess, ((DirectMemberAddedToGroup) message).getMember()));
    } else if (message instanceof IndirectMemberAddedToGroup) {
      resolvingMessages.add(resolveEvent(sess, ((IndirectMemberAddedToGroup) message).getMember()));
    } else if (message instanceof MemberRemovedFromGroupTotally) {
      resolvingMessages.add(resolveEvent(sess, ((MemberRemovedFromGroupTotally) message).getMember()));
    } else if (message instanceof MemberExpiredInGroup) {
      resolvingMessages.add(resolveEvent(sess, ((MemberExpiredInGroup) message).getMember()));
    } else if (message instanceof MemberValidatedInGroup) {
      resolvingMessages.add(resolveEvent(sess, ((MemberValidatedInGroup) message).getMember()));
    } else if (message instanceof MemberValidated) {
      resolvingMessages.add(resolveEvent(sess, ((MemberValidated) message).getMember()));
    } else if (message instanceof MemberExpired) {
      resolvingMessages.add(resolveEvent(sess, ((MemberExpired) message).getMember()));
    } else if (message instanceof MemberSuspended) {
      resolvingMessages.add(resolveEvent(sess, ((MemberSuspended) message).getMember()));
    } else if (message instanceof MemberDisabled) {
      resolvingMessages.add(resolveEvent(sess, ((MemberDisabled) message).getMember()));
    } else if (message instanceof MemberInvalidated) {
      resolvingMessages.add(resolveEvent(sess, ((MemberInvalidated) message).getMember()));
    } else if (message instanceof GroupUpdated) {
      List<Member> members = sess.getPerunBl().getGroupsManagerBl()
                                 .getGroupMembers(sess, ((GroupUpdated) message).getGroup());
      if (!members.isEmpty()) {
        // no need to create the event for groups with no members
        resolvingMessages.add(resolveEventGroupUpdated(sess, members));
      }
    }
    return resolvingMessages;
  }

}
