package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains all group names of the user
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_groupNames extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String FRIENDLY_NAME = "groupNames";
	private static final String A_U_V_GROUP_NAMES = AttributesManager.NS_USER_ATTR_VIRT + ":" + FRIENDLY_NAME;


	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		List<String> groupNames = new ArrayList<>();

		List<Member> members = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		Set<String> voNames = new HashSet<>();
		for (Member member : members) {
			Vo vo = sess.getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
			voNames.add(vo.getShortName());

			List<Group> groups = sess.getPerunBl().getGroupsManagerBl().getMemberGroups(sess, member);
			for (Group group : groups) {
				groupNames.add(vo.getShortName() +":"+ group.getName());
			}
		}
		groupNames.addAll(voNames);

		attribute.setValue(groupNames);
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
