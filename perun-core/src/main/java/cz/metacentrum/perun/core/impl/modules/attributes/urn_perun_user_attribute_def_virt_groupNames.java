package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberExpiredInGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberValidatedInGroup;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains all group names of the user
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_groupNames extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String FRIENDLY_NAME = "groupNames";
	private static final String A_U_V_GROUP_NAMES = AttributesManager.NS_USER_ATTR_VIRT + ":" + FRIENDLY_NAME;

	protected static final RowMapper<Pair<String, String>> ROW_MAPPER = new RowMapper<Pair<String, String>>() {
		@Override
		public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			String voShortName = rs.getString("vo_short_name");
			String groupName = rs.getString("group_name");
			return new Pair<>(voShortName, groupName);
		}
	};

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Set<String> groupNames = new TreeSet<>();
		List<Pair<String, String>> names;
		try {
			names = sess.getPerunBl().getDatabaseManagerBl().getJdbcPerunTemplate().query(
				"SELECT" +
					" DISTINCT vos.short_name AS vo_short_name, groups.name AS group_name" +
					" FROM" +
					" members" +
					" JOIN vos ON vos.id = members.vo_id AND members.user_id = ?" +
					" JOIN groups_members ON groups_members.member_id = members.id AND groups_members.source_group_status = ?" +
					" JOIN groups ON groups_members.group_id = groups.id" +
					" ORDER BY vo_short_name, group_name",
					ROW_MAPPER,
					user.getId(), MemberGroupStatus.VALID.getCode());
		} catch(EmptyResultDataAccessException e) {
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

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl sess, AuditEvent message) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		if (message instanceof DirectMemberAddedToGroup) {
			resolvingMessages.addAll(resolveEvent(sess, ((DirectMemberAddedToGroup) message).getMember()));
		} else if (message instanceof IndirectMemberAddedToGroup) {
			resolvingMessages.addAll(resolveEvent(sess, ((IndirectMemberAddedToGroup) message).getMember()));
		} else if (message instanceof MemberRemovedFromGroupTotally) {
			resolvingMessages.addAll(resolveEvent(sess, ((MemberRemovedFromGroupTotally) message).getMember()));
		} else if (message instanceof MemberExpiredInGroup) {
			resolvingMessages.addAll(resolveEvent(sess, ((MemberExpiredInGroup) message).getMember()));
		} else if (message instanceof MemberValidatedInGroup) {
			resolvingMessages.addAll(resolveEvent(sess, ((MemberValidatedInGroup) message).getMember()));
		}
		return resolvingMessages;
	}

	private List<AuditEvent> resolveEvent (PerunSessionImpl sess, Member member) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<AuditEvent> resolvingMessages = new ArrayList<>();

		User user = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_V_GROUP_NAMES);
		if (attribute.valueAsList() == null || attribute.valueAsList().isEmpty()){
			AttributeDefinition attributeDefinition = new AttributeDefinition(attribute);
			resolvingMessages.add(new AttributeRemovedForUser(attributeDefinition, user));
		} else {
			resolvingMessages.add(new AttributeSetForUser(attribute, user));
		}

		return resolvingMessages;
	}

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

}
